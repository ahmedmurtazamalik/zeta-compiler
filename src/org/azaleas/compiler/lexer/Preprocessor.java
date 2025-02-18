package org.azaleas.compiler.lexer;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Preprocessor {
    public String process(String input) {
        // Remove all whitespace at the start and end of lines
        input = input.replaceAll("^\\s+|\\s+$", "");
        
        // Replace multiple whitespace with a single space
        input = input.replaceAll("\\s+", " ");
        
        // Remove comments
        input = removeComments(input);
        
        return input;
    }

    private String removeComments(String input) {
        // Remove multi-line comments
        input = input.replaceAll("<<.*?>>", "");
        
        // Remove single-line comments
        input = input.replaceAll("<[^>]*>", "");
        
        return input;
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