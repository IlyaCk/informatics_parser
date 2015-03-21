import com.martiansoftware.jsap.*;

public class ArgumentsParser {

    private String tableUrl;
    private String blockSubDir;
//    private Integer studentIndex;
//    private Integer problemsNum;
    private Integer numForChoose1;
    private Integer numForChoose2;
    private Integer timeout;

    private String[] args;

    public Integer getNumForChoose1() {
        return numForChoose1;
    }

    public Integer getNumForChoose2() {
        return numForChoose2;
    }

    public String getBlockSubDir() {
        return blockSubDir;
    }

    public ArgumentsParser(String[] args) {
        this.args = args;

    }

    public String getTableUrl() {
        return tableUrl;
    }

/*
    public int getStudentIndex() {
        return studentIndex;
    }

    public int getProblemsNum() {
        return problemsNum;
    }
*/

    public Integer getTimeout() {
        return timeout;
    }

    public void processArgs() throws JSAPException {
        JSAP jsap = new JSAP();

        FlaggedOption urlOption = new FlaggedOption("url")
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(true)
                .setLongFlag("url")
                .setShortFlag('u');
        urlOption.setHelp("Url of the table to parse (must be inside quotes, like \"http://...\")");

        jsap.registerParameter(urlOption);

/*
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
*/

        FlaggedOption timeoutOption = new FlaggedOption("timeout")
                .setStringParser(JSAP.INTEGER_PARSER)
                .setRequired(true)
                .setLongFlag("timeout")
                .setDefault("0")
                .setShortFlag('t');
        timeoutOption.setHelp("Timeout (in seconds) between requests. May be useful when server rejects too many requests in a row from one IP.");

        jsap.registerParameter(timeoutOption);

        FlaggedOption blockSubDirOption = new FlaggedOption("subdir")
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(true)
                .setLongFlag("subdir")
                .setDefault("")
                .setShortFlag('d');
        blockSubDirOption.setHelp("To organize statements of many blocks to same student's folder, this can be used as subdir inside student's folder.");

        jsap.registerParameter(blockSubDirOption);

        FlaggedOption numSolvedForChooseTwoOption = new FlaggedOption("numFor2")
                .setStringParser(JSAP.INTEGER_PARSER)
                .setRequired(true)
                .setLongFlag("numSolvedForChooseTwo")
                .setDefault("5")
                .setShortFlag('2');
        numSolvedForChooseTwoOption.setHelp("For what minimal quantity of automatically-passed solutions student should defend TWO problems");

        jsap.registerParameter(numSolvedForChooseTwoOption);

        FlaggedOption numSolvedForChooseOneOption = new FlaggedOption("numFor1")
                .setStringParser(JSAP.INTEGER_PARSER)
                .setRequired(true)
                .setLongFlag("numSolvedForChooseOne")
                .setDefault("2")
                .setShortFlag('1');
        numSolvedForChooseOneOption.setHelp("For what minimal quantity of automatically-passed solutions student should defend ONE problem");

        jsap.registerParameter(numSolvedForChooseOneOption);

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
            System.err.println("Usage: java -jar " + Parser.class.getName() + ".jar");
            System.err.println("                " + jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
            System.exit(1);
        }

        tableUrl = config.getString("url");
        blockSubDir = config.getString("subdir");
//        studentIndex = config.getInt("student");
//        problemsNum = config.getInt("count");
        numForChoose1 = config.getInt("numFor1");
        numForChoose2 = config.getInt("numFor2");

        timeout = config.getInt("timeout")*1000;
    }

}
