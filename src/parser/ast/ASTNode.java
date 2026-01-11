package parser.ast;

import interpreter.Visitor;

public interface ASTNode {
    <R> R accept(Visitor<R> visitor);
}