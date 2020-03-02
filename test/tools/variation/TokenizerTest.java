package tools.variation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {
    private final Token SPACE = new Token(TokenType.SPACE, " ", null, 1);

    @Test
    void tokenizeStructural() {
        String code = "[](){},;:";

        Tokenizer tokenizer = new Tokenizer(code);
        List<Token> tokens = tokenizer.getAllTokens();

        List<Token> expectedTokens = Arrays.asList(
                new Token(TokenType.LEFT_BRACKET, "[", null, 1),
                new Token(TokenType.RIGHT_BRACKET, "]", null, 1),
                new Token(TokenType.LEFT_PAREN, "(", null, 1),
                new Token(TokenType.RIGHT_PAREN, ")", null, 1),
                new Token(TokenType.LEFT_BRACE, "{", null, 1),
                new Token(TokenType.RIGHT_BRACE, "}", null, 1),
                new Token(TokenType.COMMA, ",", null, 1),
                new Token(TokenType.SEMICOLON, ";", null, 1),
                new Token(TokenType.COLON, ":", null, 1),
                new Token(TokenType.EOF, "", null, 1)
        );

        assertEquals(expectedTokens, tokens);
    }

    @Test
    void tokenizeWhitespace() {
        String code = " \t\r\n";

        Tokenizer tokenizer = new Tokenizer(code);
        List<Token> tokens = tokenizer.getAllTokens();

        List<Token> expectedTokens = Arrays.asList(
                new Token(TokenType.SPACE, " ", null, 1),
                new Token(TokenType.TAB, "\t", null, 1),
                new Token(TokenType.CARRIAGE_RETURN, "\r", null, 1),
                new Token(TokenType.NEW_LINE, "\n", null, 2),
                new Token(TokenType.EOF, "", null, 2)
        );

        assertEquals(expectedTokens, tokens);
    }

    @Test
    void tokenizeNumbers() {
        String code = "3 234 12.345";

        Tokenizer tokenizer = new Tokenizer(code);
        List<Token> tokens = tokenizer.getAllTokens();

        List<Token> expectedTokens = Arrays.asList(
                new Token(TokenType.NUMBER, "3", 3.0, 1), SPACE,
                new Token(TokenType.NUMBER, "234", 234.0, 1), SPACE,
                new Token(TokenType.NUMBER, "12.345", 12.345, 1),
                new Token(TokenType.EOF, "", null, 1)
        );

        assertEquals(expectedTokens, tokens);
    }

    @Test
    void tokenizeMoves() {
        String code = "r urdw 4h 12lr";

        Tokenizer tokenizer = new Tokenizer(code);
        List<Token> tokens = tokenizer.getAllTokens();

        List<Token> expectedTokens = Arrays.asList(
                new Token(TokenType.MOVE, "r", "r", 1), SPACE,
                new Token(TokenType.MOVE, "urdw", "urdw", 1), SPACE,
                new Token(TokenType.MOVE, "4h", "4h", 1), SPACE,
                new Token(TokenType.MOVE, "12lr", "12lr", 1),
                new Token(TokenType.EOF, "", null, 1)
        );

        assertEquals(expectedTokens, tokens);
    }

    @Test
    void tokenizeOperators() {
        String code = "var1=3.5 var1+=7+3 var1-=7-3 var1/=7/3 var1*=7*3 var1%=7%3 var1++ var1--";

        Tokenizer tokenizer = new Tokenizer(code);
        List<Token> tokens = tokenizer.getAllTokens();

        List<Token> expectedTokens = Arrays.asList(
                new Token(TokenType.IDENTIFIER, "var1", "var1", 1),
                new Token(TokenType.EQUAL, "=", null, 1),
                new Token(TokenType.NUMBER, "3.5", 3.5, 1), SPACE,
                new Token(TokenType.IDENTIFIER, "var1", "var1", 1),
                new Token(TokenType.PLUS_EQUAL, "+=", null, 1),
                new Token(TokenType.NUMBER, "7", 7.0, 1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.NUMBER, "3", 3.0, 1), SPACE,
                new Token(TokenType.IDENTIFIER, "var1", "var1", 1),
                new Token(TokenType.MINUS_EQUAL, "-=", null, 1),
                new Token(TokenType.NUMBER, "7", 7.0, 1),
                new Token(TokenType.MINUS, "-", null, 1),
                new Token(TokenType.NUMBER, "3", 3.0, 1), SPACE,
                new Token(TokenType.IDENTIFIER, "var1", "var1", 1),
                new Token(TokenType.SLASH_EQUAL, "/=", null, 1),
                new Token(TokenType.NUMBER, "7", 7.0, 1),
                new Token(TokenType.SLASH, "/", null, 1),
                new Token(TokenType.NUMBER, "3", 3.0, 1), SPACE,
                new Token(TokenType.IDENTIFIER, "var1", "var1", 1),
                new Token(TokenType.STAR_EQUAL, "*=", null, 1),
                new Token(TokenType.NUMBER, "7", 7.0, 1),
                new Token(TokenType.STAR, "*", null, 1),
                new Token(TokenType.NUMBER, "3", 3.0, 1), SPACE,
                new Token(TokenType.IDENTIFIER, "var1", "var1", 1),
                new Token(TokenType.MODULO_EQUAL, "%=", null, 1),
                new Token(TokenType.NUMBER, "7", 7.0, 1),
                new Token(TokenType.MODULO, "%", null, 1),
                new Token(TokenType.NUMBER, "3", 3.0, 1), SPACE,
                new Token(TokenType.IDENTIFIER, "var1", "var1", 1),
                new Token(TokenType.PLUS_PLUS, "++", null, 1), SPACE,
                new Token(TokenType.IDENTIFIER, "var1", "var1", 1),
                new Token(TokenType.MINUS_MINUS, "--", null, 1),
                new Token(TokenType.EOF, "", null, 1)
        );

        assertEquals(expectedTokens, tokens);
    }

    @Test
    void tokenizeLogical() {
        String code = "7>3 && 7>=3 || 3<7 & 3<=7 | 3!=7 ! 3==3";

        Tokenizer tokenizer = new Tokenizer(code);
        List<Token> tokens = tokenizer.getAllTokens();

        List<Token> expectedTokens = Arrays.asList(
                new Token(TokenType.NUMBER, "7", 7.0, 1),
                new Token(TokenType.GREATER, ">", null, 1),
                new Token(TokenType.NUMBER, "3", 3.0, 1), SPACE,
                new Token(TokenType.AND_AND, "&&", null, 1), SPACE,
                new Token(TokenType.NUMBER, "7", 7.0, 1),
                new Token(TokenType.GREATER_EQUAL, ">=", null, 1),
                new Token(TokenType.NUMBER, "3", 3.0, 1), SPACE,
                new Token(TokenType.OR_OR, "||", null, 1), SPACE,
                new Token(TokenType.NUMBER, "3", 3.0, 1),
                new Token(TokenType.LESS, "<", null, 1),
                new Token(TokenType.NUMBER, "7", 7.0, 1), SPACE,
                new Token(TokenType.OTHER, "&", null, 1), SPACE,
                new Token(TokenType.NUMBER, "3", 3.0, 1),
                new Token(TokenType.LESS_EQUAL, "<=", null, 1),
                new Token(TokenType.NUMBER, "7", 7.0, 1), SPACE,
                new Token(TokenType.OTHER, "|", null, 1), SPACE,
                new Token(TokenType.NUMBER, "3", 3.0, 1),
                new Token(TokenType.BANG_EQUAL, "!=", null, 1),
                new Token(TokenType.NUMBER, "7", 7.0, 1), SPACE,
                new Token(TokenType.BANG, "!", null, 1), SPACE,
                new Token(TokenType.NUMBER, "3", 3.0, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, "3", 3.0, 1),
                new Token(TokenType.EOF, "", null, 1)
        );

        assertEquals(expectedTokens, tokens);
    }

    @Test
    void tokenizeOther() {
        String code = "?";

        Tokenizer tokenizer = new Tokenizer(code);
        List<Token> tokens = tokenizer.getAllTokens();

        List<Token> expectedTokens = Arrays.asList(
                new Token(TokenType.OTHER, "?", null, 1),
                new Token(TokenType.EOF, "", null, 1)
        );

        assertEquals(expectedTokens, tokens);
    }

    @Test
    void tokenizeCodeSample() {
        String code = "all;\n" +
                "[3ud](3){afterMove:{if(getForegroundTile(1,1)==DIRT)terminate;}}\n" +
                "// Finishing\n" +
                "move(4u,3r);";

        Tokenizer tokenizer = new Tokenizer(code);
        List<Token> tokens = tokenizer.getAllTokens();

        List<Token> expectedTokens = Arrays.asList(
                new Token(TokenType.ALL, "all", null, 1),
                new Token(TokenType.SEMICOLON, ";", null, 1),
                new Token(TokenType.NEW_LINE, "\n", null, 2),
                new Token(TokenType.LEFT_BRACKET, "[", null, 2),
                new Token(TokenType.MOVE, "3ud", "3ud", 2),
                new Token(TokenType.RIGHT_BRACKET, "]", null, 2),
                new Token(TokenType.LEFT_PAREN, "(", null, 2),
                new Token(TokenType.NUMBER, "3", 3.0, 2),
                new Token(TokenType.RIGHT_PAREN, ")", null, 2),
                new Token(TokenType.LEFT_BRACE, "{", null, 2),
                new Token(TokenType.AFTER_MOVE, "afterMove", null, 2),
                new Token(TokenType.COLON, ":", null, 2),
                new Token(TokenType.LEFT_BRACE, "{", null, 2),
                new Token(TokenType.IF, "if", null, 2),
                new Token(TokenType.LEFT_PAREN, "(", null, 2),
                new Token(TokenType.FUNCTION, "getForegroundTile", null, 2),
                new Token(TokenType.LEFT_PAREN, "(", null, 2),
                new Token(TokenType.NUMBER, "1", 1.0, 2),
                new Token(TokenType.COMMA, ",", null, 2),
                new Token(TokenType.NUMBER, "1", 1.0, 2),
                new Token(TokenType.RIGHT_PAREN, ")", null, 2),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 2),
                new Token(TokenType.TILE, "DIRT", "DIRT", 2),
                new Token(TokenType.RIGHT_PAREN, ")", null, 2),
                new Token(TokenType.TERMINATE, "terminate", null, 2),
                new Token(TokenType.SEMICOLON, ";", null, 2),
                new Token(TokenType.RIGHT_BRACE, "}", null, 2),
                new Token(TokenType.RIGHT_BRACE, "}", null, 2),
                new Token(TokenType.NEW_LINE, "\n", null, 3),
                new Token(TokenType.COMMENT, "// Finishing", null, 3),
                new Token(TokenType.NEW_LINE, "\n", null, 4),
                new Token(TokenType.FUNCTION, "move", null, 4),
                new Token(TokenType.LEFT_PAREN, "(", null, 4),
                new Token(TokenType.MOVE, "4u", "4u", 4),
                new Token(TokenType.COMMA, ",", null, 4),
                new Token(TokenType.MOVE, "3r", "3r", 4),
                new Token(TokenType.RIGHT_PAREN, ")", null, 4),
                new Token(TokenType.SEMICOLON, ";", null, 4),
                new Token(TokenType.EOF, "", null, 4)
        );

        assertEquals(expectedTokens, tokens);
    }

    @Test
    void prepareForInterpreter() {
        String code = "var x = 1 // Init\n" +
                "x+=1 var y = 2 z = 3";

        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.getParsableTokens();
        HashMap<String, Object> variables = tokenizer.getVariables();

        List<Token> expectedTokens = Arrays.asList(
                new Token(TokenType.IDENTIFIER, "x", "x", 1),
                new Token(TokenType.EQUAL, "=", null, 1),
                new Token(TokenType.NUMBER, "1", 1.0, 1),
                new Token(TokenType.IDENTIFIER, "x", "x", 2),
                new Token(TokenType.PLUS_EQUAL, "+=", null, 2),
                new Token(TokenType.NUMBER, "1", 1.0, 2),
                new Token(TokenType.IDENTIFIER, "y", "y", 2),
                new Token(TokenType.EQUAL, "=", null, 2),
                new Token(TokenType.NUMBER, "2", 2.0, 2),
                new Token(TokenType.IDENTIFIER, "z", "z", 2),
                new Token(TokenType.EQUAL, "=", null, 2),
                new Token(TokenType.NUMBER, "3", 3.0, 2),
                new Token(TokenType.EOF, "", null, 2)
        );
        HashMap<String, Object> expectedVariables = new HashMap<>();
        expectedVariables.put("x", null);
        expectedVariables.put("y", null);

        assertEquals(expectedTokens, tokens);
        assertEquals(expectedVariables, variables);
    }
}