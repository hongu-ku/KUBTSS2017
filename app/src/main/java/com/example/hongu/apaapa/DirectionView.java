package com.example.hongu.apaapa;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by hongu on 2017/05/23.
 */

public class DirectionView extends View {

    private Bitmap dir;
    private float Yaw=0;
    private Paint paint = new Paint();

    public DirectionView(Context context) {
        super(context);
        dir = BitmapFactory.decodeResource(getResources(), R.drawable.dir);
    }

    public DirectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        dir = BitmapFactory.decodeResource(getResources(), R.drawable.dir);
    }

    public DirectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        dir = BitmapFactory.decodeResource(getResources(), R.drawable.dir);
    }

    public void setYaw(float yaw) {
        this.Yaw = yaw;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(40,40);
        canvas.scale(1.3f, 1.3f);
        canvas.rotate(-Yaw);
        canvas.drawBitmap(dir, -40, -40, paint);
        canvas.restore();
    }
}
