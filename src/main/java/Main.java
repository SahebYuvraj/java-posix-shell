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
            System.out.println(command+": command not found");

            
        }
      
    }
}
