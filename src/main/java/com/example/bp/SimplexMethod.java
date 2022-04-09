package com.example.bp;

/**
 * Method is a representation of a Simplex method
 * It does the counting and checking result
 */
public class SimplexMethod {

    private int rows, cols; // row and column
    private double[][] table; // simplex tableau
    private boolean solutionIsUnbounded = false;
    private PURPOSE purpose;
    private PURPOSE actualPurpose;
    private boolean isTwoPhase;
    private ERROR resultError = ERROR.IS_OPTIMAL;


    public static enum ERROR {
        NOT_OPTIMAL,
        IS_OPTIMAL,
        UNBOUNDED
    };

    public static enum PURPOSE {
        MAX,
        MIN
    };

    /**
     * Simplex table constructor with setting the parameters
     * @param numOfConstraints
     * @param numOfUnknowns
     * @param purpose
     * @param isTwoPhase
     */
    public SimplexMethod(int numOfConstraints, int numOfUnknowns, PURPOSE purpose, boolean isTwoPhase) {
        rows = numOfConstraints + 1; // row number + 1
        cols = numOfUnknowns + 1;   // column number + 1
        this.purpose = purpose;
        table = new double[rows][]; // create a 2d array
        this.isTwoPhase = isTwoPhase;
        // when twophase it sets the purpose for MIN
        if (isTwoPhase) {
            actualPurpose = purpose; // saves the purpose for later
            this.purpose = PURPOSE.MIN;
        }

        // initialize references to arrays
        for (int i = 0; i < rows; i++) {
            table[i] = new double[cols];
        }
    }

    /**
     * Method  fills the simplex table with coefficients
     * @param data data from example
     */
    public void fillTable(double[][] data) {
        for (int i = 0; i < table.length; i++) {
            System.arraycopy(data[i], 0, this.table[i], 0, data[i].length);
        }
    }

    /**
     * Creates string that represents the current table
     * @return String
     */
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

    /**
     * Method finds the position of max value double in vector
     * @param vector
     * @return int position
     */
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

    /**
     * Method finds the position of min value double in vector
     * @param vector
     * @return int position
     */
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

    /**
     * Method evaluates whether all values are zero or negative in vector
     * @param vector
     * @return true if are
     * @return false if are not
     */
    private boolean areAllNullNegative(double[] vector) {
        boolean outcome = false;
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] < 0) {
                outcome = true;
            }
        }
        return outcome;
    }

    /**
     * Method evaluates whether all values are zero or positive in vector
     * @param vector
     * @return true if are
     * @return false if are not
     */
    private boolean areAllNullPositive(double[] vector) {
        boolean outcome = false;
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] > 0) {
                outcome = true;
            }
        }
        return outcome;
    }

    /**
     * Method follows the steps in simplex method
     * 1. test of optimality
     * 2. finding of the key element with entering and exiting variable
     * 3. recalculation of the table
     * 4. test of optimality
     */
    public void iterateTable() {
        ERROR error = ERROR.NOT_OPTIMAL;
        if (this.purpose.equals(PURPOSE.MAX)) {
            while (error.equals(ERROR.NOT_OPTIMAL)) {
                int enteringVariable = findPositionMin(avoidLastElement(table[rows - 1]));
                double[] valuesOfExitingVariable = new double[rows - 1];
                setValuesOfExitingVariableVector(valuesOfExitingVariable,enteringVariable);
                if(isUnbounded(valuesOfExitingVariable)){
                    error = ERROR.UNBOUNDED;
                    resultError = error;
                    break;
                }
                int exitingVariable = findPositionMin(valuesOfExitingVariable);
                double keyElement = table[exitingVariable][enteringVariable];
                for (int i = 0; i < cols; i++) {
                    table[exitingVariable][i] /= keyElement;
                }
                calculateOtherRows(enteringVariable,exitingVariable);
                error = checkSolution();
            }
        } else {
            while (error.equals(ERROR.NOT_OPTIMAL)) {
                int enteringVariable = findPositionMax(avoidLastElement(table[rows - 1]));
                double[] valuesOfExitingVariable;
                if (isTwoPhase) {
                    valuesOfExitingVariable = new double[rows - 2];
                    setValuesOfExitingVariableVector(valuesOfExitingVariable,enteringVariable);
                } else {
                    valuesOfExitingVariable = new double[rows - 1];
                    setValuesOfExitingVariableVector(valuesOfExitingVariable,enteringVariable);
                }
                if(isUnbounded(valuesOfExitingVariable)){
                    error = ERROR.UNBOUNDED;
                    resultError = error;
                    break;
                }
                int exitingVariable = findPositionMin(valuesOfExitingVariable);
                double keyElement = table[exitingVariable][enteringVariable];
                for (int i = 0; i < cols; i++) {
                    table[exitingVariable][i] /= keyElement;
                }
                calculateOtherRows(enteringVariable,exitingVariable);
                error = checkSolution();
            }
        }
        System.out.println(resultError.toString());
    }

    /**
     * Method returns string of the Solution price from simplex table
     * @return
     */
    public String returnSolutionPrice(){
        return "z = (" + String.format("%.2f", table[rows-1][cols-1]) + ")";
    }

    /**
     * Method takes the simplex table and counts the solution vector
     * @return String in LP form
     */
    public String returnSolutionVector(){
        String result = "x = (";
        String[] vector = new String[cols-1];
        for (int i = 0; i < vector.length; i++){
            vector[i] = "0,00";
        }
        for (int i = 0; i < table.length; i++){
            for (int j = 0; j < table[0].length-1; j++){
                if (table[i][j] == 1d && colIsUnitVector(i,j)){
                    vector[j] = String.format("%.2f", table[i][cols-1]);
                }
            }
        }
        result += buildVector(vector);
        result = result.substring(0, result.length() - 2);
        result += ")";
        return result;
    }

    /**
     * Method changes the array into one string
     * @param vector array of strings
     * @return one string in LP form
     */
    private String buildVector(String[] vector) {
        String outcome = "";
        for (int i = 0; i < vector.length; i++){
            outcome += vector[i] + "; ";
        }
        return outcome;
    }

    /**
     * Method checks whether is the elements (i,j) column an unit vector
     * @param i position in table
     * @param j position in table
     * @return true if it is unit vector
     */
    private boolean colIsUnitVector(int i, int j) {
        boolean isUnit = true;
        for (int k = 0; k < table.length; k++){
            if (table[k][j] != 0d && k != i){
                isUnit = false;
            }
        }
        return isUnit;
    }

    /**
     * Method calculates other rows in order to make unit vector on entering variable
     * @param enteringVariable
     * @param exitingVariable
     */
    private void calculateOtherRows(int enteringVariable, int exitingVariable){
        for (int i = 0; i < rows; i++) {
            if (i != exitingVariable) {
                double multiplier = -table[i][enteringVariable] / table[exitingVariable][enteringVariable];
                for (int j = 0; j < cols; j++) {
                    table[i][j] += table[exitingVariable][j] * multiplier;
                }
            }
        }
    }

    /**
     * Method calculates the values of exiting variables and puts them in the vector
     * @param valuesOfExitingVariable
     * @param enteringVariable
     */
    private void setValuesOfExitingVariableVector(double[] valuesOfExitingVariable, int enteringVariable){
        for (int i = 0; i < valuesOfExitingVariable.length; i++) {
            double value = table[i][cols - 1] / table[i][enteringVariable];
            if (table[i][enteringVariable] > 0){
                valuesOfExitingVariable[i] = value;
            } else {
                valuesOfExitingVariable[i] = Double.MAX_VALUE;
            }
        }
    }

    /**
     * Method evaluates whether the solution is unbounded from vector of values of exiting variable
     * @param valuesOfExitingVariable
     * @return true if is unbounded
     * @return false if is not
     */
    private boolean isUnbounded(double[] valuesOfExitingVariable) {
        boolean unbounded = true;
        for (int i = 0; i < valuesOfExitingVariable.length; i++){
            if (valuesOfExitingVariable[i] < Double.MAX_VALUE){
                unbounded = false;
            }
        }
        return unbounded;
    }

    /**
     * Method returns vector without last element in array
     * @param vector
     * @return
     */
    private double[] avoidLastElement(double[] vector){
        double[] result = new double[vector.length-1];
        for (int i = 0; i < result.length; i++){
            result[i] = vector[i];
        }
        return result;
    }

    /**
     * In case of 2phase it generates after the 1st phase a table for 2nd phase
     * It avoids the auxiliary columns and row
     * It also prepares the parameters for 2nd phase
     * @param numOfAdditional
     * @param numberOfAuxiliary
     */
    public void generateNewTable(int numOfAdditional, int numberOfAuxiliary) {
        if (isTwoPhase) {
            double newTableRows[][] = new double[rows - 1][cols];
            for (int i = 0; i < newTableRows.length; i++) {
                System.arraycopy(table[i], 0, newTableRows[i], 0, table[i].length);
            }
            double newTableRowsCols[][] = new double[rows - 1][cols-numberOfAuxiliary];
            for (int i = 0; i < newTableRowsCols.length; i++) {
                for (int j = 0; j < newTableRows[0].length; j++) {
                    if (j < 2+numOfAdditional){
                        newTableRowsCols[i][j] = newTableRows[i][j];
                    } else if (j == newTableRows[0].length-1){
                            newTableRowsCols[i][j-numberOfAuxiliary] = newTableRows[i][j];
                        }
                    }
                }
            this.table = newTableRowsCols;
            this.rows--;
            this.cols -= numberOfAuxiliary;
            this.purpose = this.actualPurpose;
            this.isTwoPhase = false;
            System.out.println(printString());
        }
    }

    /**
     * Method returns the ERROR wheter is the current table optiomal or not
     * It works on LP basic rules covered in the thesis
     * @return
     */
    private ERROR checkSolution(){
        ERROR error = ERROR.NOT_OPTIMAL;
        double[] rightSides = new double[rows-1];
        for (int i = 0; i < rows-1; i++){
            rightSides[i] = table[i][cols-1];
        }
        if (this.purpose.equals(PURPOSE.MAX)){
            if (!areAllNullNegative(table[rows - 1]) && areAllNullPositive(rightSides)){
                error = ERROR.IS_OPTIMAL;
            }
        }
        if (this.purpose.equals(PURPOSE.MIN)){
            if (!areAllNullPositive(table[rows - 1]) && areAllNullPositive(rightSides)){
                error = ERROR.IS_OPTIMAL;
            }
        }
        return error;

    }
}
