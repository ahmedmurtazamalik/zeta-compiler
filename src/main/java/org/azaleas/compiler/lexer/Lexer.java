package org.azaleas.compiler.lexer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

public class Lexer {
    private static final Map<TokenType, String> PATTERNS = new HashMap<>();
    private static final Map<TokenType, Integer> TOKEN_PRIORITIES = new HashMap<>();
    private int position = 0;
    private String input;
    private List<Token> tokens;

    static {
        PATTERNS.put(TokenType.WHITESPACE, "[ \t\n\r\f]+");
        PATTERNS.put(TokenType.MULTI_LINE_COMMENT, "<<.*?>>");
        PATTERNS.put(TokenType.SINGLE_LINE_COMMENT, "<[^>]*>");
        PATTERNS.put(TokenType.STRING_OR_CHAR, "\\{([^{}]*)\\}");
        PATTERNS.put(TokenType.KEYWORD, "(global|local|tell|ask|is|now|true|false)");
        PATTERNS.put(TokenType.EXPONENT, "\\^");
        PATTERNS.put(TokenType.OPERATOR, "[+\\-*/%]");
        PATTERNS.put(TokenType.DECIMAL, "[+-]?(\\d+\\.\\d{1,5}|\\.\\d{1,5})([eE][+-]?\\d+)?");
        PATTERNS.put(TokenType.INTEGER, "[+-]?\\d+");
        PATTERNS.put(TokenType.IDENTIFIER, "[a-z]+");

        TOKEN_PRIORITIES.put(TokenType.WHITESPACE, 0);
        TOKEN_PRIORITIES.put(TokenType.MULTI_LINE_COMMENT, 1);
        TOKEN_PRIORITIES.put(TokenType.SINGLE_LINE_COMMENT, 1);
        TOKEN_PRIORITIES.put(TokenType.STRING_OR_CHAR, 2);
        TOKEN_PRIORITIES.put(TokenType.KEYWORD, 3);
        TOKEN_PRIORITIES.put(TokenType.EXPONENT, 2);
        TOKEN_PRIORITIES.put(TokenType.OPERATOR, 2);
        TOKEN_PRIORITIES.put(TokenType.DECIMAL, 3);
        TOKEN_PRIORITIES.put(TokenType.INTEGER, 2);
        TOKEN_PRIORITIES.put(TokenType.IDENTIFIER, 1);
    }

    public Lexer() {
        this.tokens = new ArrayList<>();
    }

    public List<Token> tokenize(String input) {
        this.input = input;
        this.position = 0;
        this.tokens.clear();

        while (position < input.length()) {
            boolean matched = false;
            Token bestMatch = null;
            int bestPriority = -1;

            for (Map.Entry<TokenType, String> entry : PATTERNS.entrySet()) {
                Pattern pattern = Pattern.compile("^" + entry.getValue());
                Matcher matcher = pattern.matcher(input.substring(position));

                if (matcher.find()) {
                    String match = matcher.group();
                    int priority = TOKEN_PRIORITIES.get(entry.getKey());

                    if (bestMatch == null || priority > bestPriority) {
                        bestMatch = new Token(entry.getKey(), match);
                        bestPriority = priority;
                        matched = true;
                    }
                }
            }

            if (!matched) {
                throw new RuntimeException("Unexpected character at position " + position + ": " + input.charAt(position));
            }

            if (bestMatch.type() != TokenType.WHITESPACE) {
                tokens.add(bestMatch);
            }
            position += bestMatch.value().length();
        }

        return tokens;
    }
}




//static {
//    PATTERNS.put(TokenType.WHITESPACE, "[ \t\n\r\f]+");
//    PATTERNS.put(TokenType.MULTI_LINE_COMMENT, "<<.*?>>");
//    PATTERNS.put(TokenType.SINGLE_LINE_COMMENT, "<[^>]*>");
//    PATTERNS.put(TokenType.STRING_OR_CHAR, "\\{([^{}]*)\\}");
//    PATTERNS.put(TokenType.KEYWORD, "(global|local|tell|ask|is|now|true|false)");
//    PATTERNS.put(TokenType.EXPONENT, "\\^");
//    PATTERNS.put(TokenType.OPERATOR, "[+\\-*/%]");
//    PATTERNS.put(TokenType.DECIMAL, "[+-]?(\\d+\\.\\d{1,5}|\\.\\d{1,5})([eE][+-]?\\d+)?");
//    PATTERNS.put(TokenType.INTEGER, "[+-]?\\d+");
//    PATTERNS.put(TokenType.IDENTIFIER, "[a-z]+");
//
//    // Set token priorities
//    TOKEN_PRIORITIES.put(TokenType.WHITESPACE, 0);
//    TOKEN_PRIORITIES.put(TokenType.MULTI_LINE_COMMENT, 1);
//    TOKEN_PRIORITIES.put(TokenType.SINGLE_LINE_COMMENT, 1);
//    TOKEN_PRIORITIES.put(TokenType.STRING_OR_CHAR, 2);
//    TOKEN_PRIORITIES.put(TokenType.KEYWORD, 3);
//    TOKEN_PRIORITIES.put(TokenType.EXPONENT, 2);
//    TOKEN_PRIORITIES.put(TokenType.OPERATOR, 2);
//    TOKEN_PRIORITIES.put(TokenType.DECIMAL, 2);
//    TOKEN_PRIORITIES.put(TokenType.INTEGER, 2);
//    TOKEN_PRIORITIES.put(TokenType.IDENTIFIER, 1);
//}