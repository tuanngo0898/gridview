package com.tuan.gridviewrls;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.tuan.gridview.GridLine;
import com.tuan.gridview.GridPoint;
import com.tuan.gridview.GridView;
import com.tuan.gridview.Position;
import com.tuan.gridviewrls.Points.AnchorPoint;
import com.tuan.gridviewrls.Points.TagPoint;

public class MainActivity extends AppCompatActivity {

    private Paint gridPointPaint = new Paint();
    private Paint gridLinePaint;

    private float posx = -2000.12f;
    private float posy = -200.12f;
    private int dir = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridPointPaint.setStyle(Paint.Style.STROKE);
        gridPointPaint.setAntiAlias(true);
        gridPointPaint.setStrokeJoin(Paint.Join.ROUND);
        gridPointPaint.setStrokeCap(Paint.Cap.ROUND);

        gridLinePaint = new Paint();
        gridLinePaint.setStyle(Paint.Style.STROKE);
        gridLinePaint.setTextAlign(Paint.Align.CENTER);
        gridLinePaint.setStrokeWidth(5);
        gridLinePaint.setColor(Color.rgb(255, 150, 0));

        GridView gridView = findViewById(R.id.gridView);
        gridView.addPoint(new GridPoint(0,0,0) {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDrawPoint(Canvas canvas, float cx, float cy) {
                gridPointPaint.setColor(Color.rgb(255,0,255));
                gridPointPaint.setStyle(Paint.Style.FILL);
                gridPointPaint.setAlpha(255);

                canvas.drawCircle(cx, cy, 10, gridPointPaint);
            }
        });

        GridPoint gridPoint = new GridPoint(2000, posy,1000) {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDrawPoint(Canvas canvas, float cx, float cy) {
                gridPointPaint.setColor(Color.rgb(255,0,255));
                gridPointPaint.setStyle(Paint.Style.FILL);
                gridPointPaint.setAlpha(255);
                canvas.drawCircle(cx, cy, 10, gridPointPaint);
            }
        };
        gridView.addPoint(gridPoint);
        Button button_1 = findViewById(R.id.button_1);
        button_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gridPoint.setPosition(new Position(2000, posy + 1500.12f*dir, 1000));
                dir = dir == 1 ? 0 : 1;
                gridPoint.invalidate();
            }
        });

        TagPoint tagPoint = new TagPoint(posx, 3000, 4000);
        gridView.addPoint(tagPoint);
        Button button_2 = findViewById(R.id.button_2);
        button_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagPoint.setPosition(new Position(posx + 1500.12f*dir, 3000, 1000));
                dir = dir == 1 ? 0 : 1;
                tagPoint.invalidate();
            }
        });

        AnchorPoint anchorPoint = new AnchorPoint(2000, 4000, 4000);
        gridView.addPoint(anchorPoint);

        Position position1 = new Position(0,0,0);
        Position position2 = new Position(1000,1000,1000);
        GridLine gridLine1 = new GridLine(position1, position2) {
            @Override
            protected void onDrawLine(Canvas canvas, float cx1, float cy1, float cx2, float cy2) {
                canvas.drawLine(cx1, cy1, cx2, cy2, gridLinePaint);
            }
        };
        gridView.addLine(gridLine1);

        Position position3 = new Position(0,2000,0);
        Position position4 = new Position(1000,1000,1000);
        GridLine gridLine2 = new GridLine(position3, position4) {
            @Override
            protected void onDrawLine(Canvas canvas, float cx1, float cy1, float cx2, float cy2) {
                canvas.drawLine(cx1, cy1, cx2, cy2, gridLinePaint);
            }
        };
        gridView.addLine(gridLine2);
    }
}
