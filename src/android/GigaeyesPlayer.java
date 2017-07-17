package kr.co.anylogic.mediaplayer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import kr.co.anylogic.mediaplayer.GigaeyesConstants;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class GigaeyesPlayer extends CordovaPlugin {

    private String TAG = "GigaeysPlayer";
    private static CallbackContext callbackContext;
    private static String camId;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
       
        if (action.equals("coolMethod")) {
            this.coolMethod(args.getString(0), callbackContext);
            return true;
        } else if (action.equals("play")) {
            GigaeyesPlayer.callbackContext = callbackContext;
            String videoUrl = args.getString(0);
            GigaeyesPlayer.camId = args.getString(1);
            String title = args.getString(2);
            String roiInfo = args.getString(3);
            String sensorInfo = args.getString(4);
            String record_status = args.getString(5);
            String bookmark = args.getString(6);
            Context context = cordova.getActivity().getApplicationContext();
            Intent intent = new Intent(context, GigaeyesPlayerActivity.class);
            intent.putExtra(GigaeyesConstants.VIDEO_URL, videoUrl);
            intent.putExtra(GigaeyesConstants.VIDEO_TITLE, title);
            intent.putExtra(GigaeyesConstants.ROI_INFO, roiInfo);
            intent.putExtra(GigaeyesConstants.SENSOR_INFO, sensorInfo);
            intent.putExtra(GigaeyesConstants.REC_STATUS, record_status);
            intent.putExtra(GigaeyesConstants.BOOKMARK, bookmark);
            Log.d(TAG, "Adicionaod extra: " + videoUrl);
            cordova.startActivityForResult(this, intent, 0);
            return true;
        }

        return false;

    }


    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    /**
     * 즐겨찾기
     */
    static void setFavorites(String favorites) {
        if(callbackContext != null){
            try {
                JSONObject obj = new JSONObject();
                obj.put("type", "favorites");
                obj.put("camId", GigaeyesPlayer.camId);
                obj.put("action", favorites);
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, obj);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }catch (JSONException e) {
                Log.e("ERR", "execute: Got JSON Exception " + e.getMessage());
                callbackContext.error(e.getMessage());
            }


        }

    }
}
