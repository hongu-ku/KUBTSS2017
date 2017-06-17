package com.example.hongu.apaapa;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by hongu on 2017/02/28.
 */

public class TestView extends View {
    public TestView(Context context) {
        super(context);
    }

    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    private Paint mPaint = new Paint();
    private Paint paint = new Paint();
    private Paint paint1 = new Paint();

    private float save=0;
    private float savelongi=1;

    public void setPitch(float pitch) {
        Pitch = pitch;
    }

    public void setYaw(float yaw) {
        Yaw = yaw;
    }

    public float getPitch() {
        return Pitch;
    }

    public float getYaw() {
        return Yaw;
    }

    public float getYaw1() {
        return Yaw1;
    }

    private float Pitch = 0;
    private float Yaw = 0;

    public void setPitch1(float pitch1) {
        Pitch1 = pitch1;
    }

    public void setYaw1(float yaw1) {
        Yaw1 = yaw1;
    }

    private float Yaw1 = 0;

    public float getPitch1() {
        return Pitch1;
    }

    private float Pitch1 = 0;

    float sin10 = Deg2sin(10);
    float sin20 = Deg2sin(20);
    float sin50 = Deg2sin(50);
    float cos10 = Deg2cos(10);
    float cos20 = Deg2cos(20);
    float cos50 = Deg2cos(50);
    float bigr = 150 * sin50 /sin10;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.scale(1.65f,1.65f);
        canvas.translate(155, 155);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(4);

        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3);
        paint.setColor(Color.GRAY);

        paint1.setStyle(Paint.Style.STROKE);
        paint1.setAntiAlias(true);
        paint1.setStrokeWidth(5);
        paint1.setColor(Color.RED);

        CircleDraw(canvas);
    }

    private void CircleDraw(Canvas canvas) {
        RectF rect = new RectF(-150, -150, 150, 150);
        RectF rectR = new RectF(150*cos50 + bigr * (cos10-1),-bigr ,150*cos50 + bigr *(1+cos10) ,bigr);
        RectF rectL = new RectF(-150*cos50 - bigr *(1+cos10),-bigr ,-150*cos50 - bigr * (cos10-1) ,bigr);
        canvas.drawArc(rect, 230 , 80 , false , mPaint);
        canvas.drawArc(rect, 50 , 80 , false, mPaint);
        canvas.drawArc(rectR, 170, 20, false, mPaint);
        canvas.drawArc(rectL, 350, 20, false, mPaint);
        canvas.drawLine(0,-150,0,150, paint);  // 縦線
        canvas.drawLine(-150*cos50 + bigr*(1-cos10) , 0,150*cos50 - bigr*(1-cos10),0, paint);  // 横線
        canvas.save();

//        回転する場所の描画
        canvas.rotate( -Yaw );
        //    RectF layer = new RectF(-150*cos50, -150, 150*cos50, 150);
        //    canvas.saveLayer(layer, null, Canvas.CLIP_TO_LAYER_SAVE_FLAG);
        /* 縦線がはみ出ないようにする */
        if (Yaw<0){
            save = -Yaw;
        } else {
            save = Yaw;
        }
        final double x =150*cos50 + bigr *(1+cos10);
        if (save > 50 && save <130)
            savelongi = (float) (x * Deg2sin(save) - Math.sqrt(x*x * Deg2sin(save)*Deg2sin(save) - x*x +bigr*bigr));
        else {
            savelongi = 150;
        }


        canvas.drawLine(0,-savelongi,0,savelongi,paint1); // 縦線
        canvas.drawLine(-150*cos50 + bigr*(1-cos10) , 0,150*cos50 - bigr*(1-cos10),0,mPaint);
        canvas.drawLine(-75,-50,75,-50,mPaint);
        canvas.drawLine(-75,50,75,50,mPaint);
        //    canvas.restore();

        //canvas.drawRect(rect, mPaint);
//        canvas.save();
//        これ以降は前後する場所の描画

        if(-Pitch * 4 <=150 && -Pitch * 4 >= -150)
            canvas.translate(0, - Pitch * 4 );
        else if (-Pitch * 4 > 150)
            canvas.translate(0, 150);
        else if  (-Pitch * 4 < -150)
            canvas.translate(0,-150);
//        canvas.drawLine(-100,0,100,0,paint1);
//        canvas.translate(0, Pitch * 3 );
        canvas.drawLine(-150*cos50 + bigr*(1-cos10) , 0,150*cos50 - bigr*(1-cos10),0,paint1);

//        これ以降は動かない
        canvas.restore();
        canvas.drawLine(0, -155, 0, -145, mPaint);
        canvas.drawLine(-155 * sin10, -155 * cos10, -145 * sin10, -145 * cos10, mPaint);
        canvas.drawLine(155 * sin10, -155 * cos10, 145 * sin10, -145 * cos10, mPaint);
        canvas.drawLine(-155 * sin20, -155 * cos20, -145 * sin20, -145 * cos20, mPaint);
        canvas.drawLine(155 * sin20, -155 * cos20, 145 * sin20, -145 * cos20, mPaint);
    }

    public float Deg2sin (int deg) {
        return (float) Math.sin(Math.toRadians(deg));
    }
    public float Deg2cos (int deg) {
        return (float) Math.cos(Math.toRadians(deg));
    }

    public float Deg2sin (float deg) {
        return (float) Math.sin(Math.toRadians(deg));
    }
    public float Deg2cos (float deg) {
        return (float) Math.cos(Math.toRadians(deg));
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        super.setOnLongClickListener(l);
        this.setPitch1(this.Pitch);
    }

    public void setsetPitch1 () {
        Pitch1 = Pitch;
    }
}

//    private void CircleDraw(Canvas canvas) {
//        RectF rect = new RectF(-150, -150, 150, 150);
//        canvas.drawOval(rect, mPaint);
//        canvas.drawLine(0,-150,0,150, paint);
//        canvas.drawLine(-150,0,150,0, paint);
//        canvas.save();
//
////        回転する場所の描画
//        canvas.rotate( -Yaw );
//        canvas.drawLine(0,-150,0,150,paint1);
//        canvas.drawLine(-100,0,100,0,mPaint);
//        canvas.drawLine(-75,-50,75,-50,mPaint);
//        canvas.drawLine(-75,50,75,50,mPaint);
//
//        //canvas.drawRect(rect, mPaint);
////        canvas.save();
////        これ以降は前後する場所の描画
//        if(-Pitch * 4 <=150 && -Pitch * 4 >= -150)
//            canvas.translate(0, - Pitch * 4 );
//        else if (-Pitch * 4 > 150)
//            canvas.translate(0, 150);
//        else if  (-Pitch * 4 < -150)
//            canvas.translate(0,-150);
//        canvas.drawLine(-100,0,100,0,paint1);
//
////        これ以降は動かない
//        canvas.restore();
//        canvas.drawLine(0, -155, 0, -145, mPaint);
//        canvas.drawLine(-155 * sin10, -155 * cos10, -145 * sin10, -145 * cos10, mPaint);
//        canvas.drawLine(155 * sin10, -155 * cos10, 145 * sin10, -145 * cos10, mPaint);
//        canvas.drawLine(-155 * sin20, -155 * cos20, -145 * sin20, -145 * cos20, mPaint);
//        canvas.drawLine(155 * sin20, -155 * cos20, 145 * sin20, -145 * cos20, mPaint);
//    }
