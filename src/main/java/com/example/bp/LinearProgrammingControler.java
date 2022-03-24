package com.example.bp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
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

    private final String lineMiner = "([+-]? ?\\d+) ?[xX]1? ?([+-]? ?\\d+) ?[xXyY]2? ?([<>]?=) ?([+-]? ?\\d+)";
    private final String purposeMiner = "(max|MAX|min|MIN) ?= ?([+-]? ?\\d+)[xX]1? ?([+-]? ?\\d+)[xXyY]2?";
    private double zoom = 50d;
    private int numOfConstrains = 2;

    /**
     * Method is run by button "Vykresli"
     * It parses the text from the input into program representation then calls to show results
     */
    @FXML
    protected void count() {
        setLabels();
        output.clear();
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
        output.appendText("Přípustná základní řešení: \n");
        output.appendText(lines.findPossibleSolutions().toString());
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
            graph.getChildren().get(numOfConstrains+1).setVisible(false);
            numOfConstrains--;
            count();
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
    protected void zoomPlus(){
        zoom = zoom * 2;
        count();
    }

    @FXML
    protected void zoomMinus(){
        zoom = zoom / 2;
        count();
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

    @FXML
    private Label labelx1;

    @FXML
    private Label labelx2;

    @FXML
    private Label labelx3;

    @FXML
    private Label labelx4;

    @FXML
    private Label labely1;

    @FXML
    private Label labely2;

    @FXML
    private Label labely3;

    @FXML
    private Label labely4;

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
        String[] coefs = new String[4];
        while (matcher.find()) {
            coefs[0] = matcher.group(1).replaceAll("\\s", ""); //x1
            coefs[1] = matcher.group(2).replaceAll("\\s", ""); //x2
            coefs[2] = matcher.group(3).replaceAll("\\s", ""); //restrain
            coefs[3] = matcher.group(4).replaceAll("\\s", ""); //right side
        }
        LinearLine.RESTRAIN restrain = null;
        if (coefs[2].equalsIgnoreCase("<=")){
            restrain = LinearLine.RESTRAIN.LOWER;
        } else if (coefs[2].equalsIgnoreCase(">=")){
            restrain = LinearLine.RESTRAIN.GREATER;
        } else {
            restrain = LinearLine.RESTRAIN.EQUAL;
        }
        result = new LinearLine(Integer.parseInt(coefs[0]),Integer.parseInt(coefs[1]),Integer.parseInt(coefs[3]), restrain);
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
     * When it has both null point in positive part of axis it connects them
     * When it goes through null of axis it counts the second point at the end of the graph
     * When it has exactly one null point in positive part of axis it counts the second point
     * at the end of the graph
     * When both null points are in negative parts the line is only in the inception
     * When coeficients x or y are 0 it draws a straight line in the graph
     * While counting it figures out whether it ends on the right side or on the upper side of the graph
     * @param linearLine object representation of a contrain
     * @param line javafx Line
     */
    private void drawLine (LinearLine linearLine, Line line) {
        if (linearLine.getNullX().getX() > 0 && linearLine.getNullY().getY() > 0){
            Point x0 = linearLine.getNullX();
            line.setEndX(x0.getX() * zoom);
            line.setEndY(x0.getY() * (-zoom));
            Point y0 = linearLine.getNullY();
            line.setStartX(y0.getX() * zoom);
            line.setStartY(y0.getY() * (-zoom));
        }
        if (linearLine.getNullX().getX() < 0 && linearLine.getNullY().getY() > 0){
            line.setStartX(linearLine.getNullY().getX() * zoom);
            line.setStartY(linearLine.getNullY().getY() * (-zoom));
            double y = (((450d/zoom)*linearLine.getCoefX1())-linearLine.getRightSide())/(-linearLine.getCoefX2());
            System.out.println(y);
            if (y <= (450d/zoom)){
                line.setEndX(450d);
                line.setEndY(y * (-zoom));
            } else {
                double x = (((450d/zoom)*linearLine.getCoefX2())-linearLine.getRightSide())/(-linearLine.getCoefX1());
                line.setEndX(x * zoom);
                line.setEndY(-450d);
            }
        }
        if (linearLine.getNullX().getX() > 0 && linearLine.getNullY().getY() < 0){
            line.setStartX(linearLine.getNullX().getX() * zoom);
            line.setStartY(linearLine.getNullX().getY() * (-zoom));
            double y = (((450d/zoom)*linearLine.getCoefX1())-linearLine.getRightSide())/(-linearLine.getCoefX2());
            System.out.println(y);
            if (y <= (450d/zoom)){
                line.setEndX(450d);
                line.setEndY(y * (-zoom));
            } else {
                double x = (((450d/zoom)*linearLine.getCoefX2())-linearLine.getRightSide())/(-linearLine.getCoefX1());
                line.setEndX(x * zoom);
                line.setEndY(-450d);
            }
        }
        if (linearLine.getNullX().getX() == 0 && linearLine.getNullY().getY() == 0){
            line.setStartX(0d);
            line.setStartY(0d);
            double y = (((450d/zoom)*linearLine.getCoefX1())-linearLine.getRightSide())/(-linearLine.getCoefX2());
            if (y <= (450d/zoom)){
                line.setEndX(450d);
                line.setEndY(y * (-zoom));
            } else {
                double x = (((450d/zoom)*linearLine.getCoefX2())-linearLine.getRightSide())/linearLine.getCoefX1();
                line.setEndX(x * zoom);
                line.setEndY(-450d);
            }
        }
        if (linearLine.getNullX().getX() < 0 && linearLine.getNullY().getY() < 0){
            line.setStartX(0d);
            line.setStartY(0d);
            line.setEndX(0d);
            line.setEndY(0d);
        }
        // x is zero
        if (linearLine.getNullX().getX() == Double.POSITIVE_INFINITY && linearLine.getNullY().getY() > 0){
            line.setStartX(0d);
            line.setStartY(linearLine.getNullY().getY() * (-zoom));
            line.setEndX(450d);
            line.setEndY(linearLine.getNullY().getY() * (-zoom));
        }
        if (linearLine.getNullX().getX() == Double.POSITIVE_INFINITY && linearLine.getNullY().getY() < 0){
            line.setStartX(0d);
            line.setStartY(0d);
            line.setEndX(0d);
            line.setEndY(0d);
        }
        // y is zero
        if (linearLine.getNullY().getY() == Double.POSITIVE_INFINITY && linearLine.getNullX().getX() > 0){
            line.setStartY(0d);
            line.setStartX(linearLine.getNullX().getX() * zoom);
            line.setEndY(-450d);
            line.setEndX(linearLine.getNullX().getX() * zoom);
        }
        if (linearLine.getNullY().getY() == Double.POSITIVE_INFINITY && linearLine.getNullX().getX() < 0){
            line.setStartX(0d);
            line.setStartY(0d);
            line.setEndX(0d);
            line.setEndY(0d);
        }

    }
    /**
     * Method counts the right side of the PurposeFunction
     * Method sets the ends of a javafx line -> draws the purpose line in the graph
     * @param purposeLine object representation of a purpose line
     * @param line javafx Line
     * @param solution optimal solution Point
     */
    private void drawPurposeLine (PurposeLine purposeLine, Line line, Point solution) {
        double valueOfPurpose = ((purposeLine.getCoefX1() * solution.getX()) + (purposeLine.getCoefX2() * solution.getY()));
        LinearLine ll = new LinearLine(purposeLine.getCoefX1(), purposeLine.getCoefX2(), valueOfPurpose, LinearLine.RESTRAIN.EQUAL);
        drawLine(ll,line);
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
                points[2*i] = possiblePoints.get(i).getX() * zoom;
                points[2*i+1] = possiblePoints.get(i).getY() * (-zoom);
            }

            polygon.getPoints().clear();
            polygon.getPoints().addAll(points);
            polygon.setSmooth(true);
        }
    }

    /**
     * Method justify the order of points to draw the polygon the right way
     * It finds centroid and then sort points
     * @param edges Points that are egdes of polygon
     * @return justified edges
     */
    private ArrayList<Point> satisfyPoints(ArrayList<Point> edges){
        //Find Centroid of polygon
        Point centroid = findCentroid(edges);
        //Sort edges
        sortPointsClockwise(edges, centroid);
        return edges;
    }

    /**
     * Method goes through edges and count the aritmetical average of all edges of both coordinates
     * @param edges Points that are egdes of polygon
     * @return Point of centroid
     */
    private Point findCentroid(ArrayList<Point> edges){
        double x = 0;
        double y = 0;
        for (int i = 0; i < edges.size(); i++){
            x += edges.get(i).getX();
            y += edges.get(i).getY();
        }
        return new Point(x/edges.size(), y/edges.size());
    }

    /**
     * Method goes through edges and sorts them in clockwise order
     * @param edges Points that are egdes of polygon
     * @param center Centroid of the polygon
     * @return
     */
    private ArrayList<Point> sortPointsClockwise(ArrayList<Point> edges, Point center) {
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < edges.size() - 1; i++) {
                if (comparePoint(edges.get(i+1), edges.get(i), center)) {
                    switchElements(i, i+1, edges);
                    changed = true;
                }
            }
        } while (changed);

        return edges;
    }

    // http://stackoverflow.com/questions/6989100/sort-points-in-clockwise-order

    /**
     * Method compares two points and counts if they need to be switched
     * It does that with confronting point with centroid
     * @param a Point a
     * @param b Point b
     * @param center Centroid
     * @return true when they need to be switched
     * @return false when they stay as they are
     */
    private static boolean comparePoint(Point a, Point b, Point center) {

        if (a.getX() - center.getX() >= 0 && b.getX() - center.getX() < 0) {
            return true;
        }
        if (a.getX() - center.getX() < 0 && b.getX() - center.getX() >= 0) {
            return false;
        }
        if (a.getX() - center.getX() == 0 && b.getX() - center.getX() == 0) {
            if (a.getY() - center.getY() >= 0 || b.getY() - center.getY() >= 0) {
                return a.getY() > b.getY();
            }
            return b.getY() > a.getY();
        }

        // compute the cross product of vectors (center -> a) x (center -> b)
        double det = (a.getX() - center.getX()) * (b.getY() - center.getY()) -
                (b.getX() - center.getX()) * (a.getY() - center.getY());
        if (det < 0) {
            return true;
        }
        if (det > 0) {
            return false;
        }

        // points a and b are on the same line from the center
        // check which point is closer to the center
        double d1 = (a.getX() - center.getX()) * (a.getX() - center.getX()) +
                (a.getY() - center.getY()) * (a.getY() - center.getY());
        double d2 = (b.getX() - center.getX()) * (b.getX() - center.getX()) +
                (b.getY() - center.getY()) * (b.getY() - center.getY());
        return d1 > d2;
    }

    /**
     * Method switches two elements in array
     * @param position1
     * @param position2
     * @param points array list of points
     * @return array list with switched points
     */
    private ArrayList<Point> switchElements(int position1, int position2, ArrayList<Point> points){
        Point temp = points.get(position2);
        points.set(position2, points.get(position1));
        points.set(position1, temp);
        return points;
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

    /**
     * Method sets labes with particular scale numbers
     */
    private void setLabels(){
        double first = (100/zoom);
        double second = (200/zoom);
        double third = (300/zoom);
        double fourth = (400/zoom);
        labelx1.setText(String.valueOf(first));
        labely1.setText(String.valueOf(first));
        labelx2.setText(String.valueOf(second));
        labely2.setText(String.valueOf(second));
        labelx3.setText(String.valueOf(third));
        labely3.setText(String.valueOf(third));
        labelx4.setText(String.valueOf(fourth));
        labely4.setText(String.valueOf(fourth));
    }
}