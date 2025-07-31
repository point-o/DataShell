# dsh
A weakly typed REPL with assignments and commands

dsh is a lightweight REPL (Read-Eval-Print Loop) designed for quick evaluations, variable assignments, and shell-like commands.

Only 5 token types, global scope, and straightforward evaluation.
Instead of a full AST, it uses a dispatcher with simplification logic.

Weak typing (no type declarations, just values)
Variable assignments (x = 10)
Lists [1,2,3]
Basic arithmetic (#3+5*(2+4))^2)

Nested tokens are a weak point since I use a dispatcher, so nested tokens were something I couldn't figure out fully. Something like this wouldn't be tokenized correctly:
:isprime 1+2

##Demo:

https://www.loom.com/share/f48e5092f3fb4fb68ff6a1e14e9133a6?sid=53a7ddf2-cb96-4d7b-a399-39df0a690083
