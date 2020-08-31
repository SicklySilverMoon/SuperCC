package tools.variation;

import emulator.SuperCC;
import emulator.TickFlags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.VariationTesting;

import javax.swing.*;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {
    private SuperCC emulator = new SuperCC(false);
    private VariationTesting variationTesting = new VariationTesting(emulator, false);
    private JTextPane console = variationTesting.getConsole();

    @BeforeEach
    void setup() {
        emulator.openLevelset(new File("testData/sets/variationScriptTest.dat"));
        variationTesting.clearConsole();
    }

    @Test
    void printLiterals() {
        emulator.loadLevel(1);
        String code = "[u](){} print 1; print null; print 3.14; print true; print 4ud; print GLIDER_LEFT;";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("1\r\nnull\r\n3.14\r\ntrue\r\n4ud\r\nGlider - Left"));
    }

    @Test
    void interpretArithmetic() {
        emulator.loadLevel(1);
        String code = "[u](){} print 4*2 + 6/3 - 5%2; print 12 * (7-2) / 3; print 3.6 * 2.1;";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("9\r\n20\r\n7.56"));
    }

    @Test
    void interpretAssignment() {
        emulator.loadLevel(1);
        String code = "[u](){} var v1 = 0; v1 += 5; v1 -= 2; v1 *= 6; v1 /= 2; v1 %= 5; print v1; print v1;";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("4\r\n4"));
    }

    @Test
    void interpretConditionalOperators() {
        emulator.loadLevel(1);
        String code = "[u](){} print 1 < 2; print 2 > 1; print 3 == 3; print 4 != 5; print 6 >= 6; print 7 <= 8; print !true;";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("true\r\ntrue\r\ntrue\r\ntrue\r\ntrue\r\ntrue\r\nfalse"));
    }

    @Test
    void interpretBooleanOperators() {
        emulator.loadLevel(1);
        String code = "[u](){} print true or false; print true and false; print false || false; print true && true;";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("true\r\nfalse\r\nfalse\r\ntrue"));
    }

    @Test
    void interpretConditionals() {
        emulator.loadLevel(1);
        String code = "[u](){} if(true) print 1; else print 2; if(false) print 3; else if(true) print 4; else print 5;";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("1\r\n4"));
    }

    @Test
    void interpretNonBooleanConditionals() {
        emulator.loadLevel(1);
        String code = "[u](){} if(1) print 1; if(null) print 2; if(0) print 3; if(2ud) print 4; \n" +
                "if(v1) print 5; if(DIRT) print 6; if(0.0002) print 7; if(0.0) print 8; if(2.5*2/5-1) print 9; if(-1) print 10;";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("1\r\n4\r\n6\r\n7\r\n10"));
    }

    @Test
    void interpretLoop() {
        emulator.loadLevel(1);
        String code = "[u](){} for(var i = 0; i < 10; i += 2) { print i; }" +
                "for(var i = 0; i < 10; i += 2) { print i; if(i > 3) break; } print 0;";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("0\r\n2\r\n4\r\n6\r\n8\r\n0\r\n2\r\n4\r\n0"));
    }

    @Test
    void variableHandling() {
        emulator.loadLevel(1);
        String code = "var v1 = 0; [u,d](){ afterMove: { v1 += 1; print v1; }} " +
                "var v2 = 10; [u,d](){ afterMove: { v2 += 1; print v2; } }";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("1\r\n2\r\n11\r\n12\r\n11\r\n12\r\n1\r\n2\r\n11\r\n12\r\n11\r\n12"));
    }

    @Test
    void findSolution() {
        emulator.loadLevel(1);
        String code = "[2u, 2r](){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();

        assertEquals(1, interpreter.solutions.size());
    }

    @Test
    void findMultipleSolutions() {
        emulator.loadLevel(1);
        String code = "all; [2u, 2r](){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();

        assertEquals(6, interpreter.solutions.size());
    }

    @Test
    void findSolutionWithExistingMoves() {
        emulator.loadLevel(1);
        emulator.tick(SuperCC.UP, TickFlags.PRELOADING);
        emulator.tick(SuperCC.RIGHT, TickFlags.PRELOADING);
        emulator.tick(SuperCC.DOWN, TickFlags.PRELOADING);
        emulator.tick(SuperCC.WAIT, TickFlags.PRELOADING);
        emulator.tick(SuperCC.LEFT, TickFlags.PRELOADING);
        emulator.tick(SuperCC.LEFT, TickFlags.PRELOADING);
        emulator.tick(SuperCC.UP, TickFlags.PRELOADING);
        String code = "print getPlayerX(); print getPlayerY(); [u, 2r](){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertEquals(1, interpreter.solutions.size());
        assertTrue(consoleText.contains("14\r\n13"));
    }

    @Test
    void findSolutionMultipleSequences() {
        emulator.loadLevel(1);
        String code = "[u, r](){} [u, r](){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();

        assertEquals(1, interpreter.solutions.size());
    }

    @Test
    void findMultipleSolutionsMultipleSequences() {
        emulator.loadLevel(1);
        String code = "all; [u, r](){} [u, r](){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();

        assertEquals(4, interpreter.solutions.size());
    }

    @Test
    void findSolutionMultipleSubsets() {
        emulator.loadLevel(1);
        String code = "[3u, 2r](2,5){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();

        assertEquals(1, interpreter.solutions.size());
    }

    @Test
    void findMultipleSolutionsMultipleSubsets() {
        emulator.loadLevel(1);
        String code = "all; [3u, 2r](2, 5){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();

        assertEquals(16, interpreter.solutions.size());
    }

    @Test
    void getPlayerPosition() {
        emulator.loadLevel(1);
        String code = "[u](){} print getPlayerX(); print getPlayerY();";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("14\r\n13"));
    }

    @Test
    void getMoves() {
        emulator.loadLevel(1);
        String code = "[dl, r, l](){start: { print seqLength(); for(var i = 0; i < seqLength(); i += 1) print getMove(i); terminate; } }";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("3\r\nr\r\nd\r\nl"));
    }

    @Test
    void getRelativeMovesBeforeMove() {
        emulator.loadLevel(1);
        String code = "[dl, r, l](){ beforeMove: { print previousMove(); print nextMove(); } end: terminate -1; }";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("null\r\nr\r\nr\r\nd\r\nl\r\nl"));
    }

    @Test
    void getRelativeMovesAfterMove() {
        emulator.loadLevel(1);
        String code = "[dl, r, l](){ afterMove: { print previousMove(); print nextMove(); } end: terminate -1; }";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("r\r\nd\r\nl\r\nl\r\nl\r\nnull"));
    }

    @Test
    void oppositeMovesAndExecutedMoves() {
        emulator.loadLevel(1);
        String code = "[dl, r, l](){ beforeMove: { print movesExecuted(); print getOppositeMove(nextMove()); } end: terminate -1; }";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("0\r\nl\r\n1\r\nu\r\n2\r\nr"));
    }

    @Test
    void moveCount() {
        emulator.loadLevel(1);
        String code = "[2d, 3lr](2,4){ start: { print moveCount(lr); terminate -1; } }";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("0\r\n1\r\n2\r\n1\r\n2\r\n3\r\n2\r\n3"));
    }

    @Test
    void chipCount() {
        emulator.loadLevel(1);
        String code = "print getChipsLeft(); [ur](){} print getChipsLeft();";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("1\r\n0"));
    }

    @Test
    void tileLayers() {
        emulator.loadLevel(1);
        String code = "[u](){} print getForegroundTile(15, 9); print getBackgroundTile(15, 9);";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("Block\r\nFire"));
    }

    @Test
    void timeLeft() {
        emulator.loadLevel(1);
        String code = "print getTimeLeft(); [u](){} move(2l, 2u, d, rdrdr); print getTimeLeft();";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("100.9\r\n98.8"));
    }

    @Test
    void manhattanDistance() {
        emulator.loadLevel(1);
        String code = "[u](){} print distanceTo(14, 13); print distanceTo(11, 13); print distanceTo(18, 10); print distanceTo(16, 16);";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("0\r\n3\r\n7\r\n5"));
    }

    @Test
    void noSequenceError() {
        emulator.loadLevel(1);
        String code = "print null;";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("Script must contain at least 1 sequence"));
        assertFalse(consoleText.contains("null"));
    }

    @Test
    void invalidBoundError() {
        emulator.loadLevel(1);
        String code = "[u](0){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("Sequence upper bound must be at least 1"));
    }

    @Test
    void undefinedVariableError() {
        emulator.loadLevel(1);
        String code = "v1 += 1; [u](){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("[Line 1 near 'v1'] Variable undefined"));
    }

    @Test
    void operandMustBeNumberError() {
        emulator.loadLevel(1);
        String code = "u + 1; [u](){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("[Line 1 near '+'] Operand must be a number"));
    }

    @Test
    void functionArgumentNotNumberError() {
        emulator.loadLevel(1);
        String code = "getForegroundTile(1, d); [u](){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("[Line 1 near 'getForegroundTile'] Argument must be a number"));
    }

    @Test
    void functionArgumentNotMoveError() {
        emulator.loadLevel(1);
        String code = "getOppositeMove(1); [u](){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("[Line 1 near 'getOppositeMove'] Argument must be a move"));
    }

    @Test
    void functionArgumentWrongCountError() {
        emulator.loadLevel(1);
        String code = "getForegroundTile(1); [u](){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("[Line 1 near 'getForegroundTile'] Expected 2 arguments, got 1"));
    }

    @Test
    void ItemCount() {
        emulator.loadLevel(2);
        String code = "print getRedKeyCount(); print getYellowKeyCount(); print getGreenKeyCount(); print getBlueKeyCount(); " +
                "print hasFlippers(); print hasFireBoots(); print hasSuctionBoots(); print hasIceSkates(); " +
                "[u](){} move(u, 3r, 4d, 3l); " +
                "print getRedKeyCount(); print getYellowKeyCount(); print getGreenKeyCount(); print getBlueKeyCount(); " +
                "print hasFlippers(); print hasFireBoots(); print hasSuctionBoots(); print hasIceSkates(); ";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("0\r\n0\r\n0\r\n0\r\nfalse\r\nfalse\r\nfalse\r\nfalse\r\n" +
                "1\r\n1\r\n1\r\n1\r\ntrue\r\ntrue\r\ntrue\r\ntrue"));
    }

    @Test
    void deathHandling() {
        emulator.loadLevel(3);
        String code = "[4r](){ beforeMove: print getPlayerX(); }";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertTrue(consoleText.contains("12\r\n13"));
        assertEquals(0, interpreter.solutions.size());
    }

    @Test
    void deathHandlingMultipleSolutions() {
        emulator.loadLevel(3);
        String code = "all; [4r, u, d](){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();

        assertEquals(2, interpreter.solutions.size());
    }

    @Test
    void fullExample() {
        emulator.loadLevel(4);
        String code = "all; [10ud, 5w](4, 15){}" +
                "for(var i = 0; i < 10; i += 1) {" +
                "if(getForegroundTile(3, 3) == GLIDER_LEFT) { move(7r, 2u); } move(w); }";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertEquals(655, interpreter.solutions.size());
        assertTrue(consoleText.contains("12,360 variations"));
    }

    @Test
    void zeroSequenceDeath() {
        emulator.loadLevel(5);
        String code = "all; move(r); [2w](0,2){} move(3r);";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertEquals(2, interpreter.solutions.size());
        assertTrue(consoleText.contains("3 variations"));
    }

    @Test
    void multipleSequenceFullMexample() {
        emulator.loadLevel(6);
        String code = "all;\n" +
                "[3ud][ud, 3w](4){}\n" +
                "for(;getPlayerX() != 12;) move(r);\n" +
                "move(4u, 3r, d);\n" +
                "[2l, 2r](1,4){}\n" +
                "move(u);\n" +
                "for(;getPlayerX() != 17;) move(r);\n" +
                "move(d, 2r, 3d, r);\n" +
                "[2ww](0,2){}\n" +
                "[3r](1,3){}\n" +
                "[urd](0,1){}\n" +
                "move(4u);\n" +
                "if(getPlayerX() > 23) move(l);\n" +
                "for(;getPlayerX() != 23;) move(r);\n" +
                "move(udrru);\n" +
                "[w](0,1){}\n" +
                "move(ddrlddrr);";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
        String consoleText = console.getText();

        assertEquals(37, interpreter.solutions.size());
        assertTrue(consoleText.contains("3,240 variations"));
        assertTrue(consoleText.contains("241 variations"));
    }

    @Test
    void findingSolutionPrematurelyShouldntCrash() {
        emulator.loadLevel(3);
        String code = "all; move(rdrr); [u,r,d,l](){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
    }

    @Test
    void sequencesAfterFindingSolutionShouldntCrash() {
        emulator.loadLevel(3);
        String code = "all; move(rdrr); [u,r,d,l](){} [u,r](){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
    }

    @Test
    void terminatingAtSequenceBorderShouldntCrash() {
        emulator.loadLevel(3);
        String code = "all;\n" +
                "[u,r,d,l](3){}\n" +
                "[u,r,d,l](3){}\n" +
                "[u,r,d,l](3){}";

        Interpreter interpreter = new Interpreter(emulator, variationTesting, console, code);
        interpreter.interpret();
    }
}