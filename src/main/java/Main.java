import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // REPL - read eval print loop
        while(true){

            // Display prompt
            System.out.print("$ ");

            // Read user input -- all inputs are treated as unknown commands
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();
            
            if (command.equals("exit")) break;

            if(command.startsWith("echo ")) {
                String message = command.substring(5);
                System.out.println(message);
                continue;
            }
            if (command.startsWith("type")){
                String typeArg = command.substring(5).trim();
                if (typeArg.equals("echo")|| typeArg.equals("type")|| typeArg.equals("exit")){
                    System.out.println(typeArg + " is a built-in command");
                } else {
                    System.out.println(typeArg + " is an unknown command");
                }
            }
            System.out.println(command+": command not found");

            
        }
      
    }
}
