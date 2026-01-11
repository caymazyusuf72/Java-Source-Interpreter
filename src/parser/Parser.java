package parser;

import lexer.Token;
import lexer.TokenType;
import parser.ast.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    public List<Declaration> parse() {
        List<Declaration> declarations = new ArrayList<>();
        while (!isAtEnd()) {
            declarations.add(declaration());
        }
        return declarations;
    }
    
    // Grammar: declaration → classDecl | statement
    private Declaration declaration() {
        try {
            if (match(TokenType.CLASS)) {
                return classDeclaration();
            }
            
            // Try to parse as statement and wrap it
            Statement stmt = statement();
            // For top-level statements, we'll handle them differently in interpreter
            return new Declaration.Var(null, null, null) {
                public Statement stmt = stmt;
                @Override
                public <R> R accept(interpreter.Visitor<R> visitor) {
                    return stmt.accept(visitor);
                }
            };
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }
    
    // Grammar: classDecl → "class" IDENTIFIER "{" (varDecl | methodDecl)* "}"
    private Declaration.Class classDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect class name");
        consume(TokenType.LBRACE, "Expect '{' before class body");
        
        List<Declaration.Var> fields = new ArrayList<>();
        List<Declaration.Method> methods = new ArrayList<>();
        
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            // Check if it's a method or field
            Token type = advance(); // type token
            Token memberName = consume(TokenType.IDENTIFIER, "Expect member name");
            
            if (match(TokenType.LPAREN)) {
                // It's a method
                methods.add(finishMethodDeclaration(type, memberName));
            } else {
                // It's a field
                Expression initializer = null;
                if (match(TokenType.EQUAL)) {
                    initializer = expression();
                }
                consume(TokenType.SEMICOLON, "Expect ';' after field declaration");
                fields.add(new Declaration.Var(type, memberName, initializer));
            }
        }
        
        consume(TokenType.RBRACE, "Expect '}' after class body");
        return new Declaration.Class(name, fields, methods);
    }
    
    private Declaration.Method finishMethodDeclaration(Token returnType, Token name) {
        List<Declaration.Parameter> parameters = new ArrayList<>();
        
        if (!check(TokenType.RPAREN)) {
            do {
                Token paramType = advance();
                Token paramName = consume(TokenType.IDENTIFIER, "Expect parameter name");
                parameters.add(new Declaration.Parameter(paramType, paramName));
            } while (match(TokenType.COMMA));
        }
        
        consume(TokenType.RPAREN, "Expect ')' after parameters");
        consume(TokenType.LBRACE, "Expect '{' before method body");
        
        List<Statement> statements = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(statement());
        }
        
        consume(TokenType.RBRACE, "Expect '}' after method body");
        return new Declaration.Method(returnType, name, parameters, new Statement.Block(statements));
    }
    
    // Grammar: statement → exprStmt | block | ifStmt | whileStmt | forStmt | returnStmt | varDecl
    private Statement statement() {
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.RETURN)) return returnStatement();
        if (match(TokenType.LBRACE)) return new Statement.Block(block());
        
        // Check for variable declaration
        if (isTypeToken(peek())) {
            return varDeclarationStatement();
        }
        
        return expressionStatement();
    }
    
    private Statement varDeclarationStatement() {
        Token type = advance();
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name");
        
        Expression initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }
        
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration");
        
        final Declaration.Var varDecl = new Declaration.Var(type, name, initializer);
        return new Statement.ExpressionStmt(null) {
            public Declaration.Var decl = varDecl;
            @Override
            public <R> R accept(interpreter.Visitor<R> visitor) {
                return visitor.visitVarDecl(decl);
            }
        };
    }
    
    private boolean isTypeToken(Token token) {
        return token.type == TokenType.INT || 
               token.type == TokenType.DOUBLE || 
               token.type == TokenType.BOOLEAN ||
               token.type == TokenType.VOID ||
               token.type == TokenType.IDENTIFIER; // For class types
    }
    
    private Statement ifStatement() {
        consume(TokenType.LPAREN, "Expect '(' after 'if'");
        Expression condition = expression();
        consume(TokenType.RPAREN, "Expect ')' after if condition");
        
        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }
        
        return new Statement.If(condition, thenBranch, elseBranch);
    }
    
    private Statement whileStatement() {
        consume(TokenType.LPAREN, "Expect '(' after 'while'");
        Expression condition = expression();
        consume(TokenType.RPAREN, "Expect ')' after while condition");
        
        Statement body = statement();
        return new Statement.While(condition, body);
    }
    
    private Statement forStatement() {
        consume(TokenType.LPAREN, "Expect '(' after 'for'");
        
        // Initializer
        Statement initializer = null;
        if (!check(TokenType.SEMICOLON)) {
            if (isTypeToken(peek())) {
                initializer = varDeclarationStatement();
            } else {
                initializer = expressionStatement();
            }
        } else {
            consume(TokenType.SEMICOLON, "Expect ';'");
        }
        
        // Condition
        Expression condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after for condition");
        
        // Increment
        Expression increment = null;
        if (!check(TokenType.RPAREN)) {
            increment = expression();
        }
        consume(TokenType.RPAREN, "Expect ')' after for clauses");
        
        Statement body = statement();
        return new Statement.For(initializer, condition, increment, body);
    }
    
    private Statement returnStatement() {
        Token keyword = previous();
        Expression value = null;
        
        if (!check(TokenType.SEMICOLON)) {
            value = expression();
        }
        
        consume(TokenType.SEMICOLON, "Expect ';' after return value");
        return new Statement.Return(keyword, value);
    }
    
    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();
        
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(statement());
        }
        
        consume(TokenType.RBRACE, "Expect '}' after block");
        return statements;
    }
    
    private Statement expressionStatement() {
        Expression expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression");
        return new Statement.ExpressionStmt(expr);
    }
    
    // Grammar: expression → assignment
    private Expression expression() {
        return assignment();
    }
    
    // Grammar: assignment → IDENTIFIER "=" assignment | logicOr
    private Expression assignment() {
        Expression expr = logicOr();
        
        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expression value = assignment();
            
            if (expr instanceof Expression.Variable) {
                Token name = ((Expression.Variable) expr).name;
                return new Expression.Assign(name, value);
            } else if (expr instanceof Expression.Get) {
                Expression.Get get = (Expression.Get) expr;
                return new Expression.Set(get.object, get.name, value);
            }
            
            error(equals, "Invalid assignment target");
        }
        
        return expr;
    }
    
    // Grammar: logicOr → logicAnd ("||" logicAnd)*
    private Expression logicOr() {
        Expression expr = logicAnd();
        
        while (match(TokenType.OR)) {
            Token operator = previous();
            Expression right = logicAnd();
            expr = new Expression.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    // Grammar: logicAnd → equality ("&&" equality)*
    private Expression logicAnd() {
        Expression expr = equality();
        
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expression right = equality();
            expr = new Expression.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    // Grammar: equality → comparison (("==" | "!=") comparison)*
    private Expression equality() {
        Expression expr = comparison();
        
        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expr = new Expression.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    // Grammar: comparison → term ((">" | ">=" | "<" | "<=") term)*
    private Expression comparison() {
        Expression expr = term();
        
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expr = new Expression.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    // Grammar: term → factor (("+" | "-") factor)*
    private Expression term() {
        Expression expr = factor();
        
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = factor();
            expr = new Expression.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    // Grammar: factor → unary (("*" | "/" | "%") unary)*
    private Expression factor() {
        Expression expr = unary();
        
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            Token operator = previous();
            Expression right = unary();
            expr = new Expression.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    // Grammar: unary → ("!" | "-") unary | call
    private Expression unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = unary();
            return new Expression.Unary(operator, right);
        }
        
        return call();
    }
    
    // Grammar: call → primary ("(" arguments? ")" | "." IDENTIFIER)*
    private Expression call() {
        Expression expr = primary();
        
        while (true) {
            if (match(TokenType.LPAREN)) {
                expr = finishCall(expr);
            } else if (match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expect property name after '.'");
                expr = new Expression.Get(expr, name);
            } else {
                break;
            }
        }
        
        return expr;
    }
    
    private Expression finishCall(Expression callee) {
        List<Expression> arguments = new ArrayList<>();
        
        if (!check(TokenType.RPAREN)) {
            do {
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }
        
        Token paren = consume(TokenType.RPAREN, "Expect ')' after arguments");
        return new Expression.Call(callee, paren, arguments);
    }
    
    // Grammar: primary → NUMBER | STRING | "true" | "false" | "null"
    //                  | IDENTIFIER | "(" expression ")" | "new" IDENTIFIER "(" arguments? ")" | "this"
    private Expression primary() {
        if (match(TokenType.TRUE)) return new Expression.Literal(true);
        if (match(TokenType.FALSE)) return new Expression.Literal(false);
        if (match(TokenType.NULL)) return new Expression.Literal(null);
        
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expression.Literal(previous().literal);
        }
        
        if (match(TokenType.THIS)) {
            return new Expression.This(previous());
        }
        
        if (match(TokenType.IDENTIFIER)) {
            return new Expression.Variable(previous());
        }
        
        if (match(TokenType.LPAREN)) {
            Expression expr = expression();
            consume(TokenType.RPAREN, "Expect ')' after expression");
            return expr;
        }
        
        if (match(TokenType.NEW)) {
            Token className = consume(TokenType.IDENTIFIER, "Expect class name after 'new'");
            consume(TokenType.LPAREN, "Expect '(' after class name");
            
            List<Expression> arguments = new ArrayList<>();
            if (!check(TokenType.RPAREN)) {
                do {
                    arguments.add(expression());
                } while (match(TokenType.COMMA));
            }
            
            consume(TokenType.RPAREN, "Expect ')' after arguments");
            return new Expression.New(className, arguments);
        }
        
        throw error(peek(), "Expect expression");
    }
    
    // Helper methods
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }
    
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }
    
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    
    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }
    
    private Token peek() {
        return tokens.get(current);
    }
    
    private Token previous() {
        return tokens.get(current - 1);
    }
    
    private ParseError error(Token token, String message) {
        System.err.println("[Line " + token.line + "] Parser Error at '" + token.lexeme + "': " + message);
        return new ParseError();
    }
    
    private void synchronize() {
        advance();
        
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;
            
            switch (peek().type) {
                case CLASS:
                case IF:
                case WHILE:
                case FOR:
                case RETURN:
                    return;
            }
            
            advance();
        }
    }
    
    private static class ParseError extends RuntimeException {}
}