package org.zeta.compiler.ast;

import java.util.List;

public record Program(List<Statement> statements) implements ASTNode {}
