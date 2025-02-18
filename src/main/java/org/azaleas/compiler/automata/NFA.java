package org.azaleas.compiler.automata;

import java.util.*;

public class NFA {
    private State startState;
    private Set<State> acceptStates;
    private Set<State> allStates;

    public NFA() {
        this.startState = new State();
        this.acceptStates = new HashSet<>();
        this.allStates = new HashSet<>();
        allStates.add(startState);
    }

    private NFA(State startState, Set<State> acceptStates, Set<State> allStates) {
        this.startState = startState;
        this.acceptStates = acceptStates;
        this.allStates = allStates;
    }

    public static NFA forSymbol(char symbol) {
        State startState = new State(new HashMap<>(), 0);
        State acceptState = new State(new HashMap<>(), 1, true);
        startState.addTransition(symbol, acceptState);

        return new NFA(startState, Set.of(acceptState), Set.of(startState, acceptState));
    }

    public static NFA forRange(Range range) {
        NFA nfa = new NFA();
        State acceptState = new State(true);
        nfa.allStates.add(acceptState);
        nfa.startState.addTransition(range, acceptState); // Use Range instead of char
        nfa.acceptStates.add(acceptState);
        return nfa;
    }

    public NFA union(NFA other) {
        NFA copy1 = this.copy();
        NFA copy2 = other.copy();

        NFA result = new NFA();
        State newAccept = new State(true);

        result.allStates.clear();

        result.allStates.add(result.startState);
        result.allStates.add(newAccept);

        result.allStates.addAll(copy1.allStates);
        result.allStates.addAll(copy2.allStates);

        result.startState.addTransition('ε', copy1.getStartState());
        result.startState.addTransition('ε', copy2.getStartState());

        for (State s : copy1.getAcceptStates()) {
            s.addTransition('ε', newAccept);
        }

        for (State s : copy2.getAcceptStates()) {
            s.addTransition('ε', newAccept);
        }

        result.acceptStates.clear();
        result.acceptStates.add(newAccept);

        return result;
    }


    public NFA concatenate(NFA other) {
        NFA copy1 = this.copy();
        NFA copy2 = other.copy();

        for (State s : copy1.getAcceptStates()) {
            s.addTransition('ε', copy2.getStartState());
        }

        NFA result = new NFA();
        result.allStates.clear();

        result.startState = copy1.getStartState();

        result.acceptStates.clear();
        result.acceptStates.addAll(copy2.getAcceptStates());

        result.allStates.addAll(copy1.allStates);
        result.allStates.addAll(copy2.allStates);

        return result;
    }


    public NFA kleeneStar() {
        NFA result = new NFA();
        NFA copy = this.copy();

        result.allStates.addAll(copy.allStates);
        State newAcceptState = new State(true);
        result.allStates.add(newAcceptState);

        result.startState.addTransition('ε', copy.startState);
        result.startState.addTransition('ε', newAcceptState);

        for (State acceptState : copy.acceptStates) {
            acceptState.addTransition('ε', copy.startState);
            acceptState.addTransition('ε', newAcceptState);
        }

        result.acceptStates.clear();
        result.acceptStates.add(newAcceptState);

        return result;
    }

    public NFA copy() {
        NFA copy = new NFA();

        Map<State, State> stateMap = new HashMap<>();

        for (State oldState : this.allStates) {
            State newState = new State(new HashMap<>(), oldState.getId());
            newState.setAccepting(oldState.isAccepting());
            stateMap.put(oldState, newState);
        }

        for (State oldState : this.allStates) {
            State newState = stateMap.get(oldState);
            for (Map.Entry<Object, Object> entry : oldState.getTransitions().entrySet()) {
                Object symbol = entry.getKey();
                State oldTarget = (State) entry.getValue();
                State newTarget = stateMap.get(oldTarget);
                newState.addTransition(symbol, newTarget);
            }
        }

        copy.allStates = new HashSet<>(stateMap.values());
        copy.startState = stateMap.get(this.startState);

        copy.acceptStates = new HashSet<>();
        for (State oldAccept : this.acceptStates) {
            copy.acceptStates.add(stateMap.get(oldAccept));
        }

        return copy;
    }


    public Set<State> getAcceptStates() {
        return acceptStates;
    }

    public State getStartState() {
        return startState;
    }

    public Set<State> getAllStates() {
        return allStates;
    }

    public void printTransitionTable() {
        // Create instance of the transition table
        TransitionTable table = new TransitionTable(startState);
    }

    public Set<Character> getAlphabet() {
        Set<Character> alphabet = new HashSet<>();
        for (State s : this.allStates) {
            Map<Character, State> transitions = s.getDeterministicTransitions();
            if (transitions != null) {
                alphabet.addAll(transitions.keySet());
            }
        }
        return alphabet;
    }

    public Set<State> getEpsilonClosure(State state) {
        Set<State> closure = new HashSet<>();
        Queue<State> queue = new LinkedList<>();
        queue.offer(state);

//        while (!queue.isEmpty()) {
//            State current = queue.poll();
//            closure.add(current);
//
//            for (State next : current.getTransitions().values()) {
//                if (next.getTransitions().containsKey('ε') && !closure.contains(next)) {
//                    queue.offer(next);
//                }
//            }
//        }

        return closure;
    }

    public Set<State> getTransitions(State nfaState, Character symbol) {
        return null;
    }
}
