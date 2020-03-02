package tools.variation;

import game.Tile;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    private JTextPane console = new JTextPane();

    @Test
    void parseBlockStatement() {
        String code = "{1; 2;}";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.Block(new ArrayList<>(Arrays.asList(
                    new Stmt.Expression(new Expr.Literal(1.0)),
                    new Stmt.Expression(new Expr.Literal(2.0))
                )))
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void parseIfStatement() {
        String code = "if(1) 2; else if(3) 4; else 5;";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.If(
                        new Expr.Literal(1.0),
                        new Stmt.Expression(new Expr.Literal(2.0)),
                        new Stmt.If(
                                new Expr.Literal(3.0),
                                new Stmt.Expression(new Expr.Literal(4.0)),
                                new Stmt.Expression(new Expr.Literal(5.0))
                        )
                )
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void parseForStatement() {
        String code = "for(1; 2; 3) 4;";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.For(
                        new Stmt.Expression(new Expr.Literal(1.0)),
                        new Expr.Literal(2.0),
                        new Stmt.Expression(new Expr.Literal(3.0)),
                        new Stmt.Expression(new Expr.Literal(4.0))
                )
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void parsePrintStatement() {
        String code = "print 1;";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.Print(new Expr.Literal(1.0))
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void parseSequenceStatement() {
        String code = "[d][u](1,2){order udrlwh; start: 3; beforeMove: 4; afterMove: 5; beforeStep: 6; afterStep: 7; end: 8;}";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("u"));
        MovePool movePoolForced = new MovePool();
        movePoolForced.add(new Move("d"));

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.Sequence(
                        movePoolOptional,
                        movePoolForced,
                        1,
                        2,
                        "udrlwh",
                        new Stmt.Expression(new Expr.Literal(3.0)),
                        new Stmt.Expression(new Expr.Literal(4.0)),
                        new Stmt.Expression(new Expr.Literal(5.0)),
                        new Stmt.Expression(new Expr.Literal(6.0)),
                        new Stmt.Expression(new Expr.Literal(7.0)),
                        new Stmt.Expression(new Expr.Literal(8.0))
                )
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void parseEmptyStatement() {
        String code = ";";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.Empty()
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void parseBreakStatement() {
        String code = "break;";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.Break()
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void parseReturnStatement() {
        String code = "return;";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.Return()
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void parseTerminateStatement() {
        String code = "terminate 1;";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.Terminate(new Expr.Literal(1.0))
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void parseContinueStatement() {
        String code = "continue;";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.Continue()
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void parseAllStatement() {
        String code = "all 1;";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.All(new Expr.Literal(1.0))
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void parseExpressionStatements() {
        String code = "null; true; false; 3.7; 2du; var1; DIRT; (d);";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.Expression(new Expr.Literal(null)),
                new Stmt.Expression(new Expr.Literal(true)),
                new Stmt.Expression(new Expr.Literal(false)),
                new Stmt.Expression(new Expr.Literal(3.7)),
                new Stmt.Expression(new Expr.Literal(new Move("2du"))),
                new Stmt.Expression(new Expr.Variable(new Token(TokenType.IDENTIFIER, "var1", "var1", 1))),
                new Stmt.Expression(new Expr.Literal(Tile.valueOf("DIRT"))),
                new Stmt.Expression(new Expr.Group(new Expr.Literal(new Move("d"))))
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void parseLogicalWithPrecedence() {
        String code = "if(7>3 && 7>=3 || 3<7 and 3<=7 or 3!=7 and !(3==7)) 1;";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.If(
                        new Expr.Logical(
                                new Expr.Logical(
                                        new Expr.Logical(
                                                new Expr.Binary(
                                                        new Expr.Literal(7.0),
                                                        new Token(TokenType.GREATER, ">", null, 1),
                                                        new Expr.Literal(3.0)
                                                ),
                                                new Token(TokenType.AND_AND, "&&", null, 1),
                                                new Expr.Binary(
                                                        new Expr.Literal(7.0),
                                                        new Token(TokenType.GREATER_EQUAL, ">=", null, 1),
                                                        new Expr.Literal(3.0)
                                                )
                                        ),
                                        new Token(TokenType.OR_OR, "||", null, 1),
                                        new Expr.Logical(
                                                new Expr.Binary(
                                                        new Expr.Literal(3.0),
                                                        new Token(TokenType.LESS, "<", null, 1),
                                                        new Expr.Literal(7.0)
                                                ),
                                                new Token(TokenType.AND, "and", null, 1),
                                                new Expr.Binary(
                                                        new Expr.Literal(3.0),
                                                        new Token(TokenType.LESS_EQUAL, "<=", null, 1),
                                                        new Expr.Literal(7.0)
                                                )
                                        )
                                ),
                                new Token(TokenType.OR, "or", null, 1),
                                new Expr.Logical(
                                        new Expr.Binary(
                                                new Expr.Literal(3.0),
                                                new Token(TokenType.BANG_EQUAL, "!=", null, 1),
                                                new Expr.Literal(7.0)
                                        ),
                                        new Token(TokenType.AND, "and", null, 1),
                                        new Expr.Unary(
                                                new Token(TokenType.BANG, "!", null, 1),
                                                new Expr.Group(new Expr.Binary(
                                                        new Expr.Literal(3.0),
                                                        new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                                                        new Expr.Literal(7.0)
                                                ))
                                        )
                                )
                        ),
                        new Stmt.Expression(new Expr.Literal(1.0)),
                        null
                )
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void parseArithmeticWithPrecedence() {
        String code = "1 + 2 - 3 * 4 / 5 % 6;";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.Expression(
                        new Expr.Binary(
                                new Expr.Binary(
                                        new Expr.Literal(1.0),
                                        new Token(TokenType.PLUS, "+", null, 1),
                                        new Expr.Literal(2.0)
                                ),
                                new Token(TokenType.MINUS, "-", null, 1),
                                new Expr.Binary(
                                        new Expr.Binary(
                                                new Expr.Binary(
                                                        new Expr.Literal(3.0),
                                                        new Token(TokenType.STAR, "*", null, 1),
                                                        new Expr.Literal(4.0)
                                                ),
                                                new Token(TokenType.SLASH, "/", null, 1),
                                                new Expr.Literal(5.0)
                                        ),
                                        new Token(TokenType.MODULO, "%", null, 1),
                                        new Expr.Literal(6.0)
                                )
                        )
                )
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void parseAssignments() {
        String code = "var1 = 1; var2 += 2; var3 -= 3; var4 *= 4; var5 /= 5; var6 %= 6;";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.Expression(new Expr.Assign(
                        new Token(TokenType.IDENTIFIER, "var1", "var1", 1),
                        new Token(TokenType.EQUAL, "=", null, 1),
                        new Expr.Literal(1.0)
                )),
                new Stmt.Expression(new Expr.Assign(
                        new Token(TokenType.IDENTIFIER, "var2", "var2", 1),
                        new Token(TokenType.PLUS_EQUAL, "+=", null, 1),
                        new Expr.Literal(2.0)
                )),
                new Stmt.Expression(new Expr.Assign(
                        new Token(TokenType.IDENTIFIER, "var3", "var3", 1),
                        new Token(TokenType.MINUS_EQUAL, "-=", null, 1),
                        new Expr.Literal(3.0)
                )),
                new Stmt.Expression(new Expr.Assign(
                        new Token(TokenType.IDENTIFIER, "var4", "var4", 1),
                        new Token(TokenType.STAR_EQUAL, "*=", null, 1),
                        new Expr.Literal(4.0)
                )),
                new Stmt.Expression(new Expr.Assign(
                        new Token(TokenType.IDENTIFIER, "var5", "var5", 1),
                        new Token(TokenType.SLASH_EQUAL, "/=", null, 1),
                        new Expr.Literal(5.0)
                )),
                new Stmt.Expression(new Expr.Assign(
                        new Token(TokenType.IDENTIFIER, "var6", "var6", 1),
                        new Token(TokenType.MODULO_EQUAL, "%=", null, 1),
                        new Expr.Literal(6.0)
                ))
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void parseFunction() {
        String code = "move(2u, 4ud, w);";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        List<Stmt> expectedStatements = Arrays.asList(
                new Stmt.Expression(new Expr.Function(
                        "move",
                        new ArrayList<Expr>(Arrays.asList(
                                new Expr.Literal(new Move("2u")),
                                new Expr.Literal(new Move("4ud")),
                                new Expr.Literal(new Move("w"))
                        )),
                        new Token(TokenType.FUNCTION, "move", null, 1)
                ))
        );

        assertEquals(expectedStatements, statements);
    }

    @Test
    void syntaxErrorMissingSemicolon() {
        String code = "1";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        parser.parse();

        assertTrue(parser.hadError);
    }

    @Test
    void syntaxErrorInvalidLexicographic() {
        String code = "[u](){order udlr;}";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        parser.parse();

        assertTrue(parser.hadError);
    }

    @Test
    void syntaxErrorNoMoves() {
        String code = "[](){}";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        parser.parse();

        assertTrue(parser.hadError);
    }

    @Test
    void syntaxErrorInvalidAssignment() {
        String code = "1 = 2";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        parser.parse();

        assertTrue(parser.hadError);
    }

    @Test
    void syntaxErrorSynchronization() {
        String code = "1 = 2; 3;";
        Tokenizer tokenizer = new Tokenizer(code);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Tokenizer.prepareForInterpreter(tokens);
        Parser parser = new Parser(tokens, console);
        List<Stmt> statements = parser.parse();

        assertTrue(parser.hadError);
        assertEquals(null, statements.get(0));
        assertEquals(2, statements.size());
    }
}