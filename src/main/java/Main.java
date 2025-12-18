import java.io.File;
import java.util.List;
import java.util.Scanner;

public class Main {

    /*
    Using static: (learning)
    ✔ saves memory
    ✔ avoids creating objects unnecessarily
    ✔ makes constants accessible everywhere cleanly
    ✔ shows conceptually this value is global to the shell
    */

    /*
    Can switch to printf 
    1) printf is formatting-aware - This scales cleanly.
    2) printf is platform-independent for newlines
    3) Performance (small advantage)
     */

    private static final String PROMPT = "$ ";
    private static final String EXIT_COMMAND = "exit";
    private static final String ECHO_COMMAND = "echo";
    private static final String TYPE_COMMAND = "type";
    private static final String PWD_COMMAND = "pwd";
    private static final String CD_COMMAND = "cd";
    private static final List<String> shellBullitin = List.of(PWD_COMMAND,EXIT_COMMAND,ECHO_COMMAND,TYPE_COMMAND,CD_COMMAND);
   

    public static void main(String[] args) throws Exception {
        // REPL - read eval print loop
        while(true){

            // Display prompt
            System.out.print(PROMPT);

            // Read user input -- all inputs are treated as unknown commands
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();
            String[] commandParts = input.split(" ");
            String command = commandParts[0];

            // Evaluate command
            switch (command) {
                case EXIT_COMMAND:
                    // need to write logic to check if its the only work in the list
                    exitCommand(commandParts);
                    break; 
                case ECHO_COMMAND:
                    echoCommand(commandParts);
                    break;
                case TYPE_COMMAND:
                    typeCommand(commandParts);
                    break;
                case PWD_COMMAND:
                    pwd_command();
                    break;
                case CD_COMMAND:
                    cd_command(commandParts);
                    break;
                default:
                    externalCommnad(commandParts);
                    break;
            }
            
        }
      
    }
   

    private static void exitCommand(String[] commandParts) {
        if (commandParts.length > 1) {
            System.out.println("exit: too many arguments");
            return;
        }
        System.exit(0);
    }


    private static void echoCommand(String[] commandParts){
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < commandParts.length; i++){
            message.append(commandParts[i]);
            if (i < commandParts.length - 1){
                message.append(" ");
            }
        }
        System.out.println(message.toString());
    }

    private static void typeCommand(String[] commandParts){
        if (commandParts.length != 2) {
            System.out.println("type: invalid number of arguments");
            return;
        }
        String secondaryCommand = commandParts[1];
        if(shellBullitin.contains(secondaryCommand)){
        // if(secondaryCommand.equals(ECHO_COMMAND) || secondaryCommand.equals(TYPE_COMMAND) || secondaryCommand.equals(EXIT_COMMAND)|| secondaryCommand.equals(PWD_COMMAND)){
            // if statment seems a little redundant but its ok for now.
            System.out.println(secondaryCommand + " is a shell builtin");
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
                    
                    System.out.println(secondaryCommand + " is " + commandFile.getAbsolutePath());
                    return;
                }
            }
            System.out.println(secondaryCommand + ": not found");
            
        }
    }
    
    private static void externalCommnad(String[] commandParts){

        String executable = commandParts[0];
        String pathEnv = System.getenv("PATH"); // im assuming this gets path from 
        String[] paths = pathEnv.split(System.getProperty("path.separator"));

        for(String path:paths){
            File dir = new File(path);
            File commandFile = new File (dir, commandParts[0]);
            if(commandFile.exists() && commandFile.canExecute()){
                try {

                    Process process = Runtime.getRuntime().exec(commandParts);
                    //output this
                    process.getInputStream().transferTo(System.out);
                    process.getErrorStream().transferTo(System.err);

                    process.waitFor();
                    return;

                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    return;
                }
            }
        }

        System.out.println(executable + ": command not found");         
    }

    private static void  pwd_command(){
        // so the shell always has a current working directory tracked by the OS.
        System.out.println(System.getProperty("user.dir"));
    }

    private static void cd_command(String[] commandParts){
        if(commandParts.length != 2){
            System.out.println("cd: invalid number of arguments");
            return;
        }

        String path = commandParts[1];
        // File dir = new File(path);
        // if(!dir.exists() || !dir.isDirectory()){
        //     System.out.println("cd: no such file or directory: " + path);
        //     return;
        // }
        // System.setProperty("user.dir", dir.getAbsolutePath()); // this sets the system property to absolute directory


        if(path.startsWith("/")){ absolutepath(path);}
        else if (path.startsWith("./")){// this path 
            String current_directory = System.getProperty("user.dir");
            String new_path = current_directory + path.substring(1);
            absolutepath(new_path);
            }
        else if (path.startsWith("../")){ 
            parentrelativedir(path);
            //previous directory
        }
        else if (path.startsWith("~")){
            String home_directory = System.getProperty("user.home");
            String new_path = home_directory + path.substring(1);
            absolutepath(new_path);
            // home directory
        }
        // // relative paths
        // so absolute is /
        // relative is ./ or ../
        // home ditectory is ~


    }
    private static void absolutepath(String path){
        File dir = new File(path);
        if(!dir.exists() || !dir.isDirectory()){
            System.out.println("cd: no such file or directory: " + path);
            return;
        }
        System.setProperty("user.dir", dir.getAbsolutePath()); // this sets the system property to absolute directory

    }

    private static void parentrelativedir(String path){
        while (path.startsWith("../")){
            String current_directory = System.getProperty("user.dir");
            File currDirFile = new File(current_directory);
            String parent_directory = currDirFile.getParent();
            String new_path = parent_directory + path.substring(2);
            path = new_path;
        }

        absolutepath(path);


    }

}
