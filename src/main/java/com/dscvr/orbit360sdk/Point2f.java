package com.dscvr.orbit360sdk;

/**
 * Simple class to represent a two-dimensional point.
 */
public class Point2f {

    private final float p;
    private final float x;
    private final float y;

    public Point2f(float p, float x, float y) {
        this.p = p;
        this.x = x;
        this.y = y;
    }

    public Point2f(float x, float y) {
        this.p = 1;
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x * p;
    }

    public float getY() {
        return y * p;
    }

    public Point2f add(Point2f b) {
        return new Point2f(p, x + b.x, y + b.y);
    }

    public Point2f sub(Point2f b) {
        return new Point2f(p, x - b.x, y - b.y);
    }

    public Point2f mul(Point2f b) {
        return new Point2f(p, x * b.x, y * b.y);
    }

    public Point2f mul(float b) {
        return new Point2f(p, x * b, y * b);
    }

    public Point2f div(Point2f b) {
        return new Point2f(p, x / b.x, y / b.y);
    }

    public Point2f div(float b) {
        return new Point2f(p, x / b, y / b);
    }

    public Point2f div(float a, float b) {
        return new Point2f(p, x / a, y / b);
    }

    public Point2f abs() {
        return new Point2f(p, Math.abs(x), Math.abs(y));
    }

    public Point2f min(Point2f b) {
        return new Point2f(p, Math.min(x, b.x), Math.min(y, b.y));
    }

    public Point2f max(Point2f b) {
        return new Point2f(p, Math.max(x, b.x), Math.max(y, b.y));
    }


}
