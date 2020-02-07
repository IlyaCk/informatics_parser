import com.martiansoftware.jsap.JSAPException;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private static final String TABLES_AT_TOP = "table:nth-child(1)";

    public static String getCurrTimeStr() {
        return currTimeStr;
    }

    private static String currTimeStr;

    private static final String[] TO_REMOVE = {
            BEST_SOLVES_LOCATION, SUBMIT_LOCATION, SUBMIT_BOX_LOCATION, BOTTOM_LINE_LOCATION, UPPER_LINE_LOCATION,
            ANALYSIS_LOCATION, IDEAL_SOLUTIONS_LOCATION, TABLES_AT_TOP
    };

    static Map<String, String> alreadyParsedProblems;
    static StringBuilder globalProblemsFound;

    private static WebDriver driver = new SilentHtmlUnitDriver(true);

    private static ArgumentsParser argumentsParser;

    private static String getResourceAsString(String name) {
        return convertStreamToString(Parser.class.getResourceAsStream(name));
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static void main(String[] args) throws JSAPException, IOException {
        currTimeStr = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        argumentsParser = new ArgumentsParser(args);
        argumentsParser.processArgs();
        alreadyParsedProblems = new HashMap<>();
        globalProblemsFound = new StringBuilder();

        try {
            ObjectInputStream allPreviouslySavedProblems = new ObjectInputStream(new FileInputStream(argumentsParser.getAlreadyParsedProblemsFilename()));
            alreadyParsedProblems = (HashMap<String, String>)allPreviouslySavedProblems.readObject();
            allPreviouslySavedProblems.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Failed to load previuosly saved alreadyParsedProblems (filename:``" + argumentsParser.getAlreadyParsedProblemsFilename() + "'')");
        }

//        TableFromSubmits tableFromSubmits = new TableFromSubmits(argumentsParser);

        System.out.println("Parser started!");

        TableAbstract table = null;
        switch (argumentsParser.getWhatTable()) {
            case SUBMITS_ONLINE:
                table = new TableFromSubmits(argumentsParser);
                System.out.println("Submits table parsed!");
                break;
            case RESULTS_ONLINE:
                table = new TableFromResults(argumentsParser);
                System.out.println("Results table (online) parsed!");
                break;
            case RESULTS_SAVED:
                table = new TableFromSavedResults(argumentsParser);
                System.out.println("Results table (saved) parsed!");
                break;
            default:
                throw new IllegalArgumentException("Illegal type of table");
        }
        table.printTable();

        boolean inetDownloadAlreadyUsed = false;
        for(int studIdx=0; studIdx< table.getStudentsNumber(); studIdx++) {
            int numProblemsToGet = argumentsParser.getChosenNumberForSolved(table.getSolvedProblemsNum(studIdx));
            String studentName = table.getStudentNames(studIdx);
            String participantDir = HOME_DIR + File.separator + currTimeStr + File.separator + studentName;
            if(!table.doProposeDefence(table.getSolvedProblemsNum(studIdx))) {
                System.out.println("Student " + studentName + " skipped because too few problems");
                continue;
            }
            System.out.println("Starting to download problem statements for student " + studentName);
            if(!argumentsParser.getBlockSubDir().isEmpty()) {
                participantDir = participantDir + File.separator + argumentsParser.getBlockSubDir();
            }
            File dir = new File(participantDir);
            dir.mkdirs();
            System.setProperty("user.dir", participantDir);
            for (int theProb = 0; theProb < numProblemsToGet; theProb++) {
                try {
                    inetDownloadAlreadyUsed = parseProblemForStudent(studIdx, table, participantDir, inetDownloadAlreadyUsed);
//                    parseProblemForStudent(participant, table, participantDir);
                } catch (IOException ex) {
                    System.err.println(ex.toString());
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
        try {
            ObjectOutputStream saveAllParsedProblems = new ObjectOutputStream(new FileOutputStream(argumentsParser.getAlreadyParsedProblemsFilename()));
            saveAllParsedProblems.writeObject(alreadyParsedProblems);
            saveAllParsedProblems.flush();

            saveAllParsedProblems.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to save all alreadyParsedProblems to a text file for later use (filename: ``" + argumentsParser.getAlreadyParsedProblemsFilename() + "'')");
        }
        if(globalProblemsFound.length() > 10) {
            System.err.println("There were fails:\n" + globalProblemsFound);
        }

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

    private static boolean parseProblemForStudent(/*Participant participant, */ Integer studIdx, TableAbstract table, String participantDir, boolean sleepIfGettingFromSite) throws IOException, IllegalArgumentException {
//        String problemChar = participant.getRandomProblemChar(table);
//        String problemName = "Задача " + problemChar;
        String problemName = table.genRandomSolvedProblemName(studIdx);

        String newDir = participantDir + File.separator + problemName;
        File dir = new File(newDir);
        dir.mkdir();

        System.setProperty("user.dir", newDir);

        String textToSaveNow = null;
        if (!alreadyParsedProblems.containsKey(problemName)) {
            int triesToGetStatement = 0;
            do {
                if (sleepIfGettingFromSite) {
                    try {
                        Thread.sleep(argumentsParser.getTimeout());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(triesToGetStatement > 0) {
                    System.err.println("_R_E_trying to get problem statement for problemName = " + problemName);
                }
                Document page = table.getProblemPage(problemName);
                textToSaveNow = cleanPage(page);
                triesToGetStatement++;
                sleepIfGettingFromSite = true;
            } while((textToSaveNow==null || textToSaveNow.length() < 40)  &&  triesToGetStatement < 5);
            if(textToSaveNow==null || textToSaveNow.length() < 40) {
                globalProblemsFound.append("statement for problemName = " + problemName + " _F_A_I_L_E_D_\n");
            }
/*            if(problemName.matches("[^\\s\\dA-Za-z]+\\s+\\d{1,6}")) {
            // to use memoization for GLOBAL numbering ONLY!!!; otherwise, we'll get collisions
                alreadyParsedProblems.put(problemName, textToSaveNow);
            }
*/
            alreadyParsedProblems.put(problemName, textToSaveNow);
        } else {
            textToSaveNow = alreadyParsedProblems.get(problemName);
            System.out.println("Problem ``" + problemName + "'' statement was got from local cash");
/*
            if(argumentsParser.getWhatTable()==WhatTableEnum.SUBMITS_ONLINE  &&  problemName.matches("[^\\s\\dA-Za-z]+\\s+\\d{1,6}")
                    ||  argumentsParser.getWhatTable()==WhatTableEnum.RESULTS_SAVED) {
            // WAS to use memoization for GLOBAL numbering ONLY!!!; otherwise, we'll get collisions;
            // later memoization for inside-gri
                textToSaveNow = alreadyParsedProblems.get(problemName);
            } else {
                System.err.println("ERROR!!! alreadyParsedProblems.containsKey(problemName) is true, but problemName.matches(\"[^\\\\s\\\\dA-Za-z]+\\\\s+\\\\d{1,6}\") is false\n");
            }
*/
        }


//        participant.writeHtmlToFile(textToSaveNow, newDir, problemName);
        try (PrintWriter writer = new PrintWriter(dir + File.separator + problemName + ".html", "UTF-8")) {
            writer.print(textToSaveNow);
        }

        copyResourcesInto(dir);

        System.setProperty("user.dir", HOME_DIR);
        System.out.println(problemName + " successfully parsed!");

        return sleepIfGettingFromSite;
    }

    private static String cleanPage(Document page) {

//        for (String location : TO_REMOVE) {
//            page.select(location).remove();
//        }

//        String html = page.select(CONTENT_LOCATION).html();

//        String start = "<html>"
//                + "<head>"
//                + "<meta content=\"text/html; charset=utf-8\" http-equiv=\"content-type\"></meta>"
//                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"polygon.css\">"
//                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"statements_theme.css\">"
//                + "<script type=\"text/javascript\" src=\"LaTeXMathML.js\"></script>"
//                + "</head>"
//                + "<body>";
//        String end = "</body></html>";
//
//        Pattern p = Pattern.compile("^(\\s+(<br\\s*/>)?\\n?)", Pattern.MULTILINE);
//        html = p.matcher(html).replaceAll("");
//
//        html = start + html + end;

//        return html;

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