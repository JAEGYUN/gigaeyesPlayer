package kr.co.anylogic.myoverlay;

import android.app.Activity;
import android.os.Bundle;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by anpro on 2017-07-04.
 */

public class VAView extends View {
    Canvas ccv;
    Bitmap lineIn;
    Bitmap lineOut;
    Bitmap lineInOut;
    Bitmap lineLeft;
    Bitmap lineRight;
    Bitmap lineLeftRight;
    class Line extends Object {
        public int type;
        ArrayList<Point> points;

        Line(int t, ArrayList<Point> pts){
            type = t;
            points = pts;
        }
        public void draw(Canvas cv){
            // 라인대신 이미지로 대체
            Paint paint = new Paint();
            Bitmap dimg = null;
            if(type == 11 || type == 14){               // In
                dimg = lineRight;
            }else if(type == 12 || type == 15){         // Out
                dimg = lineLeft;
            }else {
                dimg = lineLeftRight;
            }

            Point p1 = points.get(0);
            Point p2 = points.get(1);
            double theta = Math.atan2((p2.y - p1.y), (p2.x - p1.x));

            Paint pa = new Paint();
            pa.setARGB(0xff, 0xf0, 0x46, 0x46);
            pa.setStrokeWidth(3f);

            cv.drawLine(p1.x, p1.y, p2.x, p2.y, pa);
            int w = dimg.getWidth() * 4;
            int h = dimg.getHeight() * 4;
            int x1 = (int)((p1.x + p2.x)/2 - w/2);
            int y1 = (int)((p1.y + p2.y)/2 - h/2);
            int x2 = (int)((p1.x + p2.x)/2 + w/2);
            int y2 = (int)((p1.y + p2.y)/2 + h/2);
            Rect dst = new Rect(x1, y1, x2, y2);

            Matrix matrix = new Matrix();
            matrix.postRotate((float)(theta*180/Math.PI + 90));
            Bitmap ro = Bitmap.createBitmap(dimg, 0, 0, dimg.getWidth(), dimg.getHeight(), matrix, true);

            int w1 = ro.getWidth() * 4;
            int h1 = ro.getHeight() * 4;
            int x3 = (int)((p1.x + p2.x)/2 - w1/2);
            int y3 = (int)((p1.y + p2.y)/2 - h1/2);
            int x4 = (int)((p1.x + p2.x)/2 + w1/2);
            int y4 = (int)((p1.y + p2.y)/2 + h1/2);
            Rect dst1 = new Rect(x1, y1, x2, y2);

            cv.drawBitmap(ro, null, dst1, null);
        }
    }

    class Poly extends Object {
        public int type;
        public ArrayList<Point> points;
        Poly(int t, ArrayList<Point> p){
            type = t;
            points = p;
        }
        public void draw(Canvas cv){
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
                paint.setColor(Color.parseColor("#90ff962e"));
            }else if(type == 23){
                paint.setColor(Color.parseColor("#90a75bcb"));
            }else if(type == 24){
                paint.setColor(Color.parseColor("#90ff962e"));
            }else if(type == 25){
                paint.setColor(Color.parseColor("#905aaac7"));
            }else if(type == 26){
                paint.setColor(Color.parseColor("#90BAB399"));
            }else {     // 21
                paint.setColor(Color.parseColor("#90868686"));
            }
            cv.drawPath(path, paint);


            Paint paint1 = new Paint();
            paint1.setColor(Color.RED);
            paint1.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);

            Path path2 = new Path();
            int i2 = 0;
            Point spt =  new Point();
            Point ept = new Point();
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

    ArrayList<Line> lines = new ArrayList<Line>();
    ArrayList<Poly> polies = new ArrayList<Poly>();
    ArrayList<Object> objects = new ArrayList<Object>();

    public VAView(Activity a){
        super(a);
        Resources r = getResources();
        Bundle extras  = a.getIntent().getExtras();
        int ico_red_in = extras.getInt("ico_red_in");
        int ico_red_out = extras.getInt("ico_red_out");
        int ico_red_inout = extras.getInt("ico_red_inout");
        int ico_arrow_left = extras.getInt("ico_arrow_left");
        int ico_arrow_right = extras.getInt("ico_arrow_right");
        int ico_arrow_leftright = extras.getInt("ico_arrow_leftright");

        lineIn = BitmapFactory.decodeResource(r, ico_red_in);
        lineOut = BitmapFactory.decodeResource(r, ico_red_out);
        lineInOut = BitmapFactory.decodeResource(r, ico_red_inout);
        lineLeft = BitmapFactory.decodeResource(r, ico_arrow_left);
        lineRight = BitmapFactory.decodeResource(r, ico_arrow_right);
        lineLeftRight = BitmapFactory.decodeResource(r, ico_arrow_leftright);
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
