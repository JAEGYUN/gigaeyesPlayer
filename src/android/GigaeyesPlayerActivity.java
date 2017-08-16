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
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
//import android.widget.SeekBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import android.net.Uri;
public class GigaeyesPlayerActivity extends Activity implements IVLCVout.Callback, TextureView.SurfaceTextureListener, View.OnTouchListener {

    //기본 위젯 설정및 변수 설정
    private LibVLC libvlc;
//    private SeekBar mSeekBar;

    private String roi_info ="[]";
    private String sensor_info = "[]";

    private String packageName;
    private Resources res;

//  안드로이드 MediaPlayer에서 VLC Player로 변경
    private MediaPlayer mediaPlayer;
    private TextureView textureView;

    public static String TAG = "GigaeyesPlayerActivity";

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

    SeekBar brightnessBar;
    int brightnessCount = 100;
    private TextView brightStatus;

    SeekBar volumeBar;
    int volumeCount = 0;
    private TextView volumeStatus;

    private String videoSrc = "";
    private String cctvName = "";
    public static boolean iotViewFlag = false;
    public static boolean vaViewFlag = false;
    public static boolean favFlag = false;
    public static boolean onoffFlag = true;

    ArrayList<ROI_OBJ> ROI_INFO = new ArrayList<ROI_OBJ>();
    ArrayList<ImageView> imgViews = new ArrayList<ImageView>();


    class ROI_OBJ
    {
        String id;
        int type;           // 11~16 : LINE, 21~26 : 영역, 31 : text, 32 : image
        String name;        // Text String, image file fullpath
        int order_no;
        ArrayList<Point> coord;

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
            this.cctvName = extras.getString(GigaeyesConstants.VIDEO_TITLE);
            this.roi_info = extras.getString(GigaeyesConstants.ROI_INFO);
            this.sensor_info = extras.getString(GigaeyesConstants.SENSOR_INFO);
            this.onoffFlag = extras.getString(GigaeyesConstants.REC_STATUS) != null
                    && GigaeyesConstants.STREAM_VALID_STATUS.equals(extras.getString(GigaeyesConstants.REC_STATUS));
            this.favFlag = extras.getString(GigaeyesConstants.FAVORITES) != null
                    && GigaeyesConstants.FAVORITES_ON.equals(extras.getString(GigaeyesConstants.FAVORITES));
        } else {
           finishWithError();
        }



        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        setContentView(main_layout);

        textureView = (TextureView) findViewById(texture_view);
        textureView.requestFocus();

        roiParsing();

        /*
         * MediaPlayer의 메소드 호출 순서 및 방법
         * 1. setDataSource -> initilalize
         * 2. prepare()/prepareAsync() 호출 --> prepared (스트림을 재생할 수 있는 상태)
         * 3. started. stoped, paused 에서 prepare()/prepareAsync() 호출  --> stop() --> prepared (position 0 : 다시 스트림을 재생할 수 있는 상태
         * 4. prepared, started, paused, playbackCompleted --> seekTo()
         * 5. datasource를 재정의하고자 하는 경우 reset 호출후 재정의해야 함
         * 6. relase() - End . Idle 상태로 다시 갈 수 없으며 객체를 생성하지 않는 한 MediaPlayer는 어떤 파일이나 스트림도 재생할 수 없음
         *
         * TextureView를 통한 MediaPlayer 재생 처리
         * 1. MediaPlayer의 setSurface 활용 : 비디오인 경우 사용할 수 있으며 Surface객체를 받아 MediaPlayer의 스트림을 해당 Surface에 그리며
         *    Surface는 SurfaceTexture를 사용하여 생성
         * 2. TextureView에서 setSurfaceTextureListener를 해주어 SurfaceTexture cycle과 MediaPlayer가 밀접하게 동작할 수 있도록 설정 필요
         */
        textureView.setSurfaceTextureListener(this);
        textureView.setOnTouchListener(this);

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

//      TextureView에서 SurfaceTexture를 사용할 준비가 되어있음을 의미
//      SurfaceTexture는 HardwareLayer를 가져오는 메소드 내부에서 생성되며 ,
//      onSurfaceTextureAvailableonSurfaceTextureAvailable는 생성 후 호출된다
        Log.v(TAG, "Surface is start");
        createPlayer();

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
//        TextureView의 width, height 변경시 호출되며 SurfaceTexture의 Buffer Size가 바뀌었음을 의미한다
        Log.v(TAG, "Surface is changed");
    }


    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
//      SurfaceTextView가 종료될 때 플레이어를 release 한다
        Log.v(TAG, "Surface is destroyed");


        if(mediaPlayer == null){
            return true;

        }

        Log.d(TAG,"Player is released!!!");
        mediaPlayer.release();
        mediaPlayer = null;

        return true;

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
//        int main_container = res.getIdentifier(GigaeyesConstants.MAIN_CONTAINER, GigaeyesConstants.ID, this.packageName);
//        int texture_view = res.getIdentifier(GigaeyesConstants.TEXTURE_VIEW, GigaeyesConstants.ID, this.packageName);
//        View layoutMainView = (TextureView).findViewById(texture_view);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;

//        Log.w("Layout Width - ", String.valueOf(layoutMainView.getWidth()));
//        Log.w("Layout Height - ", String.valueOf(layoutMainView.getHeight()));

        drawVA(width, height);
        drawIoT(width, height);
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

            if(vaFlag) {
                for(ROI_OBJ roiValue : ROI_INFO) {
                    if(roiValue.type == 11 || roiValue.type == 12 || roiValue.type == 13 ||         // draw line 모두 빨강색으로
                        roiValue.type == 14 || roiValue.type == 15 || roiValue.type == 16 ||
                        roiValue.type == 18 || roiValue.type == 19 || roiValue.type == 102 ){
                        ArrayList<Point> pts = new ArrayList<Point>();

                        for(Point pt : roiValue.coord){
                            int x = fullWidth * pt.x / 100000;
                            int y = fullHeight * pt.y / 100000;
                            Point p = new Point(x, y);
                            pts.add(p);
                        }
                        vaView.pushLine(roiValue.type,  pts);
                    }else if(roiValue.type == 21 || roiValue.type == 22 || roiValue.type == 23 ||         // draw poligon
                            roiValue.type == 24 || roiValue.type == 25 || roiValue.type == 26 ||
                            roiValue.type == 105 || roiValue.type == 106 || roiValue.type == 108 ||
                            roiValue.type == 109 || roiValue.type == 110 || roiValue.type == 111 ||
                            roiValue.type == 112 ) {
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
                            int x = fullWidth * pt.x  /100000;
                            int y = fullHeight * pt.y / 100000;

                            params.setMargins(x-GigaeyesConstants.SENSOR_REAL_SIZE, y-GigaeyesConstants.SENSOR_REAL_SIZE, 0, 0);
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
                                Log.d(TAG,"요청유형의 센서는 지원되지 않습니다. ["+roiValue.type+"]");
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
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    void roiParsing(){
        try {
            // ROI INFO parsing.
            JSONArray roi_objs = new JSONArray(this.roi_info); 
            Log.d(TAG, "ROI FLAG ::: jsonstr(roi)>>>"+this.roi_info);
            for(int j = 0; j < roi_objs.length(); j++){
                ROI_OBJ curObj = new ROI_OBJ();
                curObj.coord = new ArrayList<Point>();
                JSONObject roi_obj = roi_objs.getJSONObject(j);
                curObj.id = roi_obj.getString("roi_id");
                curObj.type = Integer.parseInt(roi_obj.getString("roi_type"));
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
            JSONArray sensor_objs = new JSONArray(this.sensor_info);
            Log.d(TAG, "jsonstr(roi)>>>"+this.sensor_info);
            for(int j = 0; j < sensor_objs.length(); j++){
                ROI_OBJ curObj = new ROI_OBJ();
                curObj.coord = new ArrayList<Point>();
                JSONObject roi_obj = sensor_objs.getJSONObject(j);
                curObj.id = roi_obj.getString("sensor_id");
                curObj.type = Integer.parseInt(roi_obj.getString("sensor_tag_cd"));
                if(j == 0 ){
                    iotFlag = true;
                }

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


    void enrollButton(){
//        버튼
        int btn_back = res.getIdentifier(GigaeyesConstants.button.BACK, GigaeyesConstants.ID, this.packageName);
        int btn_on_off = res.getIdentifier(GigaeyesConstants.button.ONOFF, GigaeyesConstants.ID, this.packageName);
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

        ImageButton btn2 = (ImageButton)findViewById(btn_on_off);
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
            setOverlay(true);
            return true;
        }
        return false;
    }

    Runnable mNavHider = new Runnable() {
        @Override
        public void run() {
            setOverlay(false);
        }
    };

    void setOverlay(boolean visible) {

//   버튼 타이틀 바 overlay 설정.
        if (!visible) {
            ((ViewGroup) rlTop.getParent()).removeView(rlTop);
            ((ViewGroup) backLayout.getParent()).removeView(backLayout);
            clickedFlag = false;
        }

        if (visible) {
            if(clickedFlag)
                return;
            int brightnessId =res.getIdentifier(GigaeyesConstants.BRIGHTNESS_BAR, GigaeyesConstants.ID, this.packageName);
            int brightStatusId =res.getIdentifier(GigaeyesConstants.BRIGHTNESS_STATUS, GigaeyesConstants.ID, this.packageName);

            int volumeBarId =res.getIdentifier(GigaeyesConstants.VOLUME_BAR, GigaeyesConstants.ID, this.packageName);
            int volumeStatusId =res.getIdentifier(GigaeyesConstants.VOLUME_STATUS, GigaeyesConstants.ID, this.packageName);

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
            int ico_iot_on = res.getIdentifier(GigaeyesConstants.image.ICO_IOT, GigaeyesConstants.IMAGE, this.packageName);
            int ico_iot_off = res.getIdentifier(GigaeyesConstants.image.ICO_IOT_OFF, GigaeyesConstants.IMAGE, this.packageName);
            int ico_va_on = res.getIdentifier(GigaeyesConstants.image.ICO_VA, GigaeyesConstants.IMAGE, this.packageName);
            int ico_va_off = res.getIdentifier(GigaeyesConstants.image.ICO_VA_OFF, GigaeyesConstants.IMAGE, this.packageName);

            backLayout = (RelativeLayout)inflater.inflate(back_layout, null);
            getWindow().addContentView(backLayout, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            rlTop = (RelativeLayout)inflater.inflate(control_overlay, null);
            getWindow().addContentView(rlTop, new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));


            TextView tv = (TextView)findViewById(title);
            tv.setText(this.cctvName);

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

            // iot_layer, va_layer view를 생성한다.
            if (vaFlag) {
                ImageButton btnva = (ImageButton)findViewById(btn_va);
//              IoT버튼이 없으면 위치 한칸 이동
                if(!iotFlag){
                    ImageButton btniot = (ImageButton)findViewById(btn_iot);
                    btnva.setLayoutParams(btniot.getLayoutParams());
                }
                if(vaViewFlag){
                    btnva.setImageResource(ico_va_on);
                }else{
                    btnva.setImageResource(ico_va_off);
                }
                btnva.setVisibility(View.VISIBLE);
            }else{
                ImageButton btnva = (ImageButton)findViewById(btn_va);
                btnva.setVisibility(View.INVISIBLE);
            }

            if (iotFlag) {
                ImageButton btniot = (ImageButton)findViewById(btn_iot);

                if(iotViewFlag){
                    btniot.setImageResource(ico_iot_on);
                }else{
                    btniot.setImageResource(ico_iot_off);
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
                h.postDelayed(mNavHider, GigaeyesConstants.DELAY_TIME);
            }
            clickedFlag = true;



            brightnessBar = (SeekBar)findViewById(brightnessId);

            brightnessBar.setProgress(this.brightnessCount) ;


            this.brightStatus = (TextView)findViewById(brightStatusId);
            brightStatus.setText(this.brightnessCount+" %");

            brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
                @Override
                public void onStartTrackingTouch(SeekBar seekBar){

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar){

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                    if(progress < 10){
                        progress = 10;
                        seekBar.setProgress(progress);
                    }

                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.screenBrightness = (float) progress /100;
                    getWindow().setAttributes(params);

                    brightStatus.setText(progress+" %");
                    brightnessCount = progress;

                }
            });

            volumeBar = (SeekBar)findViewById(volumeBarId);

            volumeBar.setProgress(this.volumeCount) ;
            mediaPlayer.setVolume(this.volumeCount) ;
            this.volumeStatus = (TextView)findViewById(volumeStatusId);
            volumeStatus.setText(this.volumeCount+" %");

            volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
                @Override
                public void onStartTrackingTouch(SeekBar seekBar){

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar){

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){

                    mediaPlayer.setVolume(progress);

                    volumeStatus.setText(progress+" %");
                    volumeCount= progress;

                }
            });

        }
    }




    void clickBtn1(){
//      뒤로가기
        releasePlayer();
        finish();
    }

    void clickBtn2(){
//        Toast.makeText(this, "clickBtn2 Clicked!! 녹화중임을 표시합니다.", Toast.LENGTH_LONG).show();
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
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.KOREA);
        String strNow = sdfNow.format(date);

        String mPath = Environment.getExternalStorageDirectory().toString()
                + "/Pictures/" + strNow + ".png";

        OutputStream fout ;
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
        return view.getDrawingCache();

    }

    public Bitmap getIoTOverlayBitmap() {
        int iot_layer = res.getIdentifier(GigaeyesConstants.IOT_LAYER_ID, GigaeyesConstants.ID, this.packageName);
        RelativeLayout view = (RelativeLayout)findViewById(iot_layer);

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        return view.getDrawingCache();

    }

    private void finishWithError() {
        setResult(100);
        finish();
    }

    @Override
    public void onSurfacesCreated(IVLCVout ivlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout ivlcVout) {

    }

    @Override //VLC 레이아웃 설정
    public void onNewLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {

    }

    @Override  //하드웨어 가속 에러시 플레이어 종료
    public void onHardwareAccelerationError(IVLCVout vout) {
        releasePlayer();
        Toast.makeText(this, "Error with hardware acceleration", Toast.LENGTH_LONG).show();
    }


    //VLC 플레이어 실행
    private void createPlayer() {
        releasePlayer();
        try {
//            if (this.videoSrc.length() > 0) {
//                Toast toast = Toast.makeText(this, this.videoSrc, Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
//                        0);
//                toast.show();
//            }

            // Create LibVLC
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
//            options.add("--aout=opensles");
            options.add("--rtsp-tcp"); // time stretching
            options.add("-vvv"); // verbosity
            libvlc = new LibVLC(options);

            textureView.setKeepScreenOn(true);

            // Create media player
            mediaPlayer = new MediaPlayer(libvlc);

            // Set up video output
            final IVLCVout vout = mediaPlayer.getVLCVout();
            vout.setVideoView(textureView);
            vout.addCallback(this);
            vout.attachViews();
            Uri url = Uri.parse(this.videoSrc);
            Media m = new Media(libvlc, url);
            mediaPlayer.setMedia(m);
            mediaPlayer.play();

        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    //플레이어 종료
    private void releasePlayer() {
        Log.d(TAG, "player release!!!");
        if (libvlc == null)
            return;
        mediaPlayer.stop();
        final IVLCVout vout = mediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        libvlc.release();
        libvlc = null;

    }

}
