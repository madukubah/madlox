package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;
import static com.craftinginterpreters.lox.TokenType.*;

// program        declaration* EOF ;
// declaration    varDecl
//                | statement ;
// statement      exprStmt
//                | printStmt
//                | block ;
// block          "{" declaration* "}";
// varDecl        "var" IDENTIFIER ( "=" expression )? ";" ;
// exprStmt       expression ";" ;
// printStmt      "print" expression ";" ;
// expressions    expression ( (",") expression)* ;
// expression     assigment;
// assigment      IDENTIFIER "=" assignment
//                | equality
// equality       comparison ( ( "!=" | "==" ) comparison )* ;
// comparison     term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
// term           factor ( ( "-" | "+" ) factor )* ;
// factor         unary ( ( "/" | "*" ) unary )* ;
// unary          ( "!" | "-" ) unary
//                | primary ;
// primary        NUMBER | STRING | "true" | "false" | "nil"
//                | "(" expression ")" ;

public class Parser {
    private static class ParseError extends RuntimeException{}

    public final List<Token> tokens;
    public int current = 0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    // Expr parse(){
    //     try {
    //         return expressions();
    //     } catch (ParseError e) {
    //         return null;
    //     }
    // }
    
    List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration(){
        try {
            if (match(VAR)) {
                return varDeclaration();
            }
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration(){
        Token name = consume(IDENTIFIER,"Expect variable name");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON,"Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);

    }

    private Stmt statement(){
        if (match(PRINT)) return printStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE,"Expect '}' after block.");
        return statements;
    }

    private Stmt printStatement(){
        Expr expr = expression();
        consume(SEMICOLON,"Expect ';' after value.");
        return new Stmt.Print(expr);
    }

    private Stmt expressionStatement(){
        Expr expr = expression();
        consume(SEMICOLON,"Expect ';' after value.");
        return new Stmt.Expression(expr);
    }

    // depracated
    private Expr expressions(){
        Expr expr = expression();

        while (match(COMMA)){
            Token operator = previous();
            Expr right = expression();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr expression(){
        return assigment();
    }

    private Expr assigment(){
        Expr expr = equality();
        
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assigment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assigment target.");
        }

        return expr;
    }

    private Expr equality(){
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;

    }

    private Expr comparison(){
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term(){
        Expr expr = factor();

        while (match(PLUS, MINUS)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor(){
        Expr expr = unary();

        while (match(STAR, SLASH)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary(){
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary(){
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(NIL)) return new Expr.Literal(null);
        
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PARENT)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PARENT, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression");
    }

    private Token advance(){
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean match(TokenType... types){
        for (TokenType tokenType : types) {
            if (check(tokenType)) {
                advance();
                return true;
            }   
        }
        return false;
    }

    private Token consume(TokenType type, String message){
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private boolean check(TokenType type){
        if(isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean isAtEnd(){
        return peek().type == TokenType.EOF;   
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token previous(){
        return tokens.get(current-1);
    }

    private ParseError error(Token token, String message){
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize(){
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) {
                return;
            }
            
            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }

}
