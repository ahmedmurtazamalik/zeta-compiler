package org.azaleas.compiler.automata;

import java.util.*;

public class DFA {

    public static class DFAState {
        private final Set<State> nfaStates;
        private final int id;
        private final boolean isAccepting;
        private final Map<Character, DFAState> transitions;
        private static int nextId = 0;

        public DFAState(Set<State> nfaStates, boolean isAccepting) {
            this.nfaStates = nfaStates;
            this.isAccepting = isAccepting;
            this.id = nextId++;
            this.transitions = new HashMap<>();
        }

        public int getId() {
            return id;
        }

        public Map<Character, DFAState> getTransitions() {
            return transitions;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DFAState)) return false;
            return nfaStates.equals(((DFAState) o).nfaStates);
        }

        @Override
        public int hashCode() {
            return nfaStates.hashCode();
        }

        public boolean isAccepting() {
            return isAccepting;
        }
    }

    private final DFAState startState;
    private final Set<DFAState> allStates;

    public DFA(DFAState startState, Set<DFAState> allStates) {
        this.startState = startState;
        this.allStates = allStates;
    }

    public static DFA fromNFA(NFA nfa) {
        Set<Character> alphabet = getAlphabet(nfa);
        Map<Set<State>, DFAState> dfaStateMap = new HashMap<>();
        Queue<Set<State>> queue = new LinkedList<>();
        Set<DFAState> allDfaStates = new HashSet<>();

        Set<State> startSubset = epsilonClosure(nfa, Set.of(nfa.getStartState()));
        DFAState dfaStart = new DFAState(startSubset, startSubset.stream().anyMatch(State::isAccepting));
        dfaStateMap.put(startSubset, dfaStart);
        queue.add(startSubset);
        allDfaStates.add(dfaStart);

        while (!queue.isEmpty()) {
            Set<State> currentSubset = queue.poll();
            DFAState currentDfaState = dfaStateMap.get(currentSubset);

            for (char symbol : alphabet) {
                Set<State> targetSubset = computeTargetStates(currentSubset, symbol);
                targetSubset = epsilonClosure(nfa, targetSubset);
                if (targetSubset.isEmpty()) continue;

                DFAState targetDfaState = dfaStateMap.get(targetSubset);
                if (targetDfaState == null) {
                    targetDfaState = new DFAState(targetSubset, targetSubset.stream().anyMatch(State::isAccepting));
                    dfaStateMap.put(targetSubset, targetDfaState);
                    queue.add(targetSubset);
                    allDfaStates.add(targetDfaState);
                }
                currentDfaState.getTransitions().put(symbol, targetDfaState);
            }
        }
        return new DFA(dfaStart, allDfaStates);
    }

    private static Set<State> computeTargetStates(Set<State> states, char symbol) {
        Set<State> targetStates = new HashSet<>();
        for (State s : states) {
            for (Map.Entry<Object, Set<State>> entry : s.getTransitions().entrySet()) {
                Object key = entry.getKey();
                if ((key instanceof Character && (Character) key == symbol) ||
                    (key instanceof Range && ((Range) key).contains(symbol))) {
                    targetStates.addAll(entry.getValue());
                }
            }
        }
        return targetStates;
    }

    private static Set<State> epsilonClosure(NFA nfa, Set<State> states) {
        Set<State> closure = new HashSet<>(states);
        Stack<State> stack = new Stack<>();
        stack.addAll(states);

        while (!stack.isEmpty()) {
            State s = stack.pop();
            s.getTransitions().entrySet().stream()
                .filter(e -> e.getKey() instanceof NFA.Epsilon)
                .flatMap(e -> e.getValue().stream())
                .filter(closure::add)
                .forEach(stack::push);
        }
        return closure;
    }

    private static Set<Character> getAlphabet(NFA nfa) {
        Set<Character> alphabet = new HashSet<>();
        for (State s : nfa.getAllStates()) {
            for (Object key : s.getTransitions().keySet()) {
                if (key instanceof Character) {
                    alphabet.add((Character) key);
                } else if (key instanceof Range) {
                    Range range = (Range) key;
                    for (char c = range.getStart(); c <= range.getEnd(); c++) {
                        alphabet.add(c);
                    }
                }
            }
        }
        return alphabet;
    }

    public void printTransitionTable() {
        System.out.println("Transition Table:");
        System.out.println("------------------------------------------------");
        System.out.println("State    | Symbol             | Target State");
        System.out.println("------------------------------------------------");
        
        allStates.stream()
            .sorted(Comparator.comparingInt(DFAState::getId))
            .forEach(state -> state.getTransitions().forEach((symbol, target) ->
                System.out.printf("%-8d | %-18s | %-12d%n", 
                    state.getId(), symbol, target.getId())));
                    
        System.out.println("------------------------------------------------");
    }

    public static void main(String[] args) {
        Map<String, String> patternMap = new LinkedHashMap<>();
        patternMap.put("OPERATOR", "[+\\-*/%]");
        patternMap.put("DECIMAL", "[+-]?(\\d+\\.\\d{1,5}|\\.\\d{1,5})([eE][+-]?\\d+)?");
        patternMap.put("INTEGER", "[+-]?\\d+");
        patternMap.put("IDENTIFIER", "[a-z]+");
        patternMap.put("KEYWORD", "(global|local|tell|ask|is|now|true|false)");
        patternMap.put("EXPONENT", "\\^");
        patternMap.put("STRING_OR_CHAR", "\\{([^{}]*)\\}");
        patternMap.put("SINGLE_LINE_COMMENT", "<[^>]*>");
        patternMap.put("MULTI_LINE_COMMENT", "<<.*?>>");

        patternMap.forEach((tokenType, pattern) -> {
            System.out.println("=== Token: " + tokenType + " ===");
            System.out.println("Pattern: " + pattern);
            try {
                DFA.fromNFA(NFA.fromRegex(pattern)).printTransitionTable();
            } catch (Exception e) {
                System.out.println("Error generating DFA for token: " + tokenType);
                e.printStackTrace();
            }
            System.out.println();
        });
    }
}
