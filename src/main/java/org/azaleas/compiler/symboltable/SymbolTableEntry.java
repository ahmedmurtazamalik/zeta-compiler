package org.azaleas.compiler.symboltable;

import org.azaleas.compiler.lexer.TokenType;

import java.util.Objects;

public class SymbolTableEntry {
    private String name;
    private String type;
    private Object value;
    private String scope;
    private boolean isConstant;

    public SymbolTableEntry(String name, String type, Object value, String scope, boolean isConstant) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.scope = scope;
        this.isConstant = isConstant;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public String getScope() {
        return scope;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "SymbolTableEntry{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value=" + value +
                ", scope=" + scope +
                ", isConstant=" + isConstant +
                '}';
    }

    public boolean equals(Object obj) {
        if (obj instanceof SymbolTableEntry) {
            SymbolTableEntry entry = (SymbolTableEntry) obj;
            return name.equals(entry.name) && type.equals(entry.type) && Objects.equals(scope, entry.scope) && isConstant == entry.isConstant;
        }
        return false;
    }

    public int hashCode() {
        return name.hashCode() + type.hashCode() + scope.hashCode() + (isConstant ? 1 : 0);
    }
}