package tools.variation;

import java.awt.*;
import java.util.HashMap;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object value;

    public Token(TokenType type, String lexeme, Object value) {
        this.type = type;
        this.lexeme = (type == TokenType.EOF) ? "" : lexeme;
        this.value = value;
    }
}
