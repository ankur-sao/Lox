package com.craftinginterpreters.Lox;

class RuntimeError extends RuntimeException {
    final Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
    class RuntimeBreak extends RuntimeException{
        RuntimeBreak(String message){
            super(message);
        }
    }

