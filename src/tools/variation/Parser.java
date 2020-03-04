package tools.variation;

import game.Tile;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.ArrayList;

public class Parser {
    private final char[] LEXICOGRAPHIC_CHARS = "urdlwh".toCharArray();

    private ArrayList<Token> tokens;
    private JTextPane console;
    private int current = 0;
    public boolean hadError = false;

    public Parser() {

    }

    public Parser(JTextPane console) {
        this.console = console;
        console.setText("");
    }

    public Parser(ArrayList<Token> tokens, JTextPane console) {
        this.tokens = tokens;
        this.console = console;
        console.setText("");
    }

    public ArrayList<Stmt> parseCode(String code) {
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.getParsableTokens();
        this.tokens = tokens;
        return parse();
    }

    public ArrayList<Stmt> parse() {
        ArrayList<Stmt> statements = new ArrayList<>();
        while(!isEnd()) {
            try {
                statements.add(statement(true));
            } catch(Exception e) {
                print(getToken(), "Unknown parsing error.\n  " + e.toString());
                synchronize();
                hadError = true;
                e.printStackTrace();
            }
        }
        return statements;
    }

    private Stmt statement() {
        return statement(false);
    }

    private Stmt statement(boolean isGlobal) {
        Token token = getNextToken();
        try {
            switch(token.type) {
                case LEFT_BRACE: return new Stmt.Block(block());
                case IF: return ifStatement();
                case FOR: return forStatement();
                case PRINT: return printStatement();
                case LEFT_BRACKET: return sequenceStatement(isGlobal);
                case SEMICOLON: return new Stmt.Empty();
                case BREAK: return breakStatement();
                case RETURN: return returnStatement();
                case TERMINATE: return terminateStatement();
                case CONTINUE: return continueStatement();
                case ALL: return allStatement();
                default: current--; return expressionStatement();
            }
        } catch(SyntaxError err) {
            synchronize();
            hadError = true;
            return null;
        }
    }

    private ArrayList<Stmt> block() {
        ArrayList<Stmt> statements = new ArrayList<>();

        while(!(getToken().type == TokenType.RIGHT_BRACE) && !isEnd()) {
            statements.add(statement());
        }

        expect(TokenType.RIGHT_BRACE, "Expected '}'");
        return statements;
    }

    private Stmt ifStatement() {
        expect(TokenType.LEFT_PAREN, "Expected '('");
        Expr condition = expression();
        expect(TokenType.RIGHT_PAREN, "Expected ')'");

        Stmt thenBranch = statement();
        Stmt elseBranch = isNextToken(TokenType.ELSE) ? statement() : null;

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt forStatement() {
        Stmt init = null;
        Expr condition = null;
        Stmt post = null;

        expect(TokenType.LEFT_PAREN, "Expected '('");
        if(getToken().type != TokenType.SEMICOLON) {
            init = expressionStatement();
        } else {
            expect(TokenType.SEMICOLON, "Expected ';'");
        }

        if(getToken().type != TokenType.SEMICOLON) {
            condition = expression();
        }
        expect(TokenType.SEMICOLON, "Expected ';'");

        if(getToken().type != TokenType.RIGHT_PAREN) {
            post = new Stmt.Expression(expression());
        }
        expect(TokenType.RIGHT_PAREN, "Expected ')'");

        Stmt body = statement();

        return new Stmt.For(init, condition, post, body);
    }

    private Stmt printStatement() {
        Expr expr = expression();
        expect(TokenType.SEMICOLON, "Expected ';'");

        return new Stmt.Print(expr);
    }

    private Stmt sequenceStatement(boolean isGlobal) {
        if(!isGlobal) {
            throw error(getPreviousToken(), "Sequence must be outside conditions and loops");
        }
        return sequence();
    }

    private Stmt sequence() {
        MovePoolContainer movePools = new MovePoolContainer();
        parseSequenceMovePools(movePools);

        BoundLimit limits = new BoundLimit();
        parseSequenceLimits(limits);

        expect(TokenType.LEFT_BRACE, "Expected '{'");
        String lexicographic = parseSequenceLexicographic();

        SequenceLifecycle lifecycle = new SequenceLifecycle();
        parseSequenceLifecycle(lifecycle);
        expect(TokenType.RIGHT_BRACE, "Expected '}'");

        return new Stmt.Sequence(movePools, limits, lexicographic, lifecycle);
    }

    private void parseSequenceMovePools(MovePoolContainer movePools) {
        MovePool movePoolOptional = movePools.optional;
        MovePool movePoolForced = movePools.forced;

        collectMoves(movePoolOptional);
        if(movePoolOptional.size == 0) {
            throw error(getPreviousToken(), "Moves must be provided in brackets");
        }
        expect(TokenType.RIGHT_BRACKET, "Expected ']'");
        if(getToken().type == TokenType.LEFT_BRACKET && !isEnd()) {
            movePoolForced.replace(movePoolOptional);
            movePoolOptional.clear();
            expect(TokenType.LEFT_BRACKET, "Expected '['");
            collectMoves(movePoolOptional);
            if(movePoolOptional.size == 0 || movePoolForced.size == 0) {
                throw error(getPreviousToken(), "Moves must be provided in brackets");
            }
            expect(TokenType.RIGHT_BRACKET, "Expected ']'");
        }
    }

    private void collectMoves(MovePool movePool) {
        while(getToken().type != TokenType.RIGHT_BRACKET && !isEnd()) {
            movePool.add(new Move((String)(getNextToken().value)));
            consume(TokenType.COMMA);
        }
    }

    private void parseSequenceLimits(BoundLimit limits) {
        expect(TokenType.LEFT_PAREN, "Expected '('");
        if(getToken().type != TokenType.RIGHT_PAREN) {
            limits.lower = parseInteger();
            consume(TokenType.COMMA);
        }
        if(getToken().type != TokenType.RIGHT_PAREN) {
            limits.upper = parseInteger();
        }
        expect(TokenType.RIGHT_PAREN, "Expected ')'");
    }

    private int parseInteger() {
        try {
            return Integer.parseInt(getNextToken().lexeme);
        } catch(Exception e) {
            throw error(getPreviousToken(), "Expected integer");
        }
    }

    private String parseSequenceLexicographic() {
        String lexicographic = "";
        if(isNextToken(TokenType.LEXICOGRAPHIC)) {
            lexicographic += getNextToken().lexeme.toLowerCase();
            expect(TokenType.SEMICOLON, "Expected ';'");
            if(!checkLexicographic(lexicographic)) {
                throw error(getPreviousToken(), "All 6 move types required for order");
            }
        }
        return lexicographic;
    }

    private void parseSequenceLifecycle(SequenceLifecycle lifecycle) {
        while(isNextToken(TokenType.START, TokenType.BEFORE_MOVE, TokenType.AFTER_MOVE,
                TokenType.BEFORE_STEP, TokenType.AFTER_STEP, TokenType.END)) {
            Token lifecycleToken = getPreviousToken();
            expect(TokenType.COLON, "Expected ':'");
            Stmt stmt = statement();
            switch(lifecycleToken.type) {
                case START: lifecycle.start = stmt; break;
                case BEFORE_MOVE: lifecycle.beforeMove = stmt; break;
                case AFTER_MOVE: lifecycle.afterMove = stmt; break;
                case BEFORE_STEP: lifecycle.beforeStep = stmt; break;
                case AFTER_STEP: lifecycle.afterStep = stmt; break;
                case END: lifecycle.end = stmt; break;
            }
        }
    }

    private Stmt breakStatement() {
        expect(TokenType.SEMICOLON, "Expected ';'");
        return new Stmt.Break();
    }

    private Stmt returnStatement() {
        expect(TokenType.SEMICOLON, "Expected ';'");
        return new Stmt.Return();
    }

    private Stmt terminateStatement() {
        Expr index = null;
        if(!isNextToken(TokenType.SEMICOLON)) {
            index = expression();
            expect(TokenType.SEMICOLON, "Expected ';'");
        }
        return new Stmt.Terminate(index);
    }

    private Stmt continueStatement() {
        expect(TokenType.SEMICOLON, "Expected ';'");
        return new Stmt.Continue();
    }

    private Stmt allStatement() {
        Expr amount = new Expr.Literal(1000.0);
        if(!isNextToken(TokenType.SEMICOLON)) {
            amount = expression();
            expect(TokenType.SEMICOLON, "Expected ';'");
        }
        return new Stmt.All(amount);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        expect(TokenType.SEMICOLON, "Expected ';'");
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
            throw error(getToken(), "You can only assign to a variable");
        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while(isNextToken(TokenType.OR, TokenType.OR_OR)) {
            Token operator = getPreviousToken();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while(isNextToken(TokenType.AND, TokenType.AND_AND)) {
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
            Token token = getPreviousToken();
            String name = getPreviousToken().lexeme.toLowerCase();
            ArrayList<Expr> arguments = parseFunctionArguments();
            return new Expr.Function(name, arguments, token);
        }
        return primary();
    }

    private ArrayList<Expr> parseFunctionArguments() {
        ArrayList<Expr> arguments = new ArrayList<>();

        expect(TokenType.LEFT_PAREN, "Expected '('");
        while(!isNextToken(TokenType.RIGHT_PAREN)) {
            Expr expr = expression();
            arguments.add(expr);
            consume(TokenType.COMMA);
        }

        return arguments;
    }

    private Expr primary() {
        Token token = getNextToken();
        switch(token.type) {
            case NULL: return new Expr.Literal(null);
            case TRUE: return new Expr.Literal(true);
            case FALSE: return new Expr.Literal(false);
            case NUMBER: return new Expr.Literal(token.value);
            case MOVE: return new Expr.Literal(new Move((String)token.value));
            case IDENTIFIER: return new Expr.Variable(token);
            case TILE: return new Expr.Literal(Tile.valueOf((String)token.value));
            case LEFT_PAREN: return groupExpression();
            case OTHER: throw error(token, "Unexpected symbol");
            default: throw error(token, "Expected expression");
        }
    }

    private Expr groupExpression() {
        Expr expr = expression();
        expect(TokenType.RIGHT_PAREN, "Expected ')'");
        return new Expr.Group(expr);
    }

    private Token getNextToken() {
        if(!isEnd()) current++;
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
        return getToken().type == type;
    }

    private Token getToken() {
        return tokens.get(current);
    }

    private boolean isEnd() {
        return getToken().type == TokenType.EOF;
    }

    private void expect(TokenType type, String message) {
        if(isTokenType(type)) {
            getNextToken();
            return;
        }
        throw error(getPreviousToken(), message);
    }

    private void consume(TokenType type) {
        if(getToken().type == type) {
            getNextToken();
        }
    }

    private SyntaxError error(Token token, String message) {
        print(token, message);
        return new SyntaxError();
    }

    private void print(Token token, String message) {
        if(console != null) {
            StyledDocument doc = console.getStyledDocument();
            Style style = console.addStyle("style", null);
            StyleConstants.setForeground(style, new Color(255, 68, 68));
            String str = "[Line " + token.line + " near '" + token.lexeme + "'] " + message + "\n";
            try {
                doc.insertString(doc.getLength(), str, style);
            } catch (BadLocationException e) {
            }
        }
    }

    private void synchronize() {
        while(!isEnd()) {
            switch(getToken().type) {
                case SEMICOLON:
                    getNextToken();
                case FOR:
                case IF:
                case PRINT:
                case RETURN:
                case CONTINUE:
                case TERMINATE:
                case LEXICOGRAPHIC:
                case ALL:
                    return;
            }
            getNextToken();
        }
    }

    private boolean checkLexicographic(String lexicographic) {
        if(lexicographic.length() != 6) {
            return false;
        }

        for(char moveType : LEXICOGRAPHIC_CHARS) {
            if(lexicographic.indexOf(moveType) < 0) {
                return false;
            }
        }
        return true;
    }

    private class SyntaxError extends RuntimeException { }
}
