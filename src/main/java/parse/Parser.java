package parse;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    public ParsedCommand parseCommand(String input){
        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        boolean insideSingleQuote = false;
        boolean insideDoubleQuote = false;
        boolean redirectStdout = false;
        String redirectFile = null;
        boolean redirectStderr = false;
        String stderrFile = null;
        boolean appendStdout = false;
        boolean appendStderr = false; 

        for(int i= 0; i < input.length(); i++){
            char c = input.charAt(i);

            if(c == '\''){
                if(insideDoubleQuote) {
                    currentPart.append(c);
                    continue;
                }
                insideSingleQuote = !insideSingleQuote;
                continue;
            }

            if(c == '"' && !insideSingleQuote){
                insideDoubleQuote = !insideDoubleQuote;
                continue;
            }

            if (!insideDoubleQuote && !insideSingleQuote && (c == '2' && i + 1 < input.length() && input.charAt(i + 1) == '>')){
                redirectStderr = true;
                 // skip '>'
                if (currentPart.length() > 0) {
                    parts.add(currentPart.toString());
                    currentPart.setLength(0);
                }

                i++;
                i++;
                if (i < input.length() && input.charAt(i) == '>') {
                    appendStderr = true;
                    i++; // skip second '>'
                }
                
                while (i < input.length() && input.charAt(i) == ' ') i++;

                StringBuilder file = new StringBuilder();
                while (i < input.length() && input.charAt(i) != ' ') {
                    file.append(input.charAt(i));
                    i++;
                }

                stderrFile = file.toString();
                break;

                
            }
            if (!insideSingleQuote && !insideDoubleQuote &&
                    (c == '>' || (c == '1' && i + 1 < input.length() && input.charAt(i + 1) == '>'))) {

                redirectStdout = true;
                if (c == '1') i++;

                if (i + 1 < input.length() && input.charAt(i + 1) == '>') {
                appendStdout = true;
                i++; // skip second '>'
                }

                if (currentPart.length() > 0) {
                    parts.add(currentPart.toString());
                    currentPart.setLength(0);
                }

                i++;
                while (i < input.length() && input.charAt(i) == ' ') i++;

                StringBuilder file = new StringBuilder();
                while (i < input.length() && input.charAt(i) != ' ') {
                    file.append(input.charAt(i));
                    i++;
                }

                redirectFile = file.toString();
                break;
            }

            

            // basically check / escape character
            if(c == '\\'){
                if(i + 1 < input.length()){
                    char nextChar = input.charAt(i + 1);
                    if(insideSingleQuote || (insideDoubleQuote && (nextChar != '"' && nextChar != '\\'))){
                        currentPart.append(c);
                    } else {
                        currentPart.append(nextChar);
                        i++; // skip next char as its escaped
                        continue;
                    }
                } else {
                    currentPart.append(c);
                }
                continue;
            }

            if(c == ' ' && !insideSingleQuote && !insideDoubleQuote){
                if(currentPart.length() > 0){
                    parts.add(currentPart.toString());
                    currentPart.setLength(0);
                }
            } else {
                currentPart.append(c);
            }

            
        }
        // TO DO: implement parsing logic to handle quotes and escapes

        if(currentPart.length() > 0){
            parts.add(currentPart.toString());
        }

    
        return new ParsedCommand(  
            parts.toArray(new String[0]),
            redirectStdout,
            redirectStderr,
            appendStdout,
            appendStderr,
            redirectFile,
            stderrFile

        );
    }
}
