package org.zeta.compiler.ast;

public record BinaryExpr(Expression left, String operator, Expression right) implements Expression {}
