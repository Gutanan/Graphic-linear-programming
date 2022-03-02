package com.example.bp;

/**
 * Method for testing purposes
 */
public class Testing {
    public static void main(String[] args) {
        LinearLine line1 = new LinearLine(1,1,4);
        LinearLine line2 = new LinearLine(1,3,6);
        //Line line3 = new Line(4,3,12);
        PurposeLine purposeLine = new PurposeLine(PurposeLine.PURPOSE.MAX, 3, 5);
        System.out.println("Přímka 1:");
        System.out.println(line1.toString());
        System.out.println("Přímka 2:");
        System.out.println(line2.toString());
        //System.out.println(line3.toString());
        LinesArray lines = new LinesArray();
        lines.addLine(line1);
        lines.addLine(line2);
        //lines.addLine(line3);

        System.out.println("Základní řešení");
        System.out.println(lines.findBasicSolutions());
        System.out.println("Optimální řešení");
        System.out.println(lines.findOptimalSolution(purposeLine));

        boolean quit = false;

        Simplex simplex = new Simplex(2, 4);

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
    }
}
