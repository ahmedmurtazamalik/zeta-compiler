#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <map>
#include <set>
#include <algorithm>
#include <iomanip>


using namespace std;

// class to assist in console output redirection to file
class TeeBuf : public streambuf {
public:
    // initializes two streaming buffers
    TeeBuf(streambuf* sb1, streambuf* sb2) : sb1_(sb1), sb2_(sb2) {}

protected:

    // write the character c to both output streams
    int overflow(int c) override {
        if (c == EOF) {
            return !EOF;
        }
        else {
            int r1 = sb1_->sputc(c);
            int r2 = sb2_->sputc(c);
            return (r1 == EOF || r2 == EOF) ? EOF : c;
        }
    }

    // flush both output streams
    int sync() override {
        int r1 = sb1_->pubsync();
        int r2 = sb2_->pubsync();
        return (r1 == 0 && r2 == 0) ? 0 : -1;
    }

private:
    streambuf* sb1_;
    streambuf* sb2_;
};

// Class to store and process Context-Free Grammar (CFG)
class Grammar {
public:
    // Map to store the original cfg
    map<string, vector<string>> cfg;

    // Maps to store first and follow sets
    map<string, set<string>> first;
    map<string, set<string>> follow;

    // Sets of terminals and non-terminals
    set<string> nonTerminals;
    set<string> terminals;

    // map to hold the parsing table
    map<pair<string, string>, string> parsingTable;


    // Default constructor
    Grammar() = default;

    // Function to read grammar from a file
    int readGrammar(const string& fileName) {
        string line;

        // File opening validation
        ifstream file(fileName);
        if (!file) {
            cerr << "Error: Could not open file." << endl;
            return 0;
        }

        // Read each line of the file
        while (getline(file, line)) {
            // Read one line of the cfg (a production rule) into a string stream
            istringstream iss(line);
            string lhs, arrow, rhs;

            // Extract non-terminal on the Left hand side into lhs, and the arrow "->" into arrow
            iss >> lhs >> arrow;

            // Production format validation
            if (arrow != "->") {
                cerr << "Error: Invalid production format." << endl;
                return 0;
            }

            // Extract right-hand side (actual productions) into rhs
            getline(iss, rhs);

            // Read the rhs of the cfg into a string stream
            istringstream rhsStream(rhs);
            string production;

            // Split productions by '|' and store them in the map
            while (getline(rhsStream, production, '|')) { cfg[lhs].push_back(production); }

        }

        file.close();
        return 1;
    }

    // Function to print the CFG
    void printGrammar() {
        for (const auto& rule : cfg) {
            // Print non-terminal of rule
            cout << rule.first << " -> ";
            for (size_t i = 0; i < rule.second.size(); ++i) {
                // Print right-hand side productions
                cout << rule.second[i];
                // Separate multiple productions
                if (i < rule.second.size() - 1) cout << " | ";
            }
            cout << endl;
        }
    }

    // Function that applies left factoring to the CFG
    int leftFactoring() {
        bool changed = true;
        // counter for new unique non-terminals
        int newSymbolCount = 0;

        // Iterate until no changes are made in an iteration
        while (changed) {
            changed = false;
            map<string, vector<string>> new_cfg;

            // Iterate over the CFG
            for (auto& rule : cfg) {
                string lhs = rule.first;
                vector<string> productions = rule.second;

                // remove all leading and trailing whitespaces from  the productions
                for (auto& prod : productions) {
                    if (prod.find_first_not_of(" \t") != string::npos) {
                        prod.erase(0, prod.find_first_not_of(" \t"));
                        prod.erase(prod.find_last_not_of(" \t") + 1);
                    }
                }

                // Continue factoring until no more changes can be made
                bool localChanged = true;
                // Iterate until productions remain and there was a change made
                while (localChanged && productions.size() > 1) {
                    localChanged = false;

                    // finding longest common prefix among the productions
                    for (size_t i = 0; i < productions.size(); i++) {
                        // Indexes of production with common prefixes
                        vector<size_t> commonGroup;
                        string longestPrefix;

                        // Compare current production with others to find common prefixes
                        for (size_t j = i + 1; j < productions.size(); j++) {
                            size_t minLen = min(productions[i].length(), productions[j].length());
                            size_t k = 0;

                            // getting the length of the common prefix
                            while (k < minLen && productions[i][k] == productions[j][k]) k++;

                            // get the prefix in a string
                            string prefix = productions[i].substr(0, k);

                            // validation for empty prefixes or whitespace prefixes
                            // this eliminates meaningless prefixes
                            if (!prefix.empty() && prefix.find_first_not_of(" \t") != string::npos && k > 0) {
                                if (longestPrefix.empty() || prefix.length() >= longestPrefix.length()) {
                                    // Update longest prefix and group
                                    if (prefix.length() > longestPrefix.length()) {
                                        commonGroup.clear();
                                        longestPrefix = prefix;
                                    }
                                    commonGroup.push_back(j);
                                }
                            }
                        }

                        // factoring out the meaningful common prefix
                        if (!longestPrefix.empty() && !commonGroup.empty()) {
                            // update flags to reflect changes in the grammar
                            localChanged = changed = true;
                            string newNonTerminal = lhs + "_" + to_string(++newSymbolCount);
                            vector<string> suffixes;

                            // Extract suffixes from factored productions
                            string suffix_i = productions[i].substr(longestPrefix.length());
                            if (suffix_i.empty()) suffix_i = "ε";

                            suffixes.push_back(suffix_i);

                            // keeping track of what productions to remove
                            vector<bool> toRemove(productions.size(), false);
                            toRemove[i] = true;

                            // Process all grouped productions
                            for (size_t idx : commonGroup) {
                                string suffix = productions[idx].substr(longestPrefix.length());

                                if (suffix.empty()) suffix = "ε";

                                suffixes.push_back(suffix);
                                toRemove[idx] = true;
                            }

                            // Making new productions
                            vector<string> new_prods;

                            // adding the common productions
                            new_prods.push_back(longestPrefix + newNonTerminal);

                            // add the non-common productions
                            for (size_t k = 0; k < productions.size(); k++) {
                                if (!toRemove[k]) { new_prods.push_back(productions[k]); }
                            }

                            // update the cfg with new rules
                            new_cfg[newNonTerminal] = suffixes;
                            productions = new_prods;

                            // restart checking
                            break;
                        }
                    }
                }

                // update current non-terminal's productions
                new_cfg[lhs] = productions;
            }

            // replace old cfg with new left factored updated cfg
            cfg = new_cfg;
        }

        // success
        return 1;
    }

    // Function to remove left recursion in productions
    int leftRecursion() {
        // map to store updated cfg
        map<string, vector<string>> new_cfg;

        // Iterate over the CFG
        for (const auto& rule : cfg) {
            string lhs = rule.first;
            vector<string> productions = rule.second;

            // vectors to separate valid and left recursive (invalid) productions
            vector<string> leftRecursiveProds;
            vector<string> nonLeftRecursiveProds;

            // iterate over each production in the cfg
            for (const string& prod : productions) {

                // check if the production starts with the same non-terminal
                istringstream iss(prod);
                string firstSymbol;
                iss >> firstSymbol;

                if (firstSymbol == lhs) {
                    // store the part after the recursive symbol
                    string alpha;
                    getline(iss, alpha);
                    // add the invalid production to LR productions vector
                    leftRecursiveProds.push_back(alpha);
                }
                else {
                    // add valid producion to valid set
                    nonLeftRecursiveProds.push_back(prod);
                }
            }

            // If there are no left-recursive productions, keep the original rule
            if (leftRecursiveProds.empty()) {
                new_cfg[lhs] = productions;
                continue;
            }

            // Create a new non-terminal for the LR non-terminal
            string newNonTerminal = lhs + "'";

            // Create new productions
            vector<string> newLhsProds;
            vector<string> newNonTerminalProds;

            // If there are no valid productions, add epsilon to avoid empty rule
            if (nonLeftRecursiveProds.empty()) { nonLeftRecursiveProds.emplace_back("ε"); }

            // Create productions for A -> β A'
            for (const string& beta : nonLeftRecursiveProds) {
                string newProd = beta;

                // Don't append the new non-terminal to epsilon
                if (newProd != "ε") { newProd += " " + newNonTerminal; }
                else { newProd = newNonTerminal; }

                newLhsProds.push_back(newProd);
            }

            // Create productions for A' -> α A' | ε
            for (const string& alpha : leftRecursiveProds) {
                string newProd = alpha + " " + newNonTerminal;
                newNonTerminalProds.push_back(newProd);
            }

            // Add epsilon production for A'
            newNonTerminalProds.push_back("ε");

            // Update the grammar
            new_cfg[lhs] = newLhsProds;
            new_cfg[newNonTerminal] = newNonTerminalProds;
        }

        // Replace old grammar with new one
        cfg = new_cfg;
        return 1;
    }

    // identify all terminals and non-terminals in the grammar and store them
    void initializeSymbols() {
        nonTerminals.clear();
        terminals.clear();

        // add all LHS symbols to non-terminal set
        for (const auto& rule : cfg) {
            nonTerminals.insert(rule.first);
        }

        // Find terminals
        for (const auto& rule : cfg) {
            for (const string& prod : rule.second) {
                istringstream iss(prod);
                string token;

                while (iss >> token) {
                    // if it is not in non-terminal set and is not epislon, it is a terminal
                    if (token != "ε" && nonTerminals.find(token) == nonTerminals.end()) { terminals.insert(token); }
                }
            }
        }
    }

    // function to make FIRST set for all non-terminals
    int computeFirst() {
        initializeSymbols();

        // Initialize first sets
        first.clear();
        for (const string& nonTerm : nonTerminals) {
            first[nonTerm] = {};
        }

        // For all terminals a, First(a) = {a}
        for (const string& term : terminals) {
            first[term].insert(term);
        }

        bool changed = true;
        // iterate until no changes in an iteration
        while (changed) {
            changed = false;

            // iterate over the cfg
            for (const auto& rule : cfg) {
                string lhs = rule.first;

                // iterate over each production of a rule
                for (const string& prod : rule.second) {
                    // add epislons in first sets
                    if (prod == "ε") {
                        if (first[lhs].insert("ε").second) changed = true;
                        continue;
                    }


                    istringstream iss(prod);
                    string symbol;
                    vector<string> symbols;

                    // get string symbols (non-terminals and terminals both) from the production
                    while (iss >> symbol) {
                        symbols.push_back(symbol);
                    }

                    // compute First for this production
                    // flag to track if all symbols in production can derive ε
                    bool allDeriveEpsilon = true;

                    for (const auto & symbol : symbols) {
                        // flag to track if current symbol derives an epsilon
                        bool currDerivedEpsilon = false;

                        // Add all elements from First(symbol) except epsilon to First(lhs)
                        for (const string& elem : first[symbol]) {
                            if (elem == "ε") currDerivedEpsilon = true;
                            else {
                                // mark change if it's a new addition
                                if (first[lhs].insert(elem).second) changed = true;
                            }
                        }

                        // if this symbol cannot derive epsilon, stop processing more symbols
                        if (!currDerivedEpsilon) {
                            allDeriveEpsilon = false;
                            break;
                        }
                    }

                    // If all symbols of the production can derive epsilon, add epsilon to First(lhs)
                    if (allDeriveEpsilon && !symbols.empty()) {
                        if (first[lhs].insert("ε").second) changed = true;
                    }
                }
            }
        }

        return 1;
    }


    // Helper function to compute First of a production
    bool computeFirstOfProduction(const string& prod, set<string>& firstSet) {
        // If production is epsilon, First includes epsilon
        if (prod == "ε") {
            firstSet.insert("ε");
            return true;
        }

        // Split the production into symbols
        istringstream iss(prod);
        string symbol;
        vector<string> symbols;

        while (iss >> symbol) symbols.push_back(symbol);

        if (symbols.empty()) return false;

        // Compute First for this production
        bool allDeriveEpsilon = true;

        for (const auto & symbol : symbols) {
            bool currDerivedEpsilon = false;

            // For terminals, First(a) = {a}
            if (nonTerminals.find(symbol) == nonTerminals.end() && symbol != "ε") {
                firstSet.insert(symbol);
                allDeriveEpsilon = false;
                break;
            }

            // For non-terminals, use the precomputed First set
            for (const string& elem : first[symbol]) {
                if (elem == "ε") currDerivedEpsilon = true;
                else firstSet.insert(elem);
            }

            // If this symbol cannot derive epsilon, stop here
            if (!currDerivedEpsilon) {
                allDeriveEpsilon = false;
                break;
            }
        }

        return allDeriveEpsilon;
    }


    // function to make FIRST set for all non-terminals
    int computeFollow() {

        // in case someone tries to make follow before first lol
        if (first.empty()) computeFirst();

        // initialize Follow sets
        follow.clear();
        for (const string& nonTerm : nonTerminals) follow[nonTerm] = {};

        // Add $ to Follow of start symbol
        string startSymbol = cfg.begin()->first;
        follow[startSymbol].insert("$");


        bool changed = true;
        // iterate until no changes in an iteration
        while (changed) {
            changed = false;

            // Iterate over rules in the CFG
            for (const auto& rule : cfg) {
                string lhs = rule.first;


                for (const string& prod : rule.second) {
                    // Skip epsilon productions
                    if (prod == "ε") continue;

                    istringstream iss(prod);
                    string token;
                    vector<string> symbols;

                    // get non-terminals and terminals as string symbols
                    while (iss >> token) symbols.push_back(token);

                    // iterate over each symbol in the production
                    for (size_t i = 0; i < symbols.size(); i++) {
                        // skip terminals
                        if (nonTerminals.find(symbols[i]) == nonTerminals.end()) continue;

                        // Compute First of the rest of the production
                        bool restDerivedEpsilon = true;
                        // symbol has something after it
                        if (i < symbols.size() - 1) {
                            for (size_t j = i + 1; j < symbols.size(); j++) {
                                bool currDerivedEpsilon = false;

                                // Add all elements of FIRST(symbols[j]) except ε to FOLLOW(symbols[i])
                                for (const string& elem : first[symbols[j]]) {
                                    if (elem == "ε") {
                                        // Mark that current symbol can derive ε
                                        currDerivedEpsilon = true;
                                    }
                                    else {
                                        // insert element into FOLLOW(symbols[i]) and mark a change was made
                                        if (follow[symbols[i]].insert(elem).second) changed = true;
                                    }
                                }

                                // if current symbol cannot derive epsilon, stop processing more symbols
                                if (!currDerivedEpsilon) {
                                    restDerivedEpsilon = false;
                                    break;
                                }
                            }
                        }

                        // if symbol is at the end or all symbols after it can derive epsilon
                        // add Follow(A) to Follow(B)
                        if (i == symbols.size() - 1 || restDerivedEpsilon) {
                            for (const string& elem : follow[lhs]) {
                                if (follow[symbols[i]].insert(elem).second) changed = true;
                            }
                        }
                    }
                }
            }
        }

        return 1;
    }

    // function to print First and Follow sets
    void printFirstAndFollow() {
        cout << "\nFirst Sets:" << endl;
        // iterate over all non-terminals and print items of each non-terminal's first set
        for (const auto& nonTerm : nonTerminals) {
            cout << "First(" << nonTerm << ") = { ";
            for (const auto& term : first[nonTerm]) cout << term << ", ";
            cout << "}" << endl;
        }


        cout << "\nFollow Sets:" << endl;
        // iterate over all non-terminals and print items of each non-terminal's follow set
        for (const auto& nonTerm : nonTerminals) {
            cout << "Follow(" << nonTerm << ") = { ";
            for (const auto& term : follow[nonTerm]) cout << term << ", ";
            cout << "}" << endl;
        }
    }

    int computeParsingTable() {
        // Make sure First and Follow sets are computed
        if (first.empty() || follow.empty()) {
            computeFirst();
            computeFollow();
        }

        // Clear the existing parsing table
        parsingTable.clear();

        // Add $ as a terminal for end of input
        terminals.insert("$");

        // Iterative over rules in the cfg
        for (const auto& rule : cfg) {
            string nonTerm = rule.first;

            for (const string& prod : rule.second) {

                // skip empty productions
                if (prod.empty()) continue;

                // extract FIRST set for the right-hand side (RHS) of the production
                set<string> firstOfRHS;
                bool derivesEpsilon = computeFirstOfProduction(prod, firstOfRHS);


                // Step 1: Populate parsing table for FIRST(α)
                // If a terminal 'a' is in FIRST(α), add A → α to M[A, a]
                for (const string& term : firstOfRHS) {
                    // skip epislon, they will be handled by follows
                    if (term != "ε") {
                        pair<string, string> tableKey = make_pair(nonTerm, term);

                        // c    heck for conflicts (non-LL(1) grammar)
                        if (parsingTable.find(tableKey) != parsingTable.end()) {
                            cout << "Grammar is not LL(1): Conflict at [" << nonTerm
                                << ", " << term << "] between \""
                                << parsingTable[tableKey] << "\" and \""
                                << prod << "\"" << endl;
                        }

                        // if not epsilon and not conflict, add the production to the parsing table
                        parsingTable[tableKey] = prod;
                    }
                }

                // Step 2: Populate parsing table for FOLLOW(A) if ε is in FIRST(α)
                // If α can derive ε, add A → α to M[A, b] for all b in FOLLOW(A)
                if (derivesEpsilon || firstOfRHS.find("ε") != firstOfRHS.end()) {
                    for (const string& b : follow[nonTerm]) {
                        pair<string, string> tableKey = make_pair(nonTerm, b);

                        // check for conflicts
                        if (parsingTable.find(tableKey) != parsingTable.end()) {
                            cout << "Grammar is not LL(1): Conflict at [" << nonTerm
                                << ", " << b << "] between \""
                                << parsingTable[tableKey] << "\" and \""
                                << prod << "\"" << endl;
                        }

                        // if not conflict, add the production to the parsing table
                        parsingTable[tableKey] = prod;
                    }
                }
            }
        }

        return 1;
    }

    void printParsingTable() {
        cout << "\nLL(1) Parsing Table:" << endl;

        const int colWidth = 20;
        map<pair<string, string>, string> truncatedEntries;

        // Top border
        cout << "+" << string(colWidth, '-');
        for (const auto& term : terminals) cout << "+" << string(colWidth, '-');
        cout << "+" << endl;

        // Header row
        cout << "|" << setw(colWidth) << left << " NT \\ Terminal";
        for (const auto& term : terminals) cout << "|" << setw(colWidth) << left << term;
        cout << "|" << endl;

        // Separator line
        cout << "+" << string(colWidth, '-');
        for (size_t i = 0; i < terminals.size(); i++) cout << "+" << string(colWidth, '-');
        cout << "+" << endl;

        // MAIN PARSING TABLE
        for (const auto& nonTerm : nonTerminals) {
            cout << "|" << setw(colWidth) << left << nonTerm;

            for (const auto& term : terminals) {
                pair<string, string> key = { nonTerm, term };
                string cellContent;

                if (parsingTable.count(key)) {
                    string production = parsingTable[key];

                    /*
                    // replace epsilon with ^
                    size_t pos;
                    while ((pos = production.find("\u03B5")) != string::npos) production.replace(pos, 2, "^");
                    */

                    cellContent = nonTerm + " → " + production;

                    // truncation
                    if (cellContent.length() > colWidth) {
                        // truncate after the arrow
                        size_t truncPos = cellContent.find(' ', 10);

                        if (truncPos == string::npos) truncPos = colWidth - 4;

                        truncatedEntries[key] = production;
                        cellContent = cellContent.substr(0, truncPos) + "...";
                    }

                }
                else {
                    // explicitly pad empty cells with spaces
                    cellContent = string(colWidth, ' ');
                }

                cout << "|" << setw(colWidth) << left << cellContent;
            }
            cout << "|" << endl;
        }

        // bottom border
        cout << "+" << string(colWidth, '-');
        for (size_t i = 0; i < terminals.size(); i++) cout << "+" << string(colWidth, '-');
        cout << "+" << endl;

        // Key for truncated entries
        if (!truncatedEntries.empty()) {
            cout << "\nKey:\n";
            cout << "* Truncated Entries (full production):\n";
            for (const auto& entry : truncatedEntries) cout << "  - " << entry.first.first << " → " << entry.second << endl;
        }
        cout << "* Empty cells indicate no production\n";
    }

    void writeParsingTableToCSV(const string& filename) {
        ofstream csvFile(filename);
        if (!csvFile.is_open()) {
            cerr << "Error: Could not open file " << filename << endl;
            return;
        }

        // Write CSV header (terminals)
        csvFile << "Non-Terminal";
        for (const auto& term : terminals) csvFile << "," << term;
        csvFile << "\n";

        // Write rows for each non-terminal
        for (const auto& nonTerm : nonTerminals) {
            csvFile << nonTerm; // First column: non-terminal

            for (const auto& term : terminals) {
                auto key = make_pair(nonTerm, term);
                string production;

                if (parsingTable.find(key) != parsingTable.end()) {
                    production = nonTerm + " → " + parsingTable[key];

                    /*
                    // Replace ε with ^
                    size_t pos;
                    while ((pos = production.find("ε")) != string::npos) production.replace(pos, 2, "^");
                    */
                }

                csvFile << "," << production;
            }
            csvFile << "\n";
        }

        csvFile.close();
        cout << "Parsing table saved to " << filename << endl;
    }

};

int main() {
    string fileName = "cfg.txt";
    Grammar cfg;

    // Redirect cout and cerr to both console and file
    ofstream outFile("output.log");

    // TeeBuf to duplicate cout output to both the console and the file
    TeeBuf coutTeeBuf(cout.rdbuf(), outFile.rdbuf());

    // TeeBuf to duplicate cerr output to both the console and the file
    TeeBuf cerrTeeBuf(cerr.rdbuf(), outFile.rdbuf());

    // redirect cout to use the custom TeeBuf, saving the original buffer
    streambuf* originalCout = cout.rdbuf(&coutTeeBuf);

    // redirect cerr to use the custom TeeBuf, saving the original buffer
    streambuf* originalCerr = cerr.rdbuf(&cerrTeeBuf);

    if (cfg.readGrammar(fileName)) {
        cout << "Original Grammar:" << endl;
        cfg.printGrammar();

        // left factoring
        cfg.leftFactoring();
        cout << "\nGrammar after left factoring:" << endl;
        cfg.printGrammar();


        // left recursion elimination
        cfg.leftRecursion();
        cout << "\nGrammar after left recursion elimination:" << endl;
        cfg.printGrammar();


        // Compute First and Follow sets
        cfg.computeFirst();
        cfg.computeFollow();
        cout << "\nGrammar after computing first follow:" << endl;
        cfg.printFirstAndFollow();


        // Compute LL(1) Parsing Table
        cfg.computeParsingTable();
        cout << "\nGrammar after computing parsing table:" << endl;
        cfg.printParsingTable();

        cfg.writeParsingTableToCSV("ll1_parsing_table.csv");

        }

    // restore original buffers and close file
    cout.rdbuf(originalCout);
    cerr.rdbuf(originalCerr);
    outFile.close();

    return 0;
}

