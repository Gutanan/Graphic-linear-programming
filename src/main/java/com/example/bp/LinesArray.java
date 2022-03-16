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
     *
     * @return ArrayList of Possible Solutions points
     * TODO double někdy zaokrouhlí špatně a proto se přípustné základní řešení nevykreslí
     */
    public ArrayList<Point> findPossibleSolutions() {
        ArrayList<Point> basicSolutions = findBasicSolutions();
        ArrayList<Point> possibleSolutions = new ArrayList<>();
        boolean isPossible;
        for (int i = 0; i < basicSolutions.size(); i++) {
            isPossible = true;
            for (int j = 0; j < lines.size(); j++) {
                double value = ((lines.get(j).getCoefX1() * basicSolutions.get(i).getX()) + (lines.get(j).getCoefX2() * basicSolutions.get(i).getY()));
                if (lines.get(j).getRestrain().equals(LinearLine.RESTRAIN.LOWER)) {
                    if (value > lines.get(j).getRightSide()) {
                        isPossible = false;
                    }
                }
                if (lines.get(j).getRestrain().equals(LinearLine.RESTRAIN.GREATER)) {
                    if (value < lines.get(j).getRightSide()) {
                        isPossible = false;
                    }
                }
                if (lines.get(j).getRestrain().equals(LinearLine.RESTRAIN.EQUAL)) {
                    if (value != lines.get(j).getRightSide()) {
                        isPossible = false;
                    }
                }

            }
            if (isPossible) {
                possibleSolutions.add(basicSolutions.get(i));
            }
        }
        return possibleSolutions;
    }

    /**
     * Method goes through Possible Solutions points and chooses the one, that is according to the purpose line
     * optimal
     *
     * @param purposeLine is the Purpose line
     * @return one Point which appears to be optimal accordint to the purpose line
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
            double min = 10000000;
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

    public double[][] createMatrix() {
        boolean isStandart = true;
        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).getRestrain().equals(LinearLine.RESTRAIN.LOWER)) {
                isStandart = false;
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
     * @return
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

    public double[][] createTwoPhaseMatrix() {
        int numOfAdditional = 0;
        int numOfAuxiliary = 0;
        int numOfGreaterThan = 0;
        int positionAuxiliary = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).getRestrain().equals(LinearLine.RESTRAIN.GREATER)) {
                numOfGreaterThan++;
                numOfAdditional++;
                numOfAuxiliary++;
            }
        }
        int numOfEqual = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).getRestrain().equals(LinearLine.RESTRAIN.EQUAL)) {
                numOfEqual++;
                numOfAuxiliary++;
            }
        }
        int numOfLowerThan = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).getRestrain().equals(LinearLine.RESTRAIN.LOWER)) {
                numOfLowerThan++;
                numOfAdditional++;
            }
        }
        int rows = lines.size() + 2;
        int cols = 3 + numOfAdditional + numOfAuxiliary;
        double[][] standartMatrix = new double[rows][cols];
        int i = 0;
        for (int k = 0; k < lines.size(); k++) {
            if (!lines.get(k).getRestrain().equals(LinearLine.RESTRAIN.EQUAL)) {
                standartMatrix[i][0] = lines.get(k).getCoefX1();
                standartMatrix[i][1] = lines.get(k).getCoefX2();
                for (int j = 2; j < cols - 1; j++) {
                    if (i == j - 2) {
                        if (lines.get(k).getRestrain().equals(LinearLine.RESTRAIN.LOWER)) {
                            standartMatrix[i][j] = 1;
                        } else if (lines.get(k).getRestrain().equals(LinearLine.RESTRAIN.GREATER)) {
                            standartMatrix[i][j] = -1;
                        } else {
                            standartMatrix[i][j] = 0;
                        }
                    } else if (lines.get(k).getRestrain().equals(LinearLine.RESTRAIN.GREATER) && (j == 2 + numOfAdditional + positionAuxiliary)){
                        standartMatrix[i][j] = 1;
                        positionAuxiliary++;
                    } else {
                        standartMatrix[i][j] = 0;
                    }

                    standartMatrix[i][cols - 1] = lines.get(k).getRightSide();
                }
                i++;
            }
        }

        for (int k = 0; k < lines.size(); k++) {
            if (lines.get(k).getRestrain().equals(LinearLine.RESTRAIN.EQUAL)) {
                standartMatrix[i][0] = lines.get(k).getCoefX1();
                standartMatrix[i][1] = lines.get(k).getCoefX2();
                for (int j = 2; j < cols - 1; j++) {
                    if (lines.get(k).getRestrain().equals(LinearLine.RESTRAIN.GREATER) && (j == 2 + numOfAdditional + positionAuxiliary)){
                        standartMatrix[i][j] = 1;
                        positionAuxiliary++;
                    } else {
                        standartMatrix[i][j] = 0;
                    }

                    standartMatrix[i][cols - 1] = lines.get(k).getRightSide();
                }
                i++;
            }
        }

        for (int k = 0; k < cols; k++) {
            standartMatrix[rows - 1][k] = 0; //nulls in last row for purpose function
            standartMatrix[rows - 2][k] = 0; //nulls in last row for purpose function
        }

        return standartMatrix;
    }


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
        for (int k = 0; k < lines.size() + 3; k++) {
            matrix[lines.size()][k] = vector[k];
        }
        //print(matrix);
        return matrix;
    }


}
