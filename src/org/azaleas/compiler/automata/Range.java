package org.azaleas.compiler.automata;

public class Range {
    private final char start;
    private final char end;

    public Range(char start, char end) {
        this.start = start;
        this.end = end;
    }

    public boolean contains(char c) {
        return c >= start && c <= end;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Range)) return false;
        Range range = (Range) o;
        return start == range.start && end == range.end;
    }

    public int hashCode() {
        return 31 * start + end;
    }
}