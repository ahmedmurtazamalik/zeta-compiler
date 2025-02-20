package org.azaleas;

import org.azaleas.compiler.errors.ErrorHandler;
import org.azaleas.compiler.lexer.Lexer;
import org.azaleas.compiler.lexer.Token;
import org.azaleas.compiler.lexer.TokenType;

import java.util.List;

import org.azaleas.compiler.lexer.Preprocessor;
import org.azaleas.compiler.symboltable.SymbolTable;
import org.azaleas.compiler.symboltable.SymbolTableEntry;

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
        ErrorHandler errorHandler = new ErrorHandler();
        Lexer lexer = new Lexer(errorHandler);
        List<Token> tokens = lexer.tokenize(preprocessed);

        System.out.println("Tokens:");
        for (Token token : tokens) {
            System.out.printf("Type: %-20s Value: %-20s\n",
                    token.type(),
                    token.value());
        }

        System.out.println("\nErrors:");
        errorHandler.printErrors();

        SymbolTable symbolTable = new SymbolTable();
        symbolTable.populateSymbolTable(tokens);

        System.out.println("\n-------------------\n");
        System.out.println("Symbol Table:");
        for (SymbolTableEntry entry : symbolTable.getEntries()) {
            System.out.println(entry);
        }

    }
}
