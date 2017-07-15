var exec = require('cordova/exec');

exports.play = function(moviePath, camId, title, roiInfo, sensorInfo, recordStatus, success, error) {
    exec(success, error, "GigaeyesPlayer", "play", [moviePath, camId, title, roiInfo, sensorInfo, recordStatus]);
};
