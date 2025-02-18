package org.azaleas.compiler.automata;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class State {
    private final Map<Object, State> transitions;
    private final int id;
    private boolean isAccepting;
    private static int nextId = 0;

    public State() {
        this.transitions = Map.of();
        this.id = nextId++;
        this.isAccepting = false;
    }

    public State(boolean isAccepting) {
        this.transitions = Map.of();
        this.id = nextId++;
        this.isAccepting = isAccepting;
    }

    public State(Map<Object, State> transitions, int id) {
        this.transitions = transitions;
        this.id = id;
        this.isAccepting = false;
    }

    public State(Map<Object, State> transitions, int id, boolean isAccepting) {
        this.transitions = transitions;
        this.id = id;
        this.isAccepting = isAccepting;
    }

    public void addTransition(Object symbol, State target) {
        // TODO: Add a transition on the given symbol to the target state.


    }

    public State getTransition(Object symbol) {
        // TODO: Return the target state for the given symbol.
        return null;
    }


    @Override
    public boolean equals(Object o) {
        // TODO: Define equality based on the unique state id.
        return false;
    }

    @Override
    public int hashCode() {
        // TODO: Return the hash code based on the state's unique id.
        return 0;
    }

    public int getId() {
        return id;
    }

    public Map<Object, Object> getTransitions() {
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