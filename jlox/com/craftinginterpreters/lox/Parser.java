package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

// program        : declaration* EOF ;

// declaration    : classDecl
//                | funDecl
//                | varDecl
//                | statement ;

// classDecl      : "class" IDENTIFIER ( "<" IDENTIFIER )? "{" function* "}" ;

// funDecl        : "fun" function ;

// function       : IDENTIFIER "(" parameters? ")" block ;
// parameters     : IDENTIFIER ( "," IDENTIFIER )* ;

// varDecl        : "var" IDENTIFIER ( "=" expression )? ";" ;

// statement      : exprStmt
//                | ifStmt
//                | whileStmt
//                | forStmt
//                | printStmt
//                | block 
//                | breakStmt
//                | returnStmt
//                | continueStmt ;

// returnStmt     : "return" expression? ";" ;

// ifStmt         : "if" "(" expression ")" statement ("else" statement)? ;

// whileStmt      : "while" "(" expression ")" statement ;

// forStmt        : "for" "(" (varDecl | exprStmt | ";") expression? ";" expression? ) ")" statement ;

// block          : "{" declaration* "}";

// exprStmt       : expression ";" ;

// printStmt      : "print" expression ";" ;




// expression     : assigment;

// assigment      : (call ".")? IDENTIFIER "=" assignment
//                | logic_or;

// logic_or       : logic_and ("or" logic_and)* ;

// logic_and      : equality ("and" equality)* ;

// equality       : comparison ( ( "!=" | "==" ) comparison )* ;

// comparison     : term ( ( ">" | ">=" | "<" | "<=" ) term )* ;

// term           : factor ( ( "-" | "+" ) factor )* ;

// factor         : unary ( ( "/" | "*" ) unary )* ;

// unary          : ( "!" | "-" ) unary
//                | call ;

// call           : primary ( ("(" arguments? ")") | ("." IDENTIFIER) )*  ;

// arguments      : expression ("," expression)* ;

// primary        : NUMBER | STRING | "true" | "false" | "nil" | "this"
//                | "(" expression ")" | IDENTIFIER
//                | "super" "." IDENTIFIER;

public class Parser {
    private static class ParseError extends RuntimeException{}
    public final List<Token> tokens;
    public int current = 0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }
    
    List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration(){
        try {
            if (match(FUN)) return function("function");
            if (match(VAR)) return varDeclaration();
            if (match(CLASS)) return classDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt.Class classDeclaration(){
        Token name = consume(IDENTIFIER, "Expect class name.");

        Expr.Variable superclass = null;
        if (match(LESS)) {
            consume(IDENTIFIER, "Expect superclass name.");
            superclass = new Expr.Variable(previous());
        }

        consume(LEFT_BRACE, "Expect '{' before class body.");
        
        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("methods"));
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.");
        
        return new Stmt.Class(name, superclass, methods);
    }

    private Stmt.Function function(String kind){
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        while (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() > 255) {
                    error(peek(), "Can`t have more then 255 parameters.");
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();

        return new Stmt.Function(name, parameters, body);
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
        if (match(IF)) return ifStatement();
        if (match(RETURN)) return returnStatement();

        if (match(FOR)) return forStatement();
        if (match(WHILE)) return whileStatement();

        if (match(BREAK)) return breakStatement();
        if (match(CONTINUE)) return continueStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt returnStatement(){
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON,"Expect ';' after 'return' value.");
        return new Stmt.Return(keyword, value);
    }

    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE,"Expect '}' after block.");
        return statements;
    }

    private Stmt breakStatement(){
        consume(SEMICOLON,"Expect ';' after 'break' statement.");
        return new Stmt.Break(previous());   
    }

    private Stmt continueStatement(){
        consume(SEMICOLON,"Expect ';' after 'continue' statement.");
        return new Stmt.Continue(previous());   
    }

    private Stmt printStatement(){
        Expr expr = expression();
        consume(SEMICOLON,"Expect ';' after value.");
        return new Stmt.Print(expr);
    }

    private Stmt forStatement(){
        consume(LEFT_PAREN, "Expect '(' after while.");
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = new Stmt.Expression(new Expr.Literal(NIL));
        } else if(match(VAR)) {
            initializer = varDeclaration();
        }else{
            initializer = expressionStatement();
        }
        
        Expr condition = new Expr.Literal(true);
        if (!check(SEMICOLON)){
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' at after loop condition.");

        Expr increment = new Expr.Literal(NIL);
        if (!check(RIGHT_PAREN)){
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after clauses.");

        Stmt body = statement();
        
        return new Stmt.For(initializer, condition, increment, body);
    }

    private Stmt whileStatement(){
        consume(LEFT_PAREN, "Expect '(' after while.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after while condition.");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement(){
        consume(LEFT_PAREN, "Expect '(' after if.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
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
        Expr expr = or();
        
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assigment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }else if( expr instanceof Expr.Get ){
                Expr.Get get = ((Expr.Get)expr);
                return new Expr.Set(get.object, get.name, value);
            }

            error(equals, "Invalid assigment target.");
        }

        return expr;
    }

    private Expr or(){
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and(){
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
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
        return call();
    }

    private Expr call(){
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if(match(DOT)){
                Token name = consume(IDENTIFIER,"Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            }else{
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr expr){
        List<Expr> arguments = new ArrayList<>();

        while (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() > 255) {
                    error(peek(), "Can`t have more then 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");
        return new Expr.Call(expr, paren, arguments);
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

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        if (match(THIS)) {
            return new Expr.This(previous());
        }

        if (match(SUPER)) {
            Token keyword = previous();
            consume(DOT, "Expect '.' after super.");
            Token method = consume(IDENTIFIER, "expect superclass method name.");
            return new Expr.Super(keyword, method);
        }

        throw error(peek(), "Expect expression.");
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
