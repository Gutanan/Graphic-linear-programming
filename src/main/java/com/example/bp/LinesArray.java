package com.example.bp;

import java.util.ArrayList;

public class LinesArray {

    private ArrayList<LinearLine> lines;

    public LinesArray(){
        lines = new ArrayList<>();
    }

    public ArrayList<LinearLine> getLines() {
        return lines;
    }

    public void addLine(LinearLine line){
        lines.add(line);
    }

    public ArrayList<Point> findBasicSolutions() {
        ArrayList<Point> basicSolutions = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            for (int j = i+1; j < lines.size(); j++){
                Point intersection = countIntersection(lines.get(i), lines.get(j));
                if (intersection.getX() >= 0d && intersection.getY() >= 0d){
                    basicSolutions.add(intersection);
                }
            }
            if (lines.get(i).getNullX().getX() >= 0d && lines.get(i).getNullX().getY() >= 0d){
                basicSolutions.add(lines.get(i).getNullX());
            }
            if (lines.get(i).getNullY().getX() >= 0d && lines.get(i).getNullY().getY() >= 0d){
                basicSolutions.add(lines.get(i).getNullY());
            }
        }
        basicSolutions.add(new Point(0d, 0d));
        return basicSolutions;
    }

    public ArrayList<Point> findPossibleSolutions() {
        ArrayList<Point> basicSolutions = findBasicSolutions();
        ArrayList<Point> possibleSolutions = new ArrayList<>();
        boolean isPossible;
        for (int i = 0; i < basicSolutions.size(); i++) {
            isPossible = true;
            for (int j = 0; j < lines.size(); j++) {
                double value = ((lines.get(j).getCoefX1()*basicSolutions.get(i).getX()) + (lines.get(j).getCoefX2()*basicSolutions.get(i).getY()));
                if (value > lines.get(j).getRightSide()) {
                    isPossible = false;
                }
            }
            if (isPossible) {
                possibleSolutions.add(basicSolutions.get(i));
            }
        }
        return possibleSolutions;
    }

    public Point findOptimalSolution(PurposeLine purposeLine){
        ArrayList<Point> possibleSolutions = findPossibleSolutions();
        double x1 = purposeLine.getCoefX1();
        double x2 = purposeLine.getCoefX2();

        if (purposeLine.getPurpose() == PurposeLine.PURPOSE.MAX){
            double max = 0;
            double currentmax;
            int index = 0;
            for (int i = 0; i < possibleSolutions.size(); i++){
                currentmax = possibleSolutions.get(i).getX()*x1+possibleSolutions.get(i).getY()*x2;
                if (currentmax > max){
                    max = currentmax;
                    index = i;
                }
            }
            return possibleSolutions.get(index);
        } else {
            double min = 10000000;
            double currentmin;
            int index = 0;
            for (int i = 0; i < possibleSolutions.size(); i++){
                currentmin = possibleSolutions.get(i).getX()*x1+possibleSolutions.get(i).getY()*x2;
                if (currentmin < min){
                    min = currentmin;
                    index = i;
                }
            }
            return possibleSolutions.get(index);
        }
    }

    /**
     * Method use the Cramer's rule to find the intersection of two lines
     * @param a Line
     * @param b Line
     * @return Point of intersection
     */
    private Point countIntersection(LinearLine a, LinearLine b){
        Point intersection = new Point(0d, 0d);
        double x;
        double y;
        double [][] matrix = {{a.getCoefX1(), a.getCoefX2(), a.getRightSide()},
                {b.getCoefX1(), b.getCoefX2(), b.getRightSide()}};

        double [][] detX1 = {{matrix[0][2], matrix[0][1]},
                {matrix[1][2], matrix[1][1]}};
        double [][] detX2 ={{matrix[0][0], matrix[0][1]},
                {matrix[1][0], matrix[1][1]}};

        double [][] detY1 ={{matrix[0][0], matrix[0][2]},
                {matrix[1][0], matrix[1][2]}};
        double [][] detY2 ={{matrix[0][0], matrix[0][1]},
                {matrix[1][0], matrix[1][1]}};

        x = countDeterminantTwobyTwo(detX1)/countDeterminantTwobyTwo(detX2);
        y = countDeterminantTwobyTwo(detY1)/countDeterminantTwobyTwo(detY2);
        intersection.setX(x);
        intersection.setY(y);
        return intersection;
    }

    private double countDeterminantTwobyTwo(double[][] matrix){
        double determinant;
        determinant = (matrix[0][0] * matrix[1][1]) - (matrix[1][0] * matrix[0][1]);
        return determinant;
    }

    public double[][] createStandartMatrix(){
        int rows = lines.size() + 1;
        int cols = 3 + lines.size();
        double[][] standartMatrix = new double[rows][cols];

        for (int i = 0; i < lines.size(); i++){
            standartMatrix[i][0] = lines.get(i).getCoefX1();
            standartMatrix[i][1] = lines.get(i).getCoefX2();
            for (int j = 2; j < cols - 1; j++){
                if (i == j - 2){
                    standartMatrix[i][j] = 1;
                } else {
                    standartMatrix[i][j] = 0;
                }
            }
            standartMatrix[i][cols-1] = lines.get(i).getRightSide();
        }
        for (int k = 0; k < cols; k++){
            standartMatrix[rows-1][k] = 0;
        }
        //print(standartMatrix);
        return standartMatrix;
    }

    public void print(double[][] table){
        for(int i = 0; i < lines.size() + 1; i++){
            for(int j = 0; j < lines.size() + 3; j++){
                String value = String.format("%.2f", table[i][j]);
                System.out.print(value + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }

    public double[][] addPurposeVector(double[][] matrix, double[] vector){
        for (int k = 0; k < lines.size() + 3; k++){
            matrix[lines.size()][k] = vector[k];
        }
        //print(matrix);
        return matrix;
    }


}
