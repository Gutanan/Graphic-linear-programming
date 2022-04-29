package com.example.bp;

/**
 * Class is a representation of a point in the graph area
 */
public class Point {
    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String toString(){
        String result = "[" + String.format("%.2f",x) + ";" + String.format("%.2f",y) + "]";
        return result;
    }
}
