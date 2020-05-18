package com.tuan.gridviewrls.Points;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.tuan.gridview.GridPoint;

public class TagPoint extends GridPoint {

    private Paint gridPointPaint = new Paint();

    public TagPoint(float x, float y, float z) {
        super(x, y, z);

        gridPointPaint.setStyle(Paint.Style.STROKE);
        gridPointPaint.setAntiAlias(true);
        gridPointPaint.setStrokeJoin(Paint.Join.ROUND);
        gridPointPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onDrawPoint(Canvas canvas, float cx, float cy) {
        gridPointPaint.setColor(Color.rgb(255,0,0));
        gridPointPaint.setStyle(Paint.Style.FILL);
        gridPointPaint.setAlpha(255);
        canvas.drawCircle(cx, cy, 10, gridPointPaint);
    }
}
