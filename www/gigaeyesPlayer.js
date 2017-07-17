var exec = require('cordova/exec');

exports.play = function(moviePath, camId, title, roiInfo, sensorInfo, recordStatus,is_favorites, success, error) {
    exec(success, error, "GigaeyesPlayer", "play", [moviePath, camId, title, roiInfo, sensorInfo, recordStatus, is_favorites]);
};
