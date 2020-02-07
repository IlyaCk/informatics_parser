import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

public class TableFromSavedResults extends TableAbstract {

    List<String> savedTableAsText = new ArrayList<>();

    public TableFromSavedResults(ArgumentsParser argumentsParser) throws IOException, IllegalArgumentException {
        this.argumentsParser = argumentsParser;
        System.out.print("Trying to read table from local file ``" + argumentsParser.getSavedResultsFileName() + "''... ");
        String savedFileName = argumentsParser.getSavedResultsFileName();

        File file = new File(savedFileName);
        if(!file.canRead()) {
            System.err.println("FAIL:  cannot read " + savedFileName);
        } else {
            long currTime = System.currentTimeMillis();
            long modifiedTime = file.lastModified();
            if(currTime - modifiedTime > 30*60*1000) { // 30 min = 1/2 hour
                Date fileModified = new Date(modifiedTime);
                System.err.println("\nWARNING!!! The file was modified at " + fileModified.toString() + " and it's too long (" + ((currTime - modifiedTime)/3.6e6) + " hours) ago.");
                System.err.println("\tIf you still want to use that old file, type \"YeS!!!\" (without quotes, case-sensitive)");
                Scanner sc = new Scanner(System.in);
                String confirm = sc.nextLine();
                if (!("YeS!!!".equals(confirm))) {
                    throw new IllegalArgumentException("table resultst file too old");
                }
            }
        }

        Scanner in = new Scanner(new FileInputStream(file), "Cp1251");

        String[] problems = in.nextLine().split("\\t+");
        while(in.hasNextLine()) {
            String s = in.nextLine();
            if(!s.isEmpty()) {
                savedTableAsText.add(s);
            }
        }
        System.out.println("ok");

        this.problemsNumber = problems.length - 3;
        if(argumentsParser.getTotalProblems()!=-1 && argumentsParser.getTotalProblems()!=problemsNumber)
            throw new IllegalArgumentException("Total Number of problems is given, but incorrect");
        if(argumentsParser.getTotalProblems() == -1)
            argumentsParser.setTotalProblems(problemsNumber);

        problemNames = new String[problemsNumber];
        for (int i = 3; i < problems.length; i++) {
            problemNames[i - 3] = problems[i];
        }

        System.out.println(problemNames);

        studentsNumber = calcStudentsNumber();
        studentNames = new String[studentsNumber];
        solvedProblemsNum = new int[studentsNumber];
        table = new String[studentsNumber][problemsNumber];

        studentsWhoSolvedNum = new int[problemsNumber];
        char firstChar;
        for (int i = 0; i < studentsNumber; i++) {
            String[] currStudTableLine = savedTableAsText.get(i).split("\\t");
            studentNames[i] = currStudTableLine[1].replaceAll("[\\sі]", "_");
            try {
                solvedProblemsNum[i] = Integer.valueOf(currStudTableLine[2]);
            } catch (NumberFormatException e) {
                System.err.println("Failed to get quantity of solved problems for student " + studentNames[i] + " (" + (i+1) + ")");
                System.err.println(e.getMessage());
                solvedProblemsNum[i] = -1;
            }
            int solvedProblemsNumCountedByPluses = 0;
            for (int j = 0; j < problemsNumber; j++) {
                firstChar = (j+3 >= currStudTableLine.length || currStudTableLine[j + 3].isEmpty()) ? '-' : currStudTableLine[j + 3].charAt(0);
                if (firstChar == '+') {
                    table[i][j] = "+";
                    solvedProblemsNumCountedByPluses++;
                    studentsWhoSolvedNum[j]++;
                } else {
                    table[i][j] = "-";
                }
            }
            if(solvedProblemsNum[i] != solvedProblemsNumCountedByPluses) {
                System.err.println("Inconsistent data for student " + studentNames[i] + " (" + (i+1) + ")");
                System.err.println("solvedProblemsNum (got from last column) is " + solvedProblemsNum[i] + ", but solvedProblemsNumCountedByPluses is " + solvedProblemsNumCountedByPluses);
            }
        }


        String url = "https://informatics.mccme.ru/mod/statements/view3.php?id=" + argumentsParser.getProblemsGroup();

//        WebDriver driver = new SilentHtmlUnitDriver(true);
        WebDriver driver = new SilentHtmlUnitDriver(false);
        driver.get(url);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.driver = driver;


        rnd = new SecureRandom();
    }

    private int calcStudentsNumber() {
        return savedTableAsText.size();
        /*
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
        */
    }

    public void printSourceTable() {
        for(String row : savedTableAsText) {
            for(String elem  : row.split("\\t+")) {
                System.out.println(elem + " ");
            }
            System.out.println();
        }
    }

    @Override
    protected Document getProblemPage(String problemName) {
        WebElement link = driver.findElement(By.partialLinkText(problemName));
        System.out.println("before <<link.click()>>");
        link.click();
        try {
            Thread.sleep(argumentsParser.getTimeout());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("after <<link.click()>>");
        Document page = Jsoup.parse(driver.getPageSource());
        return page;
    }

/*
    public void printTable() {
        printProblemNames();
        for (int i=0; i<table.length; i++) {
            for (String b : table[i]) {
                System.out.print(b + " ");
            }
            System.out.println("\t" + studentNames[i]);
        }
    }
*/


}