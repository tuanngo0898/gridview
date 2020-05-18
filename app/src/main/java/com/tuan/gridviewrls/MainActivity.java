package com.tuan.gridviewrls;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.tuan.gridview.GridPoint;
import com.tuan.gridview.GridView;
import com.tuan.gridview.Position;
import com.tuan.gridviewrls.Points.AnchorPoint;
import com.tuan.gridviewrls.Points.TagPoint;

public class MainActivity extends AppCompatActivity {

    private Paint gridPointPaint = new Paint();

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
    }
}
