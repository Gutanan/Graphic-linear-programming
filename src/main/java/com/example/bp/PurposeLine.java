package com.example.bp;

/**
 * Class represents the purpose line with its two coeficients (x1; x2) and purpose type (min; max)
 */
public class PurposeLine {
    private double coefX1; //multiplier of X1
    private double coefX2; //multiplier of X2
    private PURPOSE purpose;

    public PurposeLine(PURPOSE purpose, double coefX1, double coefX2) {
        this.coefX1 = coefX1;
        this.coefX2 = coefX2;
        this.purpose = purpose;
    }

    /**
     * Purpose can be MAX or MIN value
     */
    public static enum PURPOSE{
        MAX,
        MIN
    };

    public double getCoefX1() {
        return coefX1;
    }

    public double getCoefX2() {
        return coefX2;
    }

    public PURPOSE getPurpose() {
        return purpose;
    }

    public void setCoefX1(double coefX1) {
        this.coefX1 = coefX1;
    }

    public void setCoefX2(double coefX2) {
        this.coefX2 = coefX2;
    }

    /**
     * Method creates vector representation of purpose lide for simplex method
     * @param numberOfCols is number of columns in example
     * @return vector for simplex method
     */
    public double[] createVector(int numberOfCols){
        double[] vector = new double[numberOfCols];
        vector[0] = coefX1 * -1;
        vector[1] = coefX2 * -1;
        for (int i = 2; i < numberOfCols; i++){
            vector[i] = 0;
        }
        return vector;
    }
}
