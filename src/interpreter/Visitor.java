package interpreter;

import parser.ast.Expression;
import parser.ast.Statement;
import parser.ast.Declaration;

public interface Visitor<R> {
    // Expression visitors
    R visitBinaryExpr(Expression.Binary expr);
    R visitLiteralExpr(Expression.Literal expr);
    R visitVariableExpr(Expression.Variable expr);
    R visitAssignExpr(Expression.Assign expr);
    R visitUnaryExpr(Expression.Unary expr);
    R visitCallExpr(Expression.Call expr);
    R visitGetExpr(Expression.Get expr);
    R visitSetExpr(Expression.Set expr);
    R visitNewExpr(Expression.New expr);
    R visitThisExpr(Expression.This expr);
    
    // Statement visitors
    R visitExpressionStmt(Statement.ExpressionStmt stmt);
    R visitBlockStmt(Statement.Block stmt);
    R visitIfStmt(Statement.If stmt);
    R visitWhileStmt(Statement.While stmt);
    R visitForStmt(Statement.For stmt);
    R visitReturnStmt(Statement.Return stmt);
    
    // Declaration visitors
    R visitVarDecl(Declaration.Var decl);
    R visitMethodDecl(Declaration.Method decl);
    R visitClassDecl(Declaration.Class decl);
}