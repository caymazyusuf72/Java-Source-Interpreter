package lexer;

public enum TokenType {
    // Literals
    NUMBER,        // 123, 45.67
    STRING,        // "hello"
    TRUE,          // true
    FALSE,         // false
    NULL,          // null
    
    // Keywords
    CLASS,         // class
    IF,            // if
    ELSE,          // else
    WHILE,         // while
    FOR,           // for
    RETURN,        // return
    NEW,           // new
    THIS,          // this
    INT,           // int
    DOUBLE,        // double
    BOOLEAN,       // boolean
    VOID,          // void
    
    // Identifiers
    IDENTIFIER,    // variable names, method names, etc.
    
    // Operators
    PLUS,          // +
    MINUS,         // -
    STAR,          // *
    SLASH,         // /
    PERCENT,       // %
    EQUAL,         // =
    EQUAL_EQUAL,   // ==
    BANG,          // !
    BANG_EQUAL,    // !=
    LESS,          // <
    LESS_EQUAL,    // <=
    GREATER,       // >
    GREATER_EQUAL, // >=
    AND,           // &&
    OR,            // ||
    
    // Delimiters
    LPAREN,        // (
    RPAREN,        // )
    LBRACE,        // {
    RBRACE,        // }
    SEMICOLON,     // ;
    COMMA,         // ,
    DOT,           // .
    
    // Special
    EOF            // End of file
}