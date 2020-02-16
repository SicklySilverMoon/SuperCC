package tools.variation;

import game.Tile;

import java.util.ArrayList;
import java.util.HashMap;

public class Tokenizer {
    private String code;
    private ArrayList<Token> tokens;
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final String moves = "urdlwhURDLWH";
    private static final HashMap<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("start", TokenType.START);
        keywords.put("beforemove", TokenType.BEFORE_MOVE);
        keywords.put("aftermove", TokenType.AFTER_MOVE);
        keywords.put("beforestep", TokenType.BEFORE_STEP);
        keywords.put("afterstep", TokenType.AFTER_STEP);
        keywords.put("end", TokenType.END);

        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("null", TokenType.NULL);
        keywords.put("var", TokenType.VAR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("break", TokenType.BREAK);
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
        keywords.put("not", TokenType.NOT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("continue", TokenType.CONTINUE);
        keywords.put("terminate", TokenType.TERMINATE);
        keywords.put("order", TokenType.LEXICOGRAPHIC);
        keywords.put("all", TokenType.ALL);

        keywords.put("previousmove", TokenType.FUNCTION);
        keywords.put("nextmove", TokenType.FUNCTION);
        keywords.put("getmove", TokenType.FUNCTION);
        keywords.put("getoppositemove", TokenType.FUNCTION);
        keywords.put("movesexecuted", TokenType.FUNCTION);
        keywords.put("movecount", TokenType.FUNCTION);
        keywords.put("seqlength", TokenType.FUNCTION);
        keywords.put("getchipsleft", TokenType.FUNCTION);
        keywords.put("getredkeycount", TokenType.FUNCTION);
        keywords.put("getyellowkeycount", TokenType.FUNCTION);
        keywords.put("getgreenkeycount", TokenType.FUNCTION);
        keywords.put("getbluekeycount", TokenType.FUNCTION);
        keywords.put("hasflippers", TokenType.FUNCTION);
        keywords.put("hasfireboots", TokenType.FUNCTION);
        keywords.put("hassuctionboots", TokenType.FUNCTION);
        keywords.put("hasiceskates", TokenType.FUNCTION);
        keywords.put("getforegroundtile", TokenType.FUNCTION);
        keywords.put("getbackgroundtile", TokenType.FUNCTION);
        keywords.put("getplayerx", TokenType.FUNCTION);
        keywords.put("getplayery", TokenType.FUNCTION);
        keywords.put("move", TokenType.FUNCTION);
        keywords.put("distanceto", TokenType.FUNCTION);
        keywords.put("gettimeleft", TokenType.FUNCTION);

        for(Tile t : Tile.values()) {
            keywords.put(t.name().toLowerCase(), TokenType.TILE);
        }
    }

    public static HashMap<String, Object> prepareForInterpreter(ArrayList<Token> tokens) {
        removeUnimportant(tokens);
        return getVariables(tokens);
    }

    private static void removeUnimportant(ArrayList<Token> tokens) {
        for(int i = tokens.size() - 1; i >= 0; i--) {
            Token token = tokens.get(i);
            switch(token.type) {
                case SPACE:
                case TAB:
                case CARRIAGE_RETURN:
                case NEW_LINE:
                case COMMENT:
                    tokens.remove(i);
                    break;
            }
        }
    }

    private static HashMap<String, Object> getVariables(ArrayList<Token> tokens) {
        HashMap<String, Object> variables = new HashMap<>();
        for(int i = tokens.size() - 1; i >= 0; i--) {
            Token token = tokens.get(i);
            switch(token.type) {
                case VAR:
                    variables.put(tokens.get(i + 1).lexeme, null);
                    tokens.remove(i);
                    break;
            }
        }
        return variables;
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
            case ':': addToken(TokenType.COLON); break;
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
            case ' ': addToken(TokenType.SPACE); break;
            case '\t': addToken(TokenType.TAB); break;
            case '\r': addToken(TokenType.CARRIAGE_RETURN); break;
            case '\n': line++; addToken(TokenType.NEW_LINE); break;
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
            addToken(TokenType.COMMENT);
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
        else {
            addToken(TokenType.OTHER);
        }
    }

    private void processOr() {
        if(isNextChar('|')) {
            addToken(TokenType.OR_OR);
        }
        else {
            addToken(TokenType.OTHER);
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
        else {
            addToken(TokenType.OTHER);
        }
    }

    private void processNumber() {
        while(isDigit(peek())) {
            getNextChar();
        }
        if(moves.contains(peek() + "")) {
            while(moves.contains(peek() + "")) {
                getNextChar();
            }
            String substr = code.substring(start, current);
            addToken(TokenType.MOVE, substr.toLowerCase());
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
            boolean move = true;
            for(int i = 0; i < substr.length(); i++) {
                if(!moves.contains(substr.charAt(i) + "")) {
                    move = false;
                    break;
                }
            }
            if(move) {
                addToken(TokenType.MOVE, substr.toLowerCase());
                return;
            }
            addToken(TokenType.IDENTIFIER, substr);
        }
        else if(type == TokenType.TILE) {
            addToken(type, substr.toUpperCase());
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
        String lexeme = code.substring(start, current);
        tokens.add(new Token(type, lexeme, value, line));
    }
}
