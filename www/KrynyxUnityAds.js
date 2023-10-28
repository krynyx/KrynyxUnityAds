var exec = require('cordova/exec');


var KrynyxUnityAds = {
	loadAds: function (unityGameId, success, error) {
		var options = {};
		options.unityGameId = unityGameId;
		exec(success, error, 'KrynyxUnityAds', 'loadAds', [options]);
	},
	showAds: function() {
		exec(null, null, 'KrynyxUnityAds', 'showAds', null);
	}
};

module.exports = KrynyxUnityAds;
