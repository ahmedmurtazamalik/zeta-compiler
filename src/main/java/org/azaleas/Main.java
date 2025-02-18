package org.azaleas;

import org.azaleas.compiler.lexer.Lexer;
import org.azaleas.compiler.lexer.Token;

import java.util.List;

import org.azaleas.compiler.lexer.Lexer;
import org.azaleas.compiler.lexer.Token;
import org.azaleas.compiler.preprocessor.Preprocessor;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Test input
        String testProgram = """
            << This is a multi-line comment
               Testing the lexer >>
            global x is 42
            local y is 3.14
            < This is a single line comment >
            tell {Hello, World!}
            x is now x + y ^ 2
            """;

        System.out.println("Original input:");
        System.out.println(testProgram);
        System.out.println("\n-------------------\n");

        // Preprocess
        Preprocessor preprocessor = new Preprocessor();
        String preprocessed = preprocessor.process(testProgram);

        System.out.println("After preprocessing:");
        System.out.println(preprocessed);
        System.out.println("\n-------------------\n");

        // Tokenize
        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.tokenize(preprocessed);

        System.out.println("Tokens:");
        for (Token token : tokens) {
            System.out.printf("Type: %-20s Value: %-20s\n",
                    token.type(),
                    token.value());
        }
    }
}