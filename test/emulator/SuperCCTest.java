package emulator;

import game.Direction;
import game.Level;
import game.Tile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;

import static emulator.SuperCC.WAIT;
import static org.junit.jupiter.api.Assertions.*;

class SuperCCTest {
    private SuperCC emulator = new SuperCC(false);

    boolean[] solveLevelset() {
        boolean[] solved = new boolean[149];
        Arrays.fill(solved, Boolean.TRUE);

        for (int i = 1; i <= 149; i++) {
            emulator.loadLevel(i);
            Level level = emulator.getLevel();
            try {
                Solution s = emulator.twsReader.readSolution(level);
                s.load(emulator);
                level = emulator.getLevel();
                for (int waits = 0; waits < 100 & !level.getChip().isDead(); waits++) {
                    level.tick(WAIT, new Direction[] {});
                }
                if (level.getLayerFG().get(level.getChip().getPosition()) != Tile.EXITED_CHIP && !level.isCompleted()) {
                    solved[i - 1] = false;
                    System.out.println("failed level "+level.getLevelNumber()+" "+new String(level.getTitle()));
                }
            }
            catch (Exception exc) {
                solved[i - 1] = false;
                System.out.println("Error loading "+level.getLevelNumber()+" "+new String(level.getTitle()));
                exc.printStackTrace();
            }
        }

        return solved;
    }

    @Test
    void solveCHIPS() {
        emulator.openLevelset(new File("testData/sets/CHIPS.DAT"));
        emulator.setTWSFile(new File("testData/tws/public_CHIPS.dac.tws"));
        boolean[] solved = solveLevelset();

        boolean[] expectedSolved = new boolean[149];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }

    @Test
    void solveCCLP1() {
        emulator.openLevelset(new File("testData/sets/CCLP1.DAT"));
        emulator.setTWSFile(new File("testData/tws/public_CCLP1.dac.tws"));
        boolean[] solved = solveLevelset();

        boolean[] expectedSolved = new boolean[149];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }

    @Test
    void solveCCLP2() {
        emulator.openLevelset(new File("testData/sets/CCLP2.DAT"));
        emulator.setTWSFile(new File("testData/tws/public_CCLP2.dac.tws"));
        boolean[] solved = solveLevelset();

        boolean[] expectedSolved = new boolean[149];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }

    @Test
    void solveCCLP3() {
        emulator.openLevelset(new File("testData/sets/CCLP3.DAT"));
        emulator.setTWSFile(new File("testData/tws/public_CCLP3.dac.tws"));
        boolean[] solved = solveLevelset();

        boolean[] expectedSolved = new boolean[149];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }

    @Test
    void solveCCLP4() {
        emulator.openLevelset(new File("testData/sets/CCLP4.DAT"));
        emulator.setTWSFile(new File("testData/tws/public_CCLP4.dac.tws"));
        boolean[] solved = solveLevelset();

        boolean[] expectedSolved = new boolean[149];
        Arrays.fill(expectedSolved, Boolean.TRUE);

        assertArrayEquals(expectedSolved, solved);
    }
}