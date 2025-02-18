package org.azaleas.compiler.symboltable;

import org.azaleas.compiler.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SymbolTable {
    private List<SymbolTableEntry> symbolTable = new ArrayList<>();

    public void addEntry(String name, String type, Object value, TokenType scope, boolean isConstant) {
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
}
