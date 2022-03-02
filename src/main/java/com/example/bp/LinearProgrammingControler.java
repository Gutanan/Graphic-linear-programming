package com.example.bp;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class is Javafx Controller for LinearProgrammingAplication
 */
public class LinearProgrammingControler {

    private final String lineMiner = "([+-]? ?\\d+) ?[xX]1? ?([+-]? ?\\d+) ?[xXyY]2? ?<= ?([+-]? ?\\d+)";
    private final String purposeMiner = "(max|MAX|min|MIN) ?= ?([+-]? ?\\d+)[xX]1? ?([+-]? ?\\d+)[xXyY]2?";
    private double zoom = 50d;
    private int numOfConstrains = 2;

    /**
     * Method is run by button "Vykresli"
     * It parses the text from the input into program representation then calls to show results
     */
    @FXML
    protected void count() {

        LinesArray lines = new LinesArray();
        for (int i = 2; i < numOfConstrains+2; i++){
            TextField tf = (TextField) constrains.getChildren().get(i-1);
            String lineText = tf.getText();
            LinearLine ln = mineLine(lineText, lineMiner);
            Line line = (Line) graph.getChildren().get(i);
            drawLine(ln, line);
            line.setVisible(true);
            lines.addLine(ln);
        }

        String purposeText = purposeLine.getText();
        PurposeLine purpose = minePurposeLine(purposeText, purposeMiner);

        output.appendText("Základní řešení: \n");
        output.appendText(lines.findBasicSolutions().toString());
        output.appendText("\n");
        output.appendText("Optimální řešení: \n");
        output.appendText(lines.findOptimalSolution(purpose).toString());
        output.appendText("\n");

        drawShapes(purpose,lines);

    }

    /**
     * Method is run by button "Přidej Omezení"
     * When the number of contrains is lower than 10 it adds one TextField (sets it visible)
     */
    @FXML
    protected void addConstrain(){
        if (numOfConstrains < 10){
            numOfConstrains++;
            constrains.getChildren().get(numOfConstrains).setVisible(true);
        }
    }

    /**
     * Method is run by button "Odeber Omezení"
     * When the number iof constrains is higher than 1 it removes TextField (sets it invisible)
     */
    @FXML
    protected void removeConstrain(){
        if (numOfConstrains > 1){
            TextField tf = (TextField) constrains.getChildren().get(numOfConstrains);
            tf.setVisible(false);
            tf.setText("");
            numOfConstrains--;
        }
    }

    /**
     * Method is run by button "Vymazej"
     * Makes the graph elements invisible and resets the texts in TextFiels on input
     */
    @FXML
    protected void clear(){
        purpLine.setVisible(false);
        optimalCircle.setVisible(false);
        possibleSolutionsPolygon.setVisible(false);

        for (int i = 2; i < numOfConstrains+2; i++){
            TextField tf = (TextField) constrains.getChildren().get(i-1);
            tf.setText("");
            Line line = (Line) graph.getChildren().get(i);
            line.setVisible(false);
        }
    }

    @FXML
    private TextField purposeLine;

    @FXML
    private Line purpLine;

    @FXML
    private Circle optimalCircle;

    @FXML
    private Polygon possibleSolutionsPolygon;

    @FXML
    private TextArea output;

    @FXML
    private VBox constrains;

    @FXML
    private AnchorPane graph;

    /**
     * Method takes the input and with regex finds constrain coeficients
     * @param line input
     * @param miner regex
     * @return LinearLine object
     */
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

    /**
     * Method takes the input and with regex finds purpose line coeficients
     * @param line input
     * @param miner regex
     * @return PurposeLIne object
     */
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

    /**
     * Method set the ends of a javafx line -> draws it in the graph
     * @param linearLine object representation of a contrain
     * @param line javafx Line
     */
    private void drawLine (LinearLine linearLine, Line line) {
        Point x0 = linearLine.getNullX();
        line.setEndX(x0.getX() * zoom);
        Point y0 = linearLine.getNullY();
        line.setStartY(y0.getY() * (-zoom));
    }
    /**
     * Method set the ends of a javafx line -> draws the purpose line in the graph
     * @param purposeLine object representation of a purpose line
     * @param line javafx Line
     * @param solution optimal solution Point
     */
    private void drawPurposeLine (PurposeLine purposeLine, Line line, Point solution) {
        double valueOfPurpose = ((purposeLine.getCoefX1() * solution.getX()) + (purposeLine.getCoefX2() * solution.getY()));
        LinearLine ll = new LinearLine(purposeLine.getCoefX1(), purposeLine.getCoefX2(), valueOfPurpose);
        Point x0 = ll.getNullX();
        line.setEndX(x0.getX() * zoom);
        Point y0 = ll.getNullY();
        line.setStartY(y0.getY() * (-zoom));
    }

    /**
     * Method draws the javafx circle in the positions of optimal solution point
     * @param solution optimal solution point
     * @param circle javafx Circle
     */
    private void drawOptimalCircle(Point solution, Circle circle) {
        circle.setCenterX(solution.getX() * zoom);
        circle.setCenterY(solution.getY() * (-zoom));
    }

    /**
     * Method takes the Possible solutions and draws the polygon with them in the graph
     * @param lines of the constrains
     * @param polygon javafx polygon
     */
    private void drawPolygon(LinesArray lines, Polygon polygon) {
        int numberOfPoints = lines.findPossibleSolutions().size();
        ArrayList<Point> possiblePoints = lines.findPossibleSolutions();
        possiblePoints = satisfyPoints(possiblePoints);
        if (numberOfPoints > 1) {
            Double[] points = new Double[numberOfPoints*2];
            for (int i = 0; i < numberOfPoints; i++){
                System.out.println(possiblePoints.get(i).toString());
                points[2*i] = possiblePoints.get(i).getX() * zoom;
                points[2*i+1] = possiblePoints.get(i).getY() * (-zoom);
            }

            polygon.getPoints().clear();
            polygon.getPoints().addAll(points);
            polygon.setSmooth(true);
        }
    }

    /**
     * Method justify the order of points to see the polygon the right way
     * @param edges Points that are egdes of polygon
     * @return justified edges
     * TODO THIS IS NOT OPTIMAL
     */
    private ArrayList<Point> satisfyPoints(ArrayList<Point> edges){
        ArrayList<Point> justifiedEdges = new ArrayList<>();
        justifiedEdges.add(edges.get(1));
        justifiedEdges.add(edges.get(0));
        for (int i = 2; i < edges.size(); i++){
            justifiedEdges.add(edges.get(i));
        }
        return justifiedEdges;
    }

    /**
     * Method draws shapes according to the results of counting into the graph
     * @param purpose purposeLine
     * @param lines constrains
     */
    private void drawShapes(PurposeLine purpose, LinesArray lines){
        drawPurposeLine(purpose, purpLine, lines.findOptimalSolution(purpose));
        drawOptimalCircle(lines.findOptimalSolution(purpose), optimalCircle);
        drawPolygon(lines, possibleSolutionsPolygon);

        purpLine.setVisible(true);
        optimalCircle.setVisible(true);
        possibleSolutionsPolygon.setVisible(true);
    }
}