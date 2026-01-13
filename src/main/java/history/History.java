package history;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class History {
    private final List<String> entries = new ArrayList<>();
    private int cursor = -1;
    private int appendedUpTo = 0;

    public void addEntry(String entry){
        if (entry == null) return;
        entries.add(entry);
        cursor = -1;
    }

    public void loadIfExists(String filename) {
        File file = new File(filename);
        if (!file.exists() && !file.isFile()) { return ;}

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String entry;
            while ((entry = reader.readLine()) != null) {
                entries.add(entry);
            }
            appendedUpTo = entries.size();
        } catch (IOException e) {
            System.err.println("history: error reading file: " + e.getMessage());
        }
    }

    public void readFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists() || !file.isFile()) {
            System.err.println("history: file not found: " + filename);
            return;
        }
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String entry;
            while ((entry = reader.readLine()) != null) {
                entries.add(entry);
            }
        } catch (IOException e) {
            System.err.println("history: error reading file: " + e.getMessage());
        }
    }

    public void writeToFile(String filename) {
        File file = new File(filename);
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(file))) {
            for (String entry : entries) {
                writer.write(entry);
                writer.newLine();
            }
            appendedUpTo = entries.size();
        } catch (IOException e) {
            System.err.println("history: error writing to file: " + e.getMessage());
        }
    }

    public void appendToFile(String filename) {
        File file = new File(filename);
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(file, true))) {
            for (int i = appendedUpTo; i < entries.size(); i++) {
                writer.write(entries.get(i));
                writer.newLine();
            }
            appendedUpTo = entries.size();
        } catch (IOException e) {
            System.err.println("history: error appending to file: " + e.getMessage());
        }
    }

    public void printAll(PrintStream out) {
        for (int i = 0; i < entries.size(); i++) {
            out.println((i + 1) + " " + entries.get(i));
        }
    }

    public void printLastN(PrintStream out, int n) {
        if(n < 1 || n > entries.size()){ return; }

        int start = Math.max(0, entries.size() - n);
        for (int i = start; i < entries.size(); i++) {
            out.println((i + 1) + " " + entries.get(i));
        }
    }

    public String getPrevious() {
        if (entries.isEmpty()) { return null;}
        if (cursor == -1) cursor = entries.size() - 1;
        else if (cursor > 0) cursor--;
        else return null;
        return entries.get(cursor);
    }

    public String getNext() {
        if (entries.isEmpty() || cursor == -1) { return null;}
        if (cursor < entries.size() - 1) {
            cursor++;
            return entries.get(cursor);
        } else {
            cursor = -1;
            return null;
        }
    }





    
}
