package com.example.bp;

public class SimplexMethod {

    private int rows, cols; // row and column
    private double[][] table; // simplex tableau
    private boolean solutionIsUnbounded = false;
    private PURPOSE purpose;


    public static enum ERROR {
        NOT_OPTIMAL,
        IS_OPTIMAL,
        UNBOUNDED
    };

    public static enum PURPOSE {
        MAX,
        MIN
    };

    public SimplexMethod(int numOfConstraints, int numOfUnknowns, PURPOSE purpose) {
        rows = numOfConstraints + 1; // row number + 1
        cols = numOfUnknowns + 1;   // column number + 1
        this.purpose = purpose;
        table = new double[rows][]; // create a 2d array

        // initialize references to arrays
        for (int i = 0; i < rows; i++) {
            table[i] = new double[cols];
        }
    }

    // fills the simplex tableau with coefficients
    public void fillTable(double[][] data) {
        for (int i = 0; i < table.length; i++) {
            System.arraycopy(data[i], 0, this.table[i], 0, data[i].length);
        }
    }

    public String printString() {
        String tableau = "";
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String value = String.format("%.2f", table[i][j]);
                tableau += (value + "\t");
            }
            tableau += "\n";
        }
        tableau += "\n";
        return tableau;
    }

    private int findPositionMax(double[] vector) {
        int position = 0;
        double maxValue = vector[0];
        for (int i = 1; i < vector.length; i++) {
            if (vector[i] > maxValue) {
                maxValue = vector[i];
                position = i;
            }
        }
        return position;
    }

    private int findPositionMin(double[] vector) {
        int position = 0;
        double minValue = vector[0];
        for (int i = 1; i < vector.length; i++) {
            if (vector[i] < minValue) {
                minValue = vector[i];
                position = i;
            }
        }
        return position;
    }

    private boolean areAllNullNegative(double[] vector) {
        boolean outcome = false;
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] < 0) {
                outcome = true;
            }
        }
        return outcome;
    }

    private boolean areAllNullPositive(double[] vector) {
        boolean outcome = false;
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] > 0) {
                outcome = true;
            }
        }
        return outcome;
    }

    public void iterateTable() {
        if (this.purpose.equals(PURPOSE.MAX)) {
            while (areAllNullNegative(table[rows - 1])) {
                int enteringVariable = findPositionMin(table[rows - 1]);
                double[] valuesOfExitingVariable = new double[rows - 1];
                for (int i = 0; i < valuesOfExitingVariable.length; i++) {
                    valuesOfExitingVariable[i] = table[i][cols - 1] / table[i][enteringVariable];
                }
                int exitingVariable = findPositionMin(valuesOfExitingVariable);
                double keyElement = table[exitingVariable][enteringVariable];
                for (int i = 0; i < cols; i++) {
                    table[exitingVariable][i] /= keyElement;
                }
                for (int i = 0; i < rows; i++) {
                    if (i != exitingVariable) {
                        double multiplier = -table[i][enteringVariable] / table[exitingVariable][enteringVariable];
                        for (int j = 0; j < cols; j++) {
                            table[i][j] += table[exitingVariable][j] * multiplier;
                        }
                    }
                }
            }
        } else {
            while (areAllNullPositive(table[rows - 1])) {
                int enteringVariable = findPositionMax(table[rows - 1]);
                double[] valuesOfExitingVariable = new double[rows - 1];
                for (int i = 0; i < valuesOfExitingVariable.length; i++) {
                    valuesOfExitingVariable[i] = table[i][cols - 1] / table[i][enteringVariable];
                }
                int exitingVariable = findPositionMax(valuesOfExitingVariable);
                double keyElement = table[exitingVariable][enteringVariable];
                for (int i = 0; i < cols; i++) {
                    table[exitingVariable][i] /= keyElement;
                }
                for (int i = 0; i < rows; i++) {
                    if (i != exitingVariable) {
                        double multiplier = -table[i][enteringVariable] / table[exitingVariable][enteringVariable];
                        for (int j = 0; j < cols; j++) {
                            table[i][j] += table[exitingVariable][j] * multiplier;
                        }
                    }
                }
            }
        }
    }


}
