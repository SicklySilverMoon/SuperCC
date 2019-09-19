package tools.variation;

import game.Tile;

import java.util.ArrayList;

public class Parser {
    private final ArrayList<Token> tokens;
    private int current = 0;

    public static ArrayList<Stmt.Sequence> getSequences(ArrayList<Stmt> statements) {
        ArrayList<Stmt.Sequence> sequences = new ArrayList<>();
        for(Stmt stmt : statements) {
            if(stmt instanceof Stmt.Sequence) {
                sequences.add((Stmt.Sequence)stmt);
            }
        }
        return sequences;
    }

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }

    public ArrayList<Stmt> parse() {
        ArrayList<Stmt> statements = new ArrayList<>();
        while(!isEnd()) {
            statements.add(statement());
        }
        return statements;
    }

    private Stmt statement() {
        if(isNextToken(TokenType.LEFT_BRACE)) {
            return new Stmt.Block(block());
        }
        if(isNextToken(TokenType.IF)) {
            return ifStatement();
        }
        if(isNextToken(TokenType.FOR)) {
            return forStatement();
        }
        if(isNextToken(TokenType.PRINT)) {
            return printStatement();
        }
        if(isNextToken(TokenType.LEFT_BRACKET)) {
            return sequence();
        }
        if(isNextToken(TokenType.SEMICOLON)) {
            return new Stmt.Empty();
        }
        if(isNextToken(TokenType.BREAK)) {
            return new Stmt.Break();
        }
        if(isNextToken(TokenType.RETURN)) {
            return new Stmt.Return();
        }
        if(isNextToken(TokenType.TERMINATE)) {
            return terminate();
        }
        if(isNextToken(TokenType.CONTINUE)) {
            return new Stmt.Continue();
        }
        if(isNextToken(TokenType.ALL)) {
            return all();
        }
        return expressionStatement();
    }

    private ArrayList<Stmt> block() {
        ArrayList<Stmt> statements = new ArrayList<>();

        while(!(peek().type == TokenType.RIGHT_BRACE) && !isEnd()) {
            statements.add(statement());
        }

        getNextToken(); // Right brace
        return statements;
    }

    private Stmt ifStatement() {
        getNextToken(); // Left parenthesis
        Expr condition = expression();
        getNextToken(); // Right parenthesis

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if(isNextToken(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt forStatement() {
        Stmt init = null;
        Expr condition = null;
        Stmt post = null;

        getNextToken(); // Left parenthesis
        if(peek().type != TokenType.SEMICOLON) {
            init = expressionStatement();
        } else {
            getNextToken(); // Semicolon
        }

        if(peek().type != TokenType.SEMICOLON) {
            condition = expression();
        }
        getNextToken(); // Semicolon

        if(peek().type != TokenType.RIGHT_PAREN) {
            post = new Stmt.Expression(expression());
        }
        getNextToken(); // Left parenthesis

        Stmt body = statement();

        return new Stmt.For(init, condition, post, body);
    }

    private Stmt printStatement() {
        Expr expr = expression();
        getNextToken(); // Semicolon

        return new Stmt.Print(expr);
    }

    private Stmt sequence() {
        MovePool movePool = new MovePool();
        while(peek().type != TokenType.RIGHT_BRACKET && !isEnd()) {
            movePool.add(new Move(getNextToken().lexeme));
            if(peek().type == TokenType.COMMA) {
                getNextToken(); // Comma
            }
        }
        getNextToken(); // Right bracket
        getNextToken(); // Left parenthesis

        Integer lowerLimit = null;
        Integer upperLimit = null;
        if(peek().type != TokenType.RIGHT_PAREN) {
            lowerLimit = Integer.parseInt(getNextToken().lexeme);
            if(peek().type == TokenType.COMMA) {
                getNextToken(); // Comma
            }
        }
        if(peek().type != TokenType.RIGHT_PAREN) {
            upperLimit = Integer.parseInt(getNextToken().lexeme);
        }
        getNextToken(); // Right parenthesis
        getNextToken(); // Left brace

        String lexicographic = "";
        if(isNextToken(TokenType.LEXICOGRAPHIC)) {
            for(int i = 0; i < 5; i++) {
                lexicographic += getNextToken().lexeme;
                getNextToken(); // Comma
            }
            lexicographic += getNextToken().lexeme;
            getNextToken(); // Semicolon
        }

        Stmt start = null;
        Stmt beforeMove = null;
        Stmt afterMove = null;

        while(isNextToken(TokenType.START, TokenType.BEFORE_MOVE, TokenType.AFTER_MOVE)) {
            Token lifecycle = getPreviousToken();
            getNextToken(); // Colon
            Stmt stmt = statement();
            switch(lifecycle.type) {
                case START:
                    start = stmt;
                    break;
                case BEFORE_MOVE:
                    beforeMove = stmt;
                    break;
                case AFTER_MOVE:
                    afterMove = stmt;
                    break;
            }
        }
        getNextToken(); // Right brace
        return new Stmt.Sequence(movePool, lowerLimit, upperLimit, lexicographic, start, beforeMove, afterMove);
    }

    private Stmt terminate() {
        Expr index = null;
        if(!isNextToken(TokenType.SEMICOLON)) {
            index = expression();
            getNextToken(); // Semicolon
        }
        return new Stmt.Terminate(index);
    }

    private Stmt all() {
        Expr amount = new Expr.Literal(100.0);
        if(!isNextToken(TokenType.SEMICOLON)) {
            amount = expression();
            getNextToken(); // Semicolon
        }
        return new Stmt.All(amount);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        getNextToken(); // Semicolon
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if(isNextToken(TokenType.EQUAL, TokenType.PLUS_EQUAL, TokenType.MINUS_EQUAL, TokenType.STAR_EQUAL,
                       TokenType.SLASH_EQUAL, TokenType.MODULO_EQUAL)) {
            Token operator = getPreviousToken();
            Expr value = assignment();

            if(expr instanceof Expr.Variable) {
                Token var = ((Expr.Variable)expr).var;
                return new Expr.Assign(var, operator, value);
            }
        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while(isNextToken(TokenType.OR)) {
            Token operator = getPreviousToken();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while(isNextToken(TokenType.AND)) {
            Token operator = getPreviousToken();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while(isNextToken(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            Token operator = getPreviousToken();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = addition();

        while(isNextToken(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)) {
            Token operator = getPreviousToken();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr addition() {
        Expr expr = multiplication();

        while(isNextToken(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = getPreviousToken();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr multiplication() {
        Expr expr = unary();

        while(isNextToken(TokenType.STAR, TokenType.SLASH, TokenType.MODULO)) {
            Token operator = getPreviousToken();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if(isNextToken(TokenType.MINUS, TokenType.BANG, TokenType.NOT)) {
            Token operator = getPreviousToken();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return function();
    }

    private Expr function() {
        if(isNextToken(TokenType.FUNCTION)) {
            String name = getPreviousToken().lexeme.toLowerCase();
            ArrayList<Expr> arguments = new ArrayList<>();

            getNextToken(); // Left parenthesis
            while(!isNextToken(TokenType.RIGHT_PAREN)) {
                Expr expr = expression();
                arguments.add(expr);
                if(peek().type == TokenType.COMMA) {
                    getNextToken(); // Comma
                }
            }
            return new Expr.Function(name, arguments);
        }
        return primary();
    }

    private Expr primary() {
        if(isNextToken(TokenType.NULL)) {
            return new Expr.Literal(null);
        }
        if(isNextToken(TokenType.TRUE)) {
            return new Expr.Literal(true);
        }
        if(isNextToken(TokenType.FALSE)) {
            return new Expr.Literal(false);
        }
        if(isNextToken(TokenType.NUMBER)) {
            return new Expr.Literal(getPreviousToken().value);
        }
        if(isNextToken(TokenType.MOVE)) {
            return new Expr.Literal(new Move(getPreviousToken().lexeme));
        }
        if(isNextToken(TokenType.IDENTIFIER)) {
            return new Expr.Variable(getPreviousToken());
        }
        if(isNextToken(TokenType.TILE)) {
            return new Expr.Literal(Tile.valueOf(getPreviousToken().lexeme));
        }
        if(isNextToken(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            getNextToken(); // Right parenthesis
            return new Expr.Group(expr);
        }
        return null;
    }

    private Token getNextToken() {
        current++;
        return getPreviousToken();
    }

    private Token getPreviousToken() {
        return tokens.get(current - 1);
    }

    private boolean isNextToken(TokenType... types) {
        for(TokenType type : types) {
            if(isTokenType(type)) {
                getNextToken();
                return true;
            }
        }
        return false;
    }

    private boolean isTokenType(TokenType type) {
        if(isEnd()) {
            return false;
        }
        return peek().type == type;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean isEnd() {
        return peek().type == TokenType.EOF;
    }
}
