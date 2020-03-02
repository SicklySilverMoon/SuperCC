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

        for (Tile t : Tile.values()) {
            keywords.put(t.name().toLowerCase(), TokenType.TILE);
        }
    }

    public Tokenizer(String code) {
        this.code = code;
        this.tokens = new ArrayList<>();
        tokenize();
    }

    public ArrayList<Token> getAllTokens() {
        return tokens;
    }

    public ArrayList<Token> getParsableTokens() {
        return getFilteredTokens(TokenType.SPACE, TokenType.TAB, TokenType.CARRIAGE_RETURN,
                TokenType.NEW_LINE, TokenType.COMMENT, TokenType.VAR);
    }

    public HashMap<String, Object> getVariables() {
        HashMap<String, Object> variables = new HashMap<>();
        ArrayList<Token> filteredTokens = getFilteredTokens(TokenType.SPACE, TokenType.TAB, TokenType.CARRIAGE_RETURN,
                TokenType.NEW_LINE, TokenType.COMMENT);
        for (int i = 0; i < filteredTokens.size(); i++) {
            Token token = filteredTokens.get(i);
            if (token.type == TokenType.VAR) {
                variables.put(filteredTokens.get(i + 1).lexeme, null);
            }
        }
        return variables;
    }

    private ArrayList<Token> getFilteredTokens(TokenType... types) {
        ArrayList<Token> newTokens = new ArrayList<>();
        for (Token token : tokens) {
            if (!isTokenType(token, types)) {
                newTokens.add(token);
            }
        }
        return newTokens;
    }

    private boolean isTokenType(Token token, TokenType... types) {
        for (TokenType type : types) {
            if (token.type == type) {
                return true;
            }
        }
        return false;
    }

    private void tokenize() {
        while (!isEnd()) {
            start = current;
            getNextToken();
        }
        addToken(TokenType.EOF);
    }

    private void getNextToken() {
        char c = getNextChar();
        switch (c) {
            case '[': addToken(TokenType.LEFT_BRACKET); break;
            case ']': addToken(TokenType.RIGHT_BRACKET); break;
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case ';': addToken(TokenType.SEMICOLON);break;
            case ':': addToken(TokenType.COLON); break;
            case '+': processPlus(); break;
            case '-': processMinus(); break;
            case '/': processSlash(); break;
            case '*': processStar(); break;
            case '%': processModulo(); break;
            case '=': processEqual(); break;
            case '&': processAnd(); break;
            case '|': processOr(); break;
            case '!': processBang(); break;
            case '<': processLess(); break;
            case '>': processGreater(); break;
            case ' ': addToken(TokenType.SPACE); break;
            case '\t': addToken(TokenType.TAB); break;
            case '\r': addToken(TokenType.CARRIAGE_RETURN); break;
            case '\n': line++; addToken(TokenType.NEW_LINE); break;
            default: processOther(c);
        }
    }

    private void processPlus() {
        char c = getNextChar();
        switch(c) {
            case '+': addToken(TokenType.PLUS_PLUS); break;
            case '=': addToken(TokenType.PLUS_EQUAL); break;
            default: getPrevChar(); addToken(TokenType.PLUS);
        }
    }

    private void processMinus() {
        char c = getNextChar();
        switch(c) {
            case '-': addToken(TokenType.MINUS_MINUS); break;
            case '=': addToken(TokenType.MINUS_EQUAL); break;
            default: getPrevChar(); addToken(TokenType.MINUS);
        }
    }

    private void processSlash() {
        char c = getNextChar();
        switch(c) {
            case '/': processComment(); break;
            case '=': addToken(TokenType.SLASH_EQUAL); break;
            default: getPrevChar(); addToken(TokenType.SLASH);
        }
    }

    private void processComment() {
        while (getChar() != '\n' && !isEnd()) {
            getNextChar();
        }
        addToken(TokenType.COMMENT);
    }

    private void processStar() {
        char c = getNextChar();
        switch(c) {
            case '=': addToken(TokenType.STAR_EQUAL); break;
            default: getPrevChar(); addToken(TokenType.STAR);
        }
    }

    private void processModulo() {
        char c = getNextChar();
        switch(c) {
            case '=': addToken(TokenType.MODULO_EQUAL); break;
            default: getPrevChar(); addToken(TokenType.MODULO);
        }
    }

    private void processEqual() {
        char c = getNextChar();
        switch(c) {
            case '=': addToken(TokenType.EQUAL_EQUAL); break;
            default: getPrevChar(); addToken(TokenType.EQUAL);
        }
    }

    private void processAnd() {
        char c = getNextChar();
        switch(c) {
            case '&': addToken(TokenType.AND_AND); break;
            default: getPrevChar(); addToken(TokenType.OTHER);
        }
    }

    private void processOr() {
        char c = getNextChar();
        switch(c) {
            case '|': addToken(TokenType.OR_OR); break;
            default: getPrevChar(); addToken(TokenType.OTHER);
        }
    }

    private void processBang() {
        char c = getNextChar();
        switch(c) {
            case '=': addToken(TokenType.BANG_EQUAL); break;
            default: getPrevChar(); addToken(TokenType.BANG);
        }
    }

    private void processLess() {
        char c = getNextChar();
        switch(c) {
            case '=': addToken(TokenType.LESS_EQUAL); break;
            default: getPrevChar(); addToken(TokenType.LESS);
        }
    }

    private void processGreater() {
        char c = getNextChar();
        switch(c) {
            case '=': addToken(TokenType.GREATER_EQUAL); break;
            default: getPrevChar(); addToken(TokenType.GREATER);
        }
    }

    private void processOther(char c) {
        if (isDigit(c)) {
            processNumber();
        } else if (isAlpha(c)) {
            processIdentifier();
        } else {
            addToken(TokenType.OTHER);
        }
    }

    private void processNumber() {
        while (isDigit(getChar())) {
            getNextChar();
        }
        if (getChar() == '.') {
            getNextChar();
            while (isDigit(getChar())) {
                getNextChar();
            }
        } else if (isMove(getChar())) {
            processMultipleMoves();
            return;
        }
        String substr = code.substring(start, current);
        addToken(TokenType.NUMBER, Double.parseDouble(substr));
    }

    private boolean isMove(char c) {
        return moves.contains(c + "");
    }

    private void processMultipleMoves() {
        while (isMove(getChar())) {
            getNextChar();
        }
        String substr = code.substring(start, current);
        addToken(TokenType.MOVE, substr.toLowerCase());
    }

    private void processIdentifier() {
        while (isAlphaNumeric(getChar())) {
            getNextChar();
        }
        String substr = code.substring(start, current);
        TokenType type = keywords.get(substr.toLowerCase());
        if (type == null) {
            type = TokenType.OTHER;
        }
        switch (type) {
            case OTHER: processNonKeyword(substr); break;
            case TILE: addToken(type, substr.toUpperCase()); break;
            default: addToken(type);
        }
    }

    private void processNonKeyword(String substr) {
        if (isMoveString(substr)) {
            addToken(TokenType.MOVE, substr.toLowerCase());
        } else {
            addToken(TokenType.IDENTIFIER, substr);
        }
    }

    private boolean isMoveString(String substr) {
        boolean move = true;
        for (int i = 0; i < substr.length(); i++) {
            if (!isMove(substr.charAt(i))) {
                move = false;
                break;
            }
        }
        return move;
    }

    private char getNextChar() {
        current++;
        return code.charAt(current - 1);
    }

    private char getPrevChar() {
        current--;
        return code.charAt(current);
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

    private char getChar() {
        if (isEnd()) {
            return '\0';
        }
        return code.charAt(current);
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
