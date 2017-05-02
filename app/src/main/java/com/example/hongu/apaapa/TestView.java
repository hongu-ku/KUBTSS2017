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

    public void setPitch(float pitch) {
        Pitch = pitch;
    }

    public void setYaw(float yaw) {
        Yaw = yaw;
    }

    public float Pitch = 0;
    public float Yaw = 0;

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

    private float Pitch1 = 30;

    private int deg10 = 10;
    private int deg20 = 20;

    double rad10= Math.toRadians(deg10);
    double rad20 = Math.toRadians(deg20);
    float sin10 = (float) Math.sin(rad10);
    float sin20 = (float) Math.sin(rad20);
    float cos10 = (float) Math.cos(rad10);
    float cos20 = (float) Math.cos(rad20);

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.scale(1.1f,1.1f);
        canvas.translate(155, 155);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(3);

        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        paint.setColor(Color.GRAY);

        paint1.setStyle(Paint.Style.STROKE);
        paint1.setAntiAlias(true);
        paint1.setStrokeWidth(4);
        paint1.setColor(Color.RED);

        CircleDraw(canvas);
    }

    private void CircleDraw(Canvas canvas) {
        RectF rect = new RectF(-150, -150, 150, 150);
        canvas.drawOval(rect, mPaint);
        canvas.drawLine(0,-150,0,150, paint);
        canvas.drawLine(-150,0,150,0, paint);
        canvas.save();

//        回転する場所の描画
        canvas.rotate( -Yaw );
        canvas.drawLine(0,-150,0,150,paint1);
        canvas.drawLine(-100,0,100,0,mPaint);
        canvas.drawLine(-75,-50,75,-50,mPaint);
        canvas.drawLine(-75,50,75,50,mPaint);

        //canvas.drawRect(rect, mPaint);
//        canvas.save();
//        これ以降は前後する場所の描画
        if((Pitch1 - Pitch) * 4 <=150 && (Pitch1 - Pitch) * 4 >= -150)
            canvas.translate(0, (Pitch1 - Pitch) * 4 );
        else if ((Pitch1 - Pitch) * 4 > 150)
            canvas.translate(0, 150);
        else if ((Pitch1 - Pitch) * 4 < -150)
            canvas.translate(0,-150);
        canvas.drawLine(-100,0,100,0,paint1);

//        これ以降は動かない
        canvas.restore();
        canvas.drawLine(0, -155, 0, -145, mPaint);
        canvas.drawLine(-155 * sin10, -155 * cos10, -145 * sin10, -145 * cos10, mPaint);
        canvas.drawLine(155 * sin10, -155 * cos10, 145 * sin10, -145 * cos10, mPaint);
        canvas.drawLine(-155 * sin20, -155 * cos20, -145 * sin20, -145 * cos20, mPaint);
        canvas.drawLine(155 * sin20, -155 * cos20, 145 * sin20, -145 * cos20, mPaint);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        super.setOnLongClickListener(l);
        this.setPitch1(this.Pitch);
    }
}
