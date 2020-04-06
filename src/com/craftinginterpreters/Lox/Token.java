package com.craftinginterpreters.Lox;

class Token
{
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType aInType, String aInLexeme, Object aInLiteral, int  aInLine)
    {
        this.type = aInType;
        this.lexeme  = aInLexeme ;
        this.literal = aInLiteral;
        this.line =  aInLine;
    }

    public String toString ()
    {
        return type + " " + lexeme + " " + literal;
    }
}
