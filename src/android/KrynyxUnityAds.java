package io.github.krynyx.cordova.unityads;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.view.ViewGroup;
import android.view.View;

import androidx.annotation.NonNull;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.UnityBannerSize;

public class KrynyxUnityAds extends CordovaPlugin implements IUnityAdsInitializationListener{
    
    /* Dados do app e anúncio */
	private String unityGameID; //app id
	private String intersticialAdUnitId;
	private String bannerAdUnitId = "Banner_Android";
	
	private ViewGroup parentView;
	private BannerView bottomBanner;
	UnityBannerListener bannerListener = new UnityBannerListener();
	
	private Boolean testMode = false;
	private static final String TAG = "KrynyxUnityAds";
	private CallbackContext callbackContext;
	
	//Carregar anúncio
	private IUnityAdsLoadListener loadListener = new IUnityAdsLoadListener() {
		@Override
		public void onUnityAdsAdLoaded(String placementId) {
			/* Envia uma resposta ao plugin JavaScript quando o anúncio
			 * já estiver pronto para ser exibido. */
			Log.d(TAG, "anuncio carregado com sucesso");								// Send a positive result to the callbackContext
			PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
			callbackContext.sendPluginResult(pluginResult);	
		}

		@Override
		public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
			Log.d(TAG, "o anuncio nao foi carregado");
			PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
			callbackContext.sendPluginResult(pluginResult);
		}
	};
	
	//Mostrando anúncio
	private IUnityAdsShowListener showListener = new IUnityAdsShowListener() {
		@Override
		public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
			Log.e(TAG, "Unity Ads failed to show ad for " + placementId + " with error: [" + error + "] " + message);
		}

		@Override
		public void onUnityAdsShowStart(String placementId) {
			//Log.v(TAG, "onUnityAdsShowStart: " + placementId);
		}

		@Override
		public void onUnityAdsShowClick(String placementId) {
			//Log.v(TAG, "onUnityAdsShowClick: " + placementId);
		}

		@Override
		public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
			//Log.v(TAG, "onUnityAdsShowComplete: " + placementId);
		}
	};
  
	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		Log.d(TAG, "plugin inicializado com sucesso");
		
        //Criar um novo layout
        parentView = new LinearLayout(webView.getContext());
		((LinearLayout) parentView).setOrientation(LinearLayout.VERTICAL);
		parentView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));

        ViewGroup cordovaParentView = (ViewGroup) webView.getView().getParent();
        cordovaParentView.removeView(webView.getView());//remover o webview da página
		webView.getView().setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0F));
		//agora adiciona o webview no novo layout
		parentView.addView(webView.getView());
		cordova.getActivity().setContentView(parentView);
		
		/* adicionar o banner no layout */
		bottomBanner = new BannerView(cordova.getActivity(), bannerAdUnitId, new UnityBannerSize(320, 50));
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100, 50);//100x50
		bottomBanner.setLayoutParams(params);
		 
		bottomBanner.setListener(bannerListener);
		//carregar o banner antes de exibir
		bottomBanner.load();
		parentView.addView(bottomBanner);
		parentView.bringToFront();
		parentView.requestLayout();
		bottomBanner.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext cbContext) throws JSONException {
		this.callbackContext = cbContext;
		
		// Obtém os dados do anúncio e carrega-o para exibição posterior
		if (action.equals("loadBanner")) {
			Log.d(TAG, "loadBanner");

			this.loadBanner();//carregar o anúncio
			
		} else if (action.equals("loadIntersticial")) {
			Log.d(TAG, "loadIntersticial");
			try {
				/* Obtém os parâmetros recebidos via JavaScript */
				JSONObject options = args.getJSONObject(0);
				this.intersticialAdUnitId = options.getString("adUnitId");//banner id
				this.loadIntersticial();//carregar o anúncio
			} catch (JSONException e) {
				Log.w(TAG, "Erro ao processar os argumentos");
				callbackContext.error("Error encountered: " + e.getMessage());
				return false;
			}
			
		} else if (action.equals("showIntersticial")) {
			this.showIntersticial();
			
		} else if (action.equals("initializeUnitySDK")) {
			Log.d(TAG, "initializeUnitySDK");
			try {
				/* Obtém os parâmetros recebidos via JavaScript */
				JSONObject options = args.getJSONObject(0);
				this.unityGameID = options.getString("unityGameId");//app id
				this.initializeUnitySDK();//iniciar o sdk do unity
				Log.d(TAG, "unityGameID configurado com sucesso");
			} catch (JSONException e) {
				Log.w(TAG, "Erro ao processar os argumentos");
				callbackContext.error("Error encountered: " + e.getMessage());
				return false;
			}
		}
		
		return true;
	}
	
	/* Inicializa o Unity Ads e carrega um anúncio em seguida */
	private void initializeUnitySDK() {
		cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
				Log.d(TAG, "iniciando o sdk do unity ads");
				UnityAds.initialize (cordova.getActivity(), unityGameID, testMode, KrynyxUnityAds.this);
			}
		});
	}
	
	private void loadIntersticial() {
		Log.d(TAG, "carregando intersticial");
		UnityAds.load(intersticialAdUnitId, loadListener);
	}
	
	private void loadBanner() {
		Log.d(TAG, "carregando banner");
		bottomBanner.load();
	}
	
	/* Mostra o anúncio depois que o mesmo já tenha sido carregado */
	private void showIntersticial() {
		cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
				Log.d(TAG, "mostrando intersticial");
				UnityAds.show(cordova.getActivity(), intersticialAdUnitId, new UnityAdsShowOptions(), showListener);
            }
        });
	}
		
	//UnityAds.initialize
	@Override
	public void onInitializationComplete() {
		Log.d(TAG, "sdk iniciado com sucesso");
		//envia uma resposta ao plugin (callback success)
		PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
		callbackContext.sendPluginResult(pluginResult);	
	}

	@Override
	public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
		Log.d(TAG, "falha ao iniciar o sdk do unity ads");
	}
	
	// Implement listener methods:
    private class UnityBannerListener implements BannerView.IListener {
		@Override
		public void onBannerShown(BannerView bannerAdView) {
			
		}
		
        @Override
        public void onBannerLoaded(BannerView bannerAdView) {
            // Called when the banner is loaded.
            Log.d(TAG, "banner carregado com sucesso");
			PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
			callbackContext.sendPluginResult(pluginResult);	
        }

        @Override
        public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo) {
            Log.d(TAG, "erro ao carregar o banner" );
        }

        @Override
        public void onBannerClick(BannerView bannerAdView) {
            // Called when a banner is clicked.
        }

        @Override
        public void onBannerLeftApplication(BannerView bannerAdView) {
            // Called when the banner links out of the application.
        }
    }
}
