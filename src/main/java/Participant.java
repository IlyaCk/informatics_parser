import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Participant {
    private int number;
    private int[] permitted;

    public Participant(int number, TableAbstract table) {
        this.number = number - 1;
        getPermissions(table);
    }

    public void printPermitted() {
        for (int i : permitted) {
            System.out.print(i + " ");
        }
        System.out.println();
    }

    public int[] getPermitted(TableFromResults tableFromResults) {
        return permitted;
    }

    public String getRandomProblem(TableFromResults tableFromResults) {
        int random = new Random().nextInt(permitted.length);
        int randInt = permitted[random];
        this.permitted = ArrayUtils.removeElement(permitted, randInt);
        return "Задача " + tableFromResults.getProblemNames()[randInt];
    }

    public String getRandomProblemChar(TableAbstract tableFromResults) throws IllegalArgumentException {
        int random = new Random().nextInt(permitted.length);
        int randInt = this.permitted[random];
        this.permitted = ArrayUtils.removeElement(permitted, randInt);
        return tableFromResults.getProblemNames()[randInt];
    }

    public void writeHtmlToFile(String html, String dir, String name) throws IOException {
    }

    private void getPermissions(TableAbstract table) {
        ArrayList<Integer> temp = new ArrayList<>();
        String[][] t = table.getTable();
        String[] line = t[number];
        for (int i = 0; i < line.length; i++) {
            if ("+".equals(line[i])) {
                temp.add(i);
            }
        }
        this.permitted = new int[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            this.permitted[i] = temp.get(i);
        }
    }

    public int getNumber() {
        return number + 1;
    }
}




/*
"-"

"-+-+-+-"
"*"

"."


$v_K$

$v_2$

$v_{12}$

$c_2$

 */