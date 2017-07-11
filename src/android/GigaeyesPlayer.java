package kr.co.anylogic.myoverlay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import kr.co.anylogic.myoverlay.GigaeyesConstants;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class GigaeyesPlayer extends CordovaPlugin {

    private static CallbackContext callbackContext;
    private static String camId;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Application app = cordova.getActivity().getApplication();
        String package_name = app.getPackageName();
        Resources res = app.getResources();

        int activity_main = res.getIdentifier("activity_main", "layout", package_name);
        Log.d("FLP", "activity_main id: " + activity_main);
        int over = res.getIdentifier("over", "layout", package_name);
        int back = res.getIdentifier("back", "layout", package_name);
        int iot = res.getIdentifier("iot", "layout", package_name);
        int va = res.getIdentifier("layout", "layout", package_name);
        int lay_iot = res.getIdentifier("lay_iot", "id", package_name);
        int lay_va = res.getIdentifier("lay_va", "id", package_name);

        int texture_view = res.getIdentifier("textureView", "id", package_name);
        // int video_view = res.getIdentifier("videoView", "id", package_name);
        int btn_back = res.getIdentifier("btn_back", "id", package_name);
        int btn_onoff = res.getIdentifier("btn_onoff", "id", package_name);
        int btn_iot = res.getIdentifier("btn_iot", "id", package_name);
        int btn_va = res.getIdentifier("btn_va", "id", package_name);
        int btn_capture = res.getIdentifier("btn_capture", "id", package_name);
        int btn_star = res.getIdentifier("btn_star", "id", package_name);
        int ico_theft = res.getIdentifier("ico_theft", "drawable", package_name);
        int ico_door = res.getIdentifier("ico_door", "drawable", package_name);
        int ico_fire = res.getIdentifier("ico_fire", "drawable", package_name);
        int ico_sound = res.getIdentifier("ico_sound", "drawable", package_name);
        int ico_temperature = res.getIdentifier("ico_temperature", "drawable", package_name);
        int ico_humidity = res.getIdentifier("ico_humidity", "drawable", package_name);
        int ico_cameraon = res.getIdentifier("ico_cameraon", "drawable", package_name);
        int ico_cameraoff = res.getIdentifier("ico_cameraoff", "drawable", package_name);
        int ico_star = res.getIdentifier("ico_star", "drawable", package_name);
        int ico_star_off = res.getIdentifier("ico_star_off", "drawable", package_name);

        int camName = res.getIdentifier("camName", "id", package_name);
     

        if (action.equals("coolMethod")) {
            this.coolMethod(args.getString(0), callbackContext);
            return true;
        } else if (action.equals("play")) {
            GigaeyesJoystick.callbackContext = callbackContext;
            String videoUrl = args.getString(0);
            GIgaeyesPlayer.camId = args.getString(1);
            String title = args.getString(2);
            String roiInfo = args.getString(3);
            Context context = cordova.getActivity().getApplicationContext();
            Intent intent = new Intent(context, GigaeyesActivity.class);
            intent.putExtra("VIDEO_URL", videoUrl);
            intent.putExtra("TITLE", title);
            intent.putExtra("ROI_INFO", roiInfo);
            intent.putExtra("activity_main", activity_main);
            intent.putExtra("texture_view", texture_view);
            intent.putExtra("over", over);
            intent.putExtra("back", back);
            intent.putExtra("iot", iot);
            intent.putExtra("va", va);
            intent.putExtra("lay_iot", lay_iot);
            intent.putExtra("lay_va", lay_va);
            intent.putExtra("btn_back", btn_back);
            intent.putExtra("btn_onoff", btn_onoff);
            intent.putExtra("btn_va", btn_va);
            intent.putExtra("btn_capture", btn_capture);
            intent.putExtra("btn_star", btn_star);
            intent.putExtra("camName", camName);
            intent.putExtra("ico_theft", ico_theft);
            intent.putExtra("ico_door", ico_door);
            intent.putExtra("ico_fire", ico_fire);
            intent.putExtra("ico_sound", ico_sound);
            intent.putExtra("ico_temperature", ico_temperature);
            intent.putExtra("ico_humidity", ico_humidity);
            intent.putExtra("ico_cameraon", ico_cameraon);
            intent.putExtra("ico_cameraoff", ico_cameraoff);
            intent.putExtra("ico_star", ico_star);
            intent.putExtra("ico_star_off", ico_star_off);

            // intent.putExtra("image_view", image_view);
            // intent.putExtra("video_view", video_view);
            Log.d("FLP", "Adicionaod extra: " + videoUrl);
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
    static void setFavorites(Context content, boolean status) {
//        Toast.makeText(content,"JoystickHandlerActivity move: UP",Toast.LENGTH_SHORT).show();
        if(callbackContext != null){
            JSONObject obj = new JSONObject();
            obj.put("type","favorites");
            obj.put("camId",GigaeyesPlayer.camId);
            String recStatus = GigaeyesConstants.FAVORITES_ON;
            if(!status){
                recStatus = GigaeyesConstants.FAVORITES_OFF;
            }
            obj.put("action", recStatus);
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, obj);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        }

    }
}
