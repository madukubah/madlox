package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.TokenType.EOF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Lox{
    private static final Interpreter interpreter = new Interpreter();
    static boolean replMode = false;
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            replMode = true;
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        String source = new String(bytes, Charset.defaultCharset());
        run(source);
        if(hadError) System.exit(65);
        if(hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            String line = "";
            String endChar = " ";
            while( !endChar.equals(";")){
                System.out.print("> ");
                line += reader.readLine();

                if (line != null) endChar = line.substring(line.length()-1);
            }
            
            if (line == null)
                break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        // Use a custom lexer to tokenize the input
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        // Expr expession = parser.parse();
        List<Stmt> statements = parser.parse();

        if (replMode) {
            for(int i=0; i< statements.size(); i++){
                if (statements.get(i) instanceof Stmt.Expression) {
                    statements.set(i, new Stmt.Print(((Stmt.Expression) statements.get(i)).expression));
                }
            }
        }

        if(hadError) return;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);
        
        if(hadError) return;
        
        interpreter.interpret(statements);
    }

    static void error(int line, String message){
        report(line, "", message);
    }

    private static void report(int line, String where, String message){
        System.err.println(
            "[line "+line+"] Error " + where + ": " + message
        );
        hadError = true;
    }

    static void error(Token token, String message){
        if (token.type == EOF) {
            report(token.line, " at end", message);
        }else{
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error){
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
