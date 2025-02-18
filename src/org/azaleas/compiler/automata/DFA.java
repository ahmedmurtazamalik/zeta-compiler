package org.azaleas.compiler.automata;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;


public class DFA {
    private State startState;
    private Set<State> acceptStates;
    private Set<State> allStates;
    
    public DFA() {
        this.startState = new State();
        this.acceptStates = Set.of(new State());
        this.allStates = Set.of(startState, acceptStates.iterator().next());
    }
    
    public boolean accepts(String input) {
        State currentState = startState;
        for (char c : input.toCharArray()) {
            currentState = getNextState(currentState, c);
            if (currentState == null) {
                // No valid transition, input is rejected.
                return false;
            }
        }
        return acceptStates.contains(currentState);
    }

    public State getStartState() {
        return startState;
    }

    public Set<State> getAcceptStates() {
        return acceptStates;
    }

    public Set<State> getAllStates() {
        return allStates;
    }

    private State getNextState(State currentState, Object symbol) {
        return currentState.getTransition(symbol);
    }

    public DFA minimize() {
        // TODO: Implement DFA minimization.
        return this; // Placeholder; return a minimized DFA.
    }

    /**
     * (Optional) Builds a DFA from an NFA using the subset construction method.
     * This might alternatively belong in a converter class.
     */
    public static DFA fromNFA(NFA nfa) {
        // ... existing code ...
        Set<Set<State>> dfaStates = new HashSet<>();
        Map<Set<State>, State> nfaStatesToDfaState = new HashMap<>();
        Queue<Set<State>> unprocessedStates = new LinkedList<>();
        
        // Create start state from NFA's epsilon closure
        Set<State> nfaStartStates = nfa.getEpsilonClosure(nfa.getStartState());
        State dfaStartState = new State();
        dfaStates.add(nfaStartStates);
        nfaStatesToDfaState.put(nfaStartStates, dfaStartState);
        unprocessedStates.offer(nfaStartStates);
        
        // Create DFA instance
        DFA dfa = new DFA();
        dfa.startState = dfaStartState;
        dfa.allStates = new HashSet<>();
        dfa.allStates.add(dfaStartState);
        dfa.acceptStates = new HashSet<>();
        
        // Process all states
        while (!unprocessedStates.isEmpty()) {
            Set<State> currentNFAStates = unprocessedStates.poll();
            State currentDFAState = nfaStatesToDfaState.get(currentNFAStates);
            
            // Check if this DFA state should be accepting
            if (currentNFAStates.stream().anyMatch(s -> nfa.getAcceptStates().contains(s))) {
                dfa.acceptStates.add(currentDFAState);
            }
            
            // For each input symbol
            for (Character symbol : nfa.getAlphabet()) {
                Set<State> nextStates = new HashSet<>();
                
                // Get all possible next states from current NFA states
                for (State nfaState : currentNFAStates) {
                    Set<State> transitions = nfa.getTransitions(nfaState, symbol);
                    if (transitions != null) {
                        for (State transition : transitions) {
                            nextStates.addAll(nfa.getEpsilonClosure(transition));
                        }
                    }
                }
                
                if (!nextStates.isEmpty()) {
                    State nextDFAState;
                    if (!nfaStatesToDfaState.containsKey(nextStates)) {
                        nextDFAState = new State();
                        nfaStatesToDfaState.put(nextStates, nextDFAState);
                        dfaStates.add(nextStates);
                        unprocessedStates.offer(nextStates);
                        dfa.allStates.add(nextDFAState);
                    } else {
                        nextDFAState = nfaStatesToDfaState.get(nextStates);
                    }
                    currentDFAState.addTransition(symbol, nextDFAState);
                }
            }
        }
        
        return dfa;
    }

    public void printTransitionTable() {
        System.out.println("DFA Transition Table:");
        for (State state : allStates) {
            System.out.println("State " + state.getId() + (acceptStates.contains(state) ? " (accept)" : ""));
            Map<Character, State> transitions = state.getDeterministicTransitions();
            for (Map.Entry<Character, State> entry : transitions.entrySet()) {
                System.out.println("   " + entry.getKey() + " -> State " + entry.getValue().getId());
            }
        }
    }

}