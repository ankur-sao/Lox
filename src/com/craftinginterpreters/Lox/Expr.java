package com.craftinginterpreters.Lox;

import java.util.List;

abstract class Expr {
    interface  Visitor<R> { 
    R visitBinaryExpr(Binary expr);
    R visitGroupingExpr(Grouping expr);
    R visitUnaryExpr(Unary expr);
    R visitLiteralExpr(Literal expr);
    R visitVariableExpr(Variable expr);
    R visitAssignExpr(Assign expr);
    R visitLogicalExpr(Logical expr);
    R visitcallExpr(call expr);
 }
static  class Binary extends Expr {
  Binary ( Expr left, Token operator, Expr right  ) {
    this.left = left;
    this.operator = operator;
    this.right = right;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
} 
static  class Grouping extends Expr {
  Grouping ( Expr expression  ) {
    this.expression = expression;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitGroupingExpr(this);
    }

    final Expr expression;
} 
static  class Unary extends Expr {
  Unary ( Token operator, Expr right  ) {
    this.operator = operator;
    this.right = right;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
} 
static  class Literal extends Expr {
  Literal ( Object value  ) {
    this.value = value;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitLiteralExpr(this);
    }

    final Object value;
} 
static  class Variable extends Expr {
  Variable ( Token name  ) {
    this.name = name;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitVariableExpr(this);
    }

    final Token name;
} 
static  class Assign extends Expr {
  Assign ( Token name, Expr Value  ) {
    this.name = name;
    this.Value = Value;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitAssignExpr(this);
    }

    final Token name;
    final Expr Value;
} 
static  class Logical extends Expr {
  Logical ( Token operator, Expr left, Expr right  ) {
    this.operator = operator;
    this.left = left;
    this.right = right;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitLogicalExpr(this);
    }

    final Token operator;
    final Expr left;
    final Expr right;
} 
static  class call extends Expr {
  call ( Expr callee, Token paren, List<Expr> Arguments  ) {
    this.callee = callee;
    this.paren = paren;
    this.Arguments = Arguments;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitcallExpr(this);
    }

    final Expr callee;
    final Token paren;
    final List<Expr> Arguments;
} 

  abstract <R> R accept(Visitor<R> visitor);
}
