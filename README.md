# 🌟 The Zeta Language
Zeta is a programming language that emphasizes readability with English-like syntax while supporting fundamental programming constructs.

---

## 🚀 Features

### 🔹 1. Data Types
- **Numbers**: Integers (e.g., `2`, `3`) and decimals (e.g., `3.14`, `10.5`). The lexer differentiates between integer and decimal tokens.
- **Strings & Characters**: Enclosed in curly braces `{}` (e.g., `{Hello, World!}`). Differentiation is based on length.

### 🔹 2. Scope
- **Local & Global Variables**: Defined using `local` or `global` keywords.
  ```zeta
  local x is 5
  global y is 2.5
  ```

### 🔹 3. I/O Operations
- **Input:** `ask x` → Requests user input.
- **Output:** `tell {Hello World}` → Prints a message.

### 🔹 4. Operators
Supports basic arithmetic operations:
- `+` (Addition)
- `-` (Subtraction)
- `*` (Multiplication)
- `/` (Division)
- `^` (Exponentiation)
- `%` (Modulus)

### 🔹 5. Expressions
- Variables can be initialized or updated with expressions:
  ```zeta
  x is 2 * 3 + 6
  x is now y + 2 / 52
  ```

### 🔹 6. Declaration & Assignment
- **Assignment:** `is` keyword
- **Update:** `now` keyword
  ```zeta
  x is 5
  x is now 20
  ```

---

## 📌 Sample Zeta Program
```zeta
<< This is a sample Zeta program
   that calculates the area of a circle >>

global pi is 3.14
local radius is 0

< This line asks the user to input value of radius >
ask radius

area is 2 * pi * radius

< This line outputs the circle's area >
tell {Area of circle is} area
```

---

# ⚡ Zeta Compiler Toolkit
The **Zeta Compiler Toolkit** is a Java-based compiler frontend that includes lexical analysis, symbol table management, and regular expression processing using **NFA/DFA automata**.

## ✨ Features

### 🔹 Lexical Analysis
- Tokenization with priority-based matching
- Supports numbers (integers/decimals), strings, comments, operators
- Handles whitespace and nested comments

### 🔹 Symbol Table
- Scope-aware variable tracking (global/local)
- Constant flagging and value updates

### 🔹 Regular Expression Engine
- Regex-to-NFA conversion
- NFA-to-DFA subset construction
- Basic pattern matching

### 🔹 Automata Implementation
- State transition system
- **ε-closure** calculations
- Transition table visualization

---

## 🛠 Installation

### **Prerequisites**
- Java JDK **17+**
- Maven **3.8+**

### **Build**
```bash
mvn clean package
```

---

🚀 **Happy Coding with Zeta!** ✨
