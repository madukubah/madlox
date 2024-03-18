package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Scanner {
    private final String source;    
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final HashMap<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",     AND);
        keywords.put("class",   CLASS);
        keywords.put("else",    ELSE);
        keywords.put("false",   FALSE);
        keywords.put("fun",     FUN);
        keywords.put("for",     FOR);
        keywords.put("if",      IF);
        keywords.put("nil",     NIL);
        keywords.put("or",      OR);
        keywords.put("print",   PRINT);
        keywords.put("return",  RETURN);
        keywords.put("super",   SUPER);
        keywords.put("this",    THIS);
        keywords.put("true",    TRUE);
        keywords.put("var",     VAR);
        keywords.put("while",   WHILE);
        keywords.put("break",   BREAK);
        keywords.put("continue",CONTINUE);
    }
    
    Scanner(String source){
        this.source = source;
    }
    
    List<Token> scanTokens(){
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken(){
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '/': 
                if (match('/')) {
                    while ( peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) { // C-style comment block
                    blockComment();
                } else {
                    addToken(SLASH); 
                }
                break;
            case '*': addToken(STAR); break;
            case '!': 
                addToken(match('=')? BANG_EQUAL : BANG); 
                break;
            case '=': 
                addToken(match('=')? EQUAL_EQUAL : EQUAL); 
                break;
            case '>': 
                addToken(match('=')? GREATER_EQUAL : GREATER); 
                break;
            case '<': 
                addToken(match('=')? LESS_EQUAL : LESS); 
                break;
            case ' ':
            case '\t':
            case '\r':
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            default:
                if (isDIgit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                }else {
                    Lox.error(line, "Unexpected character.");
                }
            break;
        }
    }

    private void blockComment(){
        while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
            advance();
        }
        if (isAtEnd()) {
            Lox.error(line, "Unexpected end of comment");
        }else{
            //closing */
            advance();
            advance();
        }
    }

    private void identifier(){
        while (isAlphaNumeric(peek()))advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if(type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number(){
        while (isDIgit(peek()))advance();

        if (peek() == '.' && isDIgit(peekNext())) {
            advance();
            while (isDIgit(peek()))advance();
        }

        Double value = Double.parseDouble( source.substring(start, current) );
        addToken(NUMBER, value);
    }

    private void string(){
        while (peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated String.");
            return;
        }
        
        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start+1, current-1);
        addToken(STRING, value);
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd(){
        return current >= source.length();
    }

    private char advance(){
        return source.charAt(current++);
    }

    private boolean match(char expected){
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek(){
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext(){
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }

    private boolean isDIgit(char c){
        return '0' <= c && c <= '9';
    }

    private boolean isAlpha(char c){
        return  ('a' <= c && c <= 'z') ||
                ('A' <= c && c <= 'Z') || 
                (c == '_');
    }

    private boolean isAlphaNumeric(char c){
        return  isAlpha(c) || isDIgit(c);
    }
}
