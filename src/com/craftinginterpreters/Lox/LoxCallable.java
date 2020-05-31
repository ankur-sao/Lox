package com.craftinginterpreters.Lox;

import java.util.List;

interface SLoxCallable{
    int arity();
    Object call(Interpreter interpreter, List<Object> argumentValues);
    String toString();
}