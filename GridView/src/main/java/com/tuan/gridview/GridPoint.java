package com.tuan.gridview;

import android.graphics.Canvas;

import androidx.annotation.NonNull;

public abstract class GridPoint {
    private OnPointStateChangeListener onPointStateChangeListener;
    private Position position;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id;

    public GridPoint(){
    }

    public GridPoint(Position position){
        this.position = position;
    }
    public GridPoint(float x, float y, float z){
        position = new Position(x,y,z);
    }

    public Position getPosition(){
        return position;
    }

    public void setPosition(Position position){
        this.position = position;
    }

    protected abstract void onDrawPoint(Canvas canvas, float cx, float cy);

    void setOnPointStateChangeListener(OnPointStateChangeListener onPointStateChangeListener) {
        this.onPointStateChangeListener = onPointStateChangeListener;
    }

    public interface OnPointStateChangeListener {
        void onPointStatChange();
    }

    public void invalidate(){
        onPointStateChangeListener.onPointStatChange();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        GridPoint other = (GridPoint) o;
        return id == other.getId();
    }

    @NonNull
    @Override
    public String toString() {
        String x_str = String.valueOf(position.getX());
        String y_str = String.valueOf(position.getY());
        String z_str = String.valueOf(position.getZ());
        String qf_str = String.valueOf(position.getQf());

        return "(" + x_str + ", " + y_str + ", " + z_str + ", " + qf_str + ")";
    }
}
