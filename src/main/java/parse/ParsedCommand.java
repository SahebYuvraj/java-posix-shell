package parse;

public final class ParsedCommand {
    public final String[] args;
    public final boolean redirectStdout;
    public final boolean redirectStderr;
    public final boolean appendStdout;
    public final boolean appendStderr;
    public final String redirectFile;
    public final String stderrFile;

    public ParsedCommand(
            String[] args,
            boolean redirectStdout,
            boolean redirectStderr,
            boolean appendStdout,
            boolean appendStderr,
            String redirectFile,
            String stderrFile
    ) {
        this.args = args;
        this.redirectStdout = redirectStdout;
        this.redirectStderr = redirectStderr;
        this.appendStdout = appendStdout;
        this.appendStderr = appendStderr;
        this.redirectFile = redirectFile;
        this.stderrFile = stderrFile;
    }
}
