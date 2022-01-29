package com.example.bp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinearProgrammingControler {

    private final String lineMiner = "([+-]? ?\\d+) ?[xX]1? ?([+-]? ?\\d+) ?[xXyY]2? ?<= ?([+-]? ?\\d+)";
    private final String purposeMiner = "(max|MAX|min|MIN) ?= ?([+-]? ?\\d+)[xX]1? ?([+-]? ?\\d+)[xXyY]2?";
    @FXML
    protected void count() {
        output.setText("Output\n");
        String lineText1 = restrain1.getText();
        String lineText2 = restrain2.getText();
        String purposeText = purposeLine.getText();

        Line line1 = mineLine(lineText1, lineMiner);
        output.appendText(line1.toString());
        output.appendText("\n");

        Line line2 = mineLine(lineText2, lineMiner);
        output.appendText(line2.toString());
        output.appendText("\n");

        PurposeLine purpose = minePurposeLine(purposeText, purposeMiner);

        LinesArray lines = new LinesArray();
        lines.addLine(line1);
        lines.addLine(line2);

        output.appendText("Přípustná řešení: \n");
        output.appendText(lines.findPossibleSolutions().toString());
        output.appendText("\n");
        output.appendText("Optimální řešení: \n");
        output.appendText(lines.findOptimalSolution(purpose).toString());
        output.appendText("\n");

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
    private TextArea output;

    private Line mineLine(String line, String miner){
        Line result = null;
        Pattern pattern = Pattern.compile(miner);
        Matcher matcher = pattern.matcher(line);
        String[] coefs = new String[3];
        while (matcher.find()) {
            coefs[0] = matcher.group(1).replaceAll("\\s", "");
            coefs[1] = matcher.group(2).replaceAll("\\s", "");
            coefs[2] = matcher.group(3).replaceAll("\\s", "");
        }
        result = new Line(Integer.parseInt(coefs[0]),Integer.parseInt(coefs[1]),Integer.parseInt(coefs[2]));
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
}