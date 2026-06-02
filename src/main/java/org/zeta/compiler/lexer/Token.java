package org.zeta.compiler.lexer;


public record Token(TokenType type, String value, int line, int column) {
}