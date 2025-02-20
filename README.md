# The Zeta Language
Zeta is a programming language that emphasizes readability with English-like syntax while supporting fundamental programming constructs.
## Features
### 1. Data Types
  - Numbers can be typed as integers e.g. 2,3 or decimals e.g. 3.14, 10.5. The lexer will produce a different token for an integer and a different token for a decimal number.
  - Strings and characters are both enclosed in curly braces e.g. {This is a string} and the tokens for character and string are differentiated by the length of the string enclosed in the braces. 
### 2. Scope
  - Variable scope is specified by keywords “local” or global” before the identifier e.g. “local x is 5” or “global y is 2.5”
### 3. I/O Operations
  A variable is inputted by the keyword “ask” e.g. “ask x” and a variable is outputted by the keyword “tell” e.g. “tell {Hello World}”
### 4. Operators
  Basic binary arithmetic operations can be performed by using the traditional symbols of +, -, * and / for addition, subtraction, multiplication, and division. Exponentiation can be performed by the symbol ^ and modulus/remainder can be obtained by the symbol %.
### 5. Expressions
  An expression can be assigned to a variable upon initialization as well as updating e.g “x is 2*3+6” or “x is now y+2/52”
### 6. Declaration / Assignment
  Variables can be assigned with the keyword “is” e.g “x is 5” or updated with the keyword “now” e.g “x is now 20”

## Sample Zeta Program:
```
<< This is a sample Zeta program
that calculates the area of a circle >>

global pi is 3.14
local radius is 0

< This line asks the user to input value of radius >
ask radius

area is 2*pi*radius

< This line outputs the circle's area >
tell {Area of circle is} area
```

# Zeta Compiler Toolkit
The project is a Java-based compiler frontend implementation featuring lexical analysis, symbol table management, and regular expression processing via NFA/DFA automata.

## Features

- **Lexical Analysis**
  - Tokenization with priority-based matching
  - Supports numbers (integers/decimals), strings, comments, operators
  - Handles whitespace and nested comments
- **Symbol Table**
  - Scope-aware variable tracking (global/local)
  - Constant flagging and value updates
- **Regular Expression Engine**
  - Regex-to-NFA conversion
  - NFA-to-DFA subset construction
  - Basic pattern matching
- **Automata Implementation**
  - State transition system
  - ε-closure calculations
  - Transition table visualization

## Installation

1. **Prerequisites**
   - Java JDK 17+
   - Maven 3.8+

2. **Build**
```bash
mvn clean package
