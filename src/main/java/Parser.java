import com.martiansoftware.jsap.JSAPException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Parser {

    private static final String HOME_DIR = System.getProperty("user.dir");

    private static final String POLYGON_CSS = getResourceAsString("/polygon.css");
    private static final String THEME_CSS = getResourceAsString("/statements_theme.css");
    private static final String LATEX_MATH_ML_JS = getResourceAsString("/LaTeXMathML.js");

    private static final String CONTENT_LOCATION = ".statements_content";
    private static final String SUBMIT_LOCATION = "#submit";
    private static final String SUBMIT_BOX_LOCATION = ".submit_box";
    private static final String BOTTOM_LINE_LOCATION = ".statements_content > h1:nth-child(5)";
    private static final String ANALYSIS_LOCATION = "#analysis";
    private static final String IDEAL_SOLUTIONS_LOCATION = "#ideal-solutions";
    private static final String UPPER_LINE_LOCATION = "div.statements_chapter_title:nth-child(1) > h1:nth-child(4)";
    private static final String BEST_SOLVES_LOCATION = "div.statements_chapter_title:nth-child(1) > div:nth-child(5)";

    private static final String[] TO_REMOVE = {
            BEST_SOLVES_LOCATION, SUBMIT_LOCATION, SUBMIT_BOX_LOCATION, BOTTOM_LINE_LOCATION, UPPER_LINE_LOCATION,
            ANALYSIS_LOCATION, IDEAL_SOLUTIONS_LOCATION
    };

    static Map<String, String> alreadyParsedProblems;

    private static WebDriver driver = new SilentHtmlUnitDriver();

    private static ArgumentsParser argumentsParser;

    private static String getResourceAsString(String name) {
        return convertStreamToString(Parser.class.getResourceAsStream(name));
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static void main(String[] args) throws JSAPException {
        argumentsParser = new ArgumentsParser(args);
        argumentsParser.processArgs();
        alreadyParsedProblems = new HashMap<>();

        System.out.println("Parser started!");

        Table table = new Table(driver, argumentsParser.getTableUrl());
        System.out.println("Table parsed!");

        for(int studIdx=0; studIdx<table.getStudentsNumber(); studIdx++) {
            if(table.getSolvedProblemsNum()[studIdx] < argumentsParser.getNumForChoose1()) {
                continue;
            }
            Participant participant = new Participant(studIdx+1, table);
            String participantDir = HOME_DIR + File.separator + table.getStudentNames()[participant.getNumber()-1];
            if(!argumentsParser.getBlockSubDir().isEmpty()) {
                participantDir = participantDir + File.separator + argumentsParser.getBlockSubDir();
            }
            File dir = new File(participantDir);
            dir.mkdirs();
            System.setProperty("user.dir", participantDir);
            int numProblemsToGet = 1;
            if(table.getSolvedProblemsNum()[studIdx] >= argumentsParser.getNumForChoose2()) {
                numProblemsToGet = 2;
            }
            for (int theProb = 0; theProb < numProblemsToGet; theProb++) {
                try {
                    if (theProb > 0) {
                        Thread.sleep(argumentsParser.getTimeout());
                    }
                    parseProblemForStudent(participant, table, participantDir);
                } catch (IllegalArgumentException e) {
                    System.out.println("All available solves parsed. Exiting...");
                    break;
                } catch (IOException ex) {
                    System.err.println(ex.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

/*
        Participant participant = new Participant(argumentsParser.getStudentIndex(), table);
        String participantDir = HOME_DIR + File.separator + table.getStudentNames()[participant.getNumber()-1];
        if(!argumentsParser.getBlockSubDir().isEmpty()) {
            participantDir = participantDir + File.separator + argumentsParser.getBlockSubDir();
        }
        File dir = new File(participantDir);
        dir.mkdirs();
        System.setProperty("user.dir", participantDir);
        for (int i = 0; i < argumentsParser.getProblemsNum(); i++) {
            try {
                if (i > 0) {
                    Thread.sleep(argumentsParser.getTimeout());
                }
                parseProblemForStudent(participant, table, participantDir);
            } catch (IllegalArgumentException e) {
                System.out.println("All available solves parsed. Exiting...");
                break;
            } catch (IOException ex) {
                System.err.println(ex.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
*/
    }

    private static void copyResourcesInto(File dir) throws IOException {
        File copyPolygonTo = new File(dir + File.separator + "polygon.css");
        File copyThemeTo = new File(dir + File.separator + "statements_theme.css");
        File copyLatexMathMlTo = new File(dir + File.separator + "LaTeXMathML.js");

        BufferedWriter mathMlWriter = new BufferedWriter(new FileWriter(copyLatexMathMlTo));
        BufferedWriter polygonWriter = new BufferedWriter(new FileWriter(copyPolygonTo));
        BufferedWriter themeWriter = new BufferedWriter(new FileWriter(copyThemeTo));

        mathMlWriter.write(LATEX_MATH_ML_JS);
        polygonWriter.write(POLYGON_CSS);
        themeWriter.write(THEME_CSS);

        mathMlWriter.close();
        polygonWriter.close();
        themeWriter.close();
    }

    private static void parseProblemForStudent(Participant participant, Table table, String participantDir) throws IOException, IllegalArgumentException {
        String problemChar = participant.getRandomProblemChar(table);
        String problemName = "Задача " + problemChar;

        String newDir = participantDir + File.separator + problemName;
        File dir = new File(newDir);
        dir.mkdir();

        System.setProperty("user.dir", newDir);

        String textToSaveNow = null;
        if(!alreadyParsedProblems.containsKey(problemName)) {
            WebElement link = driver.findElement(By.partialLinkText(problemName));
            link.click();

            Document page = Jsoup.parse(driver.getPageSource());
            textToSaveNow = cleanPage(page);
            alreadyParsedProblems.put(problemName, textToSaveNow);
        } else {
            textToSaveNow = alreadyParsedProblems.get(problemName);
        }

        participant.writeHtmlToFile(textToSaveNow, newDir, problemName);
        copyResourcesInto(dir);

        System.setProperty("user.dir", HOME_DIR);
        System.out.println("Problem " + problemChar + " successfully parsed!");
    }

    private static String cleanPage(Document page) {
        for (String location : TO_REMOVE) {
            page.select(location).remove();
        }

        String html = page.select(CONTENT_LOCATION).html();

        String start = "<html>"
                + "<head>"
                + "<meta content=\"text/html; charset=utf-8\" http-equiv=\"content-type\"></meta>"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"polygon.css\">"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"statements_theme.css\">"
                + "<script type=\"text/javascript\" src=\"LaTeXMathML.js\"></script>"
                + "</head>"
                + "<body>";
        String end = "</body></html>";

        Pattern p = Pattern.compile("^(\\s+(<br\\s*/>)?\\n?)", Pattern.MULTILINE);
        html = p.matcher(html).replaceAll("");

        html = start + html + end;

        return html;
    }

}