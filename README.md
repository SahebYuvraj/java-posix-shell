# Java POSIX-Style Shell

[![CodeCrafters Progress](https://backend.codecrafters.io/progress/shell/b44b235b-1bd1-48ce-8bbb-62ea23b39c6c)](https://app.codecrafters.io/users/SahebYuvraj?r=2qF)

A POSIX-style command-line shell implemented in Java.  
This project was built as part of the CodeCrafters **“Build Your Own Shell”** challenge, with a focus on understanding core systems concepts rather than production-level polish.

---

## Overview

This shell is capable of parsing and executing commands in an interactive REPL environment. It supports built-in commands, execution of external programs, pipelines, redirection, and persistent command history.

The goal of this project was to gain hands-on experience with:
- Shell command parsing
- Process execution
- Standard input/output redirection
- Pipelines
- Terminal raw mode handling
- State management in a long-running REPL

---

## Features

- Interactive REPL
- Built-in commands (`cd`, `pwd`, `echo`, `exit`)
- Execution of external programs
- Input/output redirection (`>`, `>>`, `2>`)
- Pipelines (`|`)
- Persistent command history
- Raw terminal mode handling

---

## Project Structure

```text
src/main/java/
├── Main.java                    # Entry point and REPL loop
├── Parser.java                  # Command parsing logic
├── ParsedCommand.java           # Parsed command representation
├── Builtins.java                # Built-in shell commands
├── PipelineRunner.java          # Pipeline execution
├── History.java                 # Command history handling
└── TerminalModeController.java  # Terminal raw mode management
```

## Running Locally

### Requirements
- Java 17+
- Maven

### Run
- git clone https://github.com/<your-username>/codecrafters-shell-java.git
- cd codecrafters-shell-java
- mvn clean package
```bash
./your_program.sh
```

Design Notes
- The shell uses a static-oriented design to simplify global shell state management.
- Command parsing and execution are separated to keep responsibilities clear.
- Error handling is intentionally minimal to match typical shell behavior.
- This implementation prioritizes correctness and learning over extensibility or performance optimizations.
- Limitations & Future Improvements
- More robust error reporting and diagnostics
- Improved parser to handle complex quoting and escaping
- Job control (fg, bg, signals)
- Refactoring toward a more instance-based architecture for extensibility
