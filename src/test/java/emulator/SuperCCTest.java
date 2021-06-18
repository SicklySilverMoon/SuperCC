package emulator;

import game.Level;
import game.Tile;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SuperCCTest {
    private SuperCC emulator = new SuperCC(false);

    /**
     * If solution is stored in TWS file, provide the exact filename.
     * If solution is stored in JSON file, provide only base filename (without level number and extension)
     */
    boolean[] solveLevelset(String solutionName) {
        int levels = emulator.lastLevelNumber() - 1;
        boolean[] solved = new boolean[levels];
        Arrays.fill(solved, Boolean.FALSE);

        if (solutionName.substring(solutionName.length() - 4).equals(".tws")) {
            emulator.setTWSFile(new File(solutionName));
        }

        for (int i = 1; i <= levels; i++) {
            emulator.loadLevel(i);
            Level level = emulator.getLevel();
            try {
                Solution s = getSolution(solutionName, i);
                s.load(emulator);
                level = emulator.getLevel();
                if (level.getLayerFG().get(level.getChip().getPosition()) != Tile.EXITED_CHIP && !level.isCompleted()) {
                    System.out.println("failed level " + level.getLevelNumber() + " " + new String(level.getTitle()));
                } else {
                    solved[i - 1] = true;
                }
            } catch (Exception exc) {
                System.out.println("Error loading " + level.getLevelNumber() + " " + new String(level.getTitle()));
                exc.printStackTrace();
            }
        }

        return solved;
    }

    Solution getSolution(String solutionName, int level) throws IOException {
        if (emulator.twsReader != null) {
            return emulator.twsReader.readSolution(emulator.getLevel());
        } else {
            byte[] fileBytes = Files.readAllBytes((new File(solutionName + level + ".json")).toPath());
            return Solution.fromJSON(new String(fileBytes, UTF_8));
        }
    }

    @Test
    void solveCHIPS() {
        emulator.openLevelset(new File("testData/sets/CHIPS.DAT"));
        boolean[] solved = solveLevelset("testData/tws/public_CHIPS.dac.tws");

        boolean[] expectedSolved = new boolean[149];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }

    @Test
    void solveCCLP1() {
        emulator.openLevelset(new File("testData/sets/CCLP1.DAT"));
        boolean[] solved = solveLevelset("testData/tws/public_CCLP1.dac.tws");

        boolean[] expectedSolved = new boolean[149];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }

    @Test
    void solveCCLP2() {
        emulator.openLevelset(new File("testData/sets/CCLP2.DAT"));
        boolean[] solved = solveLevelset("testData/tws/public_CCLP2.dac.tws");

        boolean[] expectedSolved = new boolean[149];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }

    @Test
    void solveCCLP3() {
        emulator.openLevelset(new File("testData/sets/CCLP3.DAT"));
        boolean[] solved = solveLevelset("testData/tws/public_CCLP3.dac.tws");

        boolean[] expectedSolved = new boolean[149];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }

    @Test
    void solveCCLP4() {
        emulator.openLevelset(new File("testData/sets/CCLP4.DAT"));
        boolean[] solved = solveLevelset("testData/tws/public_CCLP4.dac.tws");

        boolean[] expectedSolved = new boolean[149];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }

    @Test
    void solveCHIPSLynx() {
        emulator.openLevelset(new File("testData/sets/CHIPS-lynx.DAT"));
        boolean[] solved = solveLevelset("testData/tws/public_CHIPS-lynx.dac.tws");
        solved[144] = true; //"Thanks to...", not playable in Lynx, so we give it a free pass

        boolean[] expectedSolved = new boolean[149];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }

    @Test
    void solveCCLP1Lynx() {
        emulator.openLevelset(new File("testData/sets/CCLP1-Lynx.dat"));
        boolean[] solved = solveLevelset("testData/tws/public_CCLP1-lynx.dac.tws");

        boolean[] expectedSolved = new boolean[149];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }

    @Test
    void solveCCLXP2() {
        emulator.openLevelset(new File("testData/sets/CCLXP2.dat"));
        boolean[] solved = solveLevelset("testData/tws/public_CCLXP2.dac.tws");

        boolean[] expectedSolved = new boolean[149];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }

    @Test
    void solveCCLP3Lynx() {
        emulator.openLevelset(new File("testData/sets/CCLP3-Lynx.DAT"));
        boolean[] solved = solveLevelset("testData/tws/public_CCLP3-lynx.dac.tws");

        boolean[] expectedSolved = new boolean[149];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }

    @Test
    void solveCCLP4Lynx() {
        emulator.openLevelset(new File("testData/sets/CCLP4-Lynx.DAT"));
        boolean[] solved = solveLevelset("testData/tws/public_CCLP4-lynx.dac.tws");

        boolean[] expectedSolved = new boolean[149];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }

    @Test
    void solveUnitTests() {
        emulator.openLevelset(new File("testData/sets/unitTest.dat"));
        boolean[] solved = solveLevelset("testData/json/unitTest/unitTest");

        boolean[] expectedSolved = new boolean[emulator.lastLevelNumber() - 1];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }
}