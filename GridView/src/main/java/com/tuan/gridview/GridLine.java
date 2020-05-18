package com.tuan.gridview;

import android.graphics.Canvas;

public abstract class GridLine {
    private Position position1;
    private Position position2;
    private OnGridLineStateListener onGridLineStateListener;

    public GridLine(Position position1, Position position2) {
        this.position1 = position1;
        this.position2 = position2;
    }

    public Position getPosition1() {
        return position1;
    }

    public void setPosition1(Position position1) {
        this.position1 = position1;
    }

    public Position getPosition2() {
        return position2;
    }

    public void setPosition2(Position position2) {
        this.position2 = position2;
    }

    void setOnPointStateChangeListener(OnGridLineStateListener onGridLineStateListener) {
        this.onGridLineStateListener = onGridLineStateListener;
    }

    protected abstract void onDrawLine(Canvas canvas, float cx1, float cy1, float cx2, float cy2);

    public void invalidate() {
        onGridLineStateListener.onGridLineStateChanged();
    }

    public interface OnGridLineStateListener {
        void onGridLineStateChanged();
    }
}
