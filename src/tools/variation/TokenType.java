package tools.variation;

public enum TokenType {
    // Structural
    LEFT_BRACKET, RIGHT_BRACKET, LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE, COMMA, SEMICOLON, COLON,

    // Operations
    PLUS, MINUS, PLUS_PLUS, MINUS_MINUS, SLASH, STAR, MODULO,
    EQUAL, PLUS_EQUAL, MINUS_EQUAL, SLASH_EQUAL, STAR_EQUAL, MODULO_EQUAL,
    AND_AND, OR_OR, BANG,

    // Comparison
    EQUAL_EQUAL, BANG_EQUAL,
    LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,

    // Values
    IDENTIFIER, NUMBER, MOVE, FUNCTION, TILE,

    // Keywords
    START, BEFORE_MOVE, AFTER_MOVE, BEFORE_STEP, AFTER_STEP, END,
    IF, ELSE, FOR, TRUE, FALSE, NULL, VAR, PRINT, BREAK,
    AND, OR, NOT,

    // Sequence control
    RETURN, CONTINUE, TERMINATE, LEXICOGRAPHIC, ALL,

    // Unimportant in parsing
    SPACE, TAB, CARRIAGE_RETURN, NEW_LINE, COMMENT,

    // Unknown
    OTHER,

    // End
    EOF


}
