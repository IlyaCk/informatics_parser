import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class Participant {
    private int number;
    private int[] permitted;

    public Participant(int number, Table table) {
        this.number = number - 1;
        getPermissions(table);
    }

    public void printPermitted() {
        for (int i : permitted) {
            System.out.print(i + " ");
        }
        System.out.println();
    }

    public int[] getPermitted(Table table) {
        return permitted;
    }

    public String getRandomProblem(Table table) {
        int random = new Random().nextInt(permitted.length);
        int randInt = permitted[random];
        this.permitted = ArrayUtils.removeElement(permitted, randInt);
        return "Задача " + table.getProblemNames()[randInt];
    }

    public String getRandomProblemChar(Table table) throws IllegalArgumentException {
        int random = new Random().nextInt(permitted.length);
        int randInt = this.permitted[random];
        this.permitted = ArrayUtils.removeElement(permitted, randInt);
        return table.getProblemNames()[randInt];
    }

    public void writeHtmlToFile(String html, String dir, String name) throws IOException {
        try (PrintWriter writer = new PrintWriter(dir + File.separator + name + ".html", "UTF-8")) {
            writer.print(html);
        }
    }

    private void getPermissions(Table table) {
        ArrayList<Integer> temp = new ArrayList<>();
        String[][] t = table.getTable();
        String[] line = t[number];
        for (int i = 0; i < line.length; i++) {
            if (line[i].equals("+")) {
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