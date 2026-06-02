package org.zeta.compiler.lexer;

public class Preprocessor {
    public String process(String input) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        int len = input.length();
        boolean inString = false;

        while (i < len) {
            char c = input.charAt(i);

            // Handle string literal boundaries
            if (c == '{' && !inString) {
                inString = true;
                result.append(c);
                i++;
                continue;
            } else if (c == '}' && inString) {
                inString = false;
                result.append(c);
                i++;
                continue;
            }

            if (inString) {
                result.append(c);
                i++;
                continue;
            }

            // Look ahead for multi-line comment start: <<
            if (c == '<' && i + 1 < len && input.charAt(i + 1) == '<') {
                // Replace "<<" with two spaces
                result.append("  ");
                i += 2;
                
                // Scan until we find the end: >>
                while (i < len) {
                    if (input.charAt(i) == '>' && i + 1 < len && input.charAt(i + 1) == '>') {
                        result.append("  ");
                        i += 2;
                        break;
                    } else {
                        char commentChar = input.charAt(i);
                        if (commentChar == '\n') {
                            result.append('\n'); // Preserve the newline to maintain line indices
                        } else {
                            result.append(' '); // Blank out the character
                        }
                        i++;
                    }
                }
                continue;
            }

            // Look ahead for single-line comment start: <
            if (c == '<') {
                result.append(' ');
                i++;
                
                // Scan until we find the end: >
                while (i < len) {
                    if (input.charAt(i) == '>') {
                        result.append(' ');
                        i++;
                        break;
                    } else {
                        char commentChar = input.charAt(i);
                        if (commentChar == '\n') {
                            result.append('\n'); // Preserve the newline
                        } else {
                            result.append(' '); // Blank out the character
                        }
                        i++;
                    }
                }
                continue;
            }

            // Regular character
            result.append(c);
            i++;
        }

        return result.toString();
    }
}