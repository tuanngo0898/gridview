package com.tuan.gridview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GridView extends View implements GridPoint.OnPointStateChangeListener{

    private static final String MODULE_NAME = "GridView";
    private static final int GRID_LINES_MAX_LEVELS = 3;
    private static final int GRID_LABEL_TEXT_SIZE = 15;
    public static final int LINE_WIDTH_NODE = 2;

    private static final int NORMALIZATION_DISTANCE_UNIT_FACTOR = 10;
    private static final float SHORTEST_GRID_SQUARE_CM = 0.5f;

    private static final float INCH_PER_MM = 1f / 25.4f;

    private static final float VIRTUAL_PER_REAL_MM_INITIAL_ZOOM = 1e-2f;      // 0.01mm on screen is 1mm in reality or 1mm on the screen is 100mm in reality or 1cm on the screen is 100cm in reality
    private static final float VIRTUAL_PER_REAL_MM_MAX_ZOOM = 5e-2f;          // 0.05mm on screen is 1mm in reality or 1mm on the screen is 20mm in reality or 1cm on the screen is 20cm in reality
    private static final float VIRTUAL_PER_REAL_MM_MIN_ZOOM = 2e-6f;          // 2*10^-6mm on screen is 1mm in reality or 1mm on the screen is 500m in reality or 1cm on the screen is 5km in reality
    private static final int[] GRID_LINE_STEP_MM = new int[]{
            100,     // 10cm
            200,
            500,
            1000,    // 1m
            2000,
            5000,
            10000,   // 10m
            20000,
            50000,
            100000,  // 100m
            200000,
            500000,
            1000000, // 1km
            2000000,
            5000000,
            10000000,// 10km
    };

    public static float SCREEN_DENSITY;         // Number of pixels per dot (DPI)
    public static float SCREEN_XDPI;            // Pixel per inch (PPI) in X dimension
    public static float SCREEN_YDPI;            // Pixel per inch (PPI) in Y dimension


    float[] mDrawMatrixAsFloat = new float[9];

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private Scroller mScroller;
    protected Animator mZoomer;
    protected Animator mFpRotator;

    private Matrix mDrawMatrix;                 // keep the transformation which is result of scrolling and scaling
    private Matrix flingStartMatrix = new Matrix();
    private Matrix zoomStartMatrix = new Matrix();
    private PointF zoomFocalPoint = new PointF();

    private Paint gridLabelPaint;
    private int mWidthPx;
    private int mHeightPx;

    private PointF mInjectedFocalPoint;
    private Float mInjectedScale;
    private ArrayList<GridLine> shownGridLines = new ArrayList<>(GRID_LINES_MAX_LEVELS);

    private List<GridPoint> mGridPoints = new ArrayList<>();

    private float mMinGridMarkStepPx;   // minimal step in pixel for which we start drawing marks
    private float mPixelsPerVirMm;      // Number of pixel per 1mm of screen
    private float mPxPerRealMinZoom;    // Number of pixel per 1000mm (1m) in reality for min zoom
    private float mPxPerRealMaxZoom;    // Number of pixel per 1000mm (1m) in reality for max zoom\

    private float pixelPerRealMm;

    private Paint[] gridPaint = new Paint[GRID_LINES_MAX_LEVELS];

    // line widths (relative)
    private static final float[] GRID_LINE_WIDTH = {
            0,        // hairline
            1,
            1
    };

    public GridView(Context context) {
        super(context);
        // Step 1: Call construct
        construct(context);
    }

    public GridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // Step 1: Call construct
        construct(context);
    }

    public GridView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // Step 1: Call construct
        construct(context);
    }

    public GridView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // Step 1: Call construct
        construct(context);
    }

    public void addPoints(List<GridPoint> gridPoints) {
        mGridPoints.addAll(gridPoints);
        for(GridPoint gridPoint: mGridPoints) {
            gridPoint.setOnPointStateChangeListener(this);
        }
    }

    public void addPoint(GridPoint gridPoint) {
        mGridPoints.add(gridPoint);
        gridPoint.setOnPointStateChangeListener(this);
    }

    // Step 1: Call construct
    private void construct(Context ctx) {

        SCREEN_DENSITY = ctx.getResources().getDisplayMetrics().density;
        SCREEN_XDPI = ctx.getResources().getDisplayMetrics().xdpi;
        SCREEN_YDPI = ctx.getResources().getDisplayMetrics().xdpi;

        mGestureDetector = new GestureDetector(ctx, new GestureListener());
        mScaleGestureDetector = new ScaleGestureDetector(ctx, new OnScaleGestureListener());

        mScroller = new Scroller(ctx);
        mZoomer = new Animator(ctx, new DecelerateInterpolator());

        mFpRotator = new Animator(ctx, new AccelerateDecelerateInterpolator());

        gridLabelPaint = new Paint();
        gridLabelPaint.setColor(ContextCompat.getColor(ctx, R.color.grid_label_color));
        gridLabelPaint.setTypeface(Typeface.DEFAULT);
        // Because textSize is in pixel unit, we multiply GRID_LABEL_TEXT_SIZE by SCREEN_DENSITY
        // to keep the text in various DPI screen
        gridLabelPaint.setTextSize(GRID_LABEL_TEXT_SIZE * SCREEN_DENSITY);

        configureGridPaint(ctx, 0, R.color.grid_line_color_primary);
        configureGridPaint(ctx, 1, R.color.grid_line_color_secondary);
        configureGridPaint(ctx, 2, R.color.grid_line_color_tertiary);

        // compute immutable limits
        mPixelsPerVirMm = SCREEN_XDPI * INCH_PER_MM;
        mPxPerRealMinZoom = mPixelsPerVirMm * VIRTUAL_PER_REAL_MM_MIN_ZOOM;
        mPxPerRealMaxZoom = mPixelsPerVirMm * VIRTUAL_PER_REAL_MM_MAX_ZOOM;

        Log.d(MODULE_NAME, "mPxPerRealMinZoom = " + mPxPerRealMinZoom);
        Log.d(MODULE_NAME, "mPxPerRealMaxZoom = " + mPxPerRealMaxZoom);
    }

    private enum DrawLabels {
        NO, YES, EVERY_OTHER
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            float x = e.getX();
            float y = e.getY();
            Log.d("GridView", "onSingleTapConfirmed: " + "x = [" + x + "], y = [" + y + "]");

            return true;
        }


        @Override
        public boolean onDoubleTap(MotionEvent e) {

            float x = e.getX();
            float y = e.getY();
            Log.d("GridView", "DblTap at: (" + x + "," + y + ")");

            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {

            Log.d("GridView", "Drag:drawMatrix ");

            mDrawMatrix.postTranslate(-dx, -dy);
            onDrawMatrixTranslationChanged();

            invalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            Log.d("GridView", "xVelocity = " + velocityX);
            Log.d("GridView", "yVelocity = " + velocityY);

            flingStartMatrix.set(mDrawMatrix);
            // start the fling gesture
            mScroller.fling(
                    0, 0,
                    (int) velocityX, (int) velocityY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE,
                    Integer.MIN_VALUE, Integer.MAX_VALUE
            );
            // draw again
            invalidate();

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {

            Log.d(MODULE_NAME, "abortOngoingAnimations");

            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            if (!mZoomer.isFinished()) {
                mZoomer.abortAnimation();
            }

            return super.onDown(e);
        }
    }

    private class OnScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();

            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            Log.d("GridView", "scaleFactor: " + scaleFactor + " focusX:" + focusX + " focusY" + focusY);

            mDrawMatrix.getValues(mDrawMatrixAsFloat);
            float finalScale = mDrawMatrixAsFloat[0] * scaleFactor;
            if (finalScale >= mPxPerRealMinZoom && finalScale <= mPxPerRealMaxZoom) {
                // do scale
                mDrawMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
                onDrawMatrixChanged();
            }
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            Log.d("GridView", "");
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            Log.d("GridView", "");
        }
    }

    private static class GridLine {
        int idx;
        int stepInMm;       // mm in reality
        float stepInPx;     // px in screen

        @NotNull
        @Override
        public String toString() {
            return "GridLine{" +
                    "idx=" + idx +
                    ", stepInMm=" + stepInMm +
                    ", stepInPx=" + stepInPx +
                    '}';
        }
    }

    private void configureGridPaint(Context ctx, int idx, int colorResId) {
        gridPaint[idx] = new Paint();
        gridPaint[idx].setColor(ContextCompat.getColor(ctx, colorResId));
        gridPaint[idx].setStrokeWidth(SCREEN_DENSITY * GRID_LINE_WIDTH[idx]);
    }

    // Step 2: Override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(MODULE_NAME, "onSizeChanged() called with: " + "w = [" + w + "], h = [" + h + "], oldw = [" + oldw + "], oldh = [" + oldh + "]");

        // Remember the size
        mWidthPx = w;
        mHeightPx = h;

        if (mDrawMatrix == null) {
            // we do not have the draw matrix yet
            if (mInjectedScale != null) {
                computeInjectedMatrix();
            } else {
                // Compute the whole matrix
                computeInitialMatrix();
            }
        }

        Log.d(MODULE_NAME, "drawMatrix = " + mDrawMatrix);

        // Compute what is the minimal step in px from which we draw grid marks/labels
        if (h > w) {
            // Seems that we are in portrait
            // We do not want to have 3 grid marks in row (except when in extreme case)
            mMinGridMarkStepPx = (int) ((w / 2) * .9 + 0.5);
        } else {
            mMinGridMarkStepPx = (int) ((h / 2) * 1.05 + 0.5);
        }

        // Call super
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void computeInitialMatrix() {

        mDrawMatrix = new Matrix();

        pixelPerRealMm = mPixelsPerVirMm * VIRTUAL_PER_REAL_MM_INITIAL_ZOOM;

        int sumX = 0, sumY = 0;
        int i = 0;
        for (GridPoint point : mGridPoints) {
            Position position = point.getPosition();
            sumX += position.getX();
            sumY += position.getY();
            i++;
        }
        if (i == 0) i = 1;
        int avgX = sumX / i;
        int avgY = sumY / i;

        float[] fIn = {avgX,avgY};
        float[] fOut = new float[2];
        mDrawMatrix.mapPoints(fOut, fIn);

        // the focal point should be in 0,0
            mDrawMatrix.setTranslate(-avgX + mWidthPx / pixelPerRealMm / 2 , -avgY - mHeightPx / pixelPerRealMm / 2);
        // set the proper scale after move
        mDrawMatrix.postScale(pixelPerRealMm, -pixelPerRealMm);

        // notify the listeners
        onDrawMatrixChanged();
    }

    void computeInjectedMatrix() {
        Log.d(MODULE_NAME, "computeInjectedMatrix(), mInjectedScale = " + mInjectedScale + ", mInjectedFocalPoint = " + mInjectedFocalPoint);
        //noinspection ConstantConditions
        mDrawMatrix = new Matrix();
        // set scale first
        mDrawMatrix.setScale(mInjectedScale, -mInjectedScale);
        // now when we are in pixels, set transition according to focal point (which is in px)
        mDrawMatrix.postTranslate(mWidthPx / 2 - mInjectedFocalPoint.x, mHeightPx / 2 - mInjectedFocalPoint.y);
        // notify the listeners
        onDrawMatrixChanged();
    }

    private void onDrawMatrixChanged() {
        mDrawMatrix.getValues(mDrawMatrixAsFloat);

        // fill shown grid lines
        fillGridLinesForScale(shownGridLines, mDrawMatrixAsFloat[0]);

        Log.d(MODULE_NAME,"scaleFactorCmToPx = " + mDrawMatrixAsFloat[0]);
    }

    private void fillGridLinesForScale(List<GridLine> gridLines, float pixelPerRealMm) {

        int lineIdx = 0;

        // compute
        Integer firstGridLineStep = null;
        Integer lastGridLineStep = null;
        for (int i = 0; i < GRID_LINE_STEP_MM.length; i++) {

            GridLine line;
            int step = GRID_LINE_STEP_MM[i];

            if(pixelPerRealMm * step < 20) continue;

            if (firstGridLineStep != null) {
                // check that this gridline step is multiplication of the finest gridline step
                if ((step % firstGridLineStep) != 0) {
                    // skip to the next gridline, it wouldn't be visually pleasant
                    continue;
                }
                // check if this gridline step is 'far enough' from the previous one
                if (lastGridLineStep * 3 >= step) {
                    // skip this one
                    continue;
                }
            } else {
                firstGridLineStep = step;
            }
            if (gridLines.size() == lineIdx) {
                // add one more line
                line = new GridLine();
                gridLines.add(line);
            } else {
                line = gridLines.get(lineIdx);
            }
            // set up the parameters

            line.idx = i;
            line.stepInMm = step;

            lastGridLineStep = line.stepInMm;
            // check if we are done
            if (++lineIdx >= GRID_LINES_MAX_LEVELS) {
                break;
            }
        }
        // recompute stepInPx
        for (GridLine line : shownGridLines) {
            float stepInMm = line.stepInMm;
            line.stepInPx = pixelPerRealMm * stepInMm;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawGrid(canvas);

        boolean redraw = computeAnimations();
        if (redraw) {
            postInvalidate();
        }

        drawGridPoint(canvas);

        super.onDraw(canvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean s = mScaleGestureDetector.onTouchEvent(event);
        boolean g = mGestureDetector.onTouchEvent(event);
        return s || g;
    }

    private void drawGrid(Canvas canvas) {
        int i = 0;
        for (GridLine gridLine : shownGridLines) {

            DrawLabels drawLabels = DrawLabels.NO; // determine whether we should draw labels
            if (gridLine.stepInPx >= mMinGridMarkStepPx) { // check if it is time to draw labels
                drawLabels = DrawLabels.YES;
            } else if (gridLine.stepInPx * 2 >= mMinGridMarkStepPx) {
                drawLabels = DrawLabels.EVERY_OTHER;  // check the next grid line, if the marks are close to each other enough
            }

            drawSingleGrid(canvas,
                    gridPaint[i++],
                    gridLine.stepInPx,
                    gridLine.stepInMm,
                    drawLabels);
        }
    }

    private void drawSingleGrid(Canvas canvas, Paint paint, float gridLineStepInPx, int gridLineStepInMm, DrawLabels drawLabels) {
        // how far we must go to not miss a single line
        int maxHorizontalLineIdx = Math.round(mDrawMatrixAsFloat[5] / gridLineStepInPx);
        int minVerticalLineIdx = -Math.round(mDrawMatrixAsFloat[2] / gridLineStepInPx);

        int realX = Math.round(minVerticalLineIdx * gridLineStepInMm);
        int realY = Math.round(maxHorizontalLineIdx * gridLineStepInMm);

        float[] fIn = new float[2];
        float[] fOut = new float[2];

        fIn[0] = realX;
        fIn[1] = realY;

        mDrawMatrix.mapPoints(fOut, fIn);

        float pxPosX = fOut[0];
        float pxPosY = fOut[1];

        // draw horizontal lines
        int i = 0;
        float y;
        do {
            y = pxPosY + (i++ * gridLineStepInPx);
            canvas.drawLine(0, y, mWidthPx, y, paint);
        } while (y <= mHeightPx);

        // draw vertical lines
        i = 0;
        float x;
        do {
            x = pxPosX + (i++ * gridLineStepInPx);
            canvas.drawLine(x, 0, x, mHeightPx, paint);
        } while (x <= mWidthPx);

        float labelStepInPx = gridLineStepInPx;
        float labelRealStep = gridLineStepInMm;

        int realStartX = realX;
        if (drawLabels != DrawLabels.NO) {
            // draw labels
            pxPosY = fOut[1];
            float pxStartX = fOut[0];
            if (drawLabels == DrawLabels.EVERY_OTHER) {
                labelRealStep *= 2;
                labelStepInPx *= 2;
                int xFitsIn = Math.round((float)realStartX / gridLineStepInMm);
                if (xFitsIn % 2 != 0) {
                    realStartX += gridLineStepInMm;
                    pxStartX += gridLineStepInPx;
                }
                int yFitsIn = Math.round((float)realY / gridLineStepInMm);
                if (yFitsIn % 2 != 0) {
                    realY -= gridLineStepInMm;
                    pxPosY += gridLineStepInPx;
                }
            }
            do {
                pxPosX = pxStartX;
                realX = realStartX;
                do {
                    drawGridLabel(canvas, realX, realY, pxPosX, pxPosY);
                    pxPosX += labelStepInPx;
                    realX += labelRealStep;
                } while (pxPosX <= mWidthPx);
                pxPosY += labelStepInPx;
                realY -= labelRealStep;
            } while (pxPosY <= mHeightPx);
        }
    }

    private void drawGridLabel(Canvas canvas, int realPosX, int realPosY, float pxPosX, float pxPosY) {
        gridLabelPaint.setTextAlign(Paint.Align.RIGHT);
        // Draw label from
        String x = String.valueOf(realPosX/1000f);
        String y = String.valueOf(realPosY/1000f);
        // Draw label
        canvas.drawText(x, pxPosX - 0.4f * GRID_LABEL_TEXT_SIZE * SCREEN_DENSITY, pxPosY - 0.5f * GRID_LABEL_TEXT_SIZE * SCREEN_DENSITY, gridLabelPaint);
        gridLabelPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(y, pxPosX + 0.4f * GRID_LABEL_TEXT_SIZE * SCREEN_DENSITY, pxPosY + 1.2f * GRID_LABEL_TEXT_SIZE * SCREEN_DENSITY, gridLabelPaint);
    }

    private void onDrawMatrixTranslationChanged() {
        mDrawMatrix.getValues(mDrawMatrixAsFloat);
    }

    private boolean computeAnimations() {
        boolean redraw = false;
        if (!mScroller.isFinished()) {
            // compute the next scroll offset
            mScroller.computeScrollOffset();
            // set proper drawmatrix
            mDrawMatrix.set(flingStartMatrix);
            // get the offset from scroller
            mDrawMatrix.postTranslate(mScroller.getCurrX(), mScroller.getCurrY());
            onDrawMatrixTranslationChanged();
            redraw = true;
        }
        return redraw;
    }

    private void drawGridPoint(Canvas canvas){

        for(GridPoint gridPoint: mGridPoints){
            Position position = gridPoint.getPosition();

            if(position == null) continue;

            float[] fIn = {position.getX(),position.getY()};
            float[] fOut = new float[2];

            mDrawMatrix.mapPoints(fOut, fIn);
            gridPoint.onDrawPoint(canvas, fOut[0], fOut[1]);

        }
    }

    @Override
    public void onPointStatChange() {
        invalidate();
    }
}
