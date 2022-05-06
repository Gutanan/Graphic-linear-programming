package com.example.bp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class is Javafx Controller for LinearProgrammingAplication
 */
public class LinearProgrammingControler {

    private final String lineMiner = "([+-]? ?\\d*.?\\d+) ?[xX]1? ?([+-]? ?\\d*.?\\d+) ?[xXyY]2? ?([<>]?=) ?([+-]? ?\\d*.?\\d+)";
    private final String purposeMiner = "(max|MAX|min|MIN) ?= ?([+-]? ?\\d*.?\\d+)[xX]1? ?([+-]? ?\\d*.?\\d+)[xXyY]2?";
    private double zoom = 50d;
    private boolean doAdjustZoom = true;
    private int numOfConstrains = 2;

    /**
     * Method is run by button "Vykresli"
     * It parses the text from the input into program representation then calls to show results
     */
    @FXML
    protected void count() {
        doAdjustZoom = true;
        clearNotNeeded();
        drawAndCount();
    }

    /**
     * method deletes all unnecessary elements
     */
    private void clearNotNeeded() {
        lineUnbounded1.setVisible(false);
        lineUnbounded2.setVisible(false);
        lineUnbounded3.setVisible(false);
        twoPossibleLine.setVisible(false);
        optimalCircleSecond.setVisible(false);
        infiniteOptimalLine.setVisible(false);
    }

    /**
     * Main method of the user interface - it counts and draws
     */
    private void drawAndCount(){
        output.clear();
        //erase basic points
        for (int i = graph.getChildren().size() -1; i > 41; i--){
            graph.getChildren().remove(i);
        }
        checkboxBasicSolutions.setSelected(false);
        //start new task
        LinesArray lines = new LinesArray();
        for (int i = 2; i < numOfConstrains+2; i++){
            TextField tf = (TextField) constrains.getChildren().get(i-1);
            String lineText = tf.getText();
            LinearLine ln;
            try {
                ln = mineLine(lineText, lineMiner);
            } catch (Exception e){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Špatné zadání omezení");
                alert.showAndWait();
                return;
            }
            lines.addLine(ln);
        }

        //if there aro no possible solutions
        if (lines.findPossibleSolutions().size() == 0){
            optimalLabel.setText("Neexistuje přípustné řešení");
            purposePriceLabel.setText("Neexistuje přípustné řešení");
            optimalCircle.setVisible(false);
            possibleSolutionsPolygon.setVisible(false);
            purpLine.setVisible(false);
            return;
        }
        //adjust zoom when needed
        if (doAdjustZoom){
            adjustZoom(lines);
            doAdjustZoom = false;
        }
        //count basic points
        drawBasicSolutions(lines);
        //set labels on axis
        setLabels();
        //draw lines
        for (int i = 0; i < lines.getLines().size(); i++){
            Line line = (Line) graph.getChildren().get(i+2);
            drawLine(lines.getLines().get(i), line);
            line.setVisible(true);
        }

        String purposeText = purposeLine.getText();
        PurposeLine purpose;
        try {
            purpose = minePurposeLine(purposeText, purposeMiner);
        } catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Špatné zadání účelové funkce");
            alert.showAndWait();
            return;
        }

        drawShapes(lines);
        //Simplex method
        lines.doRightPositive();
        int numOfAuxiliary = lines.getNumOfAuxiliary();
        int numOfAdditional = lines.getNumOfAdditional();
        int twoPhase = 0;
        boolean isTwoPhase = false;
        if (numOfAuxiliary > 0){
            twoPhase = 1;
            isTwoPhase = true;
        }

        SimplexMethod simplex;
        if (purpose.getPurpose().equals(PurposeLine.PURPOSE.MAX)){
            simplex = new SimplexMethod(lines.getLines().size()+twoPhase,2+numOfAdditional+numOfAuxiliary, SimplexMethod.PURPOSE.MAX, isTwoPhase);
        } else {
            simplex = new SimplexMethod(lines.getLines().size()+twoPhase,2+numOfAdditional+numOfAuxiliary, SimplexMethod.PURPOSE.MIN, isTwoPhase);
        }


        double[][] standartMatrix = lines.createMatrix();
        double[] purpLine = purpose.createVector(3+numOfAdditional+numOfAuxiliary);
        simplex.fillTable(lines.addPurposeVector(standartMatrix,purpLine));

        simplex.iterateTable();
        if (isTwoPhase){
            simplex.generateNewTable(numOfAdditional,numOfAuxiliary);
            simplex.iterateTable();
        }
        output.appendText("Simplexová tabulka: \n");
        output.appendText(simplex.printString());
        optimalLabel.setText(simplex.returnSolutionVector());
        purposePriceLabel.setText(simplex.returnSolutionPrice());
        drawPurposeLine(purpose, this.purpLine, simplex.returnOptimalPoint());

        //parse optimal solution coordinates
        Point optimalSolPoint = simplex.returnOptimalPoint();
        drawOptimalCircle(optimalSolPoint, optimalCircle);
        setSolutionLabel(optimalSolPoint);

        if (simplex.getResultError().equals(SimplexMethod.ERROR.UNBOUNDED)){
            lineUnbounded1.setVisible(true);
            lineUnbounded2.setVisible(true);
            lineUnbounded3.setVisible(true);
            this.purpLine.setVisible(false);
            optimalCircle.setVisible(false);
            optimalSolutionPointLabel.setVisible(false);
            infiniteOptimalPointLabel.setVisible(false);
        }

        if (simplex.getResultError().equals(SimplexMethod.ERROR.OPTIMAL_INFINITE)){
            simplex.iterateToFindSecondOptimal();
            Point optimalSolPointSecond = simplex.returnOptimalPoint();
            drawOptimalCircle(optimalSolPointSecond, optimalCircleSecond);
            setSolutionLabelSecond(optimalSolPointSecond);

            optimalCircleSecond.setVisible(true);
            infiniteOptimalLine.setStartX(optimalSolPoint.getX() * zoom);
            infiniteOptimalLine.setStartY(optimalSolPoint.getY() * (-zoom));
            infiniteOptimalLine.setEndX(optimalSolPointSecond.getX() * zoom);
            infiniteOptimalLine.setEndY(optimalSolPointSecond.getY() * (-zoom));
            infiniteOptimalLine.setVisible(true);
        }

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
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Maximální počet omezení je 10");
            alert.showAndWait();
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
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Minimální počet omezení je 1");
            alert.showAndWait();
        }
    }

    /**
     * Method is controlled by checkbox. If is checked it shows the basic solutions
     * labels. If is unchecked it makes the invisible
     */
    @FXML
    protected void showBasicSolutions(){
        if (graph.getChildren().size() <= 42){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Není vypočítané žádné řešení");
            alert.showAndWait();
            return;
        }
        if (checkboxBasicSolutions.isSelected()) {
            for (int i = graph.getChildren().size() - 1; i > 41; i--) {
                graph.getChildren().get(i).setVisible(true);
            }
        } else {
            for (int i = graph.getChildren().size() - 1; i > 41; i--) {
                graph.getChildren().get(i).setVisible(false);
            }
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
        infiniteOptimalPointLabel.setVisible(false);
        optimalSolutionPointLabel.setVisible(false);
        output.clear();
        optimalLabel.setText("");
        purposePriceLabel.setText("");
        clearNotNeeded();

        for (int i = 2; i < numOfConstrains+2; i++){
            TextField tf = (TextField) constrains.getChildren().get(i-1);
            tf.setText("");
            Line line = (Line) graph.getChildren().get(i);
            line.setVisible(false);
        }

        for (int i = graph.getChildren().size() -1; i > 41; i--){
            graph.getChildren().remove(i);
        }
    }

    /**
     * Method is run by button "Nápověda" in menubar
     */
    @FXML
    protected void showUserGuide() throws IOException {
        Stage newStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(LinearProgrammingControler.class.getResource("help.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 700);
        newStage.setTitle("Linear Programming");
        newStage.setScene(scene);
        newStage.show();
    }

    @FXML
    protected void zoomPlus(){
        zoom = zoom * 2;
        drawAndCount();
    }

    @FXML
    protected void zoomMinus(){
        zoom = zoom / 2;
        drawAndCount();
    }

    @FXML
    protected void showLabel() {
        optimalSolutionPointLabel.setVisible(true);
    }

    @FXML
    protected void removeLabel() {
        optimalSolutionPointLabel.setVisible(false);
    }

    @FXML
    protected void showLabelSecond() {
        infiniteOptimalPointLabel.setVisible(true);
    }

    @FXML
    protected void removeLabelSecond() {
        infiniteOptimalPointLabel.setVisible(false);
    }

    @FXML
    private TextField purposeLine;

    @FXML
    private CheckBox checkboxBasicSolutions;

    @FXML
    private Line purpLine;

    @FXML
    private Circle optimalCircle;

    @FXML
    private Label optimalSolutionPointLabel;

    @FXML
    private Circle optimalCircleSecond;

    @FXML
    private Label infiniteOptimalPointLabel;

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

    @FXML
    private Label optimalLabel;

    @FXML
    private Label purposePriceLabel;

    @FXML
    private Line lineUnbounded1;

    @FXML
    private Line lineUnbounded2;

    @FXML
    private Line lineUnbounded3;

    @FXML
    private Line twoPossibleLine;

    @FXML
    private Line infiniteOptimalLine;

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
        result = new LinearLine(Double.parseDouble(coefs[0]),Double.parseDouble(coefs[1]),Double.parseDouble(coefs[3]), restrain);
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
            result[0] = matcher.group(1).replaceAll("\\s", ""); //purpose
            result[1] = matcher.group(2).replaceAll("\\s", ""); //x1
            result[2] = matcher.group(3).replaceAll("\\s", ""); //x2
        }
        if (result[0].equalsIgnoreCase("max")){
            purposeLine = new PurposeLine(PurposeLine.PURPOSE.MAX, Double.parseDouble(result[1]), Double.parseDouble(result[2]));
        } else {
            purposeLine = new PurposeLine(PurposeLine.PURPOSE.MIN, Double.parseDouble(result[1]), Double.parseDouble(result[2]));
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

            double xNull = x0.getX() * zoom;
            double yNull = y0.getY() * zoom;
            if (xNull >= 450d){
                line.setEndX(450d);
                double y = (((450d/zoom)*linearLine.getCoefX1())-linearLine.getRightSide())/(-linearLine.getCoefX2());
                line.setEndY(y * (-zoom));
            }
            if (yNull >= 450d){
                double x = (((450d/zoom)*linearLine.getCoefX2())-linearLine.getRightSide())/(-linearLine.getCoefX1());
                line.setStartX(x * zoom);
                line.setStartY(-450d);
            }

        }
        if (linearLine.getNullX().getX() < 0 && linearLine.getNullY().getY() > 0){
            line.setStartX(linearLine.getNullY().getX() * zoom);
            line.setStartY(linearLine.getNullY().getY() * (-zoom));
            double y = (((450d/zoom)*linearLine.getCoefX1())-linearLine.getRightSide())/(-linearLine.getCoefX2());
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
                if (y > 0){
                    line.setEndX(450d);
                    line.setEndY(y * (-zoom));
                } else {
                    line.setEndX(0d);
                    line.setEndY(0d);
                }
            } else {
                double x = (((450d/zoom)*linearLine.getCoefX2())-linearLine.getRightSide())/(-linearLine.getCoefX1());
                if (x > 0){
                    line.setEndX(x * zoom);
                    line.setEndY(-450d);
                } else {
                    line.setEndX(0d);
                    line.setEndY(0d);
                }
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
     * Method sets the labels and circles of basic solutions into the graph
     * @param lines lines
     */
    private void drawBasicSolutions(LinesArray lines) {
        ArrayList<Point> basicSolutions = lines.findBasicSolutions();
        for (Point i : basicSolutions){
            Label label = new Label(i.toString());
            label.setLayoutX(i.getX() * zoom + 10 + 30);
            label.setLayoutY(i.getY() * (-zoom) -20 + 470);
            label.setVisible(false);
            Circle circle = new Circle();
            circle.setRadius(3d);
            circle.setLayoutX(30);
            circle.setLayoutY(470d);
            circle.setCenterX(i.getX() * zoom);
            circle.setCenterY(i.getY() * (-zoom));
            circle.setVisible(false);
            graph.getChildren().add(label);
            graph.getChildren().add(circle);

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
     * Method sets the coordinates into label behind the optimal solution
     * @param solution point solution
     */
    private void setSolutionLabel(Point solution){
        optimalSolutionPointLabel.setLayoutX(solution.getX() * zoom + 10 + 30);
        optimalSolutionPointLabel.setLayoutY(solution.getY() * (-zoom) -20 + 470);
        optimalSolutionPointLabel.setText(solution.toString());
    }

    /**
     * Method sets the coordinates into label behind the second basic optimal solution
     * @param solution point solution
     */
    private void setSolutionLabelSecond(Point solution){
        infiniteOptimalPointLabel.setLayoutX(solution.getX() * zoom + 10 + 30);
        infiniteOptimalPointLabel.setLayoutY(solution.getY() * (-zoom) -20 + 470);
        infiniteOptimalPointLabel.setText(solution.toString());

    }

    /**
     * First the method adds lines of the graph border in case of unbounded solution or zoom
     * Method takes the Possible solutions and draws the polygon with them in the graph
     * @param drawingPolygon ary lines of the constrains
     * @param polygon javafx polygon
     */
    private void drawPolygon(LinesArray drawingPolygon, Polygon polygon) {
        LinesArray lines = drawingPolygon;
        lines.addLine(new LinearLine(0,1,450/zoom, LinearLine.RESTRAIN.LOWER));
        lines.addLine(new LinearLine(1,0,450/zoom, LinearLine.RESTRAIN.LOWER));
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
        if (numberOfPoints == 0){
            polygon.setVisible(false);
        }
        if (numberOfPoints == 2){
            polygon.setVisible(false);
            twoPossibleLine.setStartX(possiblePoints.get(0).getX() * zoom);
            twoPossibleLine.setStartY(possiblePoints.get(0).getY() * (-zoom));
            twoPossibleLine.setEndX(possiblePoints.get(1).getX() * zoom);
            twoPossibleLine.setEndY(possiblePoints.get(1).getY() * (-zoom));
            twoPossibleLine.setVisible(true);
        }
        lines.getLines().remove(lines.getLines().size()-1);
        lines.getLines().remove(lines.getLines().size()-1);
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
     * Method goes through edges and count the arithmetical average of all edges of both coordinates
     * @param edges Points that are egdes of polygon
     * @return Point of centroid
     */
    private Point findCentroid(ArrayList<Point> edges){
        double x = 0;
        double y = 0;
        for (Point edge : edges) {
            x += edge.getX();
            y += edge.getY();
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
     * @return true when they need to be switched & false when they stay as they are
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
     * @param position1 position
     * @param position2 position
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
     * @param lines constrains
     */
    private void drawShapes(LinesArray lines){
        purpLine.setVisible(true);
        optimalCircle.setVisible(true);
        possibleSolutionsPolygon.setVisible(true);

        drawPolygon(lines, possibleSolutionsPolygon);
    }

    /**
     * Method takes the possible solutions and finds the one with max x and max y
     * The higher number is the base for zoom recalculation
     * @param lines lines
     */
    private void adjustZoom(LinesArray lines) {
        ArrayList<Point> possiblePoints = lines.findPossibleSolutions();
        double zoomRound;
        double maxX = findMaxX(possiblePoints);
        double maxY = findMaxY(possiblePoints);
        if (maxX > maxY){
            zoomRound = 450/(maxX + maxX/10);
        } else {
            zoomRound = 450/(maxY + maxY/10);
        }
        if (zoomRound == Double.POSITIVE_INFINITY){
            zoomRound = 20d;
        }
        BigDecimal bd = new BigDecimal(zoomRound);
        bd = bd.round(new MathContext(1));
        zoom = justifyZoom(bd.doubleValue());
    }

    /**
     * Method takes the zoom number and tries to change it that it can set labels correctly
     * @param doubleValue value
     * @return justified value
     */
    private double justifyZoom(double doubleValue) {
        double result = 20d;
        if (doubleValue >= 50) {
            result = 50d;
        }
        boolean repeat = true;
        if (doubleValue > result){
            while (repeat){
                result *= 2;
                if (doubleValue <= result){
                    repeat = false;
                }
            }
            result /= 2;
        }
        if (doubleValue < result){
            while (repeat){
                result /= 2;
                if (doubleValue >= result){
                    repeat = false;
                }
            }
        }
        return result;
    }

    /**
     * @param possiblePoints possible points
     * @return value the has the maximum X
     */
    private double findMaxX(ArrayList<Point> possiblePoints) {
        double maxValue = 0;
        for (Point possiblePoint : possiblePoints) {
            if (possiblePoint.getX() > maxValue) {
                maxValue = possiblePoint.getX();
            }
        }
        return maxValue;
    }

    /**
     * @param possiblePoints possible points
     * @return value of a maximum Y
     */
    private double findMaxY(ArrayList<Point> possiblePoints) {
        double maxValue = 0;
        for (Point possiblePoint : possiblePoints) {
            if (possiblePoint.getY() > maxValue) {
                maxValue = possiblePoint.getY();
            }
        }
        return maxValue;
    }

    /**
     * Method sets labels with particular scale numbers
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