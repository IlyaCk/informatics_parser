import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Table {

    public static final String TABLE_PATH = "/html/body/div/div[3]/table/tbody/tr[2]/td[2]/div/div/div/table/tbody/tr";

    private static final Pattern STUDENT_NUM_PATTERN = Pattern.compile("(\\d+)\\Z");

    private List<WebElement> sourceTable;
    private String[][] table;
    private String[] problemNames;
    private String[] studentNames;
    private int[] solvedProblemsNum;
    private int problemsNumber;
    private int studentsNumber;

    public int getStudentsNumber() {
        return studentsNumber;
    }

    public int[] getSolvedProblemsNum() {
        return solvedProblemsNum;
    }

    public Table(WebDriver driver, String url) {
        driver.get(url);
        sourceTable = driver.findElements(By.xpath(TABLE_PATH)); // get the table
        List<WebElement> problems = sourceTable.get(0).findElements(By.tagName("td")); // get problems line of table
        problemsNumber = problems.size() - 4; // get number of problems
        
        /* get problem names */
        problemNames = new String[problems.size() - 4];
        for (int i = 2; i < problems.size() - 2; i++) {
            problemNames[i - 2] = problems.get(i).getText();
        }
        sourceTable.remove(0); // remove problems line from table


        studentsNumber = calcStudentsNumber();
        studentNames = new String[studentsNumber];
        solvedProblemsNum = new int[studentsNumber];
        table = new String[studentsNumber][problemsNumber];

        WebElement a;
        List<WebElement> b;
        char firstChar;
        for (int i = 0; i < studentsNumber; i++) {
            a = sourceTable.get(i);
            b = a.findElements(By.tagName("td"));
            studentNames[i] = b.get(1).getText().replaceAll("[\\sі]", "_");
            try {
                solvedProblemsNum[i] = Integer.valueOf(b.get(problemsNumber + 2).getText());
            } catch (NumberFormatException e) {
                System.err.println("Failed to get quantity of solved problems for student " + studentNames[i] + " (" + (i+1) + ")");
                System.err.println(e.getMessage());
                solvedProblemsNum[i] = -1;
            }
            int solvedProblemsNumCountedByPluses = 0;
            for (int j = 0; j < problemsNumber; j++) {
                firstChar = b.get(j + 2).getText().charAt(0);
                if (firstChar == '+') {
                    table[i][j] = "+";
                    solvedProblemsNumCountedByPluses++;
                } else {
                    table[i][j] = "-";
                }
            }
            if(solvedProblemsNum[i] != solvedProblemsNumCountedByPluses) {
                System.err.println("Incosistent data for student " + studentNames[i] + " (" + (i+1) + ")");
                System.err.println("solvedProblemsNum (got from last column) is " + solvedProblemsNum[i] + ", but solvedProblemsNumCountedByPluses is " + solvedProblemsNumCountedByPluses);
            }
        }
    }

    public String[][] getTable() {
        return table;
    }

    private int calcStudentsNumber() {
        int num = 0;
        List<WebElement> line;
        String row;
        for (WebElement a : sourceTable) {
            line = a.findElements(By.tagName("td"));
            row = line.get(0).getText();
            Matcher m = STUDENT_NUM_PATTERN.matcher(row);
            if (m.find()) {
                String n = m.group(1);
                try {
                    num = Integer.parseInt(n);
                } catch (NumberFormatException e) {
                    break;
                }
            } else {
                break;
            }
        }
        return num;
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

    public String[] getProblemNames() {
        return problemNames;
    }

    public void printTable() {
        printProblemNames();
        for (String[] a : table) {
            for (String b : a) {
                System.out.print(b + " ");
            }
            System.out.println();
        }
    }

    public void printSourceTable() {
        for (WebElement a : sourceTable) {
            for (WebElement b : a.findElements(By.tagName("td"))) {
                System.out.print(b.getText() + " ");
            }
            System.out.println();
        }
    }

}