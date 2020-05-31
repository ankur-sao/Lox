package com.craftinginterpreters.Lox;

import java.util.Map;
import java.util.HashMap;

/*
*  Q: Environement class get api is by Token, and define api is by String, why? GET api
*     can also be made to take String input.
*  A: To be able to define many native functions in global scope with values being a callable object.
* */

class Environment{
    private final Map<String,Object> values = new HashMap<> ();
    final Environment enclosing;

    Environment(){
        enclosing = null;
    }

    Environment(Environment enclosing){
        this.enclosing  = enclosing;
    }

    void define(String name, Object value){
        values.put(name,value);
    }

    Object get(Token name){
        //Token should be  an identifier
        if (values.containsKey(name.lexeme)){
            Object value = values.get(name.lexeme);
            if (value  == null) {
                throw new RuntimeError(name,
                        "[RuntimeError] Uninitialized variable '"+ name.lexeme+"'.");
            }else{
                return value;
            }

        }

        if (null != enclosing){
            return enclosing.get(name);
        }
        throw new RuntimeError(name,
                "[RuntimeError] Undefined variable name '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value){
        if (values.containsKey(name.lexeme)){
            values.put(name.lexeme, value);
            return;
        }

        if (null != enclosing){
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name,
                "[Assign] Undefined variable '" + name.lexeme + "'.");
    }


}
