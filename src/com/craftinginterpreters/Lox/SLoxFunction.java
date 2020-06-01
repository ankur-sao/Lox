package com.craftinginterpreters.Lox;

import java.util.List;

class SLoxFunction implements SLoxCallable{
    private final Stmt.Function declaration;

    SLoxFunction(Stmt.Function stmt){
        declaration  =  stmt;
    }

    @Override
    public int arity(){
        return declaration.params.size();
    }

    @Override
    public Object call (Interpreter interpreter, List<Object> arguments){
        Environment  environment = new Environment (interpreter.globals);

        for(int i=0;i<declaration.params.size();i++){
            environment.define((declaration.params.get(i).lexeme), arguments.get(i));
        }

        interpreter.executeBlockStmt(declaration.body, environment);
        return  null;
    }

}