package dsh;

import java.util.List;

/**
 * ASTNode implementation
 *
 * @author Ryan Pointer
 * @version 7/20/25
 */
public abstract class ASTNode {
}

/**
 * Represents one input from user
 */
class Input extends ASTNode {
    private final Statement statement;

    public Input(Statement statement) {
        this.statement = statement;
    }

    public Statement getStatement() {
        return statement;
    }
}

/**
 * Base for executable statements
 */
abstract class Statement extends ASTNode {
}

/**
 * Command execution (:help, :list, etc.)
 */
class CommandStatement extends Statement {
    private final String name;
    private final List<String> args;

    public CommandStatement(String name, List<String> args) {
        this.name = name;
        this.args = args;
    }

    public String getName() { return name; }
    public List<String> getArgs() { return args; }
}

/**
 * Macro execution (;define, ;run, etc.)
 */
class MacroStatement extends Statement {
    private final String name;
    private final List<String> args;
    private final List<String> definition;

    public MacroStatement(String name, List<String> args, List<String> definition) {
        this.name = name;
        this.args = args;
        this.definition = definition;
    }

    public String getName() { return name; }
    public List<String> getArgs() { return args; }
    public List<String> getDefinition() { return definition; }
}

/**
 * Variable assignment (x = 5)
 */
class Assignment extends Statement {
    private final String identifier;
    private final String expression;

    public Assignment(String identifier, String expression) {
        this.identifier = identifier;
        this.expression = expression;
    }

    public String getIdentifier() { return identifier; }
    public String getVariableName() { return identifier; } // Alias for Evaluator compatibility
    public String getExpression() { return expression; }
}

/**
 * Expression to evaluate and display result
 */
class Expression extends Statement {
    private final String expression;

    public Expression(String expression) {
        this.expression = expression;
    }

    public String getExpression() { return expression; }
}

/**
 * If statement
 */
class IfStatement extends Statement {
    private final String condition;
    private final List<Statement> thenBranch;
    private final List<Statement> elseBranch;

    public IfStatement(String condition, List<Statement> thenBranch,
                      List<Statement> elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public String getCondition() { return condition; }
    public List<Statement> getThenBranch() { return thenBranch; }
    public List<Statement> getElseBranch() { return elseBranch; }
}

/**
 * While loop
 */
class WhileStatement extends Statement {
    private final String condition;
    private final List<Statement> body;

    public WhileStatement(String condition, List<Statement> body) {
        this.condition = condition;
        this.body = body;
    }

    public String getCondition() { return condition; }
    public List<Statement> getBody() { return body; }
}