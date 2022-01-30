package com.example.bp;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinearProgrammingControler {

    private final String lineMiner = "([+-]? ?\\d+) ?[xX]1? ?([+-]? ?\\d+) ?[xXyY]2? ?<= ?([+-]? ?\\d+)";
    private final String purposeMiner = "(max|MAX|min|MIN) ?= ?([+-]? ?\\d+)[xX]1? ?([+-]? ?\\d+)[xXyY]2?";
    private double zoom = 50d;

    @FXML
    protected void count() {
        output.setText("Output\n");
        String lineText1 = restrain1.getText();
        String lineText2 = restrain2.getText();
        String purposeText = purposeLine.getText();

        LinearLine linearLine1 = mineLine(lineText1, lineMiner);
        output.appendText(linearLine1.toString());
        output.appendText("\n");
        drawLine(linearLine1, line1);

        LinearLine linearLine2 = mineLine(lineText2,lineMiner);
        output.appendText(linearLine2.toString());
        output.appendText("\n");
        drawLine(linearLine2, line2);

        PurposeLine purpose = minePurposeLine(purposeText, purposeMiner);

        LinesArray lines = new LinesArray();
        lines.addLine(linearLine1);
        lines.addLine(linearLine2);

        output.appendText("Základní řešení: \n");
        output.appendText(lines.findBasicSolutions().toString());
        output.appendText("\n");
        output.appendText("Optimální řešení: \n");
        output.appendText(lines.findOptimalSolution(purpose).toString());
        output.appendText("\n");

        drawPurposeLine(purpose, purpLine, lines.findOptimalSolution(purpose));
        drawOptimalCircle(lines.findOptimalSolution(purpose), optimalCircle);
        drawPolygon(lines, possibleSolutionsPolygon);

        boolean quit = false;

        Simplex simplex = new Simplex(2, 4);

        double[][] standartMatrix = lines.createStandartMatrix();
        double[] purpLine = purpose.createVector(lines.getLines().size());
        simplex.fillTable(lines.addPurposeVector(standartMatrix,purpLine));

        output.appendText("Start: \n");
        output.appendText(simplex.printString());

        // repeat until solution found or unbounded
        while(!quit){
            Simplex.ERROR error = simplex.compute();

            if(error == Simplex.ERROR.IS_OPTIMAL){
                output.appendText("Následující tabulka je optimální: \n");
                output.appendText(simplex.printString());
                quit = true;
            }
            else if(error == Simplex.ERROR.UNBOUNDED){
                System.out.println("Neomezené řešení");
                quit = true;
            }
        }
    }

    @FXML
    private TextField restrain1;

    @FXML
    private TextField restrain2;

    @FXML
    private TextField purposeLine;

    @FXML
    private Line line1;

    @FXML
    private Line line2;

    @FXML
    private Line purpLine;

    @FXML
    private Circle optimalCircle;

    @FXML
    private Polygon possibleSolutionsPolygon;

    @FXML
    private TextArea output;

    private LinearLine mineLine(String line, String miner){
        LinearLine result = null;
        Pattern pattern = Pattern.compile(miner);
        Matcher matcher = pattern.matcher(line);
        String[] coefs = new String[3];
        while (matcher.find()) {
            coefs[0] = matcher.group(1).replaceAll("\\s", "");
            coefs[1] = matcher.group(2).replaceAll("\\s", "");
            coefs[2] = matcher.group(3).replaceAll("\\s", "");
        }
        result = new LinearLine(Integer.parseInt(coefs[0]),Integer.parseInt(coefs[1]),Integer.parseInt(coefs[2]));
        return result;
    }

    private PurposeLine minePurposeLine(String line, String miner){
        PurposeLine purposeLine = null;
        Pattern pattern = Pattern.compile(miner);
        Matcher matcher = pattern.matcher(line);
        String[] result = new String[3];
        while (matcher.find()) {
            result[0] = matcher.group(1).replaceAll("\\s", "");
            result[1] = matcher.group(2).replaceAll("\\s", "");
            result[2] = matcher.group(3).replaceAll("\\s", "");
        }
        if (result[0].equalsIgnoreCase("max")){
            purposeLine = new PurposeLine(PurposeLine.PURPOSE.MAX, Integer.parseInt(result[1]), Integer.parseInt(result[2]));
        } else {
            purposeLine = new PurposeLine(PurposeLine.PURPOSE.MIN, Integer.parseInt(result[1]), Integer.parseInt(result[2]));
        }
        return purposeLine;
    }

    private void drawLine (LinearLine linearLine, Line line) {
        Point x0 = linearLine.getNullX();
        line.setEndX(x0.getX() * zoom);
        Point y0 = linearLine.getNullY();
        line.setStartY(y0.getY() * (-zoom));
    }

    private void drawPurposeLine (PurposeLine purposeLine, Line line, Point solution) {
        double valueOfPurpose = ((purposeLine.getCoefX1() * solution.getX()) + (purposeLine.getCoefX2() * solution.getY()));
        LinearLine ll = new LinearLine(purposeLine.getCoefX1(), purposeLine.getCoefX2(), valueOfPurpose);
        Point x0 = ll.getNullX();
        line.setEndX(x0.getX() * zoom);
        Point y0 = ll.getNullY();
        line.setStartY(y0.getY() * (-zoom));
    }

    private void drawOptimalCircle(Point solution, Circle circle) {
        circle.setCenterX(solution.getX() * zoom);
        circle.setCenterY(solution.getY() * (-zoom));
    }

    private void drawPolygon(LinesArray lines, Polygon polygon) {
        int numberOfPoints = lines.findPossibleSolutions().size();
        ArrayList<Point> possiblePoints = lines.findPossibleSolutions();
        if (numberOfPoints > 1) {
            Double[] points = new Double[numberOfPoints*2];
            for (int i = 0; i < numberOfPoints; i++){
                System.out.println(possiblePoints.get(i).toString());
                points[2*i] = possiblePoints.get(i).getX() * zoom;
                points[2*i+1] = possiblePoints.get(i).getY() * (-zoom);
            }
            polygon.getPoints().clear();
            polygon.getPoints().addAll(points);
        }
    }
}