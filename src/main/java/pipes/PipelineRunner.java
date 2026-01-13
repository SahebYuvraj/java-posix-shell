package pipes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import parse.ParsedCommand;
import parse.Parser;


public class PipelineRunner {

    //callback via interface
    public interface BullitinRunner {
        void run(ParsedCommand parsedCommand, PrintStream out, PrintStream err) throws IOException;
        boolean isShellBuiltin(String commandName);

    }

    private static Thread pump(InputStream in, OutputStream out, boolean closeOut) {
        Thread thread = new Thread(() -> {
            byte[] buffer = new byte[8192];
            int bytesRead;
            try {
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                }
            } catch (IOException ignored) {
            } finally {
                try { in.close(); } catch (IOException ignored) {}
                if (closeOut) {
                    try { out.close(); } catch (IOException ignored) {}
                }
            }
        });
        thread.start();
        return thread;
    }

    public static void run(String input, PrintStream out, PrintStream err, BullitinRunner builtinRunner, Parser parser) throws Exception {
        String[] segments = input.split("\\|");

        if (segments.length == 2) {
            runTwoCommands(input, out, err, builtinRunner, parser);
            return;
        }
    
        List<ParsedCommand> commands = new ArrayList<>();
        for (String seg : segments) {
            ParsedCommand pc = parser.parseCommand(seg.trim());
            if (pc.args.length == 0) return;
            commands.add(pc);
        }

        List<Process> processes = new ArrayList<>();
        for (ParsedCommand pc : commands) {
            ProcessBuilder pb = new ProcessBuilder(pc.args);
            pb.directory(new File(System.getProperty("user.dir")));
            processes.add(pb.start());
        }

    
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < processes.size() - 1; i++) {
            Process a = processes.get(i);
            Process b = processes.get(i + 1);
            threads.add(pump(a.getInputStream(), b.getOutputStream(), true));
        }

        for (Process process : processes) {
            threads.add(pump(process.getErrorStream(), err, false));
        }

        Process lastProcess = processes.get(processes.size() - 1);
        threads.add(pump(lastProcess.getInputStream(), out, false));

        processes.get(0).getOutputStream().close();

        for (Process process : processes) {
            process.waitFor();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }

    public static void runTwoCommands(String input, PrintStream out, PrintStream err, BullitinRunner builtinRunner, Parser parser) throws Exception {

        String[] commandParts = input.split("\\|", 2);
        if (commandParts.length != 2) {
            err.println("Invalid pipeline command");
            return;
        }

        ParsedCommand left = parser.parseCommand(commandParts[0].trim());
        ParsedCommand right = parser.parseCommand(commandParts[1].trim());

        boolean leftBuiltin = builtinRunner.isShellBuiltin(left.args[0]);
        boolean rightBuiltin = builtinRunner.isShellBuiltin(right.args[0]);

        if(!leftBuiltin && !rightBuiltin){
            ProcessBuilder pb1 = new ProcessBuilder(left.args);
            ProcessBuilder pb2 = new ProcessBuilder(right.args);

            pb1.directory(new File(System.getProperty("user.dir")));
            pb2.directory(new File(System.getProperty("user.dir")));

            Process p1 = pb1.start();
            Process p2 = pb2.start();

            Thread t1 = pump(p1.getInputStream(), p2.getOutputStream(), true);
            Thread t2 = pump(p1.getErrorStream(), err, false);
            Thread t3 = pump(p2.getErrorStream(), err, false);
            Thread t4 = pump(p2.getInputStream(), out, false);


            p1.getOutputStream().close();

            p1.waitFor();
            p2.getOutputStream().close();
            p2.waitFor();

            t1.join();
            t2.join();
            t3.join();
            t4.join();
           
        } 
        if (leftBuiltin && rightBuiltin) {
            ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
            PrintStream discardStream = new PrintStream(capturedOutput);
            builtinRunner.run(left, discardStream, err);
            discardStream.flush();
            builtinRunner.run(right, out, err);
            return;
        }
        if (leftBuiltin && !rightBuiltin) {
            ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
            PrintStream captureStream = new PrintStream(capturedOutput);
            builtinRunner.run(left, captureStream, err);
            captureStream.flush();

            ProcessBuilder pb2 = new ProcessBuilder(right.args);
            pb2.directory(new File(System.getProperty("user.dir")));
            Process p2 = pb2.start();

            // Pump left output to right input
            Thread t1 = pump(new java.io.ByteArrayInputStream(capturedOutput.toByteArray()), p2.getOutputStream(), true);
            Thread t2 = pump(p2.getErrorStream(), err, false);
            Thread t3 = pump(p2.getInputStream(), out, false);

            p2.getOutputStream().close();
            p2.waitFor();

            t1.join();
            t2.join();
            t3.join();
        }
        if (!leftBuiltin && rightBuiltin) {
            ProcessBuilder pb1 = new ProcessBuilder(left.args);
            pb1.directory(new File(System.getProperty("user.dir")));
            Process p1 = pb1.start();
            
            ByteArrayOutputStream ignored = new ByteArrayOutputStream();
            Thread drainStdout = pump(p1.getInputStream(), ignored, true);
            Thread drainStderr = pump(p1.getErrorStream(), err, false);

            p1.waitFor();
            drainStdout.join();
            drainStderr.join();

            // Now run builtin RHS normally
            builtinRunner.run(right, out, err);
            return;
        }   
    }
}
