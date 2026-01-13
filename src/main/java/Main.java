import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import history.History;
import parse.ParsedCommand;
import parse.Parser;
import pipes.PipelineRunner;


// a good read for pipes and forks https://beej.us/guide/bgipc/
public class Main {

/*
    Using static: (learning)
    ✔ saves memory
    ✔ avoids creating objects unnecessarily
    ✔ makes constants accessible everywhere cleanly
    ✔ shows conceptually this value is global to the shell
    static means: this belongs to the CLASS, not to an OBJECT one for all
    */

    /*
    Can switch to printf 
    1) printf is formatting-aware - This scales cleanly.
    2) printf is platform-independent for newlines
    3) Performance (small advantage)
     */


    static class ShellState {
    boolean tabPending = false;
    String tabPrefix = null;
    List<String> tabMatches = null;

    void resetTab() {
        tabPending = false;
        tabPrefix = null;
        tabMatches = null;
    } 
    }
  
    private static final String PROMPT = "$ ";
    private static final String EXIT_COMMAND = "exit";
    private static final String ECHO_COMMAND = "echo";
    private static final String TYPE_COMMAND = "type";
    private static final String PWD_COMMAND = "pwd";
    private static final String CD_COMMAND = "cd";
    private static final String HISTORY_COMMAND = "history";
    private static final List<String> shellBullitin = List.of(PWD_COMMAND,EXIT_COMMAND,ECHO_COMMAND,TYPE_COMMAND,CD_COMMAND,HISTORY_COMMAND);
    private static final Parser PARSER = new Parser();
    // private static final List<String> HISTORY = new ArrayList<>();
    // private static int historyCursor = -1;
    // private static int historyAppendedUpTo = 0; 
    private static final History HISTORY = new History();

    


   
    public static void main(String[] args) throws Exception {
        // REPL - read eval print loop
        TerminalModeController.setRawMode();
        Runtime.getRuntime().addShutdownHook(new Thread(TerminalModeController::restoreTerminal));
        
        StringBuilder buffer = new StringBuilder();
        ShellState state = new ShellState();  
        System.out.print(PROMPT);
        System.out.flush();

        String histFile = System.getenv("HISTFILE");
        if (histFile != null) {
            // loadHistoryFromFile(histFile);
            HISTORY.loadIfExists(histFile);
        }


        while(true){
            int ch  = System.in.read();
            if (ch == -1) break;
            if (ch == '\n' || ch == '\r'){
                handleEnter(buffer, state);
                continue;
            }
            if (ch == '\t' ){
                if (buffer.indexOf(" ") == -1) {
                boolean doneSomethingVisible = handleTab(buffer, state);

                if (!doneSomethingVisible) {
                    System.out.print("\007");
                    System.out.flush();
                } else {
                    // only redraw if buffer changed due to single completion
                    // BUT: we can't distinguish “second-tab printed list” vs “single completion”
                    // easiest fix: have handleTab return an enum-like int (see below)
                    System.out.print("\r\033[2K");
                    System.out.print(PROMPT);
                    System.out.print(buffer);
                    System.out.flush();
                }
            } else {
                System.out.print("\007");
                System.out.flush();
            }
            continue;

            }
            if (ch == 127 || ch == 8){ // backspace or delete
                handleBackSpace(buffer, state);
                continue;
            }

            if(ch >= 32){
                state.resetTab();
                buffer.append((char) ch);
                System.out.print((char) ch);
                System.out.flush();
            }

            if (ch == 27) { // ESC
                int ch2 = System.in.read();
                int ch3 = System.in.read();

                if (ch2 == 91) { // '['
                    if (ch3 == 65) { // 'A' = UP
                        handleHistoryUp(buffer);
                        continue;
                    }
                    if (ch3 == 66) { // 'B' = DOWN
                        handleHistoryDown(buffer);
                        continue;
                    }
                }

                // Not an arrow sequence we handle
                System.out.print("\007");
                System.out.flush();
                continue;
            }
            
    }
}

    private static void runOneCommandLine(String input) throws Exception {

            ParsedCommand parsed = PARSER.parseCommand(input);
            
            if (parsed.args.length == 0) return; 
            String[] commandParts = parsed.args;
            String command = commandParts[0];

            PrintStream out = parsed.redirectStdout ? new PrintStream(new FileOutputStream(parsed.redirectFile, parsed.appendStdout)) : System.out;
            PrintStream err = parsed.redirectStderr ? new PrintStream(new FileOutputStream(parsed.stderrFile, parsed.appendStderr)) : System.err;

            if (input.contains("|")) {
                // runPipelineTwoCommands(input, out, err);
                // runPipeline(input, out, err);
                PipelineRunner.run(input, out, err, new PipelineRunner.BullitinRunner() {
                    @Override
                    public void run(ParsedCommand parsedCommand, PrintStream out, PrintStream err) throws IOException {
                        runBuiltin(parsedCommand, out, err);
                    }

                    @Override
                    public boolean isShellBuiltin(String commandName) {
                        return isBuiltin(commandName);
                    }
                }, PARSER);
                return;
            }

            // Evaluate command
            try{
            switch (command) {
                case EXIT_COMMAND:
                    // need to write logic to check if its the only work in the list
                    exitCommand(commandParts, err);
                    break; 
                case ECHO_COMMAND:
                    echoCommand(commandParts, out);
                    break;
                case TYPE_COMMAND:
                    typeCommand(commandParts, out, err);
                    break;
                case PWD_COMMAND:
                    pwd_command(out);
                    break;
                case CD_COMMAND:
                    cd_command(commandParts, err);
                    break;
                case HISTORY_COMMAND:
                    history_command(commandParts,out);
                    break;
                default:
                    externalCommand(commandParts,out,err);
                    break;
            }
            } catch (Exception e){
                err.println("Error executing command: " + e.getMessage());
            }
            finally{
                out.flush();
                err.flush();
                if (out != System.out) {out.close();}
                if (err != System.err) {err.close();}
            }
            
        }
      

    private static void exitCommand(String[] commandParts, PrintStream err){ 
        if (commandParts.length > 1) {
            err.println("exit: too many arguments");
            return;
        }
        String histFile = System.getenv("HISTFILE");
        if (histFile != null) {
            // appendHistoryToFile(histFile);
            HISTORY.appendToFile(histFile);
        }
        System.exit(0);
        
    }


    private static void echoCommand(String[] commandParts, PrintStream out){
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < commandParts.length; i++){
            message.append(commandParts[i]);
            if (i < commandParts.length - 1){
                message.append(" ");
            }
        }
        // System.out.println(message.toString());
        out.println(message.toString());
    }

    private static void typeCommand(String[] commandParts, PrintStream out, PrintStream err){
        if (commandParts.length != 2) {
            // System.out.println("type: invalid number of arguments");
            err.println("type: invalid number of arguments");
            return;
        }
        String secondaryCommand = commandParts[1];
        if(shellBullitin.contains(secondaryCommand)){
        // if(secondaryCommand.equals(ECHO_COMMAND) || secondaryCommand.equals(TYPE_COMMAND) || secondaryCommand.equals(EXIT_COMMAND)|| secondaryCommand.equals(PWD_COMMAND)){
            // if statment seems a little redundant but its ok for now.
            // System.out.println(secondaryCommand + " is a shell builtin");
            out.println(secondaryCommand + " is a shell builtin");
        } else {
            // now to check every directory in PATH for the command
            String pathEnv = System.getenv("PATH");
            String[] paths = pathEnv.split(System.getProperty("path.separator"));
            for (String path : paths) {
                File dir = new File(path);
                // check if command exists in this path
                File commandFile = new File(dir, secondaryCommand);
                // check if it has execute permissions
                if(commandFile.exists() && commandFile.canExecute()){
                    
                    // System.out.println(secondaryCommand + " is " + commandFile.getAbsolutePath());
                    out.println(secondaryCommand + " is " + commandFile.getAbsolutePath());
                    return;
                }
            }
            // System.out.println(secondaryCommand + ": not found");
            // out.println(secondaryCommand + ": not found");
            err.println(secondaryCommand + ": not found");
            
        }
    }
    
    private static void externalCommand(String[] commandParts, PrintStream out, PrintStream err){

        String executable = commandParts[0];
        // String pathEnv = System.getenv("PATH"); // im assuming this gets path from 
        // String[] paths = pathEnv.split(System.getProperty("path.separator"));

        // for(String path:paths){
        //     File dir = new File(path);
        //     File commandFile = new File (dir, commandParts[0]);
        //     if(commandFile.exists() && commandFile.canExecute()){

        File commandFile = findExecutableFile(executable);
        if(commandFile == null){
            err.println(executable + ": command not found");
            return;
        } else {
                try {
                    Process process = Runtime.getRuntime().exec(commandParts);
                    //output this
                    process.getInputStream().transferTo(out);
                    process.getErrorStream().transferTo(err);

                    process.waitFor();
                    return;
 
                } catch (Exception e) {
                    err.println(e.getMessage());
                    return;
                }
            }
        }


    private static void  pwd_command(PrintStream out){
        // so the shell always has a current working directory tracked by the OS.
        // System.out.println(System.getProperty("user.dir"));
        out.println(System.getProperty("user.dir"));
    }

    private static void cd_command(String[] commandParts, PrintStream err){
        File target;
        if(commandParts.length != 2){
            // System.out.println("cd: invalid number of arguments");
            err.println("cd: invalid number of arguments");
            return;
        }

        String path = commandParts[1];

        if(path.startsWith("~") || path.startsWith("~/")){
            String home = System.getenv("HOME");
            path = home + path.substring(1);}
        if (new File(path).isAbsolute()) {
            target = new File(path);
        }
        else {
            String currentDir = System.getProperty("user.dir");
            target = new File(currentDir, path);
        }

        try{
            File CanonicalFile = target.getCanonicalFile();
            if(!CanonicalFile.exists() || !CanonicalFile.isDirectory()){
                // System.out.println("cd: no such file or directory: " + path);
                err.println("cd: no such file or directory: " + path);
                return;
            
            }
            System.setProperty("user.dir", CanonicalFile.getAbsolutePath());
        } 
        catch (Exception e){
            // System.out.println("cd: error changing directory: " + e.getMessage()); 
            err.println("cd: error changing directory: " + e.getMessage());
            return;
        }
    }

    private static File findExecutableFile(String command){
        String pathEnv = System.getenv("PATH");
        String[] paths = pathEnv.split(System.getProperty("path.separator"));

        for (String path : paths) {
            File dir = new File(path);
            File commandFile = new File(dir, command);
            if(commandFile.exists() && commandFile.canExecute()){
                return commandFile;
            }
        }
        return null;
    }

    private static List<String> findExecutableCompletion(String prefix) {
    List<String> matches = new ArrayList<>();
    String pathEnv = System.getenv("PATH");
    if (pathEnv == null) return matches;

    String[] paths = pathEnv.split(System.getProperty("path.separator"));

    for (String dirPath : paths) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) continue;

        for (File f : files) {
            if (f.isFile()
                && f.canExecute()
                && f.getName().startsWith(prefix)) {
                String name = f.getName();
                if (name.startsWith(prefix)) matches.add(name);
            }
        }
    }
    return matches;
}

    private static void handleEnter(StringBuilder buffer, ShellState state){
        state.resetTab();
        System.out.print("\r\n");
        System.out.flush();

        String input = buffer.toString().trim();
        buffer.setLength(0);

            // if empty line, just show prompt again
        if (!input.isEmpty()) 
            HISTORY.addEntry(input);
            // historyCursor = -1;
            {
            try { runOneCommandLine(input);} 
            catch (Exception e) { System.err.println("Error: " + e.getMessage());}
            }
            // show prompt for next command
        System.out.print(PROMPT);
        System.out.flush();
    }

    private static boolean handleTab(StringBuilder buffer, ShellState state){
            String prefix = buffer.toString();

            if ("echo".startsWith(prefix) && !prefix.equals("echo")) {
                buffer.setLength(0);
                buffer.append("echo ");
                state.resetTab();
                return true;
        
            } 
            if ("exit".startsWith(prefix) && !prefix.equals("exit")) {
                buffer.setLength(0);
                buffer.append("exit ");
                state.resetTab();
                return true;
            }
             List<String> matches = findExecutableCompletion(prefix);
             Collections.sort(matches);

            if (matches.isEmpty()) {
                state.resetTab();
                return false; // main will bell
            }

            if (matches.size() == 1) {
            buffer.setLength(0);
            buffer.append(matches.get(0)).append(" ");
            state.resetTab();
            return true;
            }
            String lcp = longestCommonPrefix(matches);
            if (lcp.length() > prefix.length()) {
                buffer.setLength(0);
                buffer.append(lcp);
                state.resetTab();
                return true;
            }

            boolean samePrefix = state.tabPending && prefix.equals(state.tabPrefix);

            if (!samePrefix) {
                state.tabPending = true;
                state.tabPrefix = prefix;
                state.tabMatches = matches;
                return false; // main will bell
            }

            System.out.print("\r\n");
            for (int i = 0; i < state.tabMatches.size(); i++) {
                if (i > 0) System.out.print("  "); // two spaces
                System.out.print(state.tabMatches.get(i));
            }
            System.out.print("\r\n");
            System.out.print(PROMPT);
            System.out.print(prefix);
            System.out.flush(); 

            state.resetTab();
            return true;

        
    }
    
    private static void handleBackSpace(StringBuilder buffer, ShellState state){
        state.resetTab();
        if (buffer.length() > 0){
            buffer.setLength(buffer.length() - 1);
            System.out.print("\b \b");
            System.out.flush();
        }
    }

    private static String longestCommonPrefix(List<String> strings) {
        if (strings.isEmpty()) return "";

        String prefix = strings.get(0);
        for (int i = 1; i < strings.size(); i++) {
            String s = strings.get(i);
            int j = 0;
            while (j < prefix.length() && j < s.length() && prefix.charAt(j) == s.charAt(j)) {
                j++;
            }
            prefix = prefix.substring(0, j);
            if (prefix.isEmpty()) break;
        }
        return prefix;
    }


    private static boolean isBuiltin(String cmd) {
    return shellBullitin.contains(cmd);
    }
    
    private static void runBuiltin(ParsedCommand parsed, PrintStream out, PrintStream err) {
    String[] args = parsed.args;
    String cmd = args[0];

    switch (cmd) {
        case EXIT_COMMAND: exitCommand(args, err); break;
        case ECHO_COMMAND: echoCommand(args, out); break;
        case TYPE_COMMAND: typeCommand(args, out, err); break;
        case PWD_COMMAND:  pwd_command(out); break;
        case CD_COMMAND:   cd_command(args, err); break;
        case HISTORY_COMMAND: history_command(args,out); break;
        default:
            err.println(cmd + ": command not found");
        }
    }


    private static void history_command(String[] commandParts, PrintStream out){
         // default to all history
        if (commandParts.length == 1) {
            HISTORY.printAll(out);
            return;
        }
        if (commandParts.length == 2) {
                HISTORY.printLastN(out, Integer.parseInt(commandParts[1]));
                return;
           }
        
        if (commandParts.length == 3 && commandParts[1].equals("-r")) {
            HISTORY.readFromFile(commandParts[2]);
            // readHistoryFromFile(commandParts[2]);
            return;
        }
        if (commandParts.length >= 3 && commandParts[1].equals("-w")) {
            HISTORY.writeToFile(commandParts[2]);
            // writeHistoryToFile(commandParts[2]);
            return;
        }
        if (commandParts.length >= 3 && commandParts[1].equals("-a")) {
            HISTORY.appendToFile(commandParts[2]);
            // appendHistoryToFile(commandParts[2]);
            return;
        }
    }

    private static void redrawLine(StringBuilder buffer) {
        System.out.print("\r\033[2K");     // clear line
        System.out.print(PROMPT);
        System.out.print(buffer);
        System.out.flush();
    }
    
    private static void handleHistoryUp(StringBuilder buffer) {
        String line = HISTORY.getPrevious();
        if (line == null) { System.out.print("\007"); System.out.flush(); return; }
        buffer.setLength(0);
        buffer.append(line);
        redrawLine(buffer);
    }

    private static void handleHistoryDown(StringBuilder buffer) {
        String line = HISTORY.getNext();
        if (line == null) { System.out.print("\007"); System.out.flush(); return; }
        buffer.setLength(0);
        buffer.append(line);
        redrawLine(buffer);
    }

}
