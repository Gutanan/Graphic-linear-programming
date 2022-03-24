package com.example.bp;

/**
 * Class represents object of a linear line with 2 coeficients (x1; x2) and its riht side
 */
public class LinearLine {
    private Point nullX;
    private Point nullY;
    private double coefX1; //multiplier of X1
    private double coefX2; //multiplier of X2
    private double rightSide; //right side of equation of line
    private RESTRAIN restrain;

    public LinearLine(double coefX1, double coefX2, double rightSide, RESTRAIN restrain) {
        this.coefX1 = coefX1;
        this.coefX2 = coefX2;
        this.rightSide = rightSide;
        this.restrain = restrain;
        countNullX(coefX1,rightSide);
        countNullY(coefX2,rightSide);
    }

    /**
     * Restrain can be GREATER or LOWER or EQUAL value
     */
    public static enum RESTRAIN{
        GREATER,
        LOWER,
        EQUAL
    };

    public Point getNullX() {
        return nullX;
    }

    public Point getNullY() {
        return nullY;
    }

    /**
     * Method counts the intersections with X axis
     * When the X1 is 0 the intersection is in infinity
     * @param coefX1 from Line
     * @param rightSide from Line
     */
    private void countNullX(double coefX1, double rightSide) {
        double x = rightSide/coefX1;
        this.nullX = new Point(x, 0d);
    }
    /**
     * Method counts the intersections with Y axis
     * When the X2 is 0 the intersection is in infinity
     * @param coefX2 from Line
     * @param rightSide from Line
     */
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

    public RESTRAIN getRestrain() {
        return restrain;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public String toString(){
        return "X0 = " + nullX.toString() + "\nY0 = " + nullY.toString();
    }

}
