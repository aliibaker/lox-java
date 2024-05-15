package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

// Our programming language
public class Lox {
    // Flag that tracks if we encountered an error
    static boolean hadError = false; 

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    // Runs when we give Lox a file to interpret 
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if(hadError) System.exit(65);
    }

    // When we want to run it interactively and interpret
    // as we type
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        // Loops until end of file
        for(;;) {
            System.out.print("> ");
            String line = reader.readLine();
            // If reaches EOF, or hit CTRL + D
            if (line == null) break;
            run(line);
            // Rest when user makes an error in this mode
            // Want them to keep writing code even if things
            // didn't work.
            hadError = false;
        }
    }

    // What runFile() and runPrompt() wrap around 
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    // We don't have the error reporter in the scanner
    // because it's general good practice to seperate 
    // the error reporter from what causes errors. 
    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line]" + line + "] Error" 
            + where + ": " + message);
        hadError = true;
    }
}