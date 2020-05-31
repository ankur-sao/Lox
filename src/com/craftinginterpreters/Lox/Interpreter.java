package com.craftinginterpreters.Lox;

import java.util.List;
import java.util.ArrayList;


class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{
    final Environment globals = new Environment();
    private Environment environment =  globals;

    void interpret (List<Stmt> statements){
        try{
            for (Stmt statement: statements){
                execute(statement);
            }
        } catch (RuntimeError error){
            Lox.runtimeError(error);
        }
    }

    Interpreter() {
        globals.define("clock", new SLoxCallable(){
            @Override
            public int arity(){return 0;}

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return  (double)System.currentTimeMillis()/1000;
            }

            @Override
            public String toString(){
                return "Native function<>";
            }
        });
    }
    private void execute(Stmt statement){
        statement.accept(this);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr){
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr){
        return evaluate(expr.expression);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr){
        Object value = evaluate(expr.Value);
        environment.assign(expr.name, value);
        return value;
    }

    /*
    Two ways to execute a  block  stmt, either call visitBlockStmt(Stmt.Block) or call
    executeBlockStmt(List<Stmt>, newEnvironment())
    */

    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        executeBlockStmt(stmt.statements, new Environment(environment));
        return  null;
    }

    @Override
    public visitFunctionStmt(Stmt.Function stmt){
        //var IDFR ( params? ) body
        // SLoxCallable object and assign it to the value in globals.
    }

    /*  Bob Nystrom doesn't like braces for if and else block and  therefore allows writing ambiguous
        code like this one:
        if  (condition1) if (condition2) else doSomething();
        Ambiguity is called dangling-else. We dont know second else was intended for which if.

        Following Go's style, I am enforcing braces for if and else. Code readability is important for me.
        Although Go also enforces that you can't start if block from the same line as if keyword.
        I dont have this restriction.

    * */

    @Override
    public Void visitIfStmt(Stmt.If ifStmt){
          Object value  = evaluate(ifStmt.expr);
          if (isTruthy(value)){
              executeBlockStmt(ifStmt.ifBlock.statements, new Environment(environment));
          } else if (ifStmt.elseBlock != null){
              executeBlockStmt(ifStmt.elseBlock.statements, new Environment(environment));
          }
          return null;
    }

   public void executeBlockStmt(List<Stmt> stmt, Environment environment){
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement: stmt){
                execute(statement);
            }
        } finally{
            this.environment = previous;
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr){
        Object leftValue =  evaluate(expr.left);
        Object rightValue = evaluate(expr.right);

        switch(expr.operator.type){
            case MINUS:
                checkNumberOperand(expr.operator, leftValue,  rightValue);
                return (double)leftValue - (double)rightValue;
            case SLASH:
                Token operator = expr.operator;
                if (leftValue instanceof Double && rightValue instanceof Double) {
                    Double rightSlash = (double) rightValue;
                    if (rightSlash.intValue() == 0) {
                        throw new RuntimeError(operator,
                                "Invalid operation, can't divide by zero");
                    }
                    return (double)leftValue / (double)rightValue;
                }
                throw new RuntimeError(operator,
                        "Invalid operation, operands must be numbers");
            case STAR:
                if (leftValue instanceof Double && rightValue instanceof Double)
                return (double)leftValue * (double)rightValue;

                int rep;
                StringBuilder value = new StringBuilder();
                StringBuilder attach = new StringBuilder();
                if (leftValue instanceof String && rightValue instanceof Double) {
                    Double repD =  (double) rightValue;
                    rep = repD.intValue();
                    value.append(leftValue.toString());
                    attach.append(leftValue.toString());
                }
                else if (rightValue instanceof  String && leftValue instanceof Double) {
                    Double repD = (double)leftValue;
                    rep = repD.intValue();
                    value.append(rightValue.toString());
                    attach.append(rightValue.toString());
                }
                else{
                    throw new RuntimeError(expr.operator,
                            "Does not match number*number or number*string rule");
                }
                for (int i=1; i<rep; i++){
                    value.append(attach);
                }
                return value;
            case PLUS:
                if (leftValue instanceof Double && rightValue instanceof Double){
                    return  (double)leftValue + (double)rightValue;
                }

                if (leftValue instanceof String &&  rightValue instanceof String){
                    return (String)leftValue + (String)rightValue;
                }
                else{
                    StringBuilder addResult = new StringBuilder();
                    if (leftValue instanceof Double){
                        String leftAdd = leftValue.toString();
                        leftAdd = leftAdd.substring(0,leftAdd.length()-2);
                        addResult.append(leftAdd);
                        addResult.append(rightValue.toString());
                    }else{
                        String rightAdd = rightValue.toString();
                        rightAdd =  rightAdd.substring(0,rightAdd.length()-2);
                        addResult.append(leftValue.toString());
                        addResult.append(rightAdd);
                    }
                    return addResult;
                }
            case GREATER:
                if (leftValue instanceof Double && rightValue instanceof Double)
                return (double)leftValue > (double)rightValue;

                if (leftValue instanceof String && rightValue instanceof String){
                    int compareValue = leftValue.toString().compareToIgnoreCase(rightValue.toString());
                    if (compareValue > 0){
                        return true;
                    }
                    else return false;
                }
                throw new RuntimeError(expr.operator,
                        "operands must be string or number");
            case GREATER_EQUAL:
                checkNumberOperand(expr.operator,  leftValue, rightValue);
                return (double)leftValue >= (double)rightValue;
            case LESS:
                checkNumberOperand(expr.operator,  leftValue, rightValue);
                return (double)leftValue < (double)rightValue;
            case LESS_EQUAL:
                checkNumberOperand(expr.operator, leftValue, rightValue);
                return (double)leftValue <= (double)rightValue;
            case BANG_EQUAL:
                return !isEqual(leftValue, rightValue);
            case EQUAL_EQUAL:
                return isEqual(leftValue, rightValue);
        }

        // cant reach here?
        return null;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr){
        Token operator = expr.operator;
        Expr right = expr.right;

        Object rightValue = evaluate(right);

        switch (operator.type){
            case  MINUS:
                checkNumberOperand(expr.operator, rightValue);
                return -(double) rightValue;
            case BANG:
                return !isTruthy(rightValue);

                /*IsTruthy determines  the boolean value of Object. we follow Ruby's rule.
                 false and  null is false and everything else   is true*/
        }

        //Unreachable part of the code
        // can't come here, it's  a syntax error, parser would have caught it.
        // some other unary operator except - and !. or a binary operator without left operand
        return  null;
    }

    /*
    * It visitVariableExpr knows that the this.enivronment is the local scope with reference
    * to parent scope.
    */
    @Override
    public Object visitVariableExpr(Expr.Variable expr){
        return environment.get(expr.name);
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical  expr){
        Object leftValue  = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR){
             if (isTruthy(leftValue)) return leftValue;
        }else{
            if (!isTruthy(leftValue)) return leftValue;
        }
        return evaluate(expr.right);
    }

    /*
    * Interpreter has a global environment which is  final. Scopes/environment in function call are treated
    * just as blocks with access to one global environment.
    * Any function Callee ultimately is an primary variable defined in global environment with value as
    * object of a class which implements SLoxCallable function.
    *
    * Any user defined function is also a key:value pair in global environment.
    *
    * Q: How does a function call works?
    * A: fun(a)(b)(c) will be parsed to these callees fun(a)(b) with arg c, func(a) with arg b, and finally
    *    fun with arg a.
    *
    *    Every callable variable must implement SLoxCallable interface which has a 'call' api.
    *
    *    A variable is callable when its value in global environment is an object of callable interface.
    *
    *    Hence a callable variable is evaluated to its callable value whose 'call'  api is invoked, resulting
    *    in invocation of native/user defined function. This is the beautiful story of how function invocation
    *    (native/user defined) happens.
    *
    * */

    @Override
    public Object visitCallExpr(Expr.call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> argumentValues = new ArrayList<>();
        for(Expr argument: expr.Arguments){
            argumentValues.add(evaluate(argument));
        }

        if (!callee instanceof SLoxCallable) {
            throw new RuntimeError(expr.paren,"only classes and function can be called");
        }
        SLoxCallable function = (SLoxCallable) callee;

        if (argumentValues.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected arguments: " + function.arity() +
                    ", recieved arguments: " + argumentValues.size());
        }
        return function.call(this, argumentValues);
    }

    /*
     Q:Why do we have Expr subclass variable?
     A:While evaluating an expression, sometimes operands might be literals, sometimes  it might be variables.
     Literal is a  subclass of Expr, so it makes sense to have aa subclass for Variables.
     Answer to this question is same as answer to the question, why Literal subclass is present for Expr class.
     */

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
            Object value = evaluate(stmt.expression);
            //System.out.println(Stringify(value));

            return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
            Object value = evaluate(stmt.expression);
            System.out.println(Stringify(value));
            return  null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt){
            Object value = null;
            if (stmt.initializer != null){
                value = evaluate(stmt.initializer);
            }

            environment.define(stmt.name.lexeme,  value);
            return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt){
         try {
             while (isTruthy(evaluate(stmt.condition))) {
                 execute(stmt.body);
             }
         } catch (RuntimeBreak breakFromLoop){
             return null;
         }
        return null;
    }

    @Override
    public  Void visitBreakStmt(Stmt.Break stmt){
        throw new RuntimeBreak("Just a break from loop");
    }

    private void checkNumberOperand(Token operator, Object rightValue){
        if (rightValue instanceof Double) return;
        throw new RuntimeError(operator,
                "operand must be a  number");

    }

    private void checkNumberOperand(Token operator, Object leftValue, Object rightValue){
        if (leftValue instanceof Double &&  rightValue instanceof Double){
            return;
        }
        throw new RuntimeError(operator,
                "Operands  must be numbers");
    }

    private boolean isTruthy(Object value){
        //Can we cast this value to boolean // programming
        // do we want to  cast this value to boolean // design

        if (value == null) return false;
        if (value instanceof Boolean) return (boolean) value;

        return true;
    }

    private boolean isEqual(Object a, Object b){
        if (a==null && b==null) return  true;
        if (a==null) return false;

        return a.equals(b);
    }

    private Object evaluate(Expr expr){
        return expr.accept(this);
    }

    private String Stringify(Object value){
        if (value == null)  return "nil";

        if (value instanceof Double){
            String text  = value.toString();
            if (text.endsWith(".0")){
                text  =  text.substring(0, text.length()-2);
            }
            return  text;
        }

        return  value.toString();

    }

}