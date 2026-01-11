package lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    
    private static final Map<String, TokenType> keywords = new HashMap<>();
    
    static {
        keywords.put("class", TokenType.CLASS);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("for", TokenType.FOR);
        keywords.put("return", TokenType.RETURN);
        keywords.put("new", TokenType.NEW);
        keywords.put("this", TokenType.THIS);
        keywords.put("int", TokenType.INT);
        keywords.put("double", TokenType.DOUBLE);
        keywords.put("boolean", TokenType.BOOLEAN);
        keywords.put("void", TokenType.VOID);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("null", TokenType.NULL);
    }
    
    public Lexer(String source) {
        this.source = source;
    }
    
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }
    
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LPAREN); break;
            case ')': addToken(TokenType.RPAREN); break;
            case '{': addToken(TokenType.LBRACE); break;
            case '}': addToken(TokenType.RBRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;
            case '*': addToken(TokenType.STAR); break;
            case '%': addToken(TokenType.PERCENT); break;
            
            // Two-character operators
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '&':
                if (match('&')) {
                    addToken(TokenType.AND);
                } else {
                    error(line, "Unexpected character: &");
                }
                break;
            case '|':
                if (match('|')) {
                    addToken(TokenType.OR);
                } else {
                    error(line, "Unexpected character: |");
                }
                break;
            
            // Comments and division
            case '/':
                if (match('/')) {
                    // Single-line comment - skip until end of line
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    // Multi-line comment
                    blockComment();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            
            // Whitespace
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace
                break;
            case '\n':
                line++;
                break;
            
            // String literals
            case '"':
                string();
                break;
            
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    error(line, "Unexpected character: " + c);
                }
                break;
        }
    }
    
    private void blockComment() {
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance(); // consume *
                advance(); // consume /
                break;
            }
            if (peek() == '\n') line++;
            advance();
        }
    }
    
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        
        // Handle boolean literals
        if (type == TokenType.TRUE) {
            addToken(type, true);
        } else if (type == TokenType.FALSE) {
            addToken(type, false);
        } else if (type == TokenType.NULL) {
            addToken(type, null);
        } else {
            addToken(type);
        }
    }
    
    private void number() {
        while (isDigit(peek())) advance();
        
        // Look for decimal part
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // consume the '.'
            while (isDigit(peek())) advance();
            
            double value = Double.parseDouble(source.substring(start, current));
            addToken(TokenType.NUMBER, value);
        } else {
            int value = Integer.parseInt(source.substring(start, current));
            addToken(TokenType.NUMBER, value);
        }
    }
    
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        
        if (isAtEnd()) {
            error(line, "Unterminated string");
            return;
        }
        
        advance(); // closing "
        
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }
    
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        
        current++;
        return true;
    }
    
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
    
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }
    
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    
    private boolean isAtEnd() {
        return current >= source.length();
    }
    
    private char advance() {
        return source.charAt(current++);
    }
    
    private void addToken(TokenType type) {
        addToken(type, null);
    }
    
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
    
    private void error(int line, String message) {
        System.err.println("[Line " + line + "] Lexer Error: " + message);
    }
}