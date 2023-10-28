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

import androidx.annotation.NonNull;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAdsShowOptions;

public class KrynyxUnityAds extends CordovaPlugin implements IUnityAdsInitializationListener{
    
	private String unityGameID = "5459645";
	private Boolean testMode = false;
	private static final String adUnitId = "Interstitial_Android";
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
	}

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext cbContext) throws JSONException {
		this.callbackContext = cbContext;
		
		if (action.equals("showAds")) {
			
			Log.d(TAG, "executando a funcao showAds");
			this.showAds();
			
		} else if (action.equals("loadAds")) {
			
			try {
				/* Obtém os parâmetros recebidos via JavaScript */
				JSONObject options = args.getJSONObject(0);
				this.unityGameID = options.getString("unityGameId");
				Log.d(TAG, "unityGameID configurado com sucesso");
				
			} catch (JSONException e) {
				
				Log.w(TAG, "Erro ao processar os argumentos");
				callbackContext.error("Error encountered: " + e.getMessage());
				return false;
			}
			
			Log.d(TAG, "executando a funcao loadAds");
			this.loadAds();			
		}
		
		return true;
	}
	
	/* Inicializa o Unity Ads e carrega um anúncio em seguida */
	private void loadAds() {
		cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
				// Initialize the SDK:
				Log.d(TAG, "iniciando o sdk do unity ads");
				UnityAds.initialize (cordova.getActivity(), unityGameID, testMode, KrynyxUnityAds.this);
			}
		});
	}
	
	/* Mostra o anúncio depois que o mesmo já tenha sido carregado */
	private void showAds() {
		cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
				Log.d(TAG, "mostrando anuncio");
				UnityAds.show(cordova.getActivity(), adUnitId, new UnityAdsShowOptions(), showListener);
            }
        });
	}
	
	//UnityAds.initialize
	@Override
	public void onInitializationComplete() {
		Log.d(TAG, "carregando anuncio");
		UnityAds.load(adUnitId, loadListener);
	}

	@Override
	public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
		Log.d(TAG, "falha ao iniciar o sdk do unity ads");
	}
}
