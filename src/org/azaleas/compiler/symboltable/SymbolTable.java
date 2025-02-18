package org.azaleas.compiler.symboltable;

import org.azaleas.compiler.lexer.Token;
import org.azaleas.compiler.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SymbolTable {
    private List<SymbolTableEntry> symbolTable = new ArrayList<>();

    public void addEntry(String name, String type, Object value, String scope, boolean isConstant) {
        this.symbolTable.add(new SymbolTableEntry(name, type, value, scope, isConstant));
    }

    public boolean exists(String name) {
        return this.symbolTable.stream().anyMatch(entry -> entry.getName().equals(name));
    }

    public SymbolTableEntry getEntry(String name) {
        return this.symbolTable.stream()
                .filter(entry -> entry.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public boolean updateEntry(String name, Object value) {
        Optional<SymbolTableEntry> entry = this.symbolTable.stream()
                .filter(e -> e.getName().equals(name))
                .findFirst();
        
        if (entry.isPresent()) {
            SymbolTableEntry symbolEntry = entry.get();
            if (!symbolEntry.isConstant()) {
                symbolEntry.setValue(value);
                return true;
            }
        }
        return false;
    }

    public List<SymbolTableEntry> getEntries() {
        return new ArrayList<>(symbolTable);
    }

    private static Object parseValue(Token valueToken) {
        switch (valueToken.type()) {
            case INTEGER:
                return Integer.parseInt(valueToken.value());
            case DECIMAL:
                return Double.parseDouble(valueToken.value());
            case STRING_OR_CHAR:
                String value = valueToken.value();
                return value.substring(1, value.length() - 1); // Remove curly braces
            default:
                throw new IllegalArgumentException("Unexpected value type: " + valueToken.type());
        }
    }

    public List<SymbolTableEntry> populateSymbolTable(List<Token> tokens){
        for(int i=0 ; i<tokens.size() ; i++){
            Token token = tokens.get(i);
            if (token.type() == TokenType.KEYWORD && (token.value().equals("global") || token.value().equals("local"))) {
                if((i+3) < tokens.size()){
                    Token varName = tokens.get(i+1);
                    Token dec = tokens.get(i+2);
                    Token varValue = tokens.get(i+3);

                    if(varName.type() == TokenType.IDENTIFIER && dec.type() == TokenType.KEYWORD && dec.value().equals("is") && ( varValue.type() == TokenType.INTEGER || varValue.type() == TokenType.DECIMAL || varValue.type() == TokenType.STRING_OR_CHAR)) {
                        String name = varName.value();
                        String type = varValue.type().name();
                        Object value = parseValue(varValue);
                        String scope = token.value();
                        boolean isConstant = true;
                        addEntry(name,type,value,scope,isConstant);
                        i += 3;
                    }

                }
            }
        }
    return symbolTable;
    }

}
