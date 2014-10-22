import com.google.common.io.Files;
import com.martiansoftware.jsap.JSAPException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;

public class Parser {

    private static final String HOME_DIR = System.getProperty("user.dir");

    private static final File POLYGON_CSS = new File(HOME_DIR + File.separator + "polygon.css");
    private static final File THEME_CSS = new File(HOME_DIR + File.separator + "statements_theme.css");
    private static final File LATEX_MATH_ML_JS = new File(HOME_DIR + File.separator + "LaTeXMathML.js");

    private static final String CONTENT_LOCATION = ".statements_content";
    private static final String SUBMIT_LOCATION = "#submit";
    private static final String SUBMIT_BOX_LOCATION = ".submit_box";
    private static final String BOTTOM_LINE_LOCATION = ".statements_content > h1:nth-child(5)";
    private static final String UPPER_LINE_LOCATION = "div.statements_chapter_title:nth-child(1) > h1:nth-child(4)";
    private static final String BEST_SOLVES_LOCATION = "div.statements_chapter_title:nth-child(1) > div:nth-child(5)";

    private static final String[] TO_REMOVE = {
            BEST_SOLVES_LOCATION, SUBMIT_LOCATION, SUBMIT_BOX_LOCATION, BOTTOM_LINE_LOCATION, UPPER_LINE_LOCATION
    };


    private static WebDriver driver = new SilentHtmlUnitDriver();

    private static ArgumentsParser argumentsParser;

    public static void main(String[] args) throws JSAPException {
        argumentsParser = new ArgumentsParser(args);
        argumentsParser.processArgs();

        System.out.println("Parser started!");

        Table table = new Table(driver, argumentsParser.getTableUrl());
        System.out.println("Table parsed!");

        Participant participant = new Participant(argumentsParser.getStudentIndex(), table);
        String participantDir = HOME_DIR + File.separator + participant.getNumber();
        File dir = new File(participantDir);
        dir.mkdir();
        System.setProperty("user.dir", participantDir);
        for (int i = 0; i < argumentsParser.getProblemsNum(); i++) {
            try {
                parseProblemForStudent(participant, table);
            } catch (IllegalArgumentException e) {
                System.out.println("All available solves parsed. Exiting...");
                break;
            } catch (IOException ex) {
                System.err.println(ex.toString());
            }
        }
    }

    private static void copyResourcesInto(File dir) throws IOException {
        File copyPolygonTo = new File(dir + File.separator + "polygon.css");
        File copyThemeTo = new File(dir + File.separator + "statements_theme.css");
        File copyLatexMathMlTo = new File(dir + File.separator + "LaTeXMathML.js");
        Files.copy(LATEX_MATH_ML_JS, copyLatexMathMlTo);
        Files.copy(POLYGON_CSS, copyPolygonTo);
        Files.copy(THEME_CSS, copyThemeTo);
    }

    private static void parseProblemForStudent(Participant participant, Table table) throws IOException, IllegalArgumentException {
        String problemChar = participant.getRandomProblemChar(table);
        String problemName = "Задача " + problemChar;

        String newDir = HOME_DIR + File.separator + participant.getNumber() + File.separator + problemName;
        File dir = new File(newDir);
        dir.mkdir();

        System.setProperty("user.dir", newDir);

        WebElement link = driver.findElement(By.partialLinkText(problemName));
        link.click();

        Document page = Jsoup.parse(driver.getPageSource());
        String html = cleanPage(page).html();

        participant.writeHtmlToFile(html, newDir, problemName);
        copyResourcesInto(dir);

        System.setProperty("user.dir", HOME_DIR);
        System.out.println("Problem " + problemChar + " successfully parsed!");
    }

    private static Elements cleanPage(Document page) {
        for (String location : TO_REMOVE) {
            page.select(location).remove();
        }

        return page.select(CONTENT_LOCATION);
    }

}