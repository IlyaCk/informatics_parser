import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Random;

/**
 * Created by ilya on 02.12.2015.
 */
public abstract class TableAbstract {
    protected WebDriver driver;

    protected String[][] table;
    protected String[] problemNames;
    protected String[] studentNames;
    protected int[] solvedProblemsNum;
    protected int[] studentsWhoSolvedNum;
    protected int problemsNumber;
    protected int studentsNumber;
    ArgumentsParser argumentsParser;
    protected Random rnd;

    public int getStudentsNumber() {
        return studentsNumber;
    }

    public int[] getSolvedProblemsNum() {
        return solvedProblemsNum;
    }

    public int getSolvedProblemsNum(int studIdx) {
        return solvedProblemsNum[studIdx];
    }

    public String[][] getTable() {
        return table;
    }

    public void printProblemNames() {
        for (String s : problemNames) {
            System.out.print(s + " ");
        }
        System.out.println();
    }

    public String[] getStudentNames() {
        return studentNames;
    }

    public String getStudentNames(int studIdx) {
        return studentNames[studIdx];
    }

    public String[] getProblemNames() {
        return problemNames;
    }

    public String getProblemNames(int probIdx) {
        return problemNames[probIdx];
    }

    public void printTable() {
        printProblemNames();
        for (int i=0; i<table.length; i++) {
            for (String b : table[i]) {
                System.out.print(b + "\t");
            }
            System.out.println(studentNames[i]);
        }

        String tableFilename = "results_" + argumentsParser.getBlockSubDir() + "_" + Parser.getCurrTimeStr() +".html";
        try {
            boolean totalProblemsNumberIsKnown = argumentsParser.getTotalProblems() != -1;
            PrintWriter out = new PrintWriter(new File(tableFilename), "cp1251");
            out.println("<html>\n\t<table>\n\t\t<tr>\n\t\t\t<td>\n\t\t\t</td>\n" +
                    (totalProblemsNumberIsKnown ?
                            "\t\t\t<td>\n\t\t\t\tif <i>proved</i>\n\t\t\t</td>" +
                                    "\t\t\t<td>\n\t\t\t\tif <i>not</i> proved\n\t\t\t</td>\n" : "")
                    + "\t\t\t<td>\n\t\t\t\tproblems solved\n\t\t\t</td>\n");
            for (String s : problemNames) {
                out.println("\t\t\t<td style=\"min-width:20px\">\n\t\t\t\t" + s + "\n\t\t\t</td>");
            }
            out.println("\t\t</tr>");
            for (int i=0; i<table.length; i++) {
                out.println("\t\t<tr>");
                out.println("\t\t\t<td>\n\t\t\t\t" + studentNames[i] + "\n\t\t\t</td>");
                if(totalProblemsNumberIsKnown) {
                    double pointsForProved = getPointsForNumberOfSolvedProblemsIfProved(solvedProblemsNum[i]);
                    double pointsForNotProved = getPointsForNumberOfSolvedProblemsIfNotProved(solvedProblemsNum[i]);
                    out.print("\t\t\t<td>\n\t\t\t\t(");
                    if(doProposeDefence(solvedProblemsNum[i])) {
                        out.print(pointsForProved);
                    } else {
                        out.print("no");
                    }
                    out.println(")\n\t\t\t</td>");
                    out.print("\t\t\t<td>\n\t\t\t\t(");
                    if(pointsForNotProved > 0.999999) {
                        out.print(pointsForNotProved);
                    } else {
                        out.print("no");
                    }
                    out.println(")\n\t\t\t</td>");
                }
                out.println("\t\t\t<td>\n\t\t\t\t" + solvedProblemsNum[i] + "\n\t\t\t</td>");
                for (String b : table[i]) {
                    out.print("\t\t\t<td>\n\t\t\t\t");
                    switch(b) {
                        case "+":
                            out.print("+");
                            break;
                        case "-":
                            out.print("&ndash;");
                            break;
                        default:
                            throw new IllegalArgumentException("should be either '+' or '-'");
                    }
                    out.println("\n\t\t\t</td>");
                }
                out.println("\t\t</tr>");
            }
            out.println("\t</table>\n</html>\n");
            out.close();
            System.out.println("Better-formed table had been put to file ``" + tableFilename + "''");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            System.out.println("Tried to put better-formed table to file ``" + tableFilename + "'', but failed");
            System.out.println(e.getMessage());
        }
    }

    protected abstract Document getProblemPage(String problemName);

    public final String genRandomSolvedProblemName(int studIdx) {
        double[] rangeForTheProblem = new double[problemsNumber+1];
        rangeForTheProblem[0] = 0.0;
        for(int j=0; j<problemsNumber; j++) {
            rangeForTheProblem[j+1] = rangeForTheProblem[j];
            if("+".equals(table[studIdx][j])) {
                rangeForTheProblem[j+1] += 1.0 / (studentsWhoSolvedNum[j] - 0.5);
            } // To enlarge probability that unique-for-the-student problems wiil be assigned him/her
        }
        double rndValue;
        int idx;
        do {
            rndValue = rnd.nextDouble() * rangeForTheProblem[problemsNumber];
            idx = 0;
            while(rndValue > rangeForTheProblem[idx+1] + 1e-9) {
                idx++;
            }
        } while(!"+".equals(table[studIdx][idx]));
        table[studIdx][idx] = "!";
        studentsWhoSolvedNum[idx] += studentsWhoSolvedNum[idx]/2; // to reduce probability that the same problem will be assigned to smbd else
        studentsWhoSolvedNum[idx] ++; // the same goal
        String cs = Charset.defaultCharset().name();
        String fe = System.getProperty("file.encoding");
        // return new String("Задача ", Charset.forName("utf-8")) + this.getProblemNames(idx);
        return "Задача " + this.getProblemNames(idx);
    }
/*
    public Double getPointsForNumberOfSolvedProblems(int solved) {
        return Math.floor(10.0*argumentsParser.getTotalPoints()*Math.pow((1.0*solved)/argumentsParser.getTotalProblems(), 1.5))/10.0;
    }
*/
    /*
    public Double getPointsForNumberOfSolvedProblemsIfProved(int solved) {
        return Math.floor(20.0*argumentsParser.getTotalPoints()*Math.pow((1.0*solved)/argumentsParser.getTotalProblems(), 1.5))/10.0;
    }
    */
    public Double getPointsForNumberOfSolvedProblemsIfProved(int solved) {
        return Math.floor(10.0*argumentsParser.getTotalPoints()*1.000000001*Math.pow((1.0*solved)/argumentsParser.getTotalProblems(), 1.5))/10.0;
    }

    boolean doProposeDefence(int solved) {
        return getPointsForNumberOfSolvedProblemsIfProved(solved) >= 1.999999999 + getPointsForNumberOfSolvedProblemsIfNotProved(solved);
    }

    /*
    public Double getPointsForNumberOfSolvedProblemsIfNotProved(int solved) {
        return Math.round((6.0*argumentsParser.getTotalPoints()*solved)/argumentsParser.getTotalProblems())/10.0;
    }
    */
    public Double getPointsForNumberOfSolvedProblemsIfNotProved(int solved) {
        return Math.round((2.5*argumentsParser.getTotalPoints()*solved)/argumentsParser.getTotalProblems())/10.0;
    }

}
