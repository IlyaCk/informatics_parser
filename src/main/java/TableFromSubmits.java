import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.*;

/**
 * Created by ilya on 02.12.2015.
 */
public class TableFromSubmits extends TableAbstract {

    public TableFromSubmits(ArgumentsParser argumentsParser) {
        this.argumentsParser = argumentsParser;
        List<Integer> problemGlobalIndicesList = new ArrayList<>();
        Map<Integer, Integer> problemsGlobalNumbersMap = new HashMap<>();
        List<String> studentsNamesList = new ArrayList<>();
        Map<String, Integer> studentsNamesMap = new HashMap<>();
        List<Pair<Integer, Integer> > listAllOk = new ArrayList<>();
        driver = new SilentHtmlUnitDriver(true);
        driver.navigate().to(argumentsParser.getSuccessfulSubmitsUrl() + "#1");
        int pageNumber = 1;
        boolean nextPageFound = true;
        while(nextPageFound) {
            WebElement source = driver.findElement(By.id("Searchresult"));
            try {
                Thread.sleep(5000 + argumentsParser.getTimeout());
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
            System.out.println(source);
            List<WebElement> tableRows = source.findElements(By.xpath("table/tbody/tr"));
            System.out.println(tableRows);
            if(tableRows.size() < 2) {
                System.out.println("page results are empty; assuming it's because everything was in previous pages, breaking the loop");
                break;
            }
            System.out.println("tableRows = <<<<<" + tableRows + ">>>>>");
            for(int theRow=1; theRow<tableRows.size(); theRow++) {
                WebElement currRow = tableRows.get(theRow);
                System.out.println("currRow = <<" + currRow.toString() + ">>");
                String studentName = currRow.findElement(By.xpath("td[2]")).getText().replaceAll("[Іі]", "_");
                System.out.print("studentName = " + studentName);
                try {
                    Integer problemGlobalIdx = Integer.parseInt(currRow.findElement(By.xpath("td[3]")).getText().split("\\.")[0]);
                    System.out.println(", problemGlobalIdx = " + problemGlobalIdx);
                    if(!problemsGlobalNumbersMap.containsKey(problemGlobalIdx)) {
                        problemsGlobalNumbersMap.put(problemGlobalIdx, problemGlobalIndicesList.size());
                        problemGlobalIndicesList.add(problemGlobalIdx);
                    }
                    if(!studentsNamesMap.containsKey(studentName)) {
                        studentsNamesMap.put(studentName, studentsNamesList.size());
                        studentsNamesList.add(studentName);
                    }
                    listAllOk.add(new Pair<Integer, Integer>(studentsNamesMap.get(studentName), problemsGlobalNumbersMap.get(problemGlobalIdx)));
                } catch (NumberFormatException e) {
                    System.err.println(currRow);
                    System.err.println("\n\n");
                    System.err.println(currRow.findElement(By.xpath("td[3]")));
                    System.err.println("\n\n");
                    System.err.println(currRow.findElement(By.xpath("td[3]")).getText());
                    System.err.println("\n\n");
                    System.err.println(currRow.findElement(By.xpath("td[3]")).getText().split("\\."));
                }
            }
            System.out.println(studentsNamesMap);
            System.out.println(problemsGlobalNumbersMap);
            System.out.println(listAllOk);
// starting preparing for next page
            pageNumber++;
            System.out.println("Trying to load page #" + pageNumber + " of successful submits");
            WebElement allLinksToPages = driver.findElement(By.id("Pagination"));
            nextPageFound = false;
            List<WebElement> WE = allLinksToPages.findElements(By.tagName("a"));
//            System.out.println(WE);
            for(WebElement we : WE) {
//                System.out.println(we.toString());
//                System.out.println("getText : ``" + we.getText() + "''");
//                System.out.println(we.getTagName());
//                System.out.println(we.getClass().toString());
                if(("" + pageNumber).toString().equals(we.getText())) {
                    we.click();
                    try {
                        Thread.sleep(1000 + argumentsParser.getTimeout());
                    } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                    }
                    nextPageFound = true;
                    break;
                }
            }
            // TODO: change to click(), it should be faster!!!
//            driver.navigate().to(argumentsParser.getSuccessfulSubmitsUrl() + "#" + pageNumber);
//            System.out.println("before sleep");
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                System.err.println(e.getMessage());
//            }
//            driver.navigate().refresh();
//            try {
//                Thread.sleep(7000);
//            } catch (InterruptedException e) {
//                System.err.println(e.getMessage());
//            }
//            System.out.println("after sleep");

        }
        studentsNumber = studentsNamesList.size();
        studentNames = new String[studentsNumber];
        for(int i=0; i<studentsNumber; i++) {
            studentNames[i] = studentsNamesList.get(i);
        }
        problemsNumber = problemGlobalIndicesList.size();
        problemNames = new String[problemsNumber];
        for(int j=0; j<problemsNumber; j++) {
            problemNames[j] = problemGlobalIndicesList.get(j).toString();
        }
        table = new String[studentsNumber][problemsNumber];
        solvedProblemsNum = new int[studentsNumber];
        Arrays.fill(solvedProblemsNum, 0);
        for(Pair<Integer, Integer> p : listAllOk) {
            if(!("+".equals(table[p.getKey()][p.getValue()]))) {
                solvedProblemsNum[p.getKey()]++;
                table[p.getKey()][p.getValue()] = "+";
            } else {
                System.out.println("NON-first solution of " + problemNames[p.getValue()] + " by " + studentNames[p.getKey()]);
            }
        }
/*
        int maxNameLen = 0;
        studentNames = new String[studentsNumber];
        for(int i=0; i<studentsNumber; i++) {
            studentNames[i] = studentsNamesList.get(i);
            maxNameLen = Math.max(maxNameLen, studentNames[i].length());
        }
        String[] studentNamesWithSpaces = new String[studentsNumber];
        for(int i=0; i<studentsNumber; i++) {
            StringBuilder sb = new StringBuilder(studentNames[i]);
            for(int j=studentNames[i].length(); j<maxNameLen; j++)
                sb.append(' ');
            studentNamesWithSpaces[i] = sb.toString();
        }
*/
    }

/*
    @Override
    public void printTable() {
    }
*/

    @Override
    protected Document getProblemPage(String problemName) {
        String url = "http://informatics.mccme.ru/moodle/mod/statements/view3.php?chapterid=" + problemName.split("\\s")[1];
        driver.get(url);
        System.out.println("before sleep");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
        driver.navigate().to(url);
        System.out.println("before sleep");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
        driver.navigate().refresh();
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
        System.out.println("after sleep");
        String pageSource = driver.getPageSource();
        System.out.println(pageSource);
        return Jsoup.parse(pageSource);
    }

}
