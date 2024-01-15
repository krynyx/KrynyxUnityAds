var exec = require('cordova/exec');

var KrynyxUnityAds = {
	initialize: function(unityGameId, success, error) {
		var options = {};
		options.unityGameId = unityGameId;
		exec(success, error, 'KrynyxUnityAds', 'initializeUnitySDK', [options]);
	},
	loadBanner: function () {
		exec(null, null, 'KrynyxUnityAds', 'loadBanner', null);		
	},
	loadIntersticial: function (adUnitId, success, error) {
		var options = {};
		options.adUnitId = adUnitId;
		exec(success, error, 'KrynyxUnityAds', 'loadIntersticial', [options]);
	},
	showIntersticial: function() {
		exec(null, null, 'KrynyxUnityAds', 'showIntersticial', null);
	}
};

module.exports = KrynyxUnityAds;
