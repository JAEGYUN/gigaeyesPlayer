package kr.co.anylogic.mediaplayer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GigaeyesPlayerActivity extends Activity implements TextureView.SurfaceTextureListener, View.OnTouchListener {

    public static String strjson = "{\n" +
            "\t'roi_info': {\n" +
            "\t\t'roi_objs': [{\n" +
            "\t\t\t'roi_id': 'ROI-LineCount-INOUT-00001',\n" +
            "\t\t\t'roi_type' : '13',\n" +
            "\t\t\t'order_no' : 1,\n" +
            "\t\t\t'roi_coord' : [{\n" +
            "\t\t\t\t'x': '47.344',\n" +
            "\t\t\t\t'y' : '41.481'\n" +
            "\t\t\t}, { 'x': '61.458',\n" +
            "\t\t\t\t'y' : '51.481'\n" +
            "\t\t\t}],\n" +
            "\t\t\t'roi_color' : {\n" +
            "\t\t\t\t'r' : '255',\n" +
            "\t\t\t\t'g' : '255',\n" +
            "\t\t\t\t'b' : '255'\n" +
            "\t\t\t}\n" +
            "\t\t}, { 'roi_id': 'ROI-LineCrossing-IN-00002',\n" +
            "\t\t\t'roi_type' : '14',\n" +
            "\t\t\t'order_no' : 2,\n" +
            "\t\t\t'roi_coord' : [{\n" +
            "\t\t\t\t'x': '29.583',\n" +
            "\t\t\t\t'y' : '43.796'\n" +
            "\t\t\t},{ 'x': '25.938',\n" +
            "\t\t\t\t'y' : '50.463'\n" +
            "\t\t\t}],\n" +
            "\t\t\t'roi_color' : {\n" +
            "\t\t\t\t'r' : '255',\n" +
            "\t\t\t\t'g' : '0',\n" +
            "\t\t\t\t'b' : '0'\n" +
            "\t\t\t}\n" +
            "\t\t}, { 'roi_id': 'ROI-AreaCount-00003',\n" +
            "\t\t\t'roi_type' : '21',\n" +
            "\t\t\t'order_no' : 3,\n" +
            "\t\t\t'roi_coord' : [{\n" +
            "\t\t\t\t'x': '61.823',\n" +
            "\t\t\t\t'y' : '29.537'\n" +
            "\t\t\t},{ 'x': '69.740',\n" +
            "\t\t\t\t'y' : '29.537'\n" +
     //       "\t\t\t},{ 'x': '69.740',\n" +
     //       "\t\t\t\t'y' : '54.815'\n" +
            "\t\t\t},{ 'x': '61.823',\n" +
            "\t\t\t\t'y' : '54.815'\n" +
            "\t\t\t}],\n" +
            "\t\t\t'roi_color' : {\n" +
            "\t\t\t\t'r' : '0',\n" +
            "\t\t\t\t'g' : '0',\n" +
            "\t\t\t\t'b' : '255'\n" +
            "\t\t\t}\n" +
            "\t\t}, { 'roi_id': 'ROI-AreaAtack-IN-00004',\n" +
            "\t\t\t'roi_type' : '22',\n" +
            "\t\t\t'order_no' : 4,\n" +
            "\t\t\t'roi_coord' : [{\n" +
            "\t\t\t\t'x': '1.927',\n" +
            "\t\t\t\t'y' : '31.389'\n" +
            "\t\t\t},{ 'x': '17.188',\n" +
            "\t\t\t\t'y' : '31.389'\n" +
            "\t\t\t},{ 'x': '17.188',\n" +
            "\t\t\t\t'y' : '47.037'\n" +
            "\t\t\t},{ 'x': '1.927',\n" +
            "\t\t\t\t'y' : '47.037'\n" +
            "\t\t\t}],\n" +
            "\t\t\t'roi_color' : {\n" +
            "\t\t\t\t'r' : '255',\n" +
            "\t\t\t\t'g' : '0',\n" +
            "\t\t\t\t'b' : '0'\n" +
            "\t\t\t}\n" +
            "\t\t}, {'roi_id': 'ROI-TEXT-00007',\n" +
            "\t\t\t'roi_type' : '10002',\n" +
            "\t\t\t'roi_name' : 'Test String : TEST',\n" +
            "\t\t\t'order_no' : 5,\n" +
            "\t\t\t'roi_coord' : [{\n" +
            "\t\t\t\t'x': '71.927',\n" +
            "\t\t\t\t'y' : '33.389'\n" +
            "\t\t\t}],\n" +
            "\t\t\t'roi_color' : {\n" +
            "\t\t\t\t'r' : '255',\n" +
            "\t\t\t\t'g' : '255',\n" +
            "\t\t\t\t'b' : '255'\n" +
            "\t\t\t}\n" +            "\t\t}, {'roi_id': 'ROI-TEXT-00005',\n" +
            "\t\t\t'roi_type' : '10003',\n" +
            "\t\t\t'roi_name' : 'Test String : TEST',\n" +
            "\t\t\t'order_no' : 5,\n" +
            "\t\t\t'roi_coord' : [{\n" +
            "\t\t\t\t'x': '31.927',\n" +
            "\t\t\t\t'y' : '43.389'\n" +
            "\t\t\t}],\n" +
            "\t\t\t'roi_color' : {\n" +
            "\t\t\t\t'r' : '255',\n" +
            "\t\t\t\t'g' : '255',\n" +
            "\t\t\t\t'b' : '255'\n" +
            "\t\t\t}\n" +
            "\t\t}, {'roi_id': 'ROI-IMAGE-00006',\n" +
            "\t\t\t'roi_type' : '10002',\n" +
            "\t\t\t'roi_name' : '사람아이콘2.png',\n" +
            "\t\t\t'order_no' : 6,\n" +
            "\t\t\t'roi_coord' : [{\n" +
            "\t\t\t\t'x' : '50.000',\n" +
            "\t\t\t\t'y' : '50.000'\n" +
            "\t\t\t}],\n" +
            "\t\t\t'ratio' : 50.000\n" +
            "\t\t}]\n" +
            "\t}\n" +
            "}\n";

    private String roi_info ="[]";
    private String sensor_info = "[]";

    private String packageName;
    private Resources res;

    private MediaPlayer mediaPlayer;
    private TextureView textureView;

    public static String TAG = "TextureViewActivity";

//    private Renderer mRenderer;

    LayoutInflater inflater;
    RelativeLayout rlTop;
    RelativeLayout backLayout;
    RelativeLayout iotLayer;
    RelativeLayout vaLayer;
    VAView vaView;
    boolean clickedFlag = false;
//  센서 등록유무
    boolean iotFlag = false;
//  ROI 등록유무
    boolean vaFlag = false;

    private String videoSrc = "rtsp://211.54.3.139:1935/gigaeyesmonitor/test-1004.stream";
    public static String cctvName = "CCTV-정문";
    public static boolean iotViewFlag = false;
    public static boolean vaViewFlag = false;
    public static boolean favFlag = false;
    public static boolean onoffFlag = true;

    ArrayList<ROI_OBJ> ROI_INFO = new ArrayList<ROI_OBJ>();
    ArrayList<ImageView> imgViews = new ArrayList<ImageView>();
//    GigaeyesPlayerActivity my;


    class ROI_OBJ
    {
        String id;
        String type;           // 11~16 : LINE, 21~26 : 영역, 31 : text, 32 : image
        String name;        // Text String, image file fullpath
        int order_no;
        ArrayList<Point> coord;
        // int colorR;
        // int colorG;
        // int colorB;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.packageName = getApplication().getPackageName();
        this.res = getApplication().getResources();
        int main_layout = res.getIdentifier(GigaeyesConstants.MAIN_LAYOUT, GigaeyesConstants.LAYOUT, this.packageName);
        int texture_view = res.getIdentifier(GigaeyesConstants.TEXTURE_VIEW, GigaeyesConstants.ID, this.packageName);

        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            return;
        }

        Bundle extras  = getIntent().getExtras();
        if (extras != null) {
            this.videoSrc = extras.getString(GigaeyesConstants.VIDEO_URL);
            // this.strjson = extras.getString("ROI_INFO");
            this.cctvName = extras.getString(GigaeyesConstants.VIDEO_TITLE);
            this.roi_info = extras.getString(GigaeyesConstants.ROI_INFO);
            this.sensor_info = extras.getString(GigaeyesConstants.SENSOR_INFO);
            if(extras.getString(GigaeyesConstants.REC_STATUS) != null 
                && GigaeyesConstants.STREAM_VALID_STATUS.equals(extras.getString(GigaeyesConstants.REC_STATUS))){
               this.onoffFlag = true;
            }else{
               this.onoffFlag = false;
            }
        } else {
           finishWithError();
        }



        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(main_layout);

        textureView = (TextureView) findViewById(texture_view);
        textureView.requestFocus();

        roiParsing();

        textureView.setSurfaceTextureListener(this);
        textureView.setOnTouchListener(this);


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        int main_container = res.getIdentifier(GigaeyesConstants.MAIN_CONTAINER, GigaeyesConstants.ID, this.packageName);
        View layoutMainView = (View)this.findViewById(main_container);

        Log.w("Layout Width - ", String.valueOf(layoutMainView.getWidth()));
        Log.w("Layout Height - ", String.valueOf(layoutMainView.getHeight()));

        drawVA(layoutMainView.getWidth(), layoutMainView.getHeight());
        drawIoT(layoutMainView.getWidth(), layoutMainView.getHeight());
    }


    private void drawVA(int fullWidth, int fullHeight) {
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int va_layout = res.getIdentifier(GigaeyesConstants.VA_LAYOUT, GigaeyesConstants.LAYOUT, this.packageName);
        vaLayer = (RelativeLayout) inflater.inflate(va_layout, null);

        if(vaView == null) {
            getWindow().addContentView(vaLayer, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            vaView = new VAView(this);
            vaLayer.addView(vaView);

            if(iotFlag) {
                for(ROI_OBJ roiValue : ROI_INFO) {
                    if(roiValue.type == 11 || roiValue.type == 12 || roiValue.type == 13 ||         // draw line 모두 빨강색으로
                        roiValue.type == 14 || roiValue.type == 15 || roiValue.type == 16  ){
                        ArrayList<Point> pts = new ArrayList<Point>();
                        for(Point pt : roiValue.coord){
                            int x = fullWidth * pt.x / 100000;
                            int y = fullHeight * pt.y / 100000;
                            Point p = new Point(x, y);
                            pts.add(p);
                        }
                        vaView.pushLine(roiValue.type,  pts);
                    }else if(roiValue.type == 21 || roiValue.type == 22 || roiValue.type == 23 ||         // draw poligon
                            roiValue.type == 24 || roiValue.type == 25 || roiValue.type == 26  ) {
                        ArrayList<Point> pts = new ArrayList<Point>();
                        for(Point pt : roiValue.coord){
                            int x = fullWidth * pt.x / 100000;
                            int y = fullHeight * pt.y / 100000;

                            Point p = new Point(x, y);
                            pts.add(p);
                        }
                        vaView.pushPolygon(roiValue.type,  pts);
                    }
                }
                vaViewFlag = true;
            }
        }
    }



    private void drawIoT(int fullWidth, int fullHeight){

        int iot_layer = res.getIdentifier(GigaeyesConstants.IOT_LAYOUT, GigaeyesConstants.LAYOUT, this.packageName);
//       센서 아이콘
        int ico_theft = res.getIdentifier(GigaeyesConstants.image.ICO_THEFT, GigaeyesConstants.IMAGE, this.packageName);
        int ico_door = res.getIdentifier(GigaeyesConstants.image.ICO_DOOR, GigaeyesConstants.IMAGE, this.packageName);
        int ico_sound = res.getIdentifier(GigaeyesConstants.image.ICO_SOUND, GigaeyesConstants.IMAGE, this.packageName);
        int ico_fire = res.getIdentifier(GigaeyesConstants.image.ICO_FIRE, GigaeyesConstants.IMAGE, this.packageName);
        int ico_temperature = res.getIdentifier(GigaeyesConstants.image.ICO_TEMPERATURE, GigaeyesConstants.IMAGE, this.packageName);
        int ico_humidity = res.getIdentifier(GigaeyesConstants.image.ICO_HUMIDITY, GigaeyesConstants.IMAGE, this.packageName);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        iotLayer = (RelativeLayout) inflater.inflate(iot_layer, null);

        if(imgViews.size() == 0) {
            getWindow().addContentView(iotLayer, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            if (iotFlag) {

                for (ROI_OBJ roiValue : ROI_INFO) {
                    if (roiValue.type == 10001 || roiValue.type == 10002 || roiValue.type == 10003 ||
                            roiValue.type == 10004 || roiValue.type == 10005 || roiValue.type == 10006) {
                        ImageView ivg = new ImageView(this);
                        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        for (Point pt : roiValue.coord) {
                            int x = fullWidth * pt.x / 100000;
                            int y = fullHeight * pt.y / 100000;

                            params.setMargins(x, y, 0, 0);
                            ivg.setLayoutParams(params);
                            if (roiValue.type == 10001) {         // 움직임
                                ivg.setBackgroundResource(ico_theft);
                            } else if (roiValue.type == 10002) {   // 문열림
                                ivg.setBackgroundResource(ico_door);
                            } else if (roiValue.type == 10003) {   // 소리
                                ivg.setBackgroundResource(ico_sound);
                            } else if (roiValue.type == 10004) {   // 화재
                                ivg.setBackgroundResource(ico_fire);
                            } else if (roiValue.type == 10005) {   // 온도
                                ivg.setBackgroundResource(ico_temperature);
                            } else if (roiValue.type == 10006) {  // 습도
                                ivg.setBackgroundResource(ico_humidity);
                            } else {
                            }
                            iotLayer.addView(ivg);
                        }
                        imgViews.add(ivg);
                    }
                }
                iotViewFlag = true;
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
    }
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Surface s = new Surface(surface);

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(videoSrc);
            mediaPlayer.setSurface(s);
            mediaPlayer.prepare();

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.start();

        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    void roiParsing(){
        try {
            // ROI INFO parsing.
            JSONObject roi_objs = new JSONObject(this.roi_info); 
            
            for(int j = 0; j < roi_objs.length(); j++){
                ROI_OBJ curObj = new ROI_OBJ();
                curObj.coord = new ArrayList<Point>();
                JSONObject roi_obj = roi_objs.getJSONObject(j);
                curObj.id = roi_obj.getString("roi_id");
                curObj.type = roi_obj.getString("roi_type");
                if(j == 0 ){
                    vaFlag = true;
                }
                curObj.order_no = roi_obj.getInt("order_no");

                JSONArray roi_coord = roi_obj.getJSONArray("roi_coord");
                for(int k = 0; k < roi_coord.length(); k++){
                    JSONObject pt = roi_coord.getJSONObject(k);
                    Point p = new Point();
                    p.x = (int)(pt.getDouble("x") * 1000.0);
                    p.y = (int)(pt.getDouble("y") * 1000.0);
                    curObj.coord.add(p);
                }

                ROI_INFO.add(curObj);
            }

             // SENSOR INFO parsing.
            JSONObject sensor_objs = new JSONObject(this.sensor_info); 
            
            for(int j = 0; j < sensor_objs.length(); j++){
                ROI_OBJ curObj = new ROI_OBJ();
                curObj.coord = new ArrayList<Point>();
                JSONObject roi_obj = sensor_objs.getJSONObject(j);
                curObj.id = roi_obj.getString("sensor_id");
                curObj.type = roi_obj.getInt("sensor_type");
                if(j == 0 ){
                    iotFlag = true;
                }
                // if(curObj.type == 10001 || curObj.type == 10002 || curObj.type == 10003 ||
                //     curObj.type == 10004 || curObj.type == 10005 || curObj.type == 10006  ){
                //     iotFlag = true;
                // }
                // if(curObj.type == 11 || curObj.type == 12 || curObj.type == 13 || curObj.type == 14 || curObj.type == 15 || curObj.type == 16 ||
                //     curObj.type == 21 || curObj.type == 22 || curObj.type == 23 || curObj.type == 24 || curObj.type == 25 || curObj.type == 26 ){
                //     vaFlag = true;
                // }

                curObj.order_no = roi_obj.getInt("order_no");

                Point p = new Point();
                p.x = (int)(Double.parseDouble(roi_obj.getString("location_x")) * 1000.0);
                p.y = (int)(Double.parseDouble(roi_obj.getString("location_y")) * 1000.0);
                curObj.coord.add(p);

                ROI_INFO.add(curObj);
            }
            // }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("TestAppActivity", "onStart");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


        Log.d("TestAppActivity", "onPostCreate");
    }


    void enrollButton(){
//        버튼
        int btn_back = res.getIdentifier(GigaeyesConstants.button.BACK, GigaeyesConstants.ID, this.packageName);
        int btn_onoff = res.getIdentifier(GigaeyesConstants.button.ONOFF, GigaeyesConstants.ID, this.packageName);
        int btn_iot = res.getIdentifier(GigaeyesConstants.button.IOT, GigaeyesConstants.ID, this.packageName);
        int btn_va = res.getIdentifier(GigaeyesConstants.button.VA, GigaeyesConstants.ID, this.packageName);
        int btn_capture = res.getIdentifier(GigaeyesConstants.button.CAPTURE, GigaeyesConstants.ID, this.packageName);
        int btn_star = res.getIdentifier(GigaeyesConstants.button.STAR, GigaeyesConstants.ID, this.packageName);

        ImageButton btn1 = (ImageButton)findViewById(btn_back);
        btn1.setOnClickListener((new ImageButton.OnClickListener(){
            public void onClick(View v){
                clickBtn1();
            }
        }));

        ImageButton btn2 = (ImageButton)findViewById(btn_onoff);
        btn2.setOnClickListener((new ImageButton.OnClickListener(){
            public void onClick(View v){
                clickBtn2();
            }
        }));


        ImageButton btn3 = (ImageButton) findViewById(btn_iot);
        btn3.setOnClickListener((new ImageButton.OnClickListener() {
            public void onClick(View v) {
                clickBtn3();
            }
        }));



        ImageButton btn4 = (ImageButton) findViewById(btn_va);
        btn4.setOnClickListener((new ImageButton.OnClickListener() {
            public void onClick(View v) {
                clickBtn4();
            }
        }));

        ImageButton btn5 = (ImageButton)findViewById(btn_capture);
        btn5.setOnClickListener((new ImageButton.OnClickListener(){
            public void onClick(View v){
                clickBtn5();
            }
        }));

        ImageButton btn6 = (ImageButton)findViewById(btn_star);
        btn6.setOnClickListener((new ImageButton.OnClickListener(){
            public void onClick(View v){
                clickBtn6();
            }
        }));
    }


    public boolean onTouch(View v, MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN){
//            Toast.makeText(this, "Touch Event Received", Toast.LENGTH_LONG).show();
            setOverlay(true);
            return true;
        }
        return false;
    }

    private int DELAY_TIME = 4000;

    Runnable mNavHider = new Runnable() {
        @Override
        public void run() {
//            Toast.makeText(GigaeyesPlayerActivity.this, "mNavHide function called", Toast.LENGTH_LONG).show();
            setOverlay(false);
        }
    };

    void setOverlay(boolean visible) {
        if (!visible) {
            ((ViewGroup) rlTop.getParent()).removeView(rlTop);
            ((ViewGroup) backLayout.getParent()).removeView(backLayout);
            clickedFlag = false;
        }

        if (visible) {
            if(clickedFlag)
                return;

            // addContentView를 호출한다.
            inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int back_layout = res.getIdentifier(GigaeyesConstants.BACK_LAYOUT, GigaeyesConstants.LAYOUT, this.packageName);
            int control_overlay = res.getIdentifier(GigaeyesConstants.CONTROL_LAYOUT, GigaeyesConstants.LAYOUT, this.packageName);
            int title = res.getIdentifier(GigaeyesConstants.TITLE, GigaeyesConstants.ID, this.packageName);
            int btn_on_off = res.getIdentifier(GigaeyesConstants.button.ONOFF, GigaeyesConstants.ID, this.packageName);
            int btn_va = res.getIdentifier(GigaeyesConstants.button.VA, GigaeyesConstants.ID, this.packageName);
            int btn_iot = res.getIdentifier(GigaeyesConstants.button.IOT, GigaeyesConstants.ID, this.packageName);
            int btn_star = res.getIdentifier(GigaeyesConstants.button.STAR, GigaeyesConstants.ID, this.packageName);
            int ico_camera_on = res.getIdentifier(GigaeyesConstants.image.ICO_CAMERA_ON, GigaeyesConstants.IMAGE, this.packageName);
            int ico_camera_off = res.getIdentifier(GigaeyesConstants.image.ICO_CAMERA_OFF, GigaeyesConstants.IMAGE, this.packageName);
            int ico_star_on = res.getIdentifier(GigaeyesConstants.image.ICO_STAR_ON, GigaeyesConstants.IMAGE, this.packageName);
            int ico_star_off = res.getIdentifier(GigaeyesConstants.image.ICO_STAR_OFF, GigaeyesConstants.IMAGE, this.packageName);



            backLayout = (RelativeLayout)inflater.inflate(back_layout, null);
            getWindow().addContentView(backLayout, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            rlTop = (RelativeLayout)inflater.inflate(control_overlay, null);
            getWindow().addContentView(rlTop, new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));


            TextView tv = (TextView)findViewById(title);
            tv.setText(cctvName);

            ImageButton btnOnoff = (ImageButton)findViewById(btn_on_off);
            if(onoffFlag) {
                btnOnoff.setImageResource(ico_camera_on);
            }else{
                btnOnoff.setImageResource(ico_camera_off);
            }
            ImageButton btnStar = (ImageButton)findViewById(btn_star);
            if(favFlag) {
                btnStar.setImageResource(ico_star_on);
            }else{
                btnStar.setImageResource(ico_star_off);
            }

//            vaFlag = false;
//            iotFlag = true;
            // iot_layer, va_layer view를 생성한다.
            if (vaFlag) {
                ImageButton btnva = (ImageButton)findViewById(btn_va);
                btnva.setVisibility(View.VISIBLE);
            }else{
                ImageButton btnva = (ImageButton)findViewById(btn_va);
                btnva.setVisibility(View.INVISIBLE);
            }

            if (iotFlag) {
                ImageButton btniot = (ImageButton)findViewById(btn_iot);
                if(!vaFlag){
                    ImageButton btnva = (ImageButton)findViewById(btn_va);
                    btniot.setLayoutParams(btnva.getLayoutParams());
                }
                btniot.setVisibility(View.VISIBLE);
            }else{
                ImageButton btniot = (ImageButton)findViewById(btn_iot);
                btniot.setVisibility(View.INVISIBLE);
            }


            enrollButton();

            Handler h = tv.getHandler();
            if (h != null) {
                h.removeCallbacks(mNavHider);
                h.postDelayed(mNavHider, DELAY_TIME);
            }
            clickedFlag = true;
        }
    }


    void clickBtn1(){
//        Toast.makeText(this, "clickBtn1 Clicked!! 종료합니다.", Toast.LENGTH_LONG).show();
        moveTaskToBack(true);
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    void clickBtn2(){
        Toast.makeText(this, "clickBtn2 Clicked!! 녹화중임을 표시합니다.", Toast.LENGTH_LONG).show();
    }

    void clickBtn3(){
//        Toast.makeText(this, "clickBtn3 Clicked!! IoT를 그립니다.", Toast.LENGTH_LONG).show();
        int btn_iot = res.getIdentifier(GigaeyesConstants.button.IOT, GigaeyesConstants.ID, this.packageName);
        int ico_iot_on = res.getIdentifier(GigaeyesConstants.image.ICO_IOT, GigaeyesConstants.IMAGE, this.packageName);
        int ico_iot_off = res.getIdentifier(GigaeyesConstants.image.ICO_IOT_OFF, GigaeyesConstants.IMAGE, this.packageName);
        ImageButton btnIot = (ImageButton)findViewById(btn_iot);
        if(iotFlag){
            if(!iotViewFlag) {  // 그려라
                for(ImageView imgView : imgViews) {
                    imgView.setVisibility(View.VISIBLE);
                }
                btnIot.setImageResource(ico_iot_on);
                iotViewFlag = true;
            }else{
                for(ImageView imgView : imgViews) {
                    imgView.setVisibility(View.INVISIBLE);
                }
                btnIot.setImageResource(ico_iot_off);
                iotViewFlag = false;
            }
        }
    }

    void clickBtn4(){
//        Toast.makeText(this, "clickBtn4 Clicked!! VA를 표시 합니다.", Toast.LENGTH_LONG).show();
        int btn_va = res.getIdentifier(GigaeyesConstants.button.VA, GigaeyesConstants.ID, this.packageName);
        int ico_va_on = res.getIdentifier(GigaeyesConstants.image.ICO_VA, GigaeyesConstants.IMAGE, this.packageName);
        int ico_va_off = res.getIdentifier(GigaeyesConstants.image.ICO_VA_OFF, GigaeyesConstants.IMAGE, this.packageName);

        ImageButton btnva = (ImageButton)findViewById(btn_va);
        if(vaFlag){
            if(!vaViewFlag) {  // 그려라
                vaView.setVisibility(View.VISIBLE);
                btnva.setImageResource(ico_va_on);
                vaViewFlag = true;
            }else{
                vaView.setVisibility(View.INVISIBLE);
                btnva.setImageResource(ico_va_off);
                vaViewFlag = false;
            }
        }
    }
    void clickBtn5(){
        Log.d(TAG, "스크린샷을 저장합니다" );
        getBitmap(textureView);


    }
    void clickBtn6(){
        Log.d(TAG, "즐겨찾기를 설정합니다" );
        int btn_star = res.getIdentifier(GigaeyesConstants.button.STAR, GigaeyesConstants.ID, this.packageName);
        int ico_star_on = res.getIdentifier(GigaeyesConstants.image.ICO_STAR_ON, GigaeyesConstants.IMAGE, this.packageName);
        int ico_star_off = res.getIdentifier(GigaeyesConstants.image.ICO_STAR_OFF, GigaeyesConstants.IMAGE, this.packageName);

        ImageButton btnStar = (ImageButton)findViewById(btn_star);
        if(favFlag) {
            Toast.makeText(this, "즐겨찾기 해제.", Toast.LENGTH_LONG).show();
            btnStar.setImageResource(ico_star_off);
            GigaeyesPlayer.setFavorites(GigaeyesConstants.FAVORITES_OFF);
        }else{
            Toast.makeText(this, "즐겨찾기 등록", Toast.LENGTH_LONG).show();
            btnStar.setImageResource(ico_star_on);
            GigaeyesPlayer.setFavorites(GigaeyesConstants.FAVORITES_ON);
        }

        favFlag = !favFlag;
    }

//    private void setViewInvalidate(View... views){
//        for(View v : views){
//            v.invalidate();
//        }
//    }



    public void getBitmap(TextureView vv) {
        Bitmap bm = vv.getBitmap();

        Bitmap tarVABM = getVAOverlayBitmap();
        Bitmap tarIoTBM = getIoTOverlayBitmap();


        //캔버스를 통해 비트맵을 겹치기한다.
        Canvas canvas = new Canvas(bm);

        if(vaViewFlag) {
            canvas.drawBitmap(tarVABM, new Matrix(), null);
        }
        if(iotViewFlag) {
            canvas.drawBitmap(tarIoTBM, new Matrix(), null);
        }


        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String strNow = sdfNow.format(date);

        String mPath = Environment.getExternalStorageDirectory().toString()
                + "/Pictures/" + strNow + ".png";

        OutputStream fout = null;
        File imageFile = new File(mPath);

        try {
            fout = new FileOutputStream(imageFile);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fout);
            fout.flush();
            fout.close();
            Toast.makeText(getApplicationContext(), "스크린샷이 저장되었습니다 : " + mPath, Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException");
            e.printStackTrace();
        }

    }

    public Bitmap getVAOverlayBitmap() {
        int va_layer = res.getIdentifier(GigaeyesConstants.VA_LAYER_ID, GigaeyesConstants.ID, this.packageName);
        RelativeLayout view = (RelativeLayout)findViewById(va_layer);

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bm = view.getDrawingCache();

        return bm;
    }
    public Bitmap getIoTOverlayBitmap() {
        int iot_layer = res.getIdentifier(GigaeyesConstants.IOT_LAYER_ID, GigaeyesConstants.ID, this.packageName);
        RelativeLayout view = (RelativeLayout)findViewById(iot_layer);

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bm = view.getDrawingCache();

        return bm;
    }

    private void finishWithError() {
        setResult(100);
        finish();
    }
}
