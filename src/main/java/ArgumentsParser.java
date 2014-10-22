import com.martiansoftware.jsap.*;

public class ArgumentsParser {

    private String tableUrl;
    private Integer studentIndex;
    private Integer problemsNum = 2;

    private String[] args;

    public ArgumentsParser(String[] args) {
        this.args = args;
    }

//    public boolean parse() {
//        String arg;
//        String argContent;
//        for (String a : args) {
//            arg = a.substring(0, a.indexOf("="));
//            argContent = a.substring(a.indexOf("=") + 1);
//            switch (arg) {
//                case "url":
//                    tableUrl = argContent;
//                    break;
//                case "student":
//                    studentIndex = Integer.parseInt(argContent);
//                    break;
//                case "num":
//                    problemsNum = Integer.parseInt(argContent);
//                    break;
//                case "help":
//                    printHelp();
//                    return false;
//                default:
//                    System.out.println("Wrong argument provided. Format is:\n" +
//                            "url=<Table url> student=<Student's index in table> num=<Number of problems to parse>");
//                    return false;
//            }
//        }
//        if (studentIndex == null) {
//            System.out.println("The 'student' argument is required");
//            return false;
//        } else if (tableUrl == null) {
//            System.out.println("The 'url' argument is required");
//            return false;
//        }
//        return true;
//    }

    public String getTableUrl() {
        return tableUrl;
    }

    public int getStudentIndex() {
        return studentIndex;
    }

    public int getProblemsNum() {
        return problemsNum;
    }

//    private void printHelp() {
//        String help = "params:" + "\n"
//                + "url=<String> - URL of the results table page" + "\n"
//                + "student=<int> - Student's index in the table" + "\n"
//                + "num=<int> - Number of solved problems to parse";
//
//        System.out.println(help);
//    }

    public void processArgs() throws JSAPException {
        JSAP jsap = new JSAP();

        FlaggedOption urlOption = new FlaggedOption("url")
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(true)
                .setLongFlag("url")
                .setShortFlag('u');
        urlOption.setHelp("Url of the table to parse");

        jsap.registerParameter(urlOption);

        FlaggedOption numOption = new FlaggedOption("count")
                .setStringParser(JSAP.INTEGER_PARSER)
                .setRequired(true)
                .setDefault("2")
                .setLongFlag("count")
                .setShortFlag('n');
        numOption.setHelp("Number of problems to parse");

        jsap.registerParameter(numOption);

        FlaggedOption studentOption = new FlaggedOption("student")
                .setStringParser(JSAP.INTEGER_PARSER)
                .setRequired(true)
                .setLongFlag("student")
                .setShortFlag('s');
        studentOption.setHelp("Student's index in the table");

        jsap.registerParameter(studentOption);

        JSAPResult config = jsap.parse(args);

        // check whether the command line was valid, and if it wasn't,
        // display usage information and exit.
        if (!config.success()) {
            System.err.println();

            // print out specific error messages describing the problems
            // with the command line, THEN print usage, THEN print full
            // help.  This is called "beating the user with a clue stick."
            for (java.util.Iterator errs = config.getErrorMessageIterator();
                 errs.hasNext();) {
                System.err.println("Error: " + errs.next());
            }

            System.err.println();
            System.err.println("Usage: java " + Parser.class.getName());
            System.err.println("                " + jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
            System.exit(1);
        }

        tableUrl = config.getString("url");
        studentIndex = config.getInt("student");
        problemsNum = config.getInt("num");
    }

}
