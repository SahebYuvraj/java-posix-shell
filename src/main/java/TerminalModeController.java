public class TerminalModeController {

    public static void setRawMode() {
        String[] cmd = {"/bin/sh", "-c", "stty -echo -icanon min 1 < /dev/tty"};
        try {
            Runtime.getRuntime().exec(cmd).waitFor();
        } catch (Exception ignored) {}
    }

    public static void restoreTerminal() {
        String[] cmd = {"/bin/sh", "-c", "stty sane < /dev/tty"};
        try {
            Runtime.getRuntime().exec(cmd).waitFor();
        } catch (Exception ignored) {}
    }
    
}
