package com.example.bp;

public class LinearLine {
    private Point nullX;
    private Point nullY;
    private double coefX1; //multiplier of X1
    private double coefX2; //multiplier of X2
    private double rightSide; //right side of equation of line

    public LinearLine(double coefX1, double coefX2, double rightSide) {
        this.coefX1 = coefX1;
        this.coefX2 = coefX2;
        this.rightSide = rightSide;
        countNullX(coefX1,rightSide);
        countNullY(coefX2,rightSide);
    }

    public Point getNullX() {
        return nullX;
    }

    public Point getNullY() {
        return nullY;
    }

    private void countNullX(double coefX1, double rightSide) {
        double x = rightSide/coefX1;
        this.nullX = new Point(x, 0d);
    }

    private void countNullY(double coefX2, double rightSide) {
        double y = rightSide/coefX2;
        this.nullY = new Point(0d,y);
    }

    public double getCoefX1() {
        return coefX1;
    }

    public void setCoefX1(double coefX1) {
        this.coefX1 = coefX1;
    }

    public double getCoefX2() {
        return coefX2;
    }

    public void setCoefX2(double coefX2) {
        this.coefX2 = coefX2;
    }

    public double getRightSide() {
        return rightSide;
    }

    public void setRightSide(double rightSide) {
        this.rightSide = rightSide;
    }

    public String toString(){
        return "X0 = " + nullX.toString() + "\nY0 = " + nullY.toString();
    }

}
