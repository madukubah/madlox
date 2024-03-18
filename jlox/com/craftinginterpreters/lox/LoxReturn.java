package com.craftinginterpreters.lox;

public class LoxReturn extends RuntimeException {
    final Object value;

    LoxReturn(Object value){
        this.value = value;
    }
}
