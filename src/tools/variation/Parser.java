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
    private final ArrayList<Token> tokens;
    private final JTextPane console;
    private int current = 0;
    public boolean hadError = false;

    public static ArrayList<Stmt.Sequence> getSequences(ArrayList<Stmt> statements) {
        ArrayList<Stmt.Sequence> sequences = new ArrayList<>();
        for(Stmt stmt : statements) {
            if (stmt instanceof Stmt.Sequence) {
                sequences.add((Stmt.Sequence) stmt);
            }
        }
        return sequences;
    }

    public Parser(ArrayList<Token> tokens, JTextPane console) {
        this.tokens = tokens;
        this.console = console;
        console.setText("");
    }

    public ArrayList<Stmt> parse() {
        ArrayList<Stmt> statements = new ArrayList<>();
        while(!isEnd()) {
            try {
                statements.add(statement(true));
            } catch(Exception e) {
                print(peek(), "Unknown parsing error.\n  " + e.toString());
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
        try {
            if (isNextToken(TokenType.LEFT_BRACE)) {
                return new Stmt.Block(block());
            }
            if (isNextToken(TokenType.IF)) {
                return ifStatement();
            }
            if (isNextToken(TokenType.FOR)) {
                return forStatement();
            }
            if (isNextToken(TokenType.PRINT)) {
                return printStatement();
            }
            if (isNextToken(TokenType.LEFT_BRACKET)) {
                if(!isGlobal) {
                    throw error(getPreviousToken(), "Sequence must be outside conditions and loops");
                }
                return sequence();
            }
            if (isNextToken(TokenType.SEMICOLON)) {
                return new Stmt.Empty();
            }
            if (isNextToken(TokenType.BREAK)) {
                expect(TokenType.SEMICOLON, "Expected ';'");
                return new Stmt.Break();
            }
            if (isNextToken(TokenType.RETURN)) {
                expect(TokenType.SEMICOLON, "Expected ';'");
                return new Stmt.Return();
            }
            if (isNextToken(TokenType.TERMINATE)) {
                return terminate();
            }
            if (isNextToken(TokenType.CONTINUE)) {
                expect(TokenType.SEMICOLON, "Expected ';'");
                return new Stmt.Continue();
            }
            if (isNextToken(TokenType.ALL)) {
                return all();
            }
            return expressionStatement();
        } catch(SyntaxError err) {
            synchronize();
            hadError = true;
            return null;
        }
    }

    private ArrayList<Stmt> block() {
        ArrayList<Stmt> statements = new ArrayList<>();

        while(!(peek().type == TokenType.RIGHT_BRACE) && !isEnd()) {
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

        expect(TokenType.LEFT_PAREN, "Expected '('");
        if(peek().type != TokenType.SEMICOLON) {
            init = expressionStatement();
        } else {
            expect(TokenType.SEMICOLON, "Expected ';'");
        }

        if(peek().type != TokenType.SEMICOLON) {
            condition = expression();
        }
        expect(TokenType.SEMICOLON, "Expected ';'");

        if(peek().type != TokenType.RIGHT_PAREN) {
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

    private Stmt sequence() {
        MovePool movePoolOptional = new MovePool();
        MovePool movePoolForced = new MovePool();
        while(peek().type != TokenType.RIGHT_BRACKET && !isEnd()) {
            movePoolOptional.add(new Move((String)(getNextToken().value)));
            if(peek().type == TokenType.COMMA) {
                expect(TokenType.COMMA, "Expected ','");
            }
        }
        if(movePoolOptional.size == 0) {
            throw error(getPreviousToken(), "Moves must be provided in brackets");
        }
        expect(TokenType.RIGHT_BRACKET, "Expected ']'");
        if(peek().type == TokenType.LEFT_BRACKET && !isEnd()) {
            movePoolForced = movePoolOptional;
            movePoolOptional = new MovePool();
            expect(TokenType.LEFT_BRACKET, "Expected '['");
            while(peek().type != TokenType.RIGHT_BRACKET && !isEnd()) {
                movePoolOptional.add(new Move((String)(getNextToken().value)));
                if(peek().type == TokenType.COMMA) {
                    expect(TokenType.COMMA, "Expected ','");
                }
            }
            if(movePoolOptional.size == 0 || movePoolForced.size == 0) {
                throw error(getPreviousToken(), "Moves must be provided in brackets");
            }
            expect(TokenType.RIGHT_BRACKET, "Expected ']'");
        }
        expect(TokenType.LEFT_PAREN, "Expected '('");

        Integer lowerLimit = null;
        Integer upperLimit = null;
        if(peek().type != TokenType.RIGHT_PAREN) {
            try {
                lowerLimit = Integer.parseInt(getNextToken().lexeme);
            } catch(Exception e) {
                throw error(getPreviousToken(), "Expected integer");
            }
            if(peek().type == TokenType.COMMA) {
                expect(TokenType.COMMA, "Expected ','");
            }
        }
        if(peek().type != TokenType.RIGHT_PAREN) {
            try {
                upperLimit = Integer.parseInt(getNextToken().lexeme);
            } catch(Exception e) {
                throw error(getPreviousToken(), "Expected integer");
            }
        }
        expect(TokenType.RIGHT_PAREN, "Expected ')'");
        expect(TokenType.LEFT_BRACE, "Expected '{'");

        String lexicographic = "";
        if(isNextToken(TokenType.LEXICOGRAPHIC)) {
            lexicographic += getNextToken().lexeme.toLowerCase();
            expect(TokenType.SEMICOLON, "Expected ';'");
            if(!checkLexicographic(lexicographic)) {
                throw error(getPreviousToken(), "All 6 move types required for order");
            }
        }

        Stmt start = null;
        Stmt beforeMove = null;
        Stmt afterMove = null;
        Stmt beforeStep = null;
        Stmt afterStep = null;
        Stmt end = null;

        while(isNextToken(TokenType.START, TokenType.BEFORE_MOVE, TokenType.AFTER_MOVE,
                TokenType.BEFORE_STEP, TokenType.AFTER_STEP, TokenType.END)) {
            Token lifecycle = getPreviousToken();
            expect(TokenType.COLON, "Expected ':'");
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
                case BEFORE_STEP:
                    beforeStep = stmt;
                    break;
                case AFTER_STEP:
                    afterStep = stmt;
                    break;
                case END:
                    end = stmt;
                    break;
            }
        }
        expect(TokenType.RIGHT_BRACE, "Expected '}'");
        return new Stmt.Sequence(movePoolOptional, movePoolForced, lowerLimit, upperLimit, lexicographic,
                start, beforeMove, afterMove, beforeStep, afterStep, end);
    }

    private Stmt terminate() {
        Expr index = null;
        if(!isNextToken(TokenType.SEMICOLON)) {
            index = expression();
            expect(TokenType.SEMICOLON, "Expected ';'");
        }
        return new Stmt.Terminate(index);
    }

    private Stmt all() {
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
            throw error(peek(), "You can only assign to a variable");
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
            ArrayList<Expr> arguments = new ArrayList<>();

            expect(TokenType.LEFT_PAREN, "Expected '('");
            while(!isNextToken(TokenType.RIGHT_PAREN)) {
                Expr expr = expression();
                arguments.add(expr);
                if(peek().type == TokenType.COMMA) {
                    expect(TokenType.COMMA, "Expected ','");
                }
            }
            return new Expr.Function(name, arguments, token);
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
            return new Expr.Literal(new Move((String)(getPreviousToken().value)));
        }
        if(isNextToken(TokenType.IDENTIFIER)) {
            return new Expr.Variable(getPreviousToken());
        }
        if(isNextToken(TokenType.TILE)) {
            return new Expr.Literal(Tile.valueOf((String)getPreviousToken().value));
        }
        if(isNextToken(TokenType.OTHER)) {
            throw error(getPreviousToken(), "Unexpected symbol");
        }
        if(isNextToken(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            expect(TokenType.RIGHT_PAREN, "Expected ')'");
            return new Expr.Group(expr);
        }
        throw error(peek(), "Expected expression");
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
        return peek().type == type;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean isEnd() {
        return peek().type == TokenType.EOF;
    }

    private void expect(TokenType type, String message) {
        if(isTokenType(type)) {
            getNextToken();
            return;
        }
        throw error(getPreviousToken(), message);
    }

    private SyntaxError error(Token token, String message) {
        print(token, message);

        return new SyntaxError();
    }

    private void print(Token token, String message) {
        StyledDocument doc = console.getStyledDocument();
        Style style = console.addStyle("style", null);
        StyleConstants.setForeground(style, new Color(255, 68, 68));
        String str = "[Line " + token.line + " near '" + token.lexeme + "'] " + message + "\n";
        try {
            doc.insertString(doc.getLength(), str, style);
        } catch (BadLocationException e) {}
    }

    private void synchronize() {
        getNextToken();

        while(!isEnd()) {
            if(getPreviousToken().type == TokenType.SEMICOLON) {
                return;
            }

            switch(peek().type) {
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
        String toCheck = "urdlwh";
        for(int i = 0; i < toCheck.length(); i++) {
            if(lexicographic.indexOf(toCheck.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }

    private class SyntaxError extends RuntimeException { }
}
