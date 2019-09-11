package tools.variation;

public class Token {
    public final TokenType type;
    public final Object value;

    public Token(TokenType type, Object value) {
        this.type = type;
        this.value = value;
    }
}
