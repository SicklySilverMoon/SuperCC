package tools.variation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Tokenizer {
    private String code;
    private ArrayList<Token> tokens;
    private int start = 0;
    private int current = 0;
    private static final String moves = "urdlwhURDLWH";
    private static final HashMap<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("u", TokenType.MOVE);
        keywords.put("r", TokenType.MOVE);
        keywords.put("d", TokenType.MOVE);
        keywords.put("l", TokenType.MOVE);
        keywords.put("w", TokenType.MOVE);
        keywords.put("h", TokenType.MOVE);

        keywords.put("start", TokenType.START);
        keywords.put("beforemove", TokenType.BEFORE_MOVE);
        keywords.put("aftermove", TokenType.AFTER_MOVE);

        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("null", TokenType.NULL);
        keywords.put("var", TokenType.VAR);
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
        keywords.put("not", TokenType.NOT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("continue", TokenType.CONTINUE);
        keywords.put("terminate", TokenType.TERMINATE);
    }

    public Tokenizer(String code) {
        this.code = code;
        this.tokens = new ArrayList<Token>();
    }

    public ArrayList<Token> tokenize() {
        while(!isEnd()) {
            start = current;
            getNextToken();
        }
        addToken(TokenType.EOF);
        return tokens;
    }

    private void getNextToken() {
        char c = getNextChar();
        switch(c) {
            case '[': addToken(TokenType.LEFT_BRACKET); break;
            case ']': addToken(TokenType.RIGHT_BRACKET); break;
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '+': processPlus(); break;
            case '-': processMinus(); break;
            case '/': processSlash(); break;
            case '*': addToken(isNextChar('=') ? TokenType.STAR_EQUAL : TokenType.STAR); break;
            case '%': addToken(isNextChar('=') ? TokenType.MODULO_EQUAL : TokenType.MODULO); break;
            case '=': addToken(isNextChar('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '&': processAnd(); break;
            case '|': processOr(); break;
            case '!': processBang(); break;
            case '<': addToken(isNextChar('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
            case '>': addToken(isNextChar('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
            case ' ':
            case '\t':
            case '\r':
            case '\n': break;
            default: processOther(c); break;
        }
    }

    private void processPlus() {
        if(isNextChar('+')) {
            addToken(TokenType.PLUS_PLUS);
        }
        else if(isNextChar('=')) {
            addToken(TokenType.PLUS_EQUAL);
        }
        else {
            addToken(TokenType.PLUS);
        }
    }

    private void processMinus() {
        if(isNextChar('-')) {
            addToken(TokenType.MINUS_MINUS);
        }
        else if(isNextChar('=')) {
            addToken(TokenType.MINUS_EQUAL);
        }
        else {
            addToken(TokenType.MINUS);
        }
    }
    private void processSlash() {
        if(isNextChar('/')) {
            while(peek() != '\n' && !isEnd()) {
                getNextChar();
            }
        }
        else if(isNextChar('=')) {
            addToken(TokenType.SLASH_EQUAL);
        }
        else {
            addToken(TokenType.SLASH);
        }
    }

    private void processAnd() {
        if(isNextChar('&')) {
            addToken(TokenType.AND_AND);
        }
    }

    private void processOr() {
        if(isNextChar('|')) {
            addToken(TokenType.OR_OR);
        }
    }

    private void processBang() {
        if(isNextChar('=')) {
            addToken(TokenType.BANG_EQUAL);
        }
        else {
            addToken(TokenType.BANG);
        }
    }

    private void processOther(char c) {
        if(isDigit(c)) {
            processNumber();
        }
        else if(isAlpha(c)) {
            processIdentifier();
        }
    }

    private void processNumber() {
        while(isDigit(peek())) {
            getNextChar();
        }
        if(moves.contains(peek() + "")) {
            getNextChar();
            String substr = code.substring(start, current);
            addToken(TokenType.MOVE, substr);
            return;
        }
        else if(peek() == '.' && isDigit(peekNext())) {
            getNextChar();
            while(isDigit(peek())) {
                getNextChar();
            }
        }
        String substr = code.substring(start, current);
        addToken(TokenType.NUMBER, Double.parseDouble(substr));
    }

    private void processIdentifier() {
        while(isAlphaNumeric(peek())) {
            getNextChar();
        }
        String substr = code.substring(start, current);
        TokenType type = keywords.get(substr.toLowerCase());
        if(type == null) {
            addToken(TokenType.IDENTIFIER, substr);
        }
        else if(type == TokenType.MOVE) {
            addToken(type, substr);
        }
        else {
            addToken(type);
        }
    }

    private char getNextChar() {
        current++;
        return code.charAt(current - 1);
    }

    private boolean isNextChar(char c) {
        if(isEnd()) {
            return false;
        }
        if(code.charAt(current) != c) {
            return false;
        }
        current++;
        return true;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_' || c == '@' || c == '#' || c == '$';
    }

    private boolean isAlphaNumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }

    private char peek() {
        if(isEnd()) {
            return '\0';
        }
        return code.charAt(current);
    }

    private char peekNext() {
        if(current + 1 >= code.length()) {
            return '\0';
        }
        return code.charAt(current + 1);
    }

    private boolean isEnd() {
        return this.current >= this.code.length();
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object value) {
        tokens.add(new Token(type, value));
    }
}
