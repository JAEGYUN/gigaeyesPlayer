var exec = require('cordova/exec');

exports.play = function(moviePath, camId, title, recordStatus, roiInfo, success, error) {
    exec(success, error, "gigaeyesplayer", "play", [moviePath, camId, title, recordStatus, roiInfo]);
};
