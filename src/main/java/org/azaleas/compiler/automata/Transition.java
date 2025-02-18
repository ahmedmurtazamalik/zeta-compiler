package org.azaleas.compiler.automata;

public class Transition {
    private final State target;
    private final Object symbol; // Character, Range, or ε

    public Transition(State target, Object symbol) {
        this.target = target;
        this.symbol = symbol;
    }
}
