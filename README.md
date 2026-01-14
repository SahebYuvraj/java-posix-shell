# Java POSIX-Style Shell

[![CodeCrafters Progress](https://backend.codecrafters.io/progress/shell/b44b235b-1bd1-48ce-8bbb-62ea23b39c6c)](https://app.codecrafters.io/users/SahebYuvraj?r=2qF)

A POSIX-style command-line shell implemented in Java.  
Built as part of the CodeCrafters **“Build Your Own Shell”** challenge, this project focuses on understanding how real shells work under the hood: parsing, process execution, pipes, redirection, and terminal behavior.

This is a learning-first systems project rather than a production-ready shell.

---

## Description

This project implements a simplified POSIX-like shell that runs in an interactive REPL.  
Users can execute external programs, use built-in commands, chain commands using pipelines, redirect output and errors, and navigate command history using the keyboard.

The shell is designed to mirror real shell behavior closely enough to expose the underlying systems concepts, while keeping the implementation readable and debuggable.

---

## What users can do

- Run external programs (e.g. `ls`, `cat`, `grep`)
- Use built-in commands:
  - `cd`
  - `pwd`
  - `echo`
  - `exit`
- Chain commands using pipelines (`|`)
- Redirect output and errors:
  - `>` overwrite output
  - `>>` append output
  - `2>` redirect stderr
- Navigate command history with arrow keys
- Persist command history across sessions
- Use the shell in raw terminal mode for real-time input handling

---

## Features

- Interactive REPL
- Built-in command handling
- External command execution
- Input/output/error redirection
- Pipelines
- Persistent command history
- Raw terminal mode support

---

## Topics covered

This project touches most core shell and systems topics, including:

- Command parsing and tokenization
- Process creation and execution
- Standard input, output, and error streams
- Pipes and inter-process communication
- Terminal raw mode and key handling
- State management in long-running processes
- Error propagation and shell-style failure handling
- Git-based iterative development

---

## How I built it (process)

1. **Started with a minimal REPL**
   - Read input line-by-line
   - Execute simple external commands

2. **Added parsing and structure**
   - Parsed commands into structured representations
   - Separated parsing from execution logic

3. **Implemented built-ins**
   - Added `cd`, `pwd`, `echo`, and `exit`
   - Handled shell state explicitly

4. **Introduced redirection and pipelines**
   - Implemented stdout/stderr redirection
   - Added multi-stage pipelines using pipes

5. **Improved UX**
   - Added persistent history
   - Implemented arrow-key navigation
   - Switched terminal into raw mode

6. **Refactored for clarity**
   - Cleaned up responsibilities
   - Prepared the project for public release on GitHub

---

## What I learned

- How shells actually execute commands under the hood
- Why parsing is one of the hardest parts of shell design
- How pipes and file descriptors work in practice
- The complexity hidden behind “simple” terminal features
- How to debug long-running interactive programs
- How to manage state safely in a REPL-style application

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
- git git clone https://github.com/<your-username>/java-posix-shell.git
- cd java-posix-shell
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

### CodeCrafters Ranking

At the time of completion, this solution ranked **#154 globally** on the CodeCrafters leaderboard for the *Build Your Own Shell* challenge.
