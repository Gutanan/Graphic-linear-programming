package com.example.bp;

/**
 * Method for testing purposes
 */
public class Testing {
    public static void main(String[] args) {
        //LinearLine line1 = new LinearLine(2,1,2, LinearLine.RESTRAIN.GREATER);
        LinearLine line2 = new LinearLine(4,2,8, LinearLine.RESTRAIN.LOWER);
        LinearLine line1 = new LinearLine(1,1,4, LinearLine.RESTRAIN.LOWER);
        LinearLine line3 = new LinearLine(1,3,6, LinearLine.RESTRAIN.LOWER);
        //LinearLine line3 = new LinearLine(4,3,12, LinearLine.RESTRAIN.EQUAL);
        PurposeLine purposeLine = new PurposeLine(PurposeLine.PURPOSE.MAX, 3, 5);
        System.out.println("Přímka 1:");
        System.out.println(line1.toString());
        System.out.println("Přímka 2:");
        System.out.println(line2.toString());
        //System.out.println(line3.toString());
        LinesArray lines = new LinesArray();
        lines.addLine(line1);
        lines.addLine(line2);
        lines.addLine(line3);

        System.out.println("Základní řešení");
        System.out.println(lines.findBasicSolutions());
        System.out.println("Přípustná základní řešení");
        System.out.println(lines.findPossibleSolutions());
        System.out.println("Optimální řešení");
        System.out.println(lines.findOptimalSolution(purposeLine));

        boolean quit = false;

        int numOfAuxiliary = lines.getNumOfAuxiliary();
        int numOfAdditional = lines.getNumOfAdditional();
        int twoPhase = 0;
        boolean isTwoPhase = false;
        if (numOfAuxiliary > 0){
            twoPhase = 1;
           isTwoPhase = true;
        }

        SimplexMethod simplex = new SimplexMethod(lines.getLines().size()+twoPhase,2+numOfAdditional+numOfAuxiliary, SimplexMethod.PURPOSE.MAX, isTwoPhase);

        double[][] standartMatrix = lines.createMatrix();
        double[] purpLine = purposeLine.createVector(3+numOfAdditional+numOfAuxiliary);
        simplex.fillTable(lines.addPurposeVector(standartMatrix,purpLine));

        System.out.println(simplex.printString());
        simplex.iterateTable();
        System.out.println(simplex.printString());



        /*Simplex simplex = new Simplex(2, 4);

        double[][] standartMatrix = lines.createStandartMatrix();
        double[] purpLine = purposeLine.createVector(lines.getLines().size());
        simplex.fillTable(lines.addPurposeVector(standartMatrix,purpLine));

        System.out.println("Start");
        simplex.print();

        // repeat until solution found or unbounded
        while(!quit){
            Simplex.ERROR error = simplex.compute();

            if(error == Simplex.ERROR.IS_OPTIMAL){
                System.out.println("Následující tabulka je optimální:");
                simplex.print();
                quit = true;
            }
            else if(error == Simplex.ERROR.UNBOUNDED){
                System.out.println("Neomezené řešení");
                quit = true;
            }
        }
        */
        /* from Controler
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
         */
    }
}
