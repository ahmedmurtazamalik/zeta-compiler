package org.azaleas.compiler.automata;

import java.util.*;

class State {
    private final Map<Object, Set<State>> transitions;
    private final int id;
    private boolean isAccepting;
    private static int nextId = 0;

    public State() {
        this.transitions = new HashMap<>();
        this.id = nextId++;
        this.isAccepting = false;
    }

    public State(boolean isAccepting) {
        this.transitions = new HashMap<>();
        this.id = nextId++;
        this.isAccepting = isAccepting;
    }

    public void addTransition(Object symbol, State target) {
        transitions.computeIfAbsent(symbol, k -> new HashSet<>()).add(target);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        return this.id == ((State) o).id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public int getId() {
        return id;
    }

    // Returns the complete transition map.
    public Map<Object, Set<State>> getTransitions() {
        return transitions;
    }

    public boolean isAccepting() {
        return isAccepting;
    }

    public void setAccepting(boolean accepting) {
        this.isAccepting = accepting;
    }
}