package org.azaleas.compiler.automata;

import org.azaleas.Main;
import org.azaleas.compiler.lexer.TokenType;

import java.util.*;

public class NFA {
    public State getStartState() {
        return startState;
    }

    // --- Internal epsilon symbol support ---
    static class Epsilon {
        private static final Epsilon INSTANCE = new Epsilon();
        private Epsilon() {} // Private constructor
        @Override
        public String toString() {
            return "ε";
        }
    }

    private static Object getEpsilon() {
        return Epsilon.INSTANCE;
    }

    // --- NFA fields ---
    private State startState;
    private Set<State> acceptStates;
    private Set<State> allStates;

    public NFA(State startState, Set<State> acceptStates, Set<State> allStates) {
        this.startState = startState;
        this.acceptStates = acceptStates;
        this.allStates = allStates;
    }

    // --- Basic constructions ---

    // Single literal symbol
    public static NFA forSymbol(char symbol) {
        State start = new State();
        State accept = new State(true);
        start.addTransition(symbol, accept);
        Set<State> states = new HashSet<>(Arrays.asList(start, accept));
        return new NFA(start, Set.of(accept), states);
    }

    // For a character range (used in character classes)
    public static NFA forRange(Range range) {
        State start = new State();
        State accept = new State(true);
        start.addTransition(range, accept);
        Set<State> states = new HashSet<>(Arrays.asList(start, accept));
        return new NFA(start, Set.of(accept), states);
    }

    // --- Thompson's Construction Operators ---

    // Union (alternation)
    public NFA union(NFA other) {
        NFA copy1 = this.copy();
        NFA copy2 = other.copy();
        State newStart = new State();
        State newAccept = new State(true);
        Set<State> states = new HashSet<>();
        states.add(newStart);
        states.add(newAccept);
        states.addAll(copy1.allStates);
        states.addAll(copy2.allStates);

        newStart.addTransition(getEpsilon(), copy1.startState);
        newStart.addTransition(getEpsilon(), copy2.startState);

        for (State s : copy1.acceptStates) {
            s.addTransition(getEpsilon(), newAccept);
            s.setAccepting(false);
        }
        for (State s : copy2.acceptStates) {
            s.addTransition(getEpsilon(), newAccept);
            s.setAccepting(false);
        }

        return new NFA(newStart, Set.of(newAccept), states);
    }

    // Concatenation
    public NFA concatenate(NFA other) {
        NFA copy1 = this.copy();
        NFA copy2 = other.copy();
        Set<State> states = new HashSet<>();
        states.addAll(copy1.allStates);
        states.addAll(copy2.allStates);

        for (State s : copy1.acceptStates) {
            s.addTransition(getEpsilon(), copy2.startState);
            s.setAccepting(false);
        }
        return new NFA(copy1.startState, copy2.acceptStates, states);
    }

    // Kleene Star (zero or more repetitions)
    public NFA kleeneStar() {
        NFA copy = this.copy();
        State newStart = new State();
        State newAccept = new State(true);
        Set<State> states = new HashSet<>();
        states.add(newStart);
        states.add(newAccept);
        states.addAll(copy.allStates);

        // Add epsilon transition from new start to new accept (for empty string)
        newStart.addTransition(getEpsilon(), newAccept);
        // Add epsilon transition from new start to copy's start state
        newStart.addTransition(getEpsilon(), copy.startState);

        for (State s : copy.acceptStates) {
            // Add epsilon transition back to copy's start state (for repetition)
            s.addTransition(getEpsilon(), copy.startState);
            // Add epsilon transition to new accept state
            s.addTransition(getEpsilon(), newAccept);
            s.setAccepting(false);
        }
        return new NFA(newStart, Set.of(newAccept), states);
    }

    // One or more (plus)
    public NFA plus() {
        NFA copy = this.copy();
        return copy.concatenate(copy.kleeneStar());
    }

    // Optional (zero or one occurrence)
    public NFA optional() {
        NFA copy = this.copy();
        State newStart = new State();
        State newAccept = new State(true);
        Set<State> states = new HashSet<>();
        states.add(newStart);
        states.add(newAccept);
        states.addAll(copy.allStates);

        newStart.addTransition(getEpsilon(), copy.startState);
        newStart.addTransition(getEpsilon(), newAccept);

        for (State s : copy.acceptStates) {
            s.addTransition(getEpsilon(), newAccept);
            s.setAccepting(false);
        }
        return new NFA(newStart, Set.of(newAccept), states);
    }

    // --- Copying the NFA ---
    public NFA copy() {
        // Create a mapping from old state to new state.
        Map<State, State> mapping = new HashMap<>();
        for (State s : allStates) {
            mapping.put(s, new State(s.isAccepting()));
        }
        // Copy transitions.
        for (State s : allStates) {
            State newState = mapping.get(s);
            for (Map.Entry<Object, Set<State>> entry : s.getTransitions().entrySet()) {
                Object symbol = entry.getKey();
                Set<State> targets = entry.getValue();
                for (State target : targets) {
                    newState.addTransition(symbol, mapping.get(target));
                }
            }
        }
        State newStart = mapping.get(startState);
        Set<State> newAccept = new HashSet<>();
        for (State s : acceptStates) {
            newAccept.add(mapping.get(s));
        }
        return new NFA(newStart, newAccept, new HashSet<>(mapping.values()));
    }

    // --- Helper Methods for Simulation ---

    // Compute the epsilon closure for a single state.
    private Set<State> epsilonClosure(State state) {
        Set<State> closure = new HashSet<>();
        Stack<State> stack = new Stack<>();
        closure.add(state);
        stack.push(state);
        while (!stack.isEmpty()) {
            State s = stack.pop();
            for (Map.Entry<Object, Set<State>> entry : s.getTransitions().entrySet()) {
                Object symbol = entry.getKey();
                if (symbol instanceof Epsilon) {
                    Set<State> targets = entry.getValue();
                    for (State target : targets) {
                        if (!closure.contains(target)) {
                            closure.add(target);
                            stack.push(target);
                        }
                    }
                }
            }
        }
        return closure;
    }

    // Compute epsilon closure for a set of states.
    private Set<State> epsilonClosure(Set<State> states) {
        Set<State> closure = new HashSet<>();
        for (State s : states) {
            closure.addAll(epsilonClosure(s));
        }
        return closure;
    }

    // Simulate the NFA on an input string.
    public boolean matches(String input) {
        Set<State> current = epsilonClosure(Set.of(startState));

        if (input.isEmpty()) {
            return current.stream().anyMatch(State::isAccepting);
        }

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            Set<State> next = new HashSet<>();

            // Process all current states
            for (State s : current) {
                // Check all transitions from this state
                for (Map.Entry<Object, Set<State>> entry : s.getTransitions().entrySet()) {
                    Object key = entry.getKey();
                    Set<State> targets = entry.getValue();

                    // Handle literal character transitions
                    if (key instanceof Character && ((Character) key) == c) {
                        next.addAll(targets);
                    }
                    // Handle range transitions
                    else if (key instanceof Range && ((Range) key).contains(c)) {
                        next.addAll(targets);
                    }
                }
            }

            // Compute epsilon closure of all next states
            current = epsilonClosure(next);

            if (current.isEmpty()) {
                return false;
            }
        }

        // Check if we ended in an accepting state
        return current.stream().anyMatch(State::isAccepting);
    }

    // --- Regex Parsing via Thompson's Construction ---

    private enum RegexTokenType {
        LITERAL, UNION, STAR, PLUS, QUESTION, LPAREN, RPAREN, CHAR_CLASS, CONCAT
    }

    private static class RegexToken {
        RegexTokenType type;
        String value;
        RegexToken(RegexTokenType type, String value) {
            this.type = type;
            this.value = value;
        }
        @Override
        public String toString() {
            return "{" + type + ", " + value + "}";
        }
    }

    // Tokenize the regex pattern.
    private static List<RegexToken> tokenizeRegex(String regex) {
        List<RegexToken> tokens = new ArrayList<>();
        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);
            switch (c) {
                case '\\':
                    if (i + 1 < regex.length()) {
                        i++;
                        char escaped = regex.charAt(i);
                        if ("dw".indexOf(escaped) >= 0) {
                            tokens.add(new RegexToken(RegexTokenType.CHAR_CLASS, "\\" + escaped));
                        } else {
                            tokens.add(new RegexToken(RegexTokenType.LITERAL, Character.toString(escaped)));
                        }
                    }
                    break;
                case '|':
                    tokens.add(new RegexToken(RegexTokenType.UNION, "|"));
                    break;
                case '*':
                    tokens.add(new RegexToken(RegexTokenType.STAR, "*"));
                    break;
                case '+':
                    tokens.add(new RegexToken(RegexTokenType.PLUS, "+"));
                    break;
                case '?':
                    tokens.add(new RegexToken(RegexTokenType.QUESTION, "?"));
                    break;
                case '(':
                    tokens.add(new RegexToken(RegexTokenType.LPAREN, "("));
                    break;
                case ')':
                    tokens.add(new RegexToken(RegexTokenType.RPAREN, ")"));
                    break;
                case '[':
                    StringBuilder sb = new StringBuilder();
                    i++; // Skip '['
                    if (i < regex.length() && regex.charAt(i) == '^') {
                        sb.append('^');
                        i++;
                    }
                    while (i < regex.length() && regex.charAt(i) != ']') {
                        if (regex.charAt(i) == '\\' && i + 1 < regex.length()) {
                            i++;
                            sb.append(regex.charAt(i));
                        } else {
                            sb.append(regex.charAt(i));
                        }
                        i++;
                    }
                    tokens.add(new RegexToken(RegexTokenType.CHAR_CLASS, sb.toString()));
                    break;
                default:
                    tokens.add(new RegexToken(RegexTokenType.LITERAL, Character.toString(c)));
                    break;
            }
        }
        return tokens;
    }

    // Insert explicit concatenation operators.
    private static List<RegexToken> insertConcat(List<RegexToken> tokens) {
        List<RegexToken> result = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            RegexToken token = tokens.get(i);
            result.add(token);
            if (i < tokens.size() - 1) {
                RegexToken next = tokens.get(i + 1);
                if ((token.type == RegexTokenType.LITERAL || token.type == RegexTokenType.CHAR_CLASS ||
                        token.type == RegexTokenType.RPAREN || token.type == RegexTokenType.STAR ||
                        token.type == RegexTokenType.PLUS || token.type == RegexTokenType.QUESTION) &&
                        (next.type == RegexTokenType.LITERAL || next.type == RegexTokenType.CHAR_CLASS ||
                                next.type == RegexTokenType.LPAREN)) {
                    result.add(new RegexToken(RegexTokenType.CONCAT, "."));
                }
            }
        }
        return result;
    }

    // Convert infix tokens to postfix using the shunting-yard algorithm.
    private static List<RegexToken> toPostfix(List<RegexToken> tokens) {
        List<RegexToken> output = new ArrayList<>();
        Stack<RegexToken> stack = new Stack<>();
        Map<RegexTokenType, Integer> precedence = new HashMap<>();
        precedence.put(RegexTokenType.UNION, 1);
        precedence.put(RegexTokenType.CONCAT, 2);
        precedence.put(RegexTokenType.STAR, 3);
        precedence.put(RegexTokenType.PLUS, 3);
        precedence.put(RegexTokenType.QUESTION, 3);

        for (RegexToken token : tokens) {
            switch (token.type) {
                case LITERAL:
                case CHAR_CLASS:
                    output.add(token);
                    break;
                case LPAREN:
                    stack.push(token);
                    break;
                case RPAREN:
                    while (!stack.isEmpty() && stack.peek().type != RegexTokenType.LPAREN) {
                        output.add(stack.pop());
                    }
                    if (!stack.isEmpty()) {
                        stack.pop(); // Remove LPAREN
                    }
                    break;
                default:
                    while (!stack.isEmpty() && stack.peek().type != RegexTokenType.LPAREN &&
                            precedence.getOrDefault(stack.peek().type, 0) >= precedence.getOrDefault(token.type, 0)) {
                        output.add(stack.pop());
                    }
                    stack.push(token);
                    break;
            }
        }

        while (!stack.isEmpty()) {
            RegexToken token = stack.pop();
            if (token.type != RegexTokenType.LPAREN) {
                output.add(token);
            }
        }

        return output;
    }

    // Build an NFA from a postfix regex.
    private static NFA buildFromPostfix(List<RegexToken> postfix) {
        if (postfix.isEmpty()) {
            throw new IllegalArgumentException("Empty postfix expression");
        }

        Stack<NFA> stack = new Stack<>();
        for (RegexToken token : postfix) {
            try {
                switch (token.type) {
                    case LITERAL:
                        stack.push(forSymbol(token.value.charAt(0)));
                        break;
                    case CHAR_CLASS:
                        String value = token.value;
                        if (value.startsWith("\\d")) {
                            stack.push(forRange(new Range('0', '9')));
                        } else if (value.startsWith("\\w")) {
                            NFA letters = forRange(new Range('a', 'z'))
                                    .union(forRange(new Range('A', 'Z')))
                                    .union(forRange(new Range('0', '9')))
                                    .union(forSymbol('_'));
                            stack.push(letters);
                        } else {
                            boolean negated = value.startsWith("^");
                            int startIdx = negated ? 1 : 0;
                            NFA classNFA = null;

                            for (int i = startIdx; i < value.length(); i++) {
                                char c = value.charAt(i);
                                NFA charNFA = forSymbol(c);
                                classNFA = (classNFA == null) ? charNFA : classNFA.union(charNFA);
                            }

                            if (classNFA == null) {
                                throw new IllegalArgumentException("Empty character class");
                            }
                            stack.push(classNFA);
                        }
                        break;
                    case CONCAT:
                        if (stack.size() < 2) throw new IllegalArgumentException("Invalid concatenation");
                        NFA right = stack.pop();
                        NFA left = stack.pop();
                        stack.push(left.concatenate(right));
                        break;
                    case UNION:
                        if (stack.size() < 2) throw new IllegalArgumentException("Invalid union");
                        right = stack.pop();
                        left = stack.pop();
                        stack.push(left.union(right));
                        break;
                    case STAR:
                        if (stack.isEmpty()) throw new IllegalArgumentException("Invalid star operation");
                        stack.push(stack.pop().kleeneStar());
                        break;
                    case PLUS:
                        if (stack.isEmpty()) throw new IllegalArgumentException("Invalid plus operation");
                        stack.push(stack.pop().plus());
                        break;
                    case QUESTION:
                        if (stack.isEmpty()) throw new IllegalArgumentException("Invalid question operation");
                        stack.push(stack.pop().optional());
                        break;
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Error processing token: " + token, e);
            }
        }

        if (stack.isEmpty()) {
            throw new IllegalArgumentException("No result NFA");
        }
        return stack.pop();
    }

    // Public factory method.
    public static NFA fromRegex(String regex) {
        List<RegexToken> tokens = tokenizeRegex(regex);
        tokens = insertConcat(tokens);
        List<RegexToken> postfix = toPostfix(tokens);
        return buildFromPostfix(postfix);
    }

    // --- Main for Testing / Transition Table Printing ---
    public static void main(String[] args) {
        Map<String, String> patternMap = new HashMap<>();
        patternMap.put("OPERATOR", "[+\\-*/%]");
        patternMap.put("DECIMAL", "[+-]?(\\d+\\.\\d{1,5}|\\.\\d{1,5})([eE][+-]?\\d+)?");
        patternMap.put("INTEGER", "[+-]?\\d+");
        patternMap.put("IDENTIFIER", "[a-z]+");
        patternMap.put("KEYWORD", "(global|local|tell|ask|is|now|true|false)");
        patternMap.put("EXPONENT", "\\^");
        patternMap.put("STRING_OR_CHAR", "\\{([^{}]*)\\}");
        patternMap.put("SINGLE_LINE_COMMENT", "<[^>]*>");
        patternMap.put("MULTI_LINE_COMMENT", "<<.*?>>");
//        patternMap.put("WHITESPACE", "[ \t\n\r\f]+");
        // Process each pattern
        for (Map.Entry<String, String> entry : patternMap.entrySet()) {
            String tokenType = entry.getKey();
            String pattern = entry.getValue();
            System.out.println("=== Token: " + tokenType + " ===");
            System.out.println("Pattern: " + pattern);

            try {
                NFA nfa = NFA.fromRegex(pattern);

                // Print the transition table
                System.out.println("Transition Table:");
                System.out.println("------------------------------------------------");
                System.out.println("State    | Symbol             | Target States");
                System.out.println("------------------------------------------------");

                List<State> sortedStates = new ArrayList<>(nfa.allStates);
                sortedStates.sort(Comparator.comparingInt(State::getId));

                for (State s : sortedStates) {
                    for (Map.Entry<Object, Set<State>> trans : s.getTransitions().entrySet()) {
                        Object symbol = trans.getKey();
                        String symStr = (symbol instanceof Epsilon) ? "ε" :
                                      (symbol instanceof Range) ? symbol.toString() :
                                      symbol.toString();
                        Set<State> targets = trans.getValue();
                        StringBuilder targetStr = new StringBuilder();
                        for (State target : targets) {
                            if (targetStr.length() > 0) targetStr.append(", ");
                            targetStr.append(target.getId());
                        }
                        System.out.printf("%-8d | %-18s | %-12s%n",
                            s.getId(), symStr, targetStr.toString());
                    }
                }

                System.out.println("------------------------------------------------\n");

            } catch (Exception e) {
                System.out.println("Error generating NFA for token: " + tokenType);
                e.printStackTrace();
            }
        }
    }

    Set<State> getAllStates() {
        return allStates;
    }
}
