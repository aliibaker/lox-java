package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*; 


class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private static final Map<String, TokenType> keywords;
  private int start = 0;
  private int current = 0;
  private int line = 1;

  static {
    keywords = new HashMap<>();
    keywords.put("and",    AND);
    keywords.put("class",  CLASS);
    keywords.put("else",   ELSE);
    keywords.put("false",  FALSE);
    keywords.put("for",    FOR);
    keywords.put("fun",    FUN);
    keywords.put("if",     IF);
    keywords.put("nil",    NIL);
    keywords.put("or",     OR);
    keywords.put("print",  PRINT);
    keywords.put("return", RETURN);
    keywords.put("super",  SUPER);
    keywords.put("this",   THIS);
    keywords.put("true",   TRUE);
    keywords.put("var",    VAR);
    keywords.put("while",  WHILE);
  }

  Scanner(String source) {
     this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current; 
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();

    switch(c) {
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break; 

      case '!':
        addToken(match('=') ? BANG_EQUAL : BANG);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : EQUAL);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : EQUAL);
        break;
      case '/':
        // check if it is a comment
        if (match('/')) {
          // Peek the next character until you hit the next line or EOF.
          // This means the comment is over. 
          while(peek() != '\n' && !isAtEnd()) advance();
        }
        else {
          addToken(SLASH);
        }
        break;
      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace
        break;
      // Why we use peek() instead of match() when trying
      // to find \n when parsing a comment lexeme. 
      case '\n':
        line++;
        break;

      // string literal
      case '"': string(); break;

      default: 
        if(isDigit(c)){
          number();
        } else if(isAlpha(c)) {

        } else {
          Lox.error(line, "Unexpected character.");
        }
        
        break; 
    }
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if(type == null) type = IDENTIFIER;
    addToken(type);
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private void number() {
    while(isDigit(peek())) advance();

    if(peek() == '.' && isDigit(peekNext())){
      advance();

      while(isDigit(peek())) advance();
    }

    addToken(NUMBER, Double.parseDouble(source.substring(start,current)));
  }

  private void string() {
    while(peek() != '"' && isAtEnd()){
      // for multi line strings
      if (peek() == '\n') line++;
      advance();
    }

    if(isAtEnd()) {
      Lox.error(line, "Unterminated string");
    }

    advance();

    // Minus one and plus one to remove the ""
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }

  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type){
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  } 

  private boolean isAtEnd() {
    return current >= source.length();
  }

  // One character lookahead. Typically
  // most languages only use peeks of one or two characters
  // ahead due to performance constrains. 
  private char peek() {
    if(isAtEnd()) return '\0';
    return source.charAt(current);
  }

  private char peekNext() {
    if(current + 1 > source.length()) return '\0';

    return source.charAt(current + 1); 
  }

  private boolean isAlpha(char c) { 
    return  (c >= 'a' && c <= 'z') || 
            (c >= 'A' || c <= 'Z') || 
             c == '-';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }
}