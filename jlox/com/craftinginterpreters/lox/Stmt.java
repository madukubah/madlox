package com.craftinginterpreters.lox;

import java.util.List;

abstract class Stmt {
    interface Visitor<R>{
        R visitClassStmt(Class stmt);
        R visitBlockStmt(Block stmt);
        R visitExpressionStmt(Expression stmt);
        R visitFunctionStmt(Function stmt);
        R visitReturnStmt(Return stmt);
        R visitPrintStmt(Print stmt);
        R visitIfStmt(If stmt);
        R visitVarStmt(Var stmt);
        R visitWhileStmt(While stmt);
        R visitForStmt(For stmt);
        R visitBreakStmt(Break stmt);
        R visitContinueStmt(Continue stmt);
    }
    static class Class extends Stmt {
        Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods){
            this.name = name;
            this.superclass = superclass;
            this.methods = methods;
        }
        final Token name;
        final Expr.Variable superclass;
        final List<Stmt.Function> methods;

        @Override
        <R> R accept(Visitor<R> visitor){
           return visitor.visitClassStmt(this);
        }
    }
    static class Block extends Stmt {
        Block(List<Stmt> statements){
            this.statements = statements;
        }
        final List<Stmt> statements;

        @Override
        <R> R accept(Visitor<R> visitor){
           return visitor.visitBlockStmt(this);
        }
    }
    static class Expression extends Stmt {
        Expression(Expr expression){
            this.expression = expression;
        }
        final Expr expression;

        @Override
        <R> R accept(Visitor<R> visitor){
           return visitor.visitExpressionStmt(this);
        }
    }
    static class Function extends Stmt {
        Function(Token name, List<Token> params, List<Stmt> body){
            this.name = name;
            this.params = params;
            this.body = body;
        }
        final Token name;
        final List<Token> params;
        final List<Stmt> body;

        @Override
        <R> R accept(Visitor<R> visitor){
           return visitor.visitFunctionStmt(this);
        }
    }
    static class Return extends Stmt {
        Return(Token keyword, Expr value){
            this.keyword = keyword;
            this.value = value;
        }
        final Token keyword;
        final Expr value;

        @Override
        <R> R accept(Visitor<R> visitor){
           return visitor.visitReturnStmt(this);
        }
    }
    static class Print extends Stmt {
        Print(Expr expression){
            this.expression = expression;
        }
        final Expr expression;

        @Override
        <R> R accept(Visitor<R> visitor){
           return visitor.visitPrintStmt(this);
        }
    }
    static class If extends Stmt {
        If(Expr Condition, Stmt thenBranch, Stmt elseBranch){
            this.Condition = Condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
        final Expr Condition;
        final Stmt thenBranch;
        final Stmt elseBranch;

        @Override
        <R> R accept(Visitor<R> visitor){
           return visitor.visitIfStmt(this);
        }
    }
    static class Var extends Stmt {
        Var(Token name, Expr initializer){
            this.name = name;
            this.initializer = initializer;
        }
        final Token name;
        final Expr initializer;

        @Override
        <R> R accept(Visitor<R> visitor){
           return visitor.visitVarStmt(this);
        }
    }
    static class While extends Stmt {
        While(Expr condition, Stmt body){
            this.condition = condition;
            this.body = body;
        }
        final Expr condition;
        final Stmt body;

        @Override
        <R> R accept(Visitor<R> visitor){
           return visitor.visitWhileStmt(this);
        }
    }
    static class For extends Stmt {
        For(Stmt initializer, Expr condition, Expr increment, Stmt body){
            this.initializer = initializer;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
        }
        final Stmt initializer;
        final Expr condition;
        final Expr increment;
        final Stmt body;

        @Override
        <R> R accept(Visitor<R> visitor){
           return visitor.visitForStmt(this);
        }
    }
    static class Break extends Stmt {
        Break(Token keyword){
            this.keyword = keyword;
        }
        final Token keyword;

        @Override
        <R> R accept(Visitor<R> visitor){
           return visitor.visitBreakStmt(this);
        }
    }
    static class Continue extends Stmt {
        Continue(Token keyword){
            this.keyword = keyword;
        }
        final Token keyword;

        @Override
        <R> R accept(Visitor<R> visitor){
           return visitor.visitContinueStmt(this);
        }
    }

    abstract <R> R accept(Visitor<R> visitor);
}
