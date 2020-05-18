package com.tuan.gridview;

import android.graphics.Canvas;

public abstract class GridPoint {
    private OnPointStateChangeListener onPointStateChangeListener;
    private Position position;

    public GridPoint(float x, float y, float z){
        position = new Position(x,y,z);
    }

    public Position getPosition(){
        return position;
    }

    public void setPosition(Position position){
        this.position = position;
    }

    public abstract void onDrawPoint(Canvas canvas, float cx, float cy);

    void setOnPointStateChangeListener(OnPointStateChangeListener onPointStateChangeListener) {
        this.onPointStateChangeListener = onPointStateChangeListener;
    }

    public interface OnPointStateChangeListener {
        void onPointStatChange();
    }

    public void invalidate(){
        onPointStateChangeListener.onPointStatChange();
    }
}
