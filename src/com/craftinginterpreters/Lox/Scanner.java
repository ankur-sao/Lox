package com.craftinginterpreters.Lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.Lox.TokenType.*;


class Scanner
{
    private final String source;
    private final List<Token> tokens = new ArrayList<> ();

    private int start = 0;
    private int current  = 0;
    private int line = 1;

    private static final Map <String, TokenType> keywords;

    static {
        keywords = new HashMap<> ();

        keywords.put("and", AND);
        keywords.put("class", CLASS );
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("firse", FIRSE);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("jabtak", JABTAK);
    }

    Scanner(String aInSource)
    {
        this.source = aInSource;
    }

    private boolean isAtEnd()
    {
        return (current >=source.length());
    }

    List<Token> scanTokens()
    {
        while (!isAtEnd())
        {
            start = current;
            scanToken();
        }

        /* pushing EOF token at the end to make a cleaner parser */
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken()
    {
        char c = advance();
        switch (c){
            case '(' : addToken(LEFT_PAREN);break;
            case ')' : addToken(RIGHT_PAREN);break;
            case '{' : addToken(LEFT_BRACE);break;
            case '}' : addToken(RIGHT_BRACE);break;
            case ',' : addToken(COMMA);break;
            case '.' : addToken(DOT);break;
            case '-' : addToken(MINUS);break;
            case '+' : addToken(PLUS);break;
            case ';' : addToken(SEMICOLON);break;
            case '*' : addToken(STAR);break;
            case '!' : addToken(match('=') ? BANG_EQUAL : BANG);break;
            case '=' : addToken(match('=') ? EQUAL_EQUAL: EQUAL);break;
            case '<' : addToken(match('=') ? LESS_EQUAL: LESS);break;
            case '>' : addToken(match('=') ? GREATER_EQUAL: GREATER);break;
            case '/' :
                if (match('/')){
                    while(peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')){
                    // increase count for each /* , and decrease for each */ break from here once count is 0.
                    int lOpen = 1 ;
                    while (lOpen != 0 && !isAtEnd())
                    {
                        if (peek() == '/' && peekNext() == '*')
                        {
                            lOpen++ ;
                            advance();
                        }
                        else if (peek() == '*' && peekNext() == '/')
                        {
                            lOpen--;
                            advance();
                        }
                        if (peek() == '\n') line++;
                        advance();
                    }
                    if (isAtEnd() && lOpen !=0 ) Lox.error(line, "Nested comments are not terminated");
                }
                else {
                    addToken(SLASH);
                }
                break;
            case ' ' :
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"': string();break;

            default:
                if (isDigit(c)){
                    number();
                } else if (isAlpha(c)){
                    identifier();
                } else {

                    Lox.error(line, "Unexpected character.");
                }
        }
    }
    private char advance()
    {
        current++;
        return source.charAt(current-1);
    }

    private void addToken(TokenType aInType)
    {
        addToken(aInType, null);
    }

    private void addToken(TokenType aInType, Object aInLiteral)
    {
        String text = source.substring(start, current);
        tokens.add(new Token(aInType, text, aInLiteral, line));
    }

    private boolean match(char ch)
    {
        if (isAtEnd()) return false;
        if (source.charAt(current) != ch) return false;

        current++;
        return true;
    }

    private  char peek ()
    {
        if (isAtEnd()) return '\0';
        else return source.charAt(current);
    }

    private void string()
    {
        while (peek() != '"' && !isAtEnd())
        {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd())
        {
            Lox.error(line, "unterminated string");
            return;
        }

        advance();

        String value = source.substring(start+1, current-1);
        addToken(STRING, value);
    }

    private boolean isDigit(char ch)
    {
        return (ch>='0' && ch <= '9');
    }

    private void number()
    {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext()))
        {
            advance();

            while(isDigit(peek())) advance();
        }
        addToken(NUMBER, Double.parseDouble(source.substring(start,current)));
    }

    private char peekNext()
    {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char ch)
    {
        return (ch >= 'a' && ch <= 'z') ||
               (ch >= 'A' && ch <= 'Z') ||
               (ch == '_');
    }

    private void identifier() {
        while (isAlphanumeric(peek())) advance();

        String lText = source.substring(start, current);

        TokenType lType = keywords.get(lText);
        if (lType == null) lType = IDENTIFIERS;

        addToken(lType);
    }

    private boolean isAlphanumeric(char ch)
    {
        return isDigit(ch) || isAlpha(ch);
    }
}