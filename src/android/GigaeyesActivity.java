package kr.co.anylogic.myoverlay;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GigaeyesActivity extends Activity implements TextureView.SurfaceTextureListener, View.OnTouchListener {


    public static String strjson;



    //VideoView vv;
    private MediaPlayer mp;
    private TextureView tv;
    public static String TAG = "TextureViewActivity";

//    private Renderer mRenderer;
    Window win;
    LayoutInflater inflater;
    RelativeLayout rlTop;
    RelativeLayout rlTopBack;
    RelativeLayout rlIoTDraw;
    RelativeLayout rlVADraw;
    VAView vav;
    boolean clickedFlag = false;
    boolean iotFlag = false;
    boolean vaFlag = false;

    private int activity_main;
    private int iot;
    private int back;
    private int lay_va;
    private int lay_iot;
    private int va;
    private int texture_view;
    private int btn_back;
    private int btn_onoff;
    private int btn_iot;
    private int btn_va;
    private int btn_capture;
    private int btn_star;
    private int camName;
    private int over;
    private int ico_theft;
    private int ico_door;
    private int ico_sound;
    private int ico_fire;
    private int ico_temperature;
    private int ico_humidity;
    private int ico_cameraon;
    private int ico_cameraoff;
    private int ico_star;
    private int ico_star_off;

    public String videoSrc ;
    public String cctvName ;
    public boolean iotViewFlag = false;
    public boolean vaViewFlag = false;
    public boolean favFlag = false;
    public boolean onoffFlag = true;

    ArrayList<ROI_OBJ> ROI_INFO = new ArrayList<ROI_OBJ>();
    ArrayList<ImageView> imgViews = new ArrayList<ImageView>();
    // GigaeyesActivity my;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            return;
        }

        Bundle extras  = getIntent().getExtras();
        if (extras != null) {
            videoSrc = extras.getString("VIDEO_URL");
            cctvName = extras.getString("TITLE");
            strjson = extras.getString("ROI_INFO");
            activity_main = extras.getInt("activity_main");
            over = extras.getInt("over");
            back = extras.getInt("back");
            lay_iot = extras.getInt("lay_iot");
            lay_va = extras.getInt("lay_va");
            texture_view = extras.getInt("textureView");
            back =  extras.getInt("back");
            btn_back = extras.getInt("btn_back");
            btn_onoff = extras.getInt("btn_onoff");
            btn_iot = extras.getInt("btn_iot");
            btn_va = extras.getInt("btn_va");
            btn_capture = extras.getInt("btn_capture");
            btn_star = extras.getInt("btn_star");
            va = extras.getInt("va");
            iot =  extras.getInt("iot");
            camName =  extras.getInt("camName");
            ico_theft =  extras.getInt("ico_theft"); 
            ico_door =  extras.getInt("ico_door"); 
            ico_sound =  extras.getInt("ico_sound"); 
            ico_fire =  extras.getInt("ico_fire"); 
            ico_temperature =  extras.getInt("ico_temperature"); 
            ico_humidity =  extras.getInt("ico_humidity"); 
            ico_star =  extras.getInt("ico_star"); 
            ico_star_off =  extras.getInt("ico_star_off"); 
            ico_cameraoff =  extras.getInt("ico_cameraoff"); 
            ico_cameraon =  extras.getInt("ico_cameraon"); 
            
            // image_view = extras.getInt("image_view");
            // video_view = extras.getInt("video_view");
        } else {
            finishWithError();
        }


        setContentView(activity_main);
        win = getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // my = this;

        tv = (TextureView) findViewById(texture_view);
        tv.requestFocus();

        roiParsing();

        tv.setSurfaceTextureListener(this);
//        tv.setSurfaceTextureListener(mRenderer);
        tv.setOnTouchListener(this);


    }


    class ROI_OBJ
    {
        String id;
        int type;           // 11~16 : LINE, 21~26 : 영역, 31 : text, 32 : image
        String name;        // Text String, image file fullpath
        int order_no;
        ArrayList<Point> coord;
        int colorR;
        int colorG;
        int colorB;
    }

    private static class Renderer extends Thread implements TextureView.SurfaceTextureListener {
        private Object mLock = new Object();        // guards mSurfaceTexture, mDone
        private SurfaceTexture mSurfaceTexture;
        private boolean mDone;

        private int mWidth;     // from SurfaceTexture
        private int mHeight;

        private MediaPlayer mp;

        public Renderer() {
            super("TextureViewCanvas Renderer");
        }

        @Override
        public void run() {
            while (true) {
                SurfaceTexture surfaceTexture = null;

                // Latch the SurfaceTexture when it becomes available.  We have to wait for
                // the TextureView to create it.
                synchronized (mLock) {
                    while (!mDone && (surfaceTexture = mSurfaceTexture) == null) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException ie) {
                            throw new RuntimeException(ie);     // not expected
                        }
                    }
                    if (mDone) {
                        break;
                    }
                }
                Log.d(TAG, "Got surfaceTexture=" + surfaceTexture);

                // Render frames until we're told to stop or the SurfaceTexture is destroyed.
                doAnimation();
            }

            Log.d(TAG, "Renderer thread exiting");
        }

        private void doAnimation() {
            final int BLOCK_WIDTH = 80;
            final int BLOCK_SPEED = 2;
            int clearColor = 0;
            int xpos = -BLOCK_WIDTH / 2;
            int xdir = BLOCK_SPEED;

            // Create a Surface for the SurfaceTexture.
            Surface surface = null;
            synchronized (mLock) {
                SurfaceTexture surfaceTexture = mSurfaceTexture;
                if (surfaceTexture == null) {
                    Log.d(TAG, "ST null on entry");
                    return;
                }
                surface = new Surface(surfaceTexture);
            }

            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);

            boolean partial = false;
            while (true) {
                Rect dirty = null;
                if (partial) {
                    dirty = new Rect(0, mHeight * 3 / 8, mWidth, mHeight * 5 / 8);
                }
                Canvas canvas = surface.lockCanvas(dirty);
                if (canvas == null) {
                    Log.d(TAG, "lockCanvas() failed");
                    break;
                }
                try {
                    // just curious
                    if (canvas.getWidth() != mWidth || canvas.getHeight() != mHeight) {
                        Log.d(TAG, "WEIRD: width/height mismatch");
                    }

                    canvas.drawRGB(clearColor, clearColor, clearColor);
                    canvas.drawRect(xpos, mHeight / 4, xpos + BLOCK_WIDTH, mHeight * 3 / 4, paint);
                } finally {

                    try {
                        surface.unlockCanvasAndPost(canvas);
                    } catch (IllegalArgumentException iae) {
                        Log.d(TAG, "unlockCanvasAndPost failed: " + iae.getMessage());
                        break;
                    }
                }

                // Advance state
                clearColor += 4;
                if (clearColor > 255) {
                    clearColor = 0;
                    partial = !partial;
                }
                xpos += xdir;
                if (xpos <= -BLOCK_WIDTH / 2 || xpos >= mWidth - BLOCK_WIDTH / 2) {
                    Log.d(TAG, "change direction");
                    xdir = -xdir;
                }
            }

            surface.release();
        }


        public void halt() {
            synchronized (mLock) {
                mDone = true;
                mLock.notify();
            }
        }

        @Override   
        public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable(" + width + "x" + height + ")");
            mWidth = width;
            mHeight = height;
            synchronized (mLock) {
                mSurfaceTexture = st;
                mLock.notify();
            }
        }

        @Override  
        public void onSurfaceTextureSizeChanged(SurfaceTexture st, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged(" + width + "x" + height + ")");
            mWidth = width;
            mHeight = height;
        }

        @Override  
        public boolean onSurfaceTextureDestroyed(SurfaceTexture st) {
            Log.d(TAG, "onSurfaceTextureDestroyed");

            synchronized (mLock) {
                mSurfaceTexture = null;
            }
            return true;
        }

        @Override   
        public void onSurfaceTextureUpdated(SurfaceTexture st) {
            //Log.d(TAG, "onSurfaceTextureUpdated");
        }
    }

    

    // @Override
    // public void onWindowFocusChanged(boolean hasFocus) {
    //     View layoutMainView = (View)this.findViewById(R.id.main_container);

    //     Log.w("Layout Width - ", String.valueOf(layoutMainView.getWidth()));
    //     Log.w("Layout Height - ", String.valueOf(layoutMainView.getHeight()));

    //     drawVA(layoutMainView.getWidth(), layoutMainView.getHeight());
    //     drawIoT(layoutMainView.getWidth(), layoutMainView.getHeight());
    // }


    public void drawVA(int fullWidth, int fullHeight) {
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rlVADraw = (RelativeLayout) inflater.inflate(va, null);

        if(vav == null) {
            getWindow().addContentView(rlVADraw, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            vav = new VAView(this);
            rlVADraw.addView(vav);

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
                        vav.pushLine(roiValue.type,  pts);
                    }else if(roiValue.type == 21 || roiValue.type == 22 || roiValue.type == 23 ||         // draw poligon
                            roiValue.type == 24 || roiValue.type == 25 || roiValue.type == 26  ) {
                        ArrayList<Point> pts = new ArrayList<Point>();
                        for(Point pt : roiValue.coord){
                            int x = fullWidth * pt.x / 100000;
                            int y = fullHeight * pt.y / 100000;

                            Point p = new Point(x, y);
                            pts.add(p);
                        }
                        vav.pushPolygon(roiValue.type,  pts);
                    }
                }
                vaViewFlag = true;
            }
        }
    }



    public void drawIoT(int fullWidth, int fullHeight){
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rlIoTDraw = (RelativeLayout) inflater.inflate(iot, null);

        if(imgViews.size() == 0) {
            getWindow().addContentView(rlIoTDraw, new RelativeLayout.LayoutParams(
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
                            rlIoTDraw.addView(ivg);
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
            mp = new MediaPlayer();
            mp.setDataSource(videoSrc);
            mp.setSurface(s);
            mp.prepare();

            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.start();

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
            JSONObject job = new JSONObject(strjson);   // JSONArray 생성
            JSONObject roi_info = job.getJSONObject("roi_info");
            for(int i = 0; i < roi_info.length(); i++){
                JSONArray roi_objs = roi_info.getJSONArray("roi_objs");
                for(int j = 0; j < roi_objs.length(); j++){
                    ROI_OBJ curObj = new ROI_OBJ();
                    curObj.coord = new ArrayList<Point>();
                    JSONObject roi_obj = roi_objs.getJSONObject(j);
                    curObj.id = roi_obj.getString("roi_id");
                    curObj.type = roi_obj.getInt("roi_type");
                    if(curObj.type == 10001 || curObj.type == 10002 || curObj.type == 10003 ||
                       curObj.type == 10004 || curObj.type == 10005 || curObj.type == 10006  ){
                        iotFlag = true;
                    }
                    if(curObj.type == 11 || curObj.type == 12 || curObj.type == 13 || curObj.type == 14 || curObj.type == 15 || curObj.type == 16 ||
                       curObj.type == 21 || curObj.type == 22 || curObj.type == 23 || curObj.type == 24 || curObj.type == 25 || curObj.type == 26 ){
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
                    JSONObject roid_color = roi_obj.getJSONObject("roi_color");
                    curObj.colorR = roid_color.getInt("r");
                    curObj.colorG = roid_color.getInt("g");
                    curObj.colorB = roid_color.getInt("b");

                    ROI_INFO.add(curObj);
                }
            }
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
//            Toast.makeText(MainActivity.this, "mNavHide function called", Toast.LENGTH_LONG).show();
            setOverlay(false);
        }
    };

    void setOverlay(boolean visible) {
        if (!visible) {
            ((ViewGroup) rlTop.getParent()).removeView(rlTop);
            ((ViewGroup) rlTopBack.getParent()).removeView(rlTopBack);
            clickedFlag = false;
        }

        if (visible) {
            if(clickedFlag)
                return;

            // addContentView를 호출한다.
            inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rlTopBack = (RelativeLayout)inflater.inflate(back, null);
            getWindow().addContentView(rlTopBack, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            rlTop = (RelativeLayout)inflater.inflate(over, null);
            getWindow().addContentView(rlTop, new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));


            TextView tv = (TextView)findViewById(camName);
            tv.setText(cctvName);

            ImageButton btnOnoff = (ImageButton)findViewById(btn_onoff);
            if(onoffFlag) {
                btnOnoff.setImageResource(ico_cameraon);
            }else{
                btnOnoff.setImageResource(ico_cameraoff);
            }
            ImageButton btnStar = (ImageButton)findViewById(btn_star);
            if(favFlag) {
                btnStar.setImageResource(ico_star);
            }else{
                btnStar.setImageResource(ico_star_off);
            }

//            vaFlag = false;
//            iotFlag = true;
            // iot, va view를 생성한다.
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
        Toast.makeText(this, "clickBtn1 Clicked!! 종료합니다.", Toast.LENGTH_LONG).show();
        moveTaskToBack(true);
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    void clickBtn2(){
        Toast.makeText(this, "clickBtn2 Clicked!! 녹화중임을 표시합니다.", Toast.LENGTH_LONG).show();
    }

    void clickBtn3(){
        Toast.makeText(this, "clickBtn3 Clicked!! IoT를 그립니다.", Toast.LENGTH_LONG).show();

        if(iotFlag){
            if(!iotViewFlag) {  // 그려라
                for(ImageView imgView : imgViews) {
                    imgView.setVisibility(View.VISIBLE);
                }
                iotViewFlag = true;
            }else{
                for(ImageView imgView : imgViews) {
                    imgView.setVisibility(View.INVISIBLE);
                }
                iotViewFlag = false;
            }
        }
    }

    void clickBtn4(){
        Toast.makeText(this, "clickBtn4 Clicked!! VA를 표시 합니다.", Toast.LENGTH_LONG).show();

        if(vaFlag){
            if(!vaViewFlag) {  // 그려라
                vav.setVisibility(View.VISIBLE);
                vaViewFlag = true;
            }else{
                vav.setVisibility(View.INVISIBLE);
                vaViewFlag = false;
            }
        }
    }
    void clickBtn5(){
        Toast.makeText(this, "clickBtn5 Clicked!! 스크린을 스냅샷합니다.", Toast.LENGTH_LONG).show();

        getBitmap(tv);

        Toast.makeText(getApplicationContext(), "Captured!", Toast.LENGTH_LONG).show();

    }
    void clickBtn6(){
       

        ImageButton btnStar = (ImageButton)findViewById(btn_star);
        if(favFlag) {
            btnStar.setImageResource(ico_star_off);
            Toast.makeText(this, "clickBtn6 Clicked!! 즐겨찾기를 해제합니다.", Toast.LENGTH_LONG).show();
            GigaeyesPlayer.setFavoritess(getApplicationContext(), false);
        }else{
            Toast.makeText(this, "clickBtn6 Clicked!! 즐겨찾기를 설정합니다.", Toast.LENGTH_LONG).show();
            btnStar.setImageResource(ico_star);
            GigaeyesPlayer.setFavorites(getApplicationContext(), true);
        }
        favFlag = !favFlag;
    }

    private void setViewInvalidate(View... views){
        for(View v : views){
            v.invalidate();
        }
    }



    public void getBitmap(TextureView vv) {
        Bitmap bm = vv.getBitmap();

/*
        FileOutputStream fos;
        try {
            String path2 = Environment.getExternalStorageDirectory().getAbsolutePath();
            String appPath = path2 + "/data/data/kr.co.anylogic.tv1";
            File af = new File(appPath);

            boolean ret = af.mkdirs();

            String fname = appPath + "capture.jpeg";
            File ff = new File(af, "capture.jpeg");
            try {
                ff.createNewFile();
            } catch (IOException ie) {
                ie.printStackTrace();
            }

            fos = new FileOutputStream(ff);

            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        Bitmap tarVABM = getVAOverlayBitmap();
        Bitmap tarIoTBM = getIoTOverlayBitmap();


        //상단 비트맵에 알파값을 적용하기 위한 Paint
//        Paint alphaPaint = new Paint();
//        alphaPaint.setAlpha(125);

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
        Toast.makeText(getApplicationContext(), "Capturing Screenshot: " + mPath, Toast.LENGTH_SHORT).show();

        OutputStream fout = null;
        File imageFile = new File(mPath);

        try {
            fout = new FileOutputStream(imageFile);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fout);
            fout.flush();
            fout.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException");
            e.printStackTrace();
        }

    }

    public Bitmap getVAOverlayBitmap() {
        RelativeLayout view = (RelativeLayout)findViewById(lay_va);

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bm = view.getDrawingCache();

        return bm;
    }
    public Bitmap getIoTOverlayBitmap() {
        RelativeLayout view = (RelativeLayout)findViewById(lay_iot);

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
