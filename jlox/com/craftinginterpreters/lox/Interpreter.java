package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Unary;

public class Interpreter implements Expr.Visitor<Object> {

    void interpret(Expr expr){
        try {
            Object value =  evaluate(expr);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                if (left instanceof String && right instanceof Double) {
                    right = String.valueOf((double)right);
                    if(((String) right).endsWith(".0")) right = ((String) right).substring(0, ((String) right).length()-2);
                    return (String)left + (String)right;
                }
                if (left instanceof Double && right instanceof String) {
                    left = String.valueOf((double)left);
                    if(((String) left).endsWith(".0")) left = ((String) left).substring(0, ((String) left).length()-2);
                    return (String)left + (String)right;
                }
                
                throw new RuntimeError(expr.operator, "Operand must be two numbers or two strings or mix of it.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if(((double)right) == 0)
                    throw new RuntimeError(expr.operator, "Division by zero.");
                    
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
        }

        return null;
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }
        return null;
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    private Object evaluate(Expr expr){
        return expr.accept(this);
    }

    private Boolean isTruthy(Object obj){
        if(obj == null) return false;
        if(obj instanceof Boolean) return (boolean)obj;
        return true;
    }

    private Boolean isEqual(Object a, Object b){
        if(a == null && b == null) return true;
        if(a == null) return false;
        return a.equals(b);
    }

    private String stringify(Object obj){
        if(obj == null) return "nil";
        if (obj instanceof Double) {
            String text = obj.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length()-2);
            } 
            return text;
        }
        return obj.toString();
    }

    private Boolean checkNumberOperand(Token operator, Object operand){
        if(operand instanceof Double) return true;
        throw new RuntimeError(operator, "Operand must be a number.");
    }
    
    private Boolean checkNumberOperands(Token operator, Object left, Object right){
        if(left instanceof Double && right instanceof Double) return true;
        throw new RuntimeError(operator, "Operand must be a numbers.");
    }
    
}
