package org.azaleas.compiler.automata;
import java.util.*;

public class TransitionTable {
    private final State startState;


    public TransitionTable(State startState) {
        this.startState = startState;
    }

    public String toTableString() {
        Set<State> allStates = getAllStates();
        StringBuilder table = new StringBuilder();

        table.append("Transition Table\n");
        table.append("------------------------------------------------\n");
        table.append(String.format("%-8s | %-18s | %s%n",
                "State", "Symbol", "Target State"));
        table.append("------------------------------------------------\n");

        for (State state : allStates) {
            Map<Object, Set<State>> transitions = state.getTransitions();

            if (transitions.isEmpty()) {
                table.append(String.format("%-8d | %-18s | %s%n",
                        state.getId(), "∅", "∅"));
            } else {
                boolean isFirstTransition = true;
                for (Map.Entry<Object, Set<State>> entry : transitions.entrySet()) {
                    Object symbol = entry.getKey();
                    Set<State> targetStates = entry.getValue();

                    if (isFirstTransition) {
                        table.append(String.format("%-8d | %-18s | %s%n",
                                state.getId(), formatSymbol(symbol), targetStates));
                        isFirstTransition = false;
                    } else {
                        table.append(String.format("%-8s | %-18s | %s%n",
                                "", formatSymbol(symbol), targetStates));
                    }
                }
            }
        }
        return table.toString();
    }

    private String formatSymbol(Object symbol) {
        if (symbol instanceof Character) {
            char c = (Character) symbol;
            return c == 'ε' ? "ε" : "'" + c + "'";
        } else if (symbol instanceof Range) {
            Range range = (Range) symbol;
            return "'" + range.getStart() + "'-'" + range.getEnd() + "'";
        }
        return symbol.toString();
    }

    // breadth first search
    private Set<State> getAllStates() {
        Set<State> visited = new HashSet<>();
        Queue<State> queue = new LinkedList<>();
        queue.add(startState);
        visited.add(startState);

        while (!queue.isEmpty()) {
            State current = queue.poll();

            for (Set<State> neighbor : current.getTransitions().values()) {
                for (State state : neighbor) {
                    if (!visited.contains(state)) {
                        queue.add(state);
                        visited.add(state);
                    }
                }
            }
        }
        return visited;
    }
}