package tools.variation;

import java.awt.*;
import java.util.HashMap;
import java.util.Objects;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object value;
    public final int line;

    public Token(TokenType type, String lexeme, Object value, int line) {
        this.type = type;
        this.lexeme = (type == TokenType.EOF) ? "" : lexeme;
        this.value = value;
        this.line = line;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return line == token.line &&
                type == token.type &&
                lexeme.equals(token.lexeme) &&
                Objects.equals(value, token.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, lexeme, value, line);
    }
}
