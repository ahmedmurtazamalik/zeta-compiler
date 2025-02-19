package org.azaleas.compiler.automata;

import java.util.HashMap;
import java.util.Map;

public class State {
    private final Map<Object, State> transitions;
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

    public State(Map<Object, State> transitions, int id) {
        this.transitions = new HashMap<>(transitions);
        this.id = id;
        this.isAccepting = false;
    }

    public State(Map<Object, State> transitions, int id, boolean isAccepting) {
        this.transitions = new HashMap<>(transitions);
        this.id = id;
        this.isAccepting = isAccepting;
    }

    public void addTransition(Object symbol, State target) {
        // TODO: Add a transition on the given symbol to the target state.
        this.transitions.put(symbol, target);
    }

    public State getTransition(Object symbol) {
        // TODO: Return the target state for the given symbol.
        // Check for exact match first
        State exactMatch = this.transitions.get(symbol);
        if (exactMatch != null) {
            return exactMatch;
        }

        // If symbol is a Character, check for ranges
        if (symbol instanceof Character) {
            char c = (Character) symbol;
            for (Map.Entry<Object, State> entry : transitions.entrySet()) {
                Object key = entry.getKey();
                if (key instanceof Range) {
                    Range range = (Range) key;
                    if (range.contains(c)) {
                        return entry.getValue();
                    }
                }
            }
        }

        return null; // No transition found
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        return this.id == ((State) o).id;
    }

    @Override
    public int hashCode() {
        // TODO: Return the hash code based on the state's unique id.
        // id is unique anyway?
        return id;
    }

    public int getId() {
        return id;
    }

    public Map<Object, State> getTransitions() {
        return Map.copyOf(transitions);
    }

    public boolean isAccepting() {
        return isAccepting;
    }

    public void setAccepting(boolean accepting) {
        this.isAccepting = accepting;
    }

    public Map<Character, State> getDeterministicTransitions() {
        Map<Character, State> deterministicTransitions = Map.of();
        for (Map.Entry<Object, State> entry : transitions.entrySet()) {
            if (entry.getKey() instanceof Character) {
                deterministicTransitions.put((Character) entry.getKey(), entry.getValue());
            }
        }
        return deterministicTransitions;
    }
}