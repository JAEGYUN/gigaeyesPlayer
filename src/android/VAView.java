package kr.co.anylogic.mediaplayer;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import android.view.View;

import java.util.ArrayList;
import android.util.Log;

public class VAView extends View {
    String TAG = "VAView";
    Canvas ccv;
    Bitmap lineIn;
    Bitmap lineOut;
    Bitmap lineInOut;
    Bitmap lineLeft;
    Bitmap lineRight;
    Bitmap lineLeftRight;
    private class Line {
        public int type;
        ArrayList<Point> points;

        Line(int t, ArrayList<Point> pts){
            type = t;
            points = pts;
        }
        private void draw(Canvas cv){
            // 라인대신 이미지로 대체
            Bitmap dimg;
            if(type == 11 || type == 14){               // In
                dimg = lineLeft;
            }else if(type == 12 || type == 15){         // Out
                dimg = lineOut;
            }else {
                dimg = lineInOut;
            }

            Point p1 = points.get(0);
            Point p2 = points.get(1);
//          두점 사이의 기울기 계산
            double theta = Math.atan2((p2.y - p1.y), (p2.x - p1.x));
            double degree = (theta*180)/Math.PI;
//          두점 사이의 길이 계산 --> 이미지 사이즈 변경기준
            double distance = Math.sqrt(Math.pow(Math.abs(p2.x-p1.x),2)+Math.pow(Math.abs(p2.y-p1.y),2));
//            Log.d("두점사이의 거리","p1("+p1.x+","+p1.y+"), p2("+p2.x+","+p2.y+")-->"+distance+", bitmap.width"+dimg.getWidth());
//            Log.d("두점사이의 기울기","p1("+p1.x+","+p1.y+"), p2("+p2.x+","+p2.y+")-->"+theta+", degree"+degree);
            Paint pa = new Paint();
            pa.setARGB(0xff, 0xf0, 0x46, 0x46);
            pa.setStrokeWidth(12f);
//          가운데 점 구하기
            int x1 = p1.x+(p2.x-p1.x)/2;
            int y1 = p1.y+(p2.y-p1.y)/2-20;

//            Log.d("두점사이의 가운데 점","p3("+x1+","+y1+")");
            cv.drawLine(p1.x, p1.y, p2.x, p2.y, pa);


            Log.d(TAG, "distance::"+distance+", to Int:"+(int)distance);
            Matrix matrix = new Matrix();
            matrix.postRotate((float)degree);
            matrix.postTranslate(x1, y1);

            cv.drawBitmap(dimg,matrix,null);
        }
    }

    private class Poly {
        public int type;
        public ArrayList<Point> points;
        Poly(int t, ArrayList<Point> p){
            type = t;
            points = p;
        }
        private void draw(Canvas cv){
            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            int i = 0;
            for(Point pt : points) {
                if(i == 0){
                    path.moveTo(pt.x, pt.y);
                    i++;
                    continue;
                }
                path.lineTo(pt.x, pt.y);
            }
            path.close();

            Paint paint = new Paint();
            if(type == 22){
                paint.setColor(Color.parseColor("#46ff962e"));
            }else if(type == 23){
                paint.setColor(Color.parseColor("#46a75bcb"));
            }else if(type == 24){
                paint.setColor(Color.parseColor("#46ff962e"));
            }else if(type == 25){
                paint.setColor(Color.parseColor("#465aaac7"));
            }else if(type == 26){
                paint.setColor(Color.parseColor("#46969696"));
            }else {     // 21
                paint.setColor(Color.parseColor("#4636b255"));
            }
            cv.drawPath(path, paint);


            Paint paint1 = new Paint();
            paint1.setColor(Color.RED);
            paint1.setStrokeWidth(5);
            paint.setStyle(Paint.Style.STROKE);

            int i2 = 0;
            Point spt =  new Point();
            Point ept ;
            for(Point pt : points) {
                if(i2 == 0){
                    spt = pt;
                    i2++;
                    continue;
                }
                ept = pt;
                cv.drawLine(spt.x, spt.y, ept.x, ept.y, paint1);
                spt = ept;
            }
            ept = points.get(0);
            cv.drawLine(spt.x, spt.y, ept.x, ept.y, paint1);
        }
    }

    ArrayList<Object> objects = new ArrayList<Object>();

    public VAView(Activity a){
        super(a);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        Resources res = a.getApplication().getResources();
        String packageName = a.getApplication().getPackageName();

        int ico_red_in = res.getIdentifier(GigaeyesConstants.image.ICO_RED_IN, GigaeyesConstants.IMAGE, packageName);
        int ico_red_out = res.getIdentifier(GigaeyesConstants.image.ICO_RED_OUT, GigaeyesConstants.IMAGE, packageName);
        int ico_red_inout = res.getIdentifier(GigaeyesConstants.image.ICO_RED_IN_OUT, GigaeyesConstants.IMAGE, packageName);
        int ico_arrow_left = res.getIdentifier(GigaeyesConstants.image.ICO_ARROW_LEFT, GigaeyesConstants.IMAGE, packageName);
        int ico_arrow_right = res.getIdentifier(GigaeyesConstants.image.ICO_ARROW_RIGHT, GigaeyesConstants.IMAGE, packageName);
        int ico_arrow_left_right = res.getIdentifier(GigaeyesConstants.image.ICO_ARROW_LEFT_RIGHT, GigaeyesConstants.IMAGE, packageName);


        lineIn = BitmapFactory.decodeResource(res, ico_red_in);
        lineOut = BitmapFactory.decodeResource(res, ico_red_out);
        lineInOut = BitmapFactory.decodeResource(res, ico_red_inout);
        lineLeft = BitmapFactory.decodeResource(res, ico_arrow_left);
        lineRight = BitmapFactory.decodeResource(res, ico_arrow_right);
        lineLeftRight = BitmapFactory.decodeResource(res, ico_arrow_left_right);


    }

    @Override
    protected void onDraw(Canvas cv){
        ccv = cv;
        for(Object o : objects){
            if(o instanceof Line){
                ((Line)o).draw(cv);
            }
            if(o instanceof Poly){
                ((Poly)o).draw(cv);
            }
        }

        super.onDraw(cv);
    }

    public void pushLine(int type, ArrayList<Point> points){
        Line line = new Line(type, points);
        objects.add(line);
    }

    public void pushPolygon(int type, ArrayList<Point> points){
        Poly poly = new Poly(type, points);
        objects.add(poly);
    }

}
