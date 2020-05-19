package com.craftinginterpreters.Lox;


class RpnPrinter implements Expr.Visitor<String>{

    String print(Expr expression){
        return expression.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr){
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr){
        return  parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr (Expr.Variable expr){return parenthesize(expr.name.lexeme);}

    @Override
    public String visitAssignExpr (Expr.Assign expr) {return parenthesize(expr.name.lexeme, expr.Value);}

    @Override
    public String visitLogicalExpr(Expr.Logical expr){
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr){
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr){
        return parenthesize("grouping", expr.expression);
    }

    private String parenthesize(String name, Expr... exprs){
        StringBuilder builder = new StringBuilder();

        for (Expr expr : exprs){
            builder.append(expr.accept(this));
            builder.append(" ");
        }

        builder.append(name);

        return builder.toString();
    }

    public static void main(String [] args){
        Expr expression = new Expr.Literal(2);

        System.out.println(new RpnPrinter().print(expression));
    }
}