package org.azaleas.compiler.automata;

import java.util.Stack;
import java.util.HashSet;
import java.util.Set;

public class Regex {
    private final String pattern;

    public Regex(String pattern) {
        this.pattern = pattern;
    }

    public NFA toNFA() {
        String postfix = toPostfix();
        Stack<NFA> nfaStack = new Stack<>();

        for (char c : postfix.toCharArray()) {
            if (isLetter(c)) {
                nfaStack.push(NFA.forSymbol(c));
            } else if (c == '*') {
                NFA nfa = nfaStack.pop();
                nfaStack.push(nfa.kleeneStar());
            } else if (c == '.') {
                NFA nfa2 = nfaStack.pop();
                NFA nfa1 = nfaStack.pop();
                nfaStack.push(nfa1.concatenate(nfa2));
            } else if (c == '|') {
                NFA nfa2 = nfaStack.pop();
                NFA nfa1 = nfaStack.pop();
                nfaStack.push(nfa1.union(nfa2));
            }
        }

        NFA resultNFA = nfaStack.pop();
        resultNFA.printTransitionTable();
        return resultNFA;
    }

    public String toPostfix() {
        StringBuilder output = new StringBuilder();
        Stack<Character> operators = new Stack<>();
        
        // Insert explicit concatenation operator '.'
        StringBuilder augmentedPattern = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char current = pattern.charAt(i);
            augmentedPattern.append(current);
            
            if (i + 1 < pattern.length()) {
                char next = pattern.charAt(i + 1);
                if (isLetter(current) && isLetter(next) ||
                    isLetter(current) && next == '(' ||
                    current == ')' && isLetter(next) ||
                    current == '*' && isLetter(next) ||
                    current == '*' && next == '(' ||
                    current == ')' && next == '(') {
                    augmentedPattern.append('.');
                }
            }
        }
        
        for (char c : augmentedPattern.toString().toCharArray()) {
            if (isLetter(c)) {
                output.append(c);
            } else if (c == '(') {
                operators.push(c);
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    output.append(operators.pop());
                }
                if (!operators.isEmpty()) {
                    operators.pop(); // Remove '('
                }
            } else {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(c)) {
                    output.append(operators.pop());
                }
                operators.push(c);
            }
        }
        
        while (!operators.isEmpty()) {
            if (operators.peek() != '(') {
                output.append(operators.pop());
            } else {
                operators.pop();
            }
        }
        
        return output.toString();
    }
    
    private boolean isLetter(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '-';
    }

    private int precedence(char c) {
        switch (c) {
            case '|': return 1;
            case '.': return 2; // Concatenation
            case '*': case '+': case '?': return 3; // Add missing operators
            default: return 0;
        }
    }

    public boolean matches(String input) {
        // TODO: Return true if the input matches the regex pattern.
        return false;
    }
}