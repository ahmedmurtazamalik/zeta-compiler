package org.zeta.compiler.ast;

import java.util.List;

public record Tell(List<Expression> expressions) implements Statement {}
