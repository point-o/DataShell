# dsh
A weakly typed REPL with assignments and commands

dsh is a lightweight REPL (Read-Eval-Print Loop) designed for quick evaluations, variable assignments, and shell-like commands.

Only 5 token types, global scope, and straightforward evaluation.
Instead of a full AST, it uses a dispatcher with simplification logic.

Weak typing (no type declarations, just values)
Variable assignments (x = 10)
Basic arithmetic (#3+5*(2+4))^2)

Nested tokens are a weak point since I use a dispatcher, so nested tokens were something I couldn't figure out fully. Something like this wouldn't be tokenized correctly:
:isprime 1+2

The project is unfinished, features like macros aren't quite done and intake for lists and matrices is currently lacking.
