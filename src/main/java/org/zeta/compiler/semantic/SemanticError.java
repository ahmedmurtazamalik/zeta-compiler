package org.zeta.compiler.semantic;

public class SemanticError extends RuntimeException {
    public SemanticError(String message) {
        super(message);
    }
}
