package com.tuan.gridview;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Position {

    public static final byte MAX_QUALITY_FACTOR = 100;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public Byte getQf() {
        return qf;
    }

    public void setQf(Byte qf) {
        this.qf = qf;
    }

    // relative X coordinate
    private float x;
    // relative Y coordinate
    private float y;
    // relative Z coordinate
    private float z;
    // quality factor: 0 - 100
    private Byte qf;

    public Position(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Position(int x, int y, int z, Byte qualityFactor) {
        this(x, y, z);
        this.qf = qualityFactor;
    }

    @SuppressWarnings("IncompleteCopyConstructor")
    public Position(@NotNull Position position) {
        copyFrom(position);
    }

    public void divide(int factor, Position targetPosition) {
        targetPosition.x = (int) Math.round(x / (double) factor);
        targetPosition.y = (int) Math.round(y / (double) factor);
        targetPosition.z = (int) Math.round(z / (double) factor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (x != position.x) return false;
        if (y != position.y) return false;
        if (z != position.z) return false;

        return Objects.equals(qf, position.qf);
    }

    @Override
    public int hashCode() {
        int result = (int)x;
        result = 31 * result + (int)y;
        result = 31 * result + (int)z;
        result = 31 * result + (qf != null ? qf.hashCode() : 0);
        return result;
    }

    public void copyFrom(@NotNull Position source) {
        this.z = source.z;
        this.y = source.y;
        this.x = source.x;
        this.qf = source.qf;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", qualityFactor=" + qf +
                '}';
    }

    public boolean equalsInCoordinates(Position pos) {
        return pos != null
                && this.x == pos.x
                && this.y == pos.y
                && this.z == pos.z;
    }

}
