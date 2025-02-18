package org.azaleas.compiler.automata;

public class Range {
    private final char start;
    private final char end;

    public Range(char start, char end) {
        this.start = start;
        this.end = end;
    }

    public boolean contains(char c) {
        // TODO: Return true if the character is within the range.
        return false;
    }

    @Override
    public boolean equals(Object o) {
        // TODO: Define equality based on start and end characters.
        return false;
    }

    @Override
    public int hashCode() {
        // TODO: Return the hash code based on start and end characters.
        return 0;
    }
}