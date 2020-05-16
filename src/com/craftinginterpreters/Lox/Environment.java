package com.craftinginterpreters.Lox;

import java.util.Map;
import java.util.HashMap;

/*
*  Q: Environement class get api is by Token, and define api is by String, why?
*  A:
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
            return values.get(name.lexeme);
        }

        if (null != enclosing){
            return enclosing.get(name);
        }
        throw new RuntimeError(name,
                "Undefined variable name '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value){
        if (values.containsKey(name.lexeme)){
            values.put(name.lexeme, value);
            return;
        }

        if (null != enclosing){
            enclosing.assign(name, value);
        }
        throw new RuntimeError(name,
                "Undefine variable '" + name.lexeme + "'.");
    }


}
