/* ==========================================================================
   Zeta Playground App Controller
   ========================================================================== */

// 1. Example Templates Dataset
const EXAMPLES = [
    {
        id: 'hello',
        title: 'Hello World',
        desc: 'Simple greetings print statement',
        code: `< Simple Zeta Greeting >
tell {Hello, World!}
`
    },
    {
        id: 'circle-area',
        title: 'Circle Area Calculator',
        desc: 'Computes circle area using variables & math exponents',
        code: `< Calculates the area of a circle >
global pi is 3.14
local radius is 5
local area is pi * radius ^ 2

tell {Radius of circle is: } radius
tell {The calculated area is: } area
`
    },
    {
        id: 'user-input',
        title: 'User Interactivity',
        desc: 'Demonstrates console reading (ask) & responses',
        code: `< Ask for user details >
local name is {Anonymous}
local age is 0

tell {What is your name?}
ask name

tell {How old are you?}
ask age

tell {Hello, } name {. You are } age { years old.}
`
    },
    {
        id: 'variables-scope',
        title: 'Variables Scoping',
        desc: 'Illustrates global/local keywords and mutations',
        code: `< Demonstrates scope boundary mutability >
global max is 100
local count is 0

tell {Max is: } max
tell {Count starts at: } count

count is now count + 1
tell {Count is now: } count
`
    },
    {
        id: 'precedence',
        title: 'Operator Precedence',
        desc: 'Shows mathematical operators and parenthetical precedence',
        code: `< Math equations evaluating precedence >
local a is 2
local b is 3
local c is 4

local expr1 is a + b * c
local expr2 is (a + b) * c
local expr3 is a ^ b ^ 2
local expr4 is 10 / 3 + 10 % 3

tell {a + b * c = } expr1
tell {(a + b) * c = } expr2
tell {a ^ b ^ 2 = } expr3
tell {10 / 3 + 10 % 3 = } expr4
`
    },
    {
        id: 'showcase',
        title: 'Language Showcase',
        desc: 'Full language demonstration with physics and compound interest solvers',
        code: `<< -------------------------------------------------------------
   Zeta Programming Language: Comprehensive Showcase Script
   -------------------------------------------------------------
   This script demonstrates the syntax, type system, operator
   precedence, scopes, and input/output capabilities of Zeta.
   ------------------------------------------------------------- >>

< Global physics and economic constants >
global gravity is 9.81
global initial_investment is 1000.00
global interest_rate is 0.05
global compound_periods is 12

< Output a welcoming header to the terminal console >
tell {==================================================}
tell {       ZETA COMPILER & INTERPRETER SHOWCASE       }
tell {==================================================}
tell {Running client-side JVM compile evaluation...}

< 1. Physics Section: Projectile Motion Calculator >
tell {}
tell {--- SECTION 1: PROJECTILE MOTION CALCULATOR ---}
local time is 3.5
local velocity is 45.0
local launch_angle_sin is 0.5
local launch_angle_cos is 0.866

< Compute horizontal and vertical displacement >
local horizontal_distance is velocity * launch_angle_cos * time
local vertical_height is (velocity * launch_angle_sin * time) - (0.5 * gravity * time ^ 2)

tell {Calculating trajectories for } time { seconds of flight...}
tell {Horizontal Displacement: } horizontal_distance { meters}
tell {Vertical Height achieved:   } vertical_height { meters}

< 2. Economics Section: Compound Interest Estimator >
tell {}
tell {--- SECTION 2: COMPOUND INTEREST ESTIMATOR ---}
local years is 5
local compound_exponent is compound_periods * years
local rate_ratio is interest_rate / compound_periods
local interest_multiplier is (1.0 + rate_ratio) ^ compound_exponent
local final_balance is initial_investment * interest_multiplier

tell {Principal Investment: $} initial_investment
tell {Compounding at } interest_rate { interest over } years { years...}
tell {Estimated Final Balance: $} final_balance

< Mutate the timeline to showcase "is now" re-assignment >
years is now years + 5
compound_exponent is now compound_periods * years
interest_multiplier is now (1.0 + rate_ratio) ^ compound_exponent
final_balance is now initial_investment * interest_multiplier

tell {Extending compounding horizon to } years { years...}
tell {New Estimated Final Balance: $} final_balance

< 3. Interactive User Input Section >
tell {}
tell {--- SECTION 3: INTERACTIVE USER DIAGNOSTICS ---}
local user_name is {Developer}
local user_score is 100

tell {Please register your name in the input terminal below:}
ask user_name

tell {Enter your diagnostic level (INT value):}
ask user_score

local score_multiplier is 1.5
local final_score is user_score * score_multiplier

tell {==================================================}
tell {Showcase verification completed for: } user_name
tell {Diagnostic Score adjusted to: } final_score
tell {System status: compiler online & operating.}
tell {==================================================}
`
    }
];

// 2. DOM Elements
const editor = document.getElementById('code-editor');
const lineNumbers = document.getElementById('line-numbers');
const examplesMenu = document.getElementById('examples-menu');
const consoleOutput = document.getElementById('console-output');
const compilerStatus = document.getElementById('compiler-status');
const btnRun = document.getElementById('btn-run');
const runText = document.getElementById('run-text');
const runIcon = document.getElementById('run-icon');
const btnClear = document.getElementById('btn-clear-console');
const btnShare = document.getElementById('btn-share');
const themeToggle = document.getElementById('theme-toggle');
const fileInfo = document.querySelector('.file-info');

// Share Modal Elements
const shareModal = document.getElementById('share-modal');
const shareUrl = document.getElementById('share-url');
const btnCopyShare = document.getElementById('btn-copy-share');
const btnCloseModal = document.getElementById('btn-close-modal');

// 3. State Management
let isJvmReady = false;
let currentExampleId = 'hello';

// 4. Line Number Gutter Synchronization
function updateLineNumbers() {
    const lines = editor.value.split('\n');
    const lineCount = lines.length || 1;
    
    let html = '';
    for (let i = 1; i <= lineCount; i++) {
        html += `<div>${i}</div>`;
    }
    lineNumbers.innerHTML = html;
}

function syncScroll() {
    lineNumbers.scrollTop = editor.scrollTop;
}

function updateCursorInfo() {
    const text = editor.value;
    const selStart = editor.selectionStart;
    
    const textBefore = text.substring(0, selStart);
    const lines = textBefore.split('\n');
    
    const line = lines.length;
    const col = lines[lines.length - 1].length + 1;
    
    fileInfo.textContent = `Line ${line}, Col ${col}`;
}

// 5. Intercept Logs to Console
function appendConsole(type, text) {
    const div = document.createElement('div');
    div.className = `${type}-msg`;
    div.textContent = text;
    consoleOutput.appendChild(div);
    consoleOutput.scrollTop = consoleOutput.scrollHeight;
}

function clearConsole() {
    consoleOutput.innerHTML = '';
}

// 6. Preloaded Examples Rendering
function selectExample(id) {
    const example = EXAMPLES.find(e => e.id === id);
    if (!example) return;
    
    currentExampleId = id;
    editor.value = example.code;
    updateLineNumbers();
    
    // Toggle active list item
    document.querySelectorAll('.example-item').forEach(item => {
        item.classList.toggle('active', item.dataset.id === id);
    });
    
    appendConsole('sys', `Switched to template example: "${example.title}"`);
}

function renderExamplesMenu() {
    examplesMenu.innerHTML = '';
    EXAMPLES.forEach(example => {
        const btn = document.createElement('button');
        btn.className = 'example-item';
        btn.dataset.id = example.id;
        btn.innerHTML = `
            <h3>${example.title}</h3>
            <p>${example.desc}</p>
        `;
        btn.addEventListener('click', () => selectExample(example.id));
        examplesMenu.appendChild(btn);
    });
}

// 7. CheerpJ 4.3 WebAssembly JVM Integration
async function bootstrapJVM() {
    appendConsole('sys', 'Requesting CheerpJ 4.3 loader...');
    
    try {
        if (typeof cheerpjInit === 'undefined') {
            throw new Error('CheerpJ script not loaded. Ensure you are online.');
        }
        
        appendConsole('sys', 'Bootstrapping Java Virtual Machine (version 17)...');
        await cheerpjInit({
            version: 17,
            status: 'none',
            natives: {
                Java_org_zeta_compiler_interpreter_Interpreter_getNextInputLine
            }
        });
        
        isJvmReady = true;
        compilerStatus.textContent = 'JVM Status: Ready';
        compilerStatus.className = 'status-indicator ready';
        
        btnRun.disabled = false;
        runText.textContent = 'Run Code';
        runIcon.textContent = '▶';
        runIcon.className = 'icon';
        
        appendConsole('sys', 'Zeta Compiler fat JAR is mounted and ready for in-browser client compilation!');
    } catch (err) {
        appendConsole('stderr', `Bootstrap failure: ${err.message}`);
        compilerStatus.textContent = 'JVM Status: Error';
        compilerStatus.className = 'status-indicator error';
        runText.textContent = 'JVM Failed';
    }
}

// Interactive standard input handlers
let inputPromiseResolve = null;

async function Java_org_zeta_compiler_interpreter_Interpreter_getNextInputLine(lib) {
    const inputBar = document.getElementById('console-input-bar');
    const inputField = document.getElementById('console-input-field');
    if (inputBar && inputField) {
        inputBar.style.display = 'flex';
        inputField.value = '';
        inputField.focus();
    }
    return new Promise((resolve) => {
        inputPromiseResolve = resolve;
    });
}

function submitConsoleInput() {
    if (!inputPromiseResolve) return;
    
    const inputField = document.getElementById('console-input-field');
    const inputBar = document.getElementById('console-input-bar');
    if (!inputField || !inputBar) return;
    
    const value = inputField.value;
    
    // Echo the input back to the console output
    appendConsole('stdout', `> ${value}`);
    
    // Hide input bar
    inputBar.style.display = 'none';
    
    // Resolve promise
    const resolve = inputPromiseResolve;
    inputPromiseResolve = null;
    resolve(value);
}

// Execute Code
async function executeCompiler() {
    if (!isJvmReady) return;
    
    btnRun.disabled = true;
    runText.textContent = 'Executing...';
    runIcon.textContent = '⚙';
    runIcon.className = 'icon spin';
    
    clearConsole();
    appendConsole('sys', 'Running preprocessing & tokenization...');
    
    const sourceCode = editor.value;
    
    // Intercept standard console outputs
    const originalLog = console.log;
    const originalError = console.error;
    
    console.log = (...args) => {
        appendConsole('stdout', args.map(String).join(' '));
    };
    console.error = (...args) => {
        appendConsole('stderr', args.map(String).join(' '));
    };
    
    try {
        // Write program file into cheerpj virtual filesystem
        cheerpOSAddStringFile('/str/program.zeta', sourceCode);
        
        const startTime = performance.now();
        // Invoke local relative /zeta.jar binary
        const exitCode = await cheerpjRunJar('/app/zeta.jar?v=4', '/str/program.zeta');
        const endTime = performance.now();
        
        const elapsed = ((endTime - startTime) / 1000).toFixed(2);
        appendConsole('sys', `Process finished with Exit Code: ${exitCode} in ${elapsed}s`);
    } catch (err) {
        appendConsole('stderr', `Interpreter error: ${err.message}`);
    } finally {
        console.log = originalLog;
        console.error = originalError;
        
        const inputBar = document.getElementById('console-input-bar');
        if (inputBar) inputBar.style.display = 'none';
        inputPromiseResolve = null;
        
        btnRun.disabled = false;
        runText.textContent = 'Run Code';
        runIcon.textContent = '▶';
        runIcon.className = 'icon';
    }
}

// 8. Sharing Logic: Encode Code in URI Hash
function getShareUrl() {
    const code = editor.value;
    const base64Code = btoa(unescape(encodeURIComponent(code)));
    const url = new URL(window.location.href);
    url.searchParams.set('code', base64Code);
    return url.toString();
}

function loadSharedCode() {
    const params = new URLSearchParams(window.location.search);
    const sharedBase64 = params.get('code');
    if (sharedBase64) {
        try {
            const decodedCode = decodeURIComponent(escape(atob(sharedBase64)));
            editor.value = decodedCode;
            updateLineNumbers();
            
            // Uncheck active example list styles
            document.querySelectorAll('.example-item').forEach(item => {
                item.classList.remove('active');
            });
            
            appendConsole('sys', 'Restored Zeta code layout from shared permalink.');
            return true;
        } catch (e) {
            appendConsole('stderr', 'Failed to restore shared code from URL parameter.');
        }
    }
    return false;
}

// 9. Theme Management (Light / Dark)
function initTheme() {
    const localTheme = localStorage.getItem('zeta-theme') || 'dark';
    document.documentElement.setAttribute('data-theme', localTheme);
    themeToggle.querySelector('.icon').textContent = localTheme === 'dark' ? '☀' : '🌙';
}

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('zeta-theme', newTheme);
    themeToggle.querySelector('.icon').textContent = newTheme === 'dark' ? '☀' : '🌙';
    appendConsole('sys', `Switched playground interface to ${newTheme} theme.`);
}

// 10. Event Listeners Initialization
function initEventListeners() {
    editor.addEventListener('input', () => {
        updateLineNumbers();
        updateCursorInfo();
    });
    
    editor.addEventListener('scroll', syncScroll);
    
    editor.addEventListener('click', updateCursorInfo);
    editor.addEventListener('keyup', updateCursorInfo);
    
    // Support Tab indentations
    editor.addEventListener('keydown', (e) => {
        if (e.key === 'Tab') {
            e.preventDefault();
            const start = editor.selectionStart;
            const end = editor.selectionEnd;
            editor.value = editor.value.substring(0, start) + '    ' + editor.value.substring(end);
            editor.selectionStart = editor.selectionEnd = start + 4;
            updateLineNumbers();
        }
    });

    btnClear.addEventListener('click', clearConsole);
    btnRun.addEventListener('click', executeCompiler);
    themeToggle.addEventListener('click', toggleTheme);
    
    // Sharing modal handlers
    btnShare.addEventListener('click', () => {
        shareUrl.value = getShareUrl();
        shareModal.classList.add('active');
    });
    
    btnCloseModal.addEventListener('click', () => {
        shareModal.classList.remove('active');
    });
    
    shareModal.addEventListener('click', (e) => {
        if (e.target === shareModal) {
            shareModal.classList.remove('active');
        }
    });
    
    btnCopyShare.addEventListener('click', () => {
        shareUrl.select();
        navigator.clipboard.writeText(shareUrl.value)
            .then(() => {
                btnCopyShare.textContent = 'Copied!';
                setTimeout(() => {
                    btnCopyShare.textContent = 'Copy Link';
                }, 2000);
            })
            .catch(() => {
                appendConsole('stderr', 'Clipboard write failed. Copy URL manually.');
            });
    });

    // Sidebar Tabs toggles
    const tabExamples = document.getElementById('tab-btn-examples');
    const tabDocs = document.getElementById('tab-btn-docs');
    const docsContent = document.getElementById('docs-content');

    tabExamples.addEventListener('click', () => {
        tabExamples.classList.add('active');
        tabDocs.classList.remove('active');
        examplesMenu.style.display = 'flex';
        docsContent.style.display = 'none';
    });

    tabDocs.addEventListener('click', () => {
        tabDocs.classList.add('active');
        tabExamples.classList.remove('active');
        examplesMenu.style.display = 'none';
        docsContent.style.display = 'flex';
    });

    // Support interactive console input submission
    const btnSubmitInput = document.getElementById('btn-submit-input');
    const consoleInputField = document.getElementById('console-input-field');
    
    if (btnSubmitInput) {
        btnSubmitInput.addEventListener('click', submitConsoleInput);
    }
    if (consoleInputField) {
        consoleInputField.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                submitConsoleInput();
            }
        });
    }
}

// 11. App Bootstrap
window.addEventListener('DOMContentLoaded', () => {
    initTheme();
    renderExamplesMenu();
    initEventListeners();
    
    // Load shared code or fall back to default template
    const sharedLoaded = loadSharedCode();
    if (!sharedLoaded) {
        selectExample('hello');
    }
    
    // Trigger JVM setup
    bootstrapJVM();
});
