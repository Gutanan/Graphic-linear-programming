package com.example.bp;

import java.util.ArrayList;

public class LinesArray {

    private ArrayList<LinearLine> lines;

    public LinesArray() {
        lines = new ArrayList<>();
    }

    public ArrayList<LinearLine> getLines() {
        return lines;
    }

    public void addLine(LinearLine line) {
        lines.add(line);
    }

    /**
     * Method goes through array of Constrains and count Basic solutions
     *
     * @return Array list of Points that are Basic Solutions
     */
    public ArrayList<Point> findBasicSolutions() {
        ArrayList<Point> basicSolutions = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            for (int j = i + 1; j < lines.size(); j++) {
                Point intersection = countIntersection(lines.get(i), lines.get(j));
                if (intersection.getX() >= 0d && intersection.getY() >= 0d) {
                    basicSolutions.add(intersection);
                }
            }
            if (lines.get(i).getNullX().getX() >= 0d && lines.get(i).getNullX().getY() >= 0d) {
                basicSolutions.add(lines.get(i).getNullX());
            }
            if (lines.get(i).getNullY().getX() >= 0d && lines.get(i).getNullY().getY() >= 0d) {
                basicSolutions.add(lines.get(i).getNullY());
            }
        }

        boolean add;
        ArrayList<Point> basicSolutionsDuplicates = new ArrayList<>();
        if (basicSolutions.size() > 0) {
            basicSolutionsDuplicates.add(basicSolutions.get(0));
        }
        for (int i = 1; i < basicSolutions.size(); i++) {
            double xi = basicSolutions.get(i).getX();
            double yi = basicSolutions.get(i).getY();
            add = true;
            for (int j = 1; j < basicSolutionsDuplicates.size(); j++) {
                double xj = basicSolutionsDuplicates.get(j).getX();
                double yj = basicSolutionsDuplicates.get(j).getY();
                if (xi == xj && yi == yj) {
                    add = false;
                    break;
                }
            }
            if (add) {
                basicSolutionsDuplicates.add(basicSolutions.get(i));
            }
        }
        basicSolutionsDuplicates.add(new Point(0d, 0d));
        return basicSolutionsDuplicates;
    }

    /**
     * Method goes through Basic Solutions points and evaluates, whether they are also Possible Solutions points
     * While comparing it rounds the result to 6 decimals to avoid double wrong rounding
     * @return ArrayList of Possible Solutions points
     */
    public ArrayList<Point> findPossibleSolutions() {
        ArrayList<Point> basicSolutions = findBasicSolutions();
        ArrayList<Point> possibleSolutions = new ArrayList<>();
        boolean isPossible;
        for (Point basicSolution : basicSolutions) {
            isPossible = true;
            for (LinearLine line : lines) {
                double value = ((line.getCoefX1() * basicSolution.getX()) + (line.getCoefX2() * basicSolution.getY()));
                value = round(value, 6);
                if (line.getRestrain().equals(LinearLine.RESTRAIN.LOWER)) {
                    if (value > line.getRightSide()) {
                        isPossible = false;
                    }
                }
                if (line.getRestrain().equals(LinearLine.RESTRAIN.GREATER)) {
                    if (value < line.getRightSide()) {
                        isPossible = false;
                    }
                }
                if (line.getRestrain().equals(LinearLine.RESTRAIN.EQUAL)) {
                    if (value != line.getRightSide()) {
                        isPossible = false;
                    }
                }

            }
            if (isPossible) {
                possibleSolutions.add(basicSolution);
            }
        }
        return possibleSolutions;
    }

    /**
     * Method goes through Possible Solutions points and chooses the one, that is according to the purpose line
     * optimal
     *
     * @param purposeLine is the Purpose line
     * @return one Point which appears to be optimal according to the purpose line
     */
    public Point findOptimalSolution(PurposeLine purposeLine) {
        ArrayList<Point> possibleSolutions = findPossibleSolutions();
        double x1 = purposeLine.getCoefX1();
        double x2 = purposeLine.getCoefX2();

        if (purposeLine.getPurpose() == PurposeLine.PURPOSE.MAX) {
            double max = 0;
            double currentmax;
            int index = 0;
            for (int i = 0; i < possibleSolutions.size(); i++) {
                currentmax = possibleSolutions.get(i).getX() * x1 + possibleSolutions.get(i).getY() * x2;
                if (currentmax > max) {
                    max = currentmax;
                    index = i;
                }
            }
            return possibleSolutions.get(index);
        } else {
            double min = Double.MAX_VALUE;
            double currentmin;
            int index = 0;
            for (int i = 0; i < possibleSolutions.size(); i++) {
                currentmin = possibleSolutions.get(i).getX() * x1 + possibleSolutions.get(i).getY() * x2;
                if (currentmin < min) {
                    min = currentmin;
                    index = i;
                }
            }
            return possibleSolutions.get(index);
        }
    }

    /**
     * Method uses the Cramer's rule to find the intersection of two lines
     *
     * @param a Line
     * @param b Line
     * @return Point of intersection
     */
    private Point countIntersection(LinearLine a, LinearLine b) {
        Point intersection = new Point(0d, 0d);
        double x;
        double y;
        double[][] matrix = {{a.getCoefX1(), a.getCoefX2(), a.getRightSide()},
                {b.getCoefX1(), b.getCoefX2(), b.getRightSide()}};

        double[][] detX1 = {{matrix[0][2], matrix[0][1]},
                {matrix[1][2], matrix[1][1]}};
        double[][] detX2 = {{matrix[0][0], matrix[0][1]},
                {matrix[1][0], matrix[1][1]}};

        double[][] detY1 = {{matrix[0][0], matrix[0][2]},
                {matrix[1][0], matrix[1][2]}};
        double[][] detY2 = {{matrix[0][0], matrix[0][1]},
                {matrix[1][0], matrix[1][1]}};

        x = countDeterminantTwobyTwo(detX1) / countDeterminantTwobyTwo(detX2);
        y = countDeterminantTwobyTwo(detY1) / countDeterminantTwobyTwo(detY2);
        intersection.setX(x);
        intersection.setY(y);
        return intersection;
    }

    /**
     * Method counts determinant
     *
     * @param matrix from which the determinant is beeing counted
     * @return determinant
     */
    private double countDeterminantTwobyTwo(double[][] matrix) {
        double determinant;
        determinant = (matrix[0][0] * matrix[1][1]) - (matrix[1][0] * matrix[0][1]);
        return determinant;
    }

    /**
     * First step of creating a simplex matrix
     * It chooses between standart and 2phase simplex matrix
     * @return matrix
     */
    public double[][] createMatrix() {
        boolean isStandart = true;
        for (LinearLine line : lines) {
            if (!line.getRestrain().equals(LinearLine.RESTRAIN.LOWER)) {
                isStandart = false;
                break;
            }
        }
        if (isStandart) {
            return createStandartMatrix();
        } else {
            return createTwoPhaseMatrix();
        }
    }

    /**
     * Method takes the constrains and builds a matrix for simplex method when all restrains are type LOWER
     *
     * @return matrix
     */
    public double[][] createStandartMatrix() {
        int rows = lines.size() + 1;
        int cols = 3 + lines.size();
        double[][] standartMatrix = new double[rows][cols];

        for (int i = 0; i < lines.size(); i++) {
            standartMatrix[i][0] = lines.get(i).getCoefX1();
            standartMatrix[i][1] = lines.get(i).getCoefX2();
            for (int j = 2; j < cols - 1; j++) {
                if (i == j - 2) {
                    standartMatrix[i][j] = 1;
                } else {
                    standartMatrix[i][j] = 0;
                }
            }
            standartMatrix[i][cols - 1] = lines.get(i).getRightSide();
        }
        for (int k = 0; k < cols; k++) {
            standartMatrix[rows - 1][k] = 0;
        }
        //print(standartMatrix);
        return standartMatrix;
    }

    /**
     * Method counts the nubmer of auxiliary variables
     * @return num of auxiliary variables
     */
    public int getNumOfAuxiliary(){
        int numOfAuxiliary = 0;
        for (LinearLine line : lines) {
            if (line.getRestrain().equals(LinearLine.RESTRAIN.GREATER) || line.getRestrain().equals(LinearLine.RESTRAIN.EQUAL)) {
                numOfAuxiliary++;
            }
        }
        return numOfAuxiliary;
    }

    /**
     * Method counts the number od additional variables
     * @return the number
     */
    public int getNumOfAdditional(){
        int numOfAdditional = 0;
        for (LinearLine line : lines) {
            if (line.getRestrain().equals(LinearLine.RESTRAIN.GREATER) || line.getRestrain().equals(LinearLine.RESTRAIN.LOWER)) {
                numOfAdditional++;
            }
        }
        return numOfAdditional;
    }

    /**
     * Method creates simplex matrix when there are restrains GRATER or EQUAL
     *
     * @return matrix
     */
    public double[][] createTwoPhaseMatrix() {

        int numOfAdditional = getNumOfAdditional();
        int numOfAuxiliary = getNumOfAuxiliary();
        int numOfGreaterThan = 0;
        int positionAuxiliary = 0;
        for (LinearLine line : lines) {
            if (line.getRestrain().equals(LinearLine.RESTRAIN.GREATER)) {
                numOfGreaterThan++;
            }
        }
        int numOfEqual = 0;
        for (LinearLine line : lines) {
            if (line.getRestrain().equals(LinearLine.RESTRAIN.EQUAL)) {
                numOfEqual++;
            }
        }
        int numOfLowerThan = 0;
        for (LinearLine line : lines) {
            if (line.getRestrain().equals(LinearLine.RESTRAIN.LOWER)) {
                numOfLowerThan++;
            }
        }
        int rows = lines.size() + 2;
        int cols = 3 + numOfAdditional + numOfAuxiliary;
        double[][] standartMatrix = new double[rows][cols];

        for (int k = 0; k < cols; k++) {
            standartMatrix[rows - 1][k] = 0; //nulls in last row for auxiliary purpose function
            standartMatrix[rows - 2][k] = 0; //nulls in last row for purpose function
        }

        int i = 0;
        for (LinearLine line : lines) {
            if (!line.getRestrain().equals(LinearLine.RESTRAIN.EQUAL)) {
                standartMatrix[i][0] = line.getCoefX1();
                standartMatrix[i][1] = line.getCoefX2();
                if (line.getRestrain().equals(LinearLine.RESTRAIN.GREATER)) {
                    standartMatrix[rows - 1][0] += line.getCoefX1();
                    standartMatrix[rows - 1][1] += line.getCoefX2();
                }
                for (int j = 2; j < cols - 1; j++) {
                    if (i == j - 2) {
                        if (line.getRestrain().equals(LinearLine.RESTRAIN.LOWER)) {
                            standartMatrix[i][j] = 1;
                        } else if (line.getRestrain().equals(LinearLine.RESTRAIN.GREATER)) {
                            standartMatrix[i][j] = -1;
                            standartMatrix[rows - 1][j] += -1;
                        } else {
                            standartMatrix[i][j] = 0;
                        }
                    } else if (line.getRestrain().equals(LinearLine.RESTRAIN.GREATER) && (j == 2 + numOfAdditional + positionAuxiliary)) {
                        standartMatrix[i][j] = 1;
                    } else {
                        standartMatrix[i][j] = 0;
                    }
                }
                if (line.getRestrain().equals(LinearLine.RESTRAIN.GREATER)) {
                    positionAuxiliary++;
                    standartMatrix[rows - 1][cols - 1] += line.getRightSide();
                }
                standartMatrix[i][cols - 1] = line.getRightSide();
                i++;
            }
        }

        for (LinearLine line : lines) {
            if (line.getRestrain().equals(LinearLine.RESTRAIN.EQUAL)) {
                standartMatrix[i][0] = line.getCoefX1();
                standartMatrix[i][1] = line.getCoefX2();
                standartMatrix[rows - 1][0] += line.getCoefX1();
                standartMatrix[rows - 1][1] += line.getCoefX2();
                for (int j = 2; j < cols - 1; j++) {
                    if (j == 2 + numOfAdditional + positionAuxiliary) {
                        standartMatrix[i][j] = 1;
                    } else {
                        standartMatrix[i][j] = 0;
                    }
                }
                standartMatrix[i][cols - 1] = line.getRightSide();
                standartMatrix[rows - 1][cols - 1] += line.getRightSide();
                positionAuxiliary++;
                i++;
            }
        }

        return standartMatrix;
    }

    /**
     * Prints the simplex table
     * @param table table
     */
    public void print(double[][] table) {
        for (int i = 0; i < lines.size() + 1; i++) {
            for (int j = 0; j < lines.size() + 3; j++) {
                String value = String.format("%.2f", table[i][j]);
                System.out.print(value + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Method adds Purpose line to the matrix for simplex method
     *
     * @param matrix that represents constrains
     * @param vector of the purpose line
     * @return matrix for simplex method
     */
    public double[][] addPurposeVector(double[][] matrix, double[] vector) {
        if (lines.size() + 3 >= 0) System.arraycopy(vector, 0, matrix[lines.size()], 0, lines.size() + 3);
        //print(matrix);
        return matrix;
    }

    /**
     * Method does the rounding in double values
     * @param value double
     * @param places number of decimal places
     * @return result
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    /**
     * Method changes all restrains, that have their right side negative.
     * It multiplies all number of the line by -1 and if necessary changes the restraint type
     */
    public void doRightPositive(){
        for (LinearLine line : lines) {
            if (line.getRightSide() < 0) {
                line.setRightSide(line.getRightSide() * (-1));
                line.setCoefX1(line.getCoefX1() * (-1));
                line.setCoefX2(line.getCoefX2() * (-1));
                if (line.getRestrain().equals(LinearLine.RESTRAIN.LOWER)) {
                    line.setRestrain(LinearLine.RESTRAIN.GREATER);
                } else if (line.getRestrain().equals(LinearLine.RESTRAIN.GREATER)) {
                    line.setRestrain(LinearLine.RESTRAIN.LOWER);
                }
            }
        }
    }


}
