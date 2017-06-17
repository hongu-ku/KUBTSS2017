package com.example.hongu.apaapa;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by hongu on 2016/12/21.
 */

public class GraphView extends View {
    protected int X_SIZE = 270;//GraphSize
    protected int Y_SIZE = 180;//GraphSize
    protected int x0, y0;
    protected double vmax;
    protected double vmin;
    protected double[] value = new double[X_SIZE];
    protected String unit, title;
    protected int type;

    public double getV() {
        return v;
    }

    public void setV(double v) {
        this.v = v;
    }

    public double v;

    public GraphView(Context context) {
        super(context);
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attrsArray = context.obtainStyledAttributes(attrs, R.styleable.GraphView);
        this.x0 = attrsArray.getInteger(R.styleable.GraphView_x0, 1);
        this.y0 = attrsArray.getInteger(R.styleable.GraphView_y0, 1);
        this.vmin = attrsArray.getFloat(R.styleable.GraphView_vmin, 0.1f);
        this.vmax = attrsArray.getFloat(R.styleable.GraphView_vmax, 0.1f);
        this.unit = attrsArray.getString(R.styleable.GraphView_unit);
        this.title = attrsArray.getString(R.styleable.GraphView_title);
        this.type = attrsArray.getInteger(R.styleable.GraphView_type, 1);
       // this.v=attrsArray.getFloat(R.styleable.GraphView_v, 0.1f);
        attrsArray.recycle();
    }

    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray attrsArray = context.obtainStyledAttributes(attrs, R.styleable.GraphView, defStyle, 0);
        this.x0 = attrsArray.getInteger(R.styleable.GraphView_x0, 1);
        this.y0 = attrsArray.getInteger(R.styleable.GraphView_y0, 1);
        this.vmin = attrsArray.getFloat(R.styleable.GraphView_vmin, 0.1f);
        this.vmax = attrsArray.getFloat(R.styleable.GraphView_vmax, 0.1f);
        this.unit = attrsArray.getString(R.styleable.GraphView_unit);
        this.title = attrsArray.getString(R.styleable.GraphView_title);
        this.type = attrsArray.getInteger(R.styleable.GraphView_type, 1);
        attrsArray.recycle();
    }

    /*public GraphView(Context context, int x0, int y0, float vmin, float vmax, String unit, String title) {
        super(context);
        this.x0 = x0;
        this.y0 = y0;
        this.vmin = vmin;
        this.vmax = vmax;
        this.unit = unit;
        this.title = title;
    }*/
    Paint paint1 = new Paint();
    Paint paint2 = new Paint();
    Paint paint3 = new Paint();


    public void setSize(int X_SIZE, int Y_SIZE) {
        this.X_SIZE = X_SIZE;
        this.Y_SIZE = Y_SIZE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.scale(0.7f,0.7f);
        canvas.translate(10, 30);
        this.drawText(canvas, v);
       // this.drawOption(canvas);
       // this.drawGraph(v, canvas);
    }

    // TODO: 見やすいテキスト形式は？
    public void drawText(Canvas canvas, double v){
        paint1.setColor(Color.RED);
        paint1.setStrokeWidth(6);
        paint1.setTextSize(50);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setAntiAlias(true);
        paint1.setTextAlign(Paint.Align.RIGHT);


        paint2.setColor(Color.BLACK);
        paint2.setStrokeWidth(8);
        paint2.setTextSize(195);
        paint2.setStyle(Paint.Style.FILL_AND_STROKE);
        paint2.setAntiAlias(true);
        //paint2.setTextAlign(Paint.Align.CENTER);

        canvas.drawRect(0,0,330,250,paint1);
        if (type == 0) {
            canvas.drawText((int) v + "", 0, 170, paint2);
            canvas.drawText(unit, 320, 200, paint1);
        }  else if (type == 1) {
            canvas.drawText(String.format("%.1f", v) + "", 0, 170, paint2);
            canvas.drawText(unit, 320, 230, paint1);
        }
    }

    // 今回は使わない可能性が高いが、メーターを使うとき用
    public void drawOption(Canvas canvas) {

        paint1.setColor(Color.BLACK);
        paint1.setStrokeWidth(2);
        paint1.setTextSize(27);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setAntiAlias(true);

        paint2.setColor(Color.BLACK);
        paint2.setStrokeWidth(3);
        paint2.setTextSize(27);
        paint2.setStyle(Paint.Style.FILL_AND_STROKE);
        paint2.setAntiAlias(true);

        paint3.setColor(Color.BLACK);
        paint3.setStrokeWidth(5);
        paint3.setTextSize(50);
        paint3.setStyle(Paint.Style.FILL_AND_STROKE);
        paint3.setAntiAlias(true);
        canvas.drawText(title, 0, 0, paint2);

        /*Inclement inclement = new Inclement("サンプル", 100);
        inclement.start();*/


        if (type == 0)
            canvas.drawText(v + " " + unit, X_SIZE / 2, Y_SIZE /2, paint3);
        else if (type == 1)
            canvas.drawText(String.format("%.2f", v) + " " + unit, X_SIZE / 2, Y_SIZE / 2, paint3);
        paint1.setTextSize(12);
        canvas.drawText((int) vmin + unit, X_SIZE + 3, Y_SIZE + 3, paint2);
        canvas.drawText((int) vmax + unit, X_SIZE / 4, 2, paint2);
        RectF rectf = new RectF(0, 0, 2 * Y_SIZE, 2 * Y_SIZE);
        rectf.offset(-Y_SIZE / 2, 0);
        canvas.drawArc(rectf, 270, 90, true, paint1);
    }

    public void drawGraph(double v, Canvas canvas) {
        //pushMatrix(); //storing current coordinate
        //translate(x0,y0);
        Paint paint2 = new Paint();
        paint2.setColor(Color.RED);
        paint2.setStrokeWidth(3);
        paint2.setTextSize(27);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setAntiAlias(true);

        double pi = 3.14159;

        double Rad =  pi * v / (2 * vmax);
        double _x = X_SIZE - Y_SIZE + Y_SIZE * Math.cos(Rad);
        double _y = Y_SIZE - Y_SIZE  * Math.sin(Rad);

        canvas.drawLine(X_SIZE - Y_SIZE, Y_SIZE, (float) _x, (float) _y, paint2);
    }

    public void setValue(int x0, int y0, double vmin, double vmax, String unit, String title){
        this.x0 = x0;
        this.y0 = y0;
        this.vmin = vmin;
        this.vmax = vmax;
        this.unit = unit;
        this.title = title;
    }

    public void setfloat(double a) {
        this.v = a;
        invalidate();
    }

    public class Inclement extends Thread {
        private String myName;    //名前
        private long mySpan;      //周期
        private int myloopCount;  //ループ回数

        //コンストラクタ
        /*public Inclement(String name, int loopCount) {
            myName = name;
            //mySpan = span;
            myloopCount = loopCount;
        }

        public void run() {
            //何かの処理
            for (int i = 0; i < myloopCount; i++) {
                try {
                    v++;
                    sleep(200);
                } catch (Exception e) {
                    // 自動生成された catch ブロック
                    e.printStackTrace();
                }
            }
        }*/


            //translate(0,Y_SIZE);
            //scale(1,-1);
            //noFill();
            //stroke(255);
            //rect(0,0,X_SIZE,Y_SIZE);

        /*for(int i = 0; i < value.length - 1; i++){
            value[i] = value[i+1];
        }
        value[value.length - 1] = ((v - vmin) * Y_SIZE) / (vmax - vmin) ;
        for (int i = 0; i < X_SIZE - 1; i++){
            stroke(0,255,0);
            line(i,value[i],i+1,value[i+1]);
        }
        popMatrix(); //returning stored coordinate
    }
}

    public class ElevatorGraph extends GraphView{
        public ElevatorGraph(Context context ,int x0, int y0, float vmin, float vmax, String unit, String title){
            super(context ,x0, y0, vmin, vmax, unit, title);
        }

        @Override
        public void drawOption(float v){
            super.drawOption(v);
            fill(255);
            textSize(15);
            text("Up", X_SIZE + 5, Y_SIZE - 10);
            text("Down", X_SIZE + 5, 15);
            stroke(255);
            line(0, Y_SIZE/2, X_SIZE + 10, Y_SIZE/2);
        }
    }

    public class RudderGraph extends GraphView{
        public RudderGraph(Context context ,int x0, int y0, float vmin, float vmax, String unit, String title){
            super(context ,x0, y0, vmin, vmax, unit, title);
        }

        @Override
        public void drawOption(float v){
            super.drawOption(v);
            fill(255);
            textSize(15);
            text("Right", X_SIZE + 5, Y_SIZE - 10);
            text("Left", X_SIZE + 5, 15);
            stroke(255);
            line(0, Y_SIZE/2, X_SIZE + 10, Y_SIZE/2);
        }
    }*/
        }
    }

