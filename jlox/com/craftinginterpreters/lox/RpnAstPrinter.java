package com.craftinginterpreters.lox;

import java.util.Arrays;
import java.util.List;

import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Unary;

public class RpnAstPrinter implements Expr.Visitor<String> {

    String print(Expr expr){
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Binary expr) {
        return parenthesize(Arrays.asList(expr.left, expr.right), expr.operator.lexeme);
    }

    @Override
    public String visitGroupingExpr(Grouping expr) {
        return parenthesize(Arrays.asList(expr.expression), "");
    }

    @Override
    public String visitLiteralExpr(Literal expr) {
        if(expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Unary expr) {
        return parenthesize(Arrays.asList(expr.right), expr.operator.lexeme);
    }

    private String parenthesize(List<Expr> exprs, String name){
        StringBuilder builder = new StringBuilder();

        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(" ").append(name);

        return builder.toString();
    }
    
    public static void main(String[] args){
        Expr expression = new Binary(
            new Grouping(
                new Binary(
                    new Literal(1), 
                    new Token(TokenType.PLUS, "+", null, 1), 
                    new Literal(2))
            ),
            new Token(TokenType.STAR, "*", null, 1),
            new Grouping(
                new Binary(
                    new Literal(4), 
                    new Token(TokenType.MINUS, "-", null, 1), 
                    new Literal(3))
            ));
        System.out.println(new RpnAstPrinter().print(expression));
    }
}
