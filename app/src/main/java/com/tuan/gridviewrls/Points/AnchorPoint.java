package com.tuan.gridviewrls.Points;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.tuan.gridview.GridPoint;

public class AnchorPoint extends GridPoint {

    private Paint gridPointPaint = new Paint();

    public AnchorPoint(float x, float y, float z) {
        super(x, y, z);

        gridPointPaint.setStyle(Paint.Style.STROKE);
        gridPointPaint.setAntiAlias(true);
        gridPointPaint.setStrokeJoin(Paint.Join.ROUND);
        gridPointPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onDrawPoint(Canvas canvas, float cx, float cy) {
        gridPointPaint.setColor(Color.rgb(0,0,255));
        gridPointPaint.setStyle(Paint.Style.FILL);
        gridPointPaint.setAlpha(255);
        canvas.drawCircle(cx, cy, 10, gridPointPaint);
    }
}
