//
// Created by Ali Hamza Azam on 25/04/2025.
//
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <vector>
#include <string>
#include <sstream>
#include <iostream>
#include <fstream>

#define MAX_STACK_SIZE 100
#define MAX_INPUT_LEN 1000
#define MAX_PROD_LEN 100
#define MAX_SYMBOL_LEN 20
#define MAX_TABLE_ENTRIES 100

// Data structure for parsing table entries
typedef struct {
    char non_terminal[MAX_SYMBOL_LEN];
    char terminal[MAX_SYMBOL_LEN];
    char production[MAX_PROD_LEN];
} ParsingTableEntry;

ParsingTableEntry parsing_table[MAX_TABLE_ENTRIES];
int table_size = 0;
std::vector<std::string> terminals; // Store terminals from header

// Stack structure
typedef struct {
    char items[MAX_STACK_SIZE][MAX_SYMBOL_LEN];
    int top;
} Stack;

// Initialize stack with start symbol and $
void stack_init(Stack *s, const char *start_symbol) {
    s->top = -1;
    strcpy(s->items[++s->top], "$");
    strcpy(s->items[++s->top], start_symbol);
}

// Push a symbol onto the stack
void stack_push(Stack *s, const char *symbol) {
    if (s->top >= MAX_STACK_SIZE - 1) {
        fprintf(stderr, "Stack overflow!\n");
        exit(EXIT_FAILURE);
    }
    strcpy(s->items[++s->top], symbol);
}

// Pop a symbol from the stack
char* stack_pop(Stack *s) {
    if (s->top < 0) {
        fprintf(stderr, "Stack underflow!\n");
        exit(EXIT_FAILURE);
    }
    return s->items[s->top--];
}

// Peek at the top of the stack
char* stack_peek(Stack *s) {
    return (s->top >= 0) ? s->items[s->top] : NULL;
}

// Load parsing table from a CSV file
void load_parsing_table(const char *filename) {
    std::ifstream file(filename);
    if (!file.is_open()) {
        perror("Error opening parsing table file");
        exit(EXIT_FAILURE);
    }

    std::string line;
    bool header_read = false;

    while (std::getline(file, line)) {
        std::stringstream ss(line);
        std::string segment;
        std::vector<std::string> segments;

        while (std::getline(ss, segment, ',')) {
            // Trim leading/trailing whitespace if necessary (basic trim)
            segment.erase(0, segment.find_first_not_of(" \t\n\r\f\v"));
            segment.erase(segment.find_last_not_of(" \t\n\r\f\v") + 1);
            segments.push_back(segment);
        }

        if (segments.empty()) continue; // Skip empty lines

        if (!header_read) {
            // Read header: first segment is "Non-Terminal", skip it
            for (size_t i = 1; i < segments.size(); ++i) {
                terminals.push_back(segments[i]);
            }
            header_read = true;
        } else {
            // Read data row
            if (segments.size() < 1) continue; // Malformed row
            std::string non_terminal = segments[0];

            for (size_t i = 1; i < segments.size(); ++i) {
                if (i - 1 < terminals.size() && !segments[i].empty()) {
                    if (table_size >= MAX_TABLE_ENTRIES) {
                        fprintf(stderr, "Parsing table size exceeded MAX_TABLE_ENTRIES\n");
                        exit(EXIT_FAILURE);
                    }

                    std::string production_full = segments[i]; // e.g., " E → T E'"
                    std::string production_rhs;

                    // Find the arrow '→' or '->'
                    size_t arrow_pos = production_full.find("→");
                    std::string arrow_str = "→";
                    size_t arrow_len = std::string(arrow_str).length(); // Get length of arrow string

                    if (arrow_pos == std::string::npos) {
                         arrow_pos = production_full.find("->");
                         arrow_str = "->";
                         arrow_len = std::string(arrow_str).length(); // Get length of arrow string
                    }

                    if (arrow_pos != std::string::npos) {
                        // Extract substring AFTER the arrow
                        if (arrow_pos + arrow_len < production_full.length()) {
                            production_rhs = production_full.substr(arrow_pos + arrow_len);
                        } else {
                            // Arrow is at the very end, means epsilon
                            production_rhs = "ε";
                        }
                    } else if (production_full == "ε") {
                        production_rhs = "ε"; // Handle epsilon explicitly if no arrow
                    } else {
                         // No arrow found, and not epsilon. Assume the segment IS the RHS.
                         production_rhs = production_full;
                         // Optional: Add warning if format strictly requires an arrow
                         // fprintf(stderr, "Warning: No arrow found in production '%s'. Assuming it's the RHS.\n", production_full.c_str());
                    }

                    // Trim leading/trailing whitespace from the extracted RHS
                    production_rhs.erase(0, production_rhs.find_first_not_of(" \t\n\r\f\v"));
                    production_rhs.erase(production_rhs.find_last_not_of(" \t\n\r\f\v") + 1);

                    // Ensure RHS is not empty after trimming, default to epsilon if it is
                    // (unless the original segment was already epsilon)
                    if (production_rhs.empty() && production_full != "ε") {
                         production_rhs = "ε";
                    }

                    // Store the cleaned RHS
                    if (production_rhs.length() >= MAX_PROD_LEN) {
                         fprintf(stderr, "Error: Production RHS '%s' too long (max %d)\n", production_rhs.c_str(), MAX_PROD_LEN - 1);
                         exit(EXIT_FAILURE);
                    }
                    if (strlen(non_terminal.c_str()) >= MAX_SYMBOL_LEN) {
                         fprintf(stderr, "Error: Non-terminal '%s' too long (max %d)\n", non_terminal.c_str(), MAX_SYMBOL_LEN - 1);
                         exit(EXIT_FAILURE);
                    }
                     if (terminals[i - 1].length() >= MAX_SYMBOL_LEN) {
                         fprintf(stderr, "Error: Terminal '%s' too long (max %d)\n", terminals[i - 1].c_str(), MAX_SYMBOL_LEN - 1);
                         exit(EXIT_FAILURE);
                    }

                    strcpy(parsing_table[table_size].non_terminal, non_terminal.c_str());
                    strcpy(parsing_table[table_size].terminal, terminals[i - 1].c_str());
                    strcpy(parsing_table[table_size].production, production_rhs.c_str());
                    table_size++;
                }
            }
        }
    }

    if (!header_read) {
         fprintf(stderr, "Error: Could not read header from parsing table file.\n");
         exit(EXIT_FAILURE);
    }
    if (table_size == 0) {
        fprintf(stderr, "Warning: No entries loaded from parsing table.\n");
    }
}

// Get production for a non-terminal and terminal
const char* get_production(const char *nt, const char *term) {
    for (int i = 0; i < table_size; i++) {
        if (strcmp(parsing_table[i].non_terminal, nt) == 0 &&
            strcmp(parsing_table[i].terminal, term) == 0) {
            return parsing_table[i].production;
        }
    }
    return NULL; // No entry found (error)
}

// Parse a single input string
void parse_input(const char *input, const char *start_symbol) {
    Stack s;
    stack_init(&s, start_symbol);
    char input_copy[MAX_INPUT_LEN];
    strcpy(input_copy, input);
    char *token = strtok(input_copy, " "); // Initial tokenization
    int step = 1;
    bool error = false;

    printf("\nParsing: %s\n", input);
    printf("-------------------------------\n");

    while (stack_peek(&s) != NULL) {
        // Print current stack and input
        printf("Step %d:\n", step++);
        printf("Stack: ");
        for (int i = s.top; i >= 0; i--) {
            printf("%s ", s.items[i]);
        }
        // Determine current input symbol (use $ if token is NULL)
        const char *current_input = token ? token : "$";
        printf("\nInput: %s\n", current_input);

        char *top = stack_peek(&s);

        // Check for terminal match or end of input
        if (strcmp(top, current_input) == 0) {
            if (strcmp(top, "$") == 0) { // Both stack top and input are $
                printf("Action: Accept\n");
                break; // Successful parse
            } else { // Matched a terminal
                printf("Action: Match '%s'\n", token);
                stack_pop(&s);
                token = strtok(NULL, " "); // Get next token
            }
        } else { // Top is a non-terminal, need to expand
            const char *prod = get_production(top, current_input);
            if (!prod) {
                printf("Error: No production for %s on input '%s'\n", top, current_input);
                error = true;
                break;
            }
            printf("Action: Expand %s -> %s\n", top, prod);
            stack_pop(&s);

            if (strcmp(prod, "ε") != 0) { // Don't push anything for epsilon
                std::string prod_str(prod);
                std::stringstream prod_ss(prod_str);
                std::string prod_part_str;
                std::vector<std::string> parts; // Use vector to store parts

                // Split the production RHS using stringstream
                while (prod_ss >> prod_part_str) {
                    parts.push_back(prod_part_str);
                }

                // Push parts onto stack in reverse order
                for (int i = parts.size() - 1; i >= 0; i--) {
                    if (parts[i].length() >= MAX_SYMBOL_LEN) {
                        fprintf(stderr, "Error: Symbol '%s' in production '%s' too long (max %d)\n", parts[i].c_str(), prod, MAX_SYMBOL_LEN - 1);
                        error = true;
                        break; // Break inner loop
                    }
                    stack_push(&s, parts[i].c_str());
                }
                if (error) break; // Break outer loop if symbol was too long
            }
        }
        printf("\n"); // Add newline for better formatting
    }

    // Final check after loop
    if (!error && token != NULL && strcmp(stack_peek(&s), "$") == 0) {
        // If stack is accepted ($) but there's still input left
        printf("Error: Stack accepted but input remaining: %s\n", token);
        error = true;
    } else if (!error && strcmp(stack_peek(&s), "$") != 0) {
        // If input is exhausted (token is NULL) but stack isn't $
        printf("Error: Input exhausted but stack not empty. Top: %s\n", stack_peek(&s));
        error = true;
    }

    if (error) {
        printf("\nParsing failed with errors.\n");
    } else if (strcmp(stack_peek(&s), "$") == 0 && token == NULL) {
        // Ensure we accepted correctly (stack is $, input is consumed)
        printf("\nParsing succeeded.\n");
    } else {
        // Catch unexpected end states
        printf("\nParsing finished in an unexpected state.\n");
        if (token != NULL) printf("Remaining input: %s\n", token);
        printf("Final stack top: %s\n", stack_peek(&s));
    }
    printf("-------------------------------\n");
}

int main() {
    // Use the CSV file generated by Parser.cpp (adjust path if needed)
    load_parsing_table("ll1_parsing_table.csv"); 
    FILE *input_file = fopen("input_strings.txt", "r");
    if (!input_file) {
        perror("Error opening input file");
        return EXIT_FAILURE;
    }

    char line[MAX_INPUT_LEN];
    while (fgets(line, sizeof(line), input_file)) {
        line[strcspn(line, "\n")] = '\0'; // Remove newline
        if (strlen(line) > 0) {
            parse_input(line, "P"); // Assuming start symbol is 'E'
        }
    }

    fclose(input_file);
    return EXIT_SUCCESS;
}