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
            if(type == 11 || type == 14 || type == 18 || type == 102){               // In
                dimg = lineIn;
            }else if(type == 12 || type == 15 || type == 19){         // Out
                dimg = lineOut;
            }else {
                dimg = lineInOut;
            }
            Point p1 = points.get(0);
            Point p2 = points.get(1);
            double theta = Math.atan2((p2.y - p1.y), (p2.x - p1.x));
            double degree = (theta*180)/Math.PI;
//          두점 사이의 길이 계산 --> 이미지 사이즈 변경기준
            double distance = Math.sqrt(Math.pow(Math.abs(p2.x-p1.x),2)+Math.pow(Math.abs(p2.y-p1.y),2));
            Paint pa = new Paint();
            pa.setARGB(0xff, 0xf0, 0x46, 0x46);
            pa.setStrokeWidth(12f);
//          가운데 점 구하기
            double z = degree / 90+1; // 1,2,-1,-2
            double a = degree % 90;
            double buf = 20 - (20/90*a);
            int x1 = p1.x+(p2.x-p1.x)/2;
            int y1 = p1.y+(p2.y-p1.y)/2;

//          기울기 및 좌표계에 따라 가운데 화살표 위치 변경
            if(Math.abs(degree)>=0 && Math.abs(degree)<90){

                if(Math.abs(degree)>0){
                    x1 = x1 - (int)buf;
                    y1 = y1 - (int)buf;
                 }else{
                    y1 = y1 - (int)buf;
                }


            }else{

                if(Math.abs(degree) > 90){
                    y1 = y1 + (int) buf;
                    if(z>0){
                        x1 = x1 +(int)buf;
                    }else{
                        x1 = x1 -(int)buf;
                    }

                }else if(Math.abs(degree) == 180){
                    y1 = y1 + (int) buf;
                }else{
                    if(z > 0){
                        x1 = x1 + (int)buf;
                    }else{
                        x1 = x1 - (int)buf;
                    }
                }

            }

//            cv.drawLine(p1.x, p1.y, p2.x, p2.y, pa);


            Log.d(TAG, "distance::"+distance+", to Int:"+(int)distance);
            Log.d(TAG, "degree::"+degree+", to Int:"+(int)degree);
            /** 가로(distance)를 기준으로 Resize */
            double aspectRatio = (double) dimg.getHeight() / (double) dimg.getWidth();
            int targetHeight = (int) (distance*aspectRatio);
            
            dimg = Bitmap.createScaledBitmap(dimg, (int)distance, targetHeight, false);


            Matrix matrix = new Matrix();
            matrix.postRotate((float)degree);
            matrix.postTranslate(x1, y1);

            cv.drawBitmap(dimg,matrix,null);
        }
    }

    private class Poly {
        public int type;
        public ArrayList<Point> points;
        private int color = Color.parseColor("#46FF962E");
        private Paint fillPaint;
        private Paint linePaint;
        Poly(int t, ArrayList<Point> p){
            this.type = t;
            this.points = p;
            if(type == 22){
                this.color = Color.parseColor("#46FF962E");
            }else if(type == 23){
                this.color = Color.parseColor("#46A75BCB");
            }else if(type == 24){
                this.color = Color.parseColor("#46FF962E");
            }else if(type == 25){
                this.color = Color.parseColor("#465AAAC7");
            }else if(type == 26){
                this.color = Color.parseColor("#46969696");
            }else {     // 21
                this.color = Color.parseColor("#4636B255");
            }
            this.fillPaint = new Paint();
            this.fillPaint.setColor(this.color);
            this.fillPaint.setStyle(Paint.Style.FILL);
            this.linePaint = new Paint();
            this.linePaint.setColor(Color.RED);
            this.linePaint.setStrokeWidth(3);
            this.linePaint.setStyle(Paint.Style.STROKE);
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

            cv.drawPath(path, this.fillPaint);

//영역 외곽선 그리기
//            Paint paint1 = new Paint();
//            paint1.setColor(Color.RED);
//            paint1.setStrokeWidth(1);
//            paint.setStyle(Paint.Style.STROKE);

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
                cv.drawLine(spt.x, spt.y, ept.x, ept.y, this.linePaint);
                spt = ept;
            }
            ept = points.get(0);
            cv.drawLine(spt.x, spt.y, ept.x, ept.y, this.linePaint);
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
                Log.d(TAG,"color >>>"+((Poly) o).color);
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
