import com.martiansoftware.jsap.*;

public class ArgumentsParser {
    private Integer problemsGroup;
    private Integer studentsGroup;
    private Integer totalProblems;
    private Double totalPoints;

    private String tableUrl;
    private String savedResultsFileName;
    private String successfulSubmitsUrl;
    private String blockSubDir;
    private int[] numForChoose;
    private String numForChooseRawStr;
    private Integer timeout;
    private String whatTable;

    private String[] args;

    public Integer getProblemsGroup() {
        return problemsGroup;
    }

    public Integer getStudentsGroup() {
        return studentsGroup;
    }

    void setTotalProblems(Integer totalProblems) {
        this.totalProblems = totalProblems;
    }

    /**
     * @return -- how many statements to download and parse
     * @param solved -- number of solved problems (from table, which was downloaded form site)
     */
    public int getChosenNumberForSolved(int solved) {
//        return Math.abs(Arrays.binarySearch(numForChoose, solved));
        int i=0;
        while(i < numForChoose.length && numForChoose[i] < solved)
            i++;
        return i;
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

    public Integer getTimeout() {
        return timeout;
    }

    public String getSuccessfulSubmitsUrl() {
        return successfulSubmitsUrl;
    }

//    public boolean doParseSubmits() {
//        return "submits".equalsIgnoreCase(whatTable);
//    }

    public WhatTableEnum getWhatTable() {
        if("results".equalsIgnoreCase(whatTable)) {
            return WhatTableEnum.RESULTS_ONLINE;
        } else if("submits".equalsIgnoreCase(whatTable)) {
            return WhatTableEnum.SUBMITS_ONLINE;
        } else if("savedResults".equalsIgnoreCase(whatTable)) {
            return WhatTableEnum.RESULTS_SAVED;
        } else {
            throw new IllegalArgumentException("whatTable should be one of [\"results\",\"submits\",\"savedResults\", but " + whatTable + " found.");
        }
    }

    public String getAlreadyParsedProblemsFilename() {
        if(getWhatTable()==WhatTableEnum.SUBMITS_ONLINE) {
            return "alreadyParsedProblems.txt";
        } else {
            return "alreadyParsedProblems__problem_group_" + getProblemsGroup() + ".txt";
        }
    }

    public Integer getTotalProblems() {
        return totalProblems;
    }

    public Double getTotalPoints() {
        return totalPoints;
    }

    public String getSavedResultsFileName() {
        if(savedResultsFileName == null  ||  savedResultsFileName.isEmpty()) {
            return "" + problemsGroup + "_" + studentsGroup + ".txt";
        } else {
            return savedResultsFileName;
        }
    }

    public void processArgs() throws JSAPException {
        JSAP jsap = new JSAP();

        FlaggedOption whatTableOption = new FlaggedOption("whatTable")
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(false)
                .setLongFlag("whatTable")
                .setDefault("savedResults")
                .setShortFlag('w');

        whatTableOption.setHelp("Should be either ``results'' (to parse via online table of results)\n\tor ``submits'' (to parse via online table of submits) \n\tor``savedResults''  (to parse via saved-to-text-file table of submits).");
        jsap.registerParameter(whatTableOption);

        FlaggedOption savedResultsFileNameOption = new FlaggedOption("savedResultsFileName")
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(false)
                .setLongFlag("savedResultsFileName")
                .setDefault((String)null)
                .setShortFlag('f');

        savedResultsFileNameOption.setHelp("Name of local file with saved-to-text-file table of submits. If none specified, suggested to be ``<problemsGroup>_<studentsGroup>.txt''");
        jsap.registerParameter(savedResultsFileNameOption);


        FlaggedOption timeoutOption = new FlaggedOption("timeout")
                .setStringParser(JSAP.INTEGER_PARSER)
                .setRequired(true)
                .setLongFlag("timeout")
                .setDefault("2")
                .setShortFlag('t');

        timeoutOption.setHelp("Timeout (in seconds) between requests. May be useful when server rejects too many requests in a row from one IP.");
        jsap.registerParameter(timeoutOption);

        FlaggedOption problemsGroupOption = new FlaggedOption("problemsGroup")
                .setStringParser(JSAP.INTEGER_PARSER)
                .setRequired(true)
                .setLongFlag("problemsGroup")
                .setDefault("276")
                .setShortFlag('p');

        timeoutOption.setHelp("What group of problems (id=... in url).");
        jsap.registerParameter(problemsGroupOption);

        FlaggedOption studentsGroupOption = new FlaggedOption("studentsGroup")
                .setStringParser(JSAP.INTEGER_PARSER)
                .setRequired(true)
                .setLongFlag("studentsGroup")
                .setDefault("3882")
                .setShortFlag('s');
        timeoutOption.setHelp("What group of students (group_id=... in url).");

        jsap.registerParameter(studentsGroupOption);

        FlaggedOption blockSubDirOption = new FlaggedOption("subdir")
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(true)
                .setLongFlag("subdir")
                .setDefault("")
                .setShortFlag('d');

        blockSubDirOption.setHelp("To organize statements of many blocks to same student's folder, this can be used as subdir inside student's folder.");
        jsap.registerParameter(blockSubDirOption);

        FlaggedOption numForChooseOption = new FlaggedOption("numSolvedFor")
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(true)
                .setLongFlag("numSolvedFor")
                .setDefault("auto")
                .setShortFlag('n');

        numForChooseOption.setHelp("How many problems solved means we need download 1/2/3/... statements");
        jsap.registerParameter(numForChooseOption);

        FlaggedOption numTotalProblemsOption = new FlaggedOption("numTotalProblems")
                .setStringParser(JSAP.INTEGER_PARSER)
                .setRequired(true)
                .setLongFlag("numTotalProblems")
                .setDefault("-1")
                .setShortFlag('r');

        numForChooseOption.setHelp("Total quantity of problems. Really required for ``-whatTable \"submits\"'' only.");
        jsap.registerParameter(numTotalProblemsOption);

        FlaggedOption numTotalPointsOption = new FlaggedOption("numTotalPoints")
                .setStringParser(JSAP.DOUBLE_PARSER)
                .setRequired(true)
                .setLongFlag("numTotalPoints")
                .setDefault("7")
                .setShortFlag('o');

        numForChooseOption.setHelp("Total quantity of points if all problems solved.");
        jsap.registerParameter(numTotalPointsOption);


        JSAPResult config = jsap.parse(args);

        // check whether the command line was valid, and if it wasn't,
        // display usage information and exit.
        if (!config.success()) {
//        if (true) {
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


        whatTable = config.getString("whatTable");
        savedResultsFileName = config.getString("savedResultsFileName");
        problemsGroup = config.getInt("problemsGroup");
        studentsGroup = config.getInt("studentsGroup");
        totalProblems = config.getInt("numTotalProblems");
        totalPoints = config.getDouble("numTotalPoints");
        blockSubDir = config.getString("subdir");
        numForChooseRawStr = config.getString("numSolvedFor");

        if("auto".equalsIgnoreCase(numForChooseRawStr) || "auto2".equalsIgnoreCase(numForChooseRawStr)) {
            if(totalPoints < ("auto".equalsIgnoreCase(numForChooseRawStr) ? 20 : 10)) {
                numForChoose = new int[3];
                numForChoose[0] = (int) Math.ceil(0.3 * totalProblems);
                numForChoose[1] = (int) Math.ceil(0.55 * totalProblems);
                numForChoose[2] = (int) Math.ceil(0.87 * totalProblems);
            } else {
                numForChoose = new int[4];
                numForChoose[0] = (int) Math.ceil(0.15 * totalProblems);
                numForChoose[1] = (int) Math.ceil(0.4 * totalProblems);
                numForChoose[2] = (int) Math.ceil(0.65 * totalProblems);
                numForChoose[3] = (int) Math.ceil(0.9 * totalProblems);
            }
        } else {
            String[] spl = numForChooseRawStr.split("[\\s\\,]+");
            numForChoose = new int[spl.length];
            for (int i = 0; i < spl.length; i++)
                numForChoose[i] = Integer.parseInt(spl[i]);
        }
        timeout = config.getInt("timeout")*1000;

        tableUrl = "https://informatics.msk.ru/mod/statements/view3.php?id=" + problemsGroup + "&standing&group_id=" + studentsGroup;
    }
}
