package com.craftinginterpreters.Lox;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


import static  com.craftinginterpreters.Lox.TokenType.*;


class Parser{

    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current =0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    List<Stmt> parse(){
        try{
            List<Stmt> lStatements =  new ArrayList<> ();
            while (!isAtEnd()) {
                lStatements.add(declaration());
            }
            return  lStatements;
        } catch(ParseError error){
            System.out.println("Caught parsing exception");
            return null;
        }
    }


    private Stmt declaration(){
        try{
            if (match(VAR)){
                return varDeclaration();
            } else if (match (FUN)) {
                return functionDeclaration();
            }
            return statement();
        } catch(ParseError error){
            System.out.println("Caught Exception in declaration [parsing]");
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration(){
        Token  name  = consume(IDENTIFIERS,"Expect variable name");
        Expr initializer  = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';'  after variable declaration");

        return new Stmt.Var(name, initializer);
    }

    /*
    * Following function parses function  declaration and definition.
    * */
    private Stmt functionDeclaration(){
        Token name = consume(IDENTIFIERS, "Expect function name after 'fun'");
        consume(LEFT_PAREN, "Expect '(' after function name");

        List<Token> params = new ArrayList<>();
        do {
            if (params.size()>=255){
                error(peek(),"Function parameters can't be greater than 254");
            }
            if (check(IDENTIFIERS)){
                params.add(advance());
            }
        } while(match(COMMA));

        consume(RIGHT_PAREN, "Expect ')' after function parameters");
        consume(LEFT_BRACE, "Expect '{' before function body starts");

        return new Stmt.Function(name, params, block());

    }

    private Stmt statement(){
        if (match(PRINT)) {
            return printStatement();
        }
        if (match(IF)){
            return  ifStatement();
        }
        if  (match(JABTAK)){
            return whileStatement();
        }
        if (match(LEFT_BRACE)){
            return  new Stmt.Block(block());
        }
        if (match(FIRSE)){
            return forStatement();
        }
        if (match(BREAK)){
            return breakStatement();
        }
        if (match(RETURN)){
            return returnStmt();
        }
        return expressionStatement();
    }

    // Instead of consuming ( and ) separately here, can we just consume a grouping expression?
    // NO, in grouping statement () is optional,  in while it  is mandatory, hence we are consuming
    // ()  explicitly.

    private Stmt breakStatement(){
        throw error(peek(), "Break is not supported .");
    }

    /*  Return actually can be put anywhere unlike break. Since if 'return' is not inside any function,
    * it'd mean, you are terminating your SLox program.
    * */
    private Stmt returnStmt(){
        Token keyword;
        Expr expr = null;
        keyword = previous();

        if (!check(SEMICOLON)){
            expr = expression();
        }
        consume(SEMICOLON,"Expect ';' after return statement");

        return new Stmt.Return(keyword, expr);
    }

    private Stmt whileStatement(){
        consume(LEFT_PAREN, "Expect '(' after while");
        Expr condition  = expression();
        consume(RIGHT_PAREN, "Expect ')' after expression");

        Stmt body = statement();

        return new Stmt.While(condition,body);
    }

    private Stmt forStatement(){
        consume(LEFT_PAREN, "Expect '(' after for");

        Stmt initializer;
        if (match(SEMICOLON)){
            initializer  = null;
        }else if (match(VAR)) {
            initializer = varDeclaration();
        }else{
            initializer = expressionStatement();
        }

        Expr condition;
        if (match(SEMICOLON)){
            condition = null;
        }else{
            condition =  expression();
            consume(SEMICOLON, "Expect ';' after for loop condition.");
        }

        Expr increment;
        if (match(SEMICOLON)){
            increment = null;
        }else{
            increment = expression();
        }
        consume(RIGHT_PAREN,"Expect ')' after for statements.");


        List<Stmt> lBodyStmts = new ArrayList<> ();

        Stmt body = statement();
        lBodyStmts.add(body);

        if (increment != null) lBodyStmts.add(new Stmt.Expression(increment));

        // body and increment will be combined to form a new block statement.

        Stmt modifiedBody = new Stmt.Block(lBodyStmts);

        Stmt.While lWhileStmt = new Stmt.While(condition, modifiedBody);

        List<Stmt> lStmts = new ArrayList<> ();
        if (initializer  != null) lStmts.add(initializer);
        lStmts.add(lWhileStmt);

        return new Stmt.Block(lStmts);
    }

    private Stmt ifStatement(){
        consume (LEFT_PAREN, "'(' missing after if");
        Expr expr = expression();
        consume (RIGHT_PAREN, "')' missing after expression in if");
        consume(LEFT_BRACE, "if block should start with a '{'");

        Stmt.Block ifBlock = new Stmt.Block(block());
        Stmt.Block elseBlock  = null;

        if (match(ELSE)){
            consume(LEFT_BRACE, "else block should start with a '{'");
            elseBlock = new Stmt.Block(block());
        }

        return new Stmt.If(expr, ifBlock, elseBlock);
    }

    private  List<Stmt> block(){
        List<Stmt> statements  = new ArrayList<>();

        while(!check(RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;

    }
    private Stmt printStatement() {
            Expr value = expression();
            consume(SEMICOLON,"Expect ';' after print statement");
            return new  Stmt.Print(value);
    }

    private Stmt expressionStatement(){
            Expr expr  = expression();
            consume(SEMICOLON,"Expect ';' after expression");
            return  new Stmt.Expression(expr);
    }

    private Expr expression(){
        return assignment();
    }

    private Expr assignment(){
        Expr expr = logical_or();

        if (match(EQUAL)){
            Token name = ((Expr.Variable)expr).name;
            Expr right = assignment();

            if (expr instanceof Expr.Variable){
                return new Expr.Assign(name, right);
            }
            error(name, "Invalid assignment  target");
        }

        return expr;
    }

    private Expr logical_or(){
        Expr expr = logical_and();

        while(match(OR)){
            Token operator = previous();
            Expr right = logical_and();
            expr = new Expr.Logical(operator, expr, right);
        }
        return  expr;
    }

    private Expr logical_and(){
        Expr expr = equality();

        while(match(AND)){
            Token operator  = previous();
            Expr right = equality();
            expr = new Expr.Logical(operator, expr, right);
        }
        return expr;
    }

    private Expr equality(){
        Expr expr  = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)){
            Token operator = previous();
            Expr right  = comparison();
            expr = new Expr.Binary(expr,operator, right);
        }

        return expr;
    }

    private Token previous(){
        return tokens.get(current-1);
    }

    private boolean match(TokenType...  types){
        for(TokenType type : types){
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }

    private  boolean check(TokenType type){
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private  boolean  isAtEnd(){
        return peek().type == EOF;
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Expr comparison(){
        Expr expr = addition();

        while(match(GREATER_EQUAL, GREATER, LESS, LESS_EQUAL)){
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr addition(){
        Expr expr = multiplication();

        while(match(PLUS, MINUS)){
            Token operator = previous();
            Expr right  = multiplication();
            expr  =  new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr multiplication(){
        Expr expr = unary();

        while(match(SLASH, STAR)){
            Token operator  =  previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary(){
        if (match(BANG,  MINUS)){
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    // Q: Why does function Stmt class just  contain one block statement  instead of List<Stmt>?
    // A: We want to consume '{' explicitly and keep the information  that this parsing error occuredd
    // during  function declaration parsing. Hence we call block(), and not statement(). If we call statement()
    // It'd give invalid token error, which is not precise.

    /*  Function declaration is parsed as an statement.
     *  function --> fun IDFR "(" parameters? ")"
     *  parameters --> IDFR (,IDFR*);
     *
     *  Body is a block statement which is convenient.
     *  When Stmt.function is executed, we'd just define it in the 'global environment' with value
     *  as a SLoxCallable object and 'call' api would execute function statement's body which is
     *  block. This is also convenient.
     *  If we have function and variables in same namespace, writing closures  is similar to
     *  variable scoping. ??
     *
     *  We also pass a String arg to function statement parser so as to differentiate  between  functions
     *  and class methods.
     * */
    private Expr call() {
        Expr expr = primary();

        //Specifically not using while(match(LEFT_PAREN))
        while(true){
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr callee){
        List<Expr> arguments = new ArrayList<>();

        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 254) {
                    error(peek(), "Cannot accept more than 255 arguments");
                }
                arguments.add(expression());
            } while(match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN,"Expect ')' after function arguments");

        return new Expr.call(callee, paren, arguments);
    }


    private Expr primary(){
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)){
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)){
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }

        if (match(IDENTIFIERS)){
            return new Expr.Variable(previous());
        }
        throw error(peek(), "Unexpected symbol during parsing.");
    }

    private Token consume(TokenType type, String message){
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message){
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize(){
        advance();

        while(!isAtEnd()){
            if (previous().type == SEMICOLON) return;

            switch(peek().type){
                case CLASS:
                case FUN:
                case VAR:
                case FIRSE:
                case IF:
                case JABTAK:
                case PRINT:
                case RETURN:
                case BREAK:
                    return;
            }
            advance();
        }

    }

}