package org.zeta.compiler.ast;

public record VarDecl(String scope, String name, Expression initializer) implements Statement {}
