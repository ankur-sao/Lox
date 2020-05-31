package com.craftinginterpreters.Lox;

import java.util.List;

abstract class Stmt {
    interface  Visitor<R> { 
    R visitExpressionStmt(Expression stmt);
    R visitPrintStmt(Print stmt);
    R visitVarStmt(Var stmt);
    R visitBlockStmt(Block stmt);
    R visitIfStmt(If stmt);
    R visitWhileStmt(While stmt);
    R visitBreakStmt(Break stmt);
    R visitFunctionStmt(Function stmt);
 }
static  class Expression extends Stmt {
  Expression ( Expr expression  ) {
    this.expression = expression;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
} 
static  class Print extends Stmt {
  Print ( Expr expression  ) {
    this.expression = expression;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitPrintStmt(this);
    }

    final Expr expression;
} 
static  class Var extends Stmt {
  Var ( Token name, Expr initializer  ) {
    this.name = name;
    this.initializer = initializer;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitVarStmt(this);
    }

    final Token name;
    final Expr initializer;
} 
static  class Block extends Stmt {
  Block ( List<Stmt> statements  ) {
    this.statements = statements;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitBlockStmt(this);
    }

    final List<Stmt> statements;
} 
static  class If extends Stmt {
  If ( Expr expr, Stmt.Block ifBlock, Stmt.Block elseBlock  ) {
    this.expr = expr;
    this.ifBlock = ifBlock;
    this.elseBlock = elseBlock;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitIfStmt(this);
    }

    final Expr expr;
    final Stmt.Block ifBlock;
    final Stmt.Block elseBlock;
} 
static  class While extends Stmt {
  While ( Expr condition, Stmt body  ) {
    this.condition = condition;
    this.body = body;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitWhileStmt(this);
    }

    final Expr condition;
    final Stmt body;
} 
static  class Break extends Stmt {
  Break ( Stmt destBlock  ) {
    this.destBlock = destBlock;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitBreakStmt(this);
    }

    final Stmt destBlock;
} 
static  class Function extends Stmt {
  Function ( Token name, List<Token> params, List<Stmt> body  ) {
    this.name = name;
    this.params = params;
    this.body = body;
}

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitFunctionStmt(this);
    }

    final Token name;
    final List<Token> params;
    final List<Stmt> body;
} 

  abstract <R> R accept(Visitor<R> visitor);
}
