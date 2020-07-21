package tools.tsp;

import emulator.SuperCC;
import game.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.TSPGUI;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TSPSolverTest {
    private SuperCC emulator = new SuperCC(false);
    private TSPGUI tspgui = new TSPGUI(emulator, false);
    private JTextPane output = new JTextPane();
    private SimulatedAnnealingParameters simulatedAnnealingParameters = new SimulatedAnnealingParameters(100, 0.1, 0.9, 100);
    private ActingWallParameters actingWallParameters = new ActingWallParameters(false, false, false, false, false);

    @BeforeEach
    void setup() {
        emulator.openLevelset(new File("testData/sets/TSPSolverTest.dat"));
    }

    @Test
    void simpleDistances() {
        emulator.loadLevel(1);

        ArrayList<TSPGUI.ListNode> inputNodes = tspgui.getAllChips();
        ArrayList<TSPGUI.ListNode> exitNodes = tspgui.getAllExits();
        ArrayList<TSPGUI.RestrictionNode> restrictionNodes = new ArrayList<>();

        TSPSolver solver = new TSPSolver(emulator, tspgui, inputNodes, exitNodes, restrictionNodes,
                simulatedAnnealingParameters, actingWallParameters, output);
        solver.gatherNormal();
        int[][] distances = solver.getDistances();

        int[][] expectedDistances = {
                { 0, 8, 6, 2, 4 },
                { 8, 0, 14, 10, 12 },
                { 6, 14, 0, 8, 10 },
                { 2, 10, 8, 0, 6 },
                { 9999, 9999, 9999, 9999, 9999 },
        };

        assertArrayEquals(expectedDistances, distances);
    }

    @Test
    void handlesIce() {
        emulator.loadLevel(2);

        ArrayList<TSPGUI.ListNode> inputNodes = tspgui.getAllChips();
        ArrayList<TSPGUI.ListNode> exitNodes = tspgui.getAllExits();
        ArrayList<TSPGUI.RestrictionNode> restrictionNodes = new ArrayList<>();

        TSPSolver solver = new TSPSolver(emulator, tspgui, inputNodes, exitNodes, restrictionNodes,
                simulatedAnnealingParameters, actingWallParameters, output);
        solver.gatherNormal();
        int[][] distances = solver.getDistances();

        int[][] expectedDistances = {
                { 0, 26, 4 },
                { 26, 0, 30 },
                { 9999, 9999, 9999 }
        };

        assertArrayEquals(expectedDistances, distances);
    }

    @Test
    void handlesForceFloor() {
        emulator.loadLevel(3);

        ArrayList<TSPGUI.ListNode> inputNodes = tspgui.getAllChips();
        ArrayList<TSPGUI.ListNode> exitNodes = tspgui.getAllExits();
        ArrayList<TSPGUI.RestrictionNode> restrictionNodes = new ArrayList<>();

        TSPSolver solver = new TSPSolver(emulator, tspgui, inputNodes, exitNodes, restrictionNodes,
                simulatedAnnealingParameters, actingWallParameters, output);
        solver.gatherNormal();
        int[][] distances = solver.getDistances();

        int[][] expectedDistances = {
                { 0, 10, 6, 16, 22 },
                { 9999, 0, 9999, 6, 12 },
                { 9999, 4, 0, 10, 16 },
                { 9999, 9999, 9999, 0, 6 },
                { 9999, 9999, 9999, 9999, 9999 }
        };

        assertArrayEquals(expectedDistances, distances);
    }

    @Test
    void handlesTeleports() {
        emulator.loadLevel(4);

        ArrayList<TSPGUI.ListNode> inputNodes = tspgui.getAllChips();
        ArrayList<TSPGUI.ListNode> exitNodes = tspgui.getAllExits();
        ArrayList<TSPGUI.RestrictionNode> restrictionNodes = new ArrayList<>();

        TSPSolver solver = new TSPSolver(emulator, tspgui, inputNodes, exitNodes, restrictionNodes,
                simulatedAnnealingParameters, actingWallParameters, output);
        solver.gatherNormal();
        int[][] distances = solver.getDistances();

        int[][] expectedDistances = {
                { 0, 14, 12, 14 },
                { 12, 0, 12, 14 },
                { 14, 12, 0, 12 },
                { 9999, 9999, 9999, 9999 }
        };

        assertArrayEquals(expectedDistances, distances);
    }

    @Test
    void handlesBoostingBetweenNodes() {
        emulator.loadLevel(5);

        ArrayList<TSPGUI.ListNode> inputNodes = tspgui.getAllChips();
        ArrayList<TSPGUI.ListNode> exitNodes = tspgui.getAllExits();
        ArrayList<TSPGUI.RestrictionNode> restrictionNodes = new ArrayList<>();

        TSPSolver solver = new TSPSolver(emulator, tspgui, inputNodes, exitNodes, restrictionNodes,
                simulatedAnnealingParameters, actingWallParameters, output);
        solver.gatherNormal();
        int[][] distances = solver.getDistances();

        int[][] expectedDistances = {
                { 0, 18, 4 },
                { 18, 0, 22 },
                { 9999, 9999, 9999 }
        };

        assertArrayEquals(expectedDistances, distances);
    }

    @Test
    void handlesCombined() {
        emulator.loadLevel(6);

        ArrayList<TSPGUI.ListNode> inputNodes = tspgui.getAllChips();
        ArrayList<TSPGUI.ListNode> exitNodes = tspgui.getAllExits();
        ArrayList<TSPGUI.RestrictionNode> restrictionNodes = new ArrayList<>();

        TSPSolver solver = new TSPSolver(emulator, tspgui, inputNodes, exitNodes, restrictionNodes,
                simulatedAnnealingParameters, actingWallParameters, output);
        solver.gatherNormal();
        int[][] distances = solver.getDistances();

        int[][] expectedDistances = {
                { 0, 5, 6, 14 },
                { 8, 0, 11, 20 },
                { 10, 7, 0, 10 },
                { 9999, 9999, 9999, 9999 }
        };

        assertArrayEquals(expectedDistances, distances);
    }

    @Test
    void handlesCombinedDistancesBoost() {
        emulator.loadLevel(6);

        ArrayList<TSPGUI.ListNode> inputNodes = tspgui.getAllChips();
        ArrayList<TSPGUI.ListNode> exitNodes = tspgui.getAllExits();
        ArrayList<TSPGUI.RestrictionNode> restrictionNodes = new ArrayList<>();

        TSPSolver solver = new TSPSolver(emulator, tspgui, inputNodes, exitNodes, restrictionNodes,
                simulatedAnnealingParameters, actingWallParameters, output);
        solver.gatherBoost();
        int[][] distancesBoost = solver.getDistancesBoost();

        int[][] expectedDistancesBoost = {
                { 0, 4, 6, 15 },
                { 7, 0, 10, 19 },
                { 9, 6, 0, 9 },
                { 9999, 9999, 9999, 9999 }
        };

        assertArrayEquals(expectedDistancesBoost, distancesBoost);
    }

    @Test
    void handlesMultipleExits() {
        emulator.loadLevel(7);

        ArrayList<TSPGUI.ListNode> inputNodes = tspgui.getAllChips();
        ArrayList<TSPGUI.ListNode> exitNodes = tspgui.getAllExits();
        ArrayList<TSPGUI.RestrictionNode> restrictionNodes = new ArrayList<>();

        TSPSolver solver = new TSPSolver(emulator, tspgui, inputNodes, exitNodes, restrictionNodes,
                simulatedAnnealingParameters, actingWallParameters, output);
        solver.gatherNormal();
        int[][] distances = solver.getDistances();

        int[][] expectedDistances = {
                { 0, 4, 4, 4, 4, 8, 8, 8, 8 },
                { 4, 0, 4, 4, 8, 4, 8, 8, 12 },
                { 4, 4, 0, 8, 4, 8, 4, 12, 8 },
                { 4, 4, 8, 0, 4, 8, 12, 4, 8 },
                { 4, 8, 4, 4, 0, 12, 8, 8, 4 },
                { 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999 },
                { 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999 },
                { 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999 },
                { 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999 }
        };

        assertArrayEquals(expectedDistances, distances);
    }

    @Test
    void coldFusionReactorDistances() {
        emulator.loadLevel(8);

        ArrayList<TSPGUI.ListNode> inputNodes = tspgui.getAllChips();
        ArrayList<TSPGUI.ListNode> exitNodes = tspgui.getAllExits();
        ArrayList<TSPGUI.RestrictionNode> restrictionNodes = new ArrayList<>();

        TSPSolver solver = new TSPSolver(emulator, tspgui, inputNodes, exitNodes, restrictionNodes,
                simulatedAnnealingParameters, actingWallParameters, output);
        solver.gatherNormal();
        int[][] distances = solver.getDistances();

        int[][] expectedDistances = {
                { 0, 12, 14, 12, 12, 10, 12, 12, 8, 8, 6, 8, 4, 8, 7, 7, 7, 6, 8, 4, 10, 6, 8, 8, 14, 12, 12, 12, 14, 14, 12, 16, 18 },
                { 14, 0, 6, 7, 6, 6, 4, 12, 8, 10, 7, 12, 12, 14, 14, 14, 18, 16, 16, 16, 15, 16, 16, 13, 12, 12, 12, 12, 9, 10, 16, 12, 12 },
                { 15, 10, 0, 6, 8, 10, 6, 10, 10, 6, 12, 8, 12, 12, 10, 12, 18, 14, 18, 15, 14, 18, 16, 15, 14, 12, 14, 12, 10, 12, 16, 6, 12 },
                { 12, 6, 6, 0, 8, 6, 6, 8, 6, 9, 6, 10, 10, 12, 12, 14, 18, 14, 16, 16, 15, 14, 18, 13, 12, 14, 12, 12, 9, 12, 16, 10, 12 },
                { 11, 8, 8, 2, 0, 8, 6, 10, 7, 10, 8, 10, 7, 10, 10, 12, 14, 14, 14, 14, 13, 14, 14, 11, 10, 10, 10, 12, 10, 10, 14, 12, 10 },
                { 10, 8, 10, 6, 8, 0, 8, 10, 5, 8, 5, 10, 9, 10, 12, 12, 16, 14, 14, 13, 15, 16, 16, 13, 12, 12, 12, 14, 12, 12, 16, 10, 12 },
                { 13, 12, 6, 6, 6, 6, 0, 8, 8, 6, 7, 8, 9, 12, 11, 10, 16, 12, 12, 15, 16, 14, 18, 15, 14, 14, 14, 14, 12, 14, 18, 10, 14 },
                { 10, 12, 10, 8, 6, 8, 8, 0, 5, 8, 11, 6, 9, 10, 9, 12, 16, 10, 16, 13, 14, 16, 17, 17, 16, 16, 16, 18, 16, 16, 20, 14, 16 },
                { 6, 10, 8, 8, 8, 6, 6, 8, 0, 6, 8, 8, 4, 6, 8, 10, 12, 12, 14, 10, 16, 12, 14, 12, 16, 16, 16, 18, 15, 16, 18, 12, 16 },
                { 11, 10, 10, 8, 6, 10, 8, 8, 10, 0, 8, 4, 9, 8, 7, 6, 12, 10, 16, 11, 14, 14, 16, 17, 16, 14, 16, 16, 14, 14, 18, 12, 14 },
                { 13, 12, 12, 11, 10, 6, 10, 10, 9, 8, 0, 10, 8, 12, 9, 8, 12, 12, 10, 15, 16, 12, 16, 17, 16, 18, 16, 20, 17, 18, 20, 16, 18 },
                { 7, 12, 14, 8, 8, 10, 8, 6, 7, 9, 8, 0, 6, 4, 4, 9, 10, 8, 12, 7, 12, 10, 12, 14, 18, 14, 16, 15, 15, 16, 16, 16, 18 },
                { 5, 10, 10, 10, 8, 10, 8, 8, 8, 4, 6, 4, 0, 8, 8, 8, 10, 8, 10, 8, 12, 8, 14, 12, 16, 14, 14, 16, 14, 14, 14, 12, 14 },
                { 6, 10, 10, 10, 10, 10, 8, 10, 3, 8, 6, 8, 5, 0, 6, 7, 8, 8, 10, 7, 12, 12, 12, 14, 16, 16, 14, 15, 15, 16, 16, 12, 18 },
                { 6, 14, 14, 12, 10, 12, 10, 8, 7, 10, 8, 4, 6, 4, 0, 9, 8, 4, 10, 5, 8, 12, 10, 14, 16, 14, 14, 13, 15, 16, 16, 16, 18 },
                { 6, 14, 12, 11, 10, 12, 10, 10, 7, 8, 8, 4, 6, 4, 1, 0, 10, 4, 10, 7, 8, 12, 12, 14, 18, 14, 16, 15, 17, 16, 16, 16, 20 },
                { 8, 16, 14, 13, 12, 10, 12, 10, 9, 10, 8, 6, 8, 6, 3, 2, 0, 6, 10, 8, 6, 8, 10, 12, 12, 12, 16, 14, 16, 16, 14, 18, 20 },
                { 6, 14, 14, 12, 12, 14, 12, 10, 8, 12, 10, 6, 8, 6, 3, 9, 7, 0, 11, 4, 6, 10, 8, 12, 14, 12, 12, 12, 13, 16, 16, 16, 16 },
                { 11, 16, 16, 16, 14, 16, 14, 14, 12, 12, 12, 12, 12, 9, 9, 8, 8, 6, 0, 10, 9, 6, 10, 10, 8, 10, 8, 14, 10, 12, 12, 20, 14 },
                { 6, 14, 14, 15, 14, 12, 14, 14, 9, 10, 10, 10, 7, 8, 6, 5, 3, 7, 10, 0, 10, 6, 10, 10, 12, 14, 10, 9, 14, 16, 14, 16, 16 },
                { 6, 16, 16, 14, 16, 16, 16, 16, 11, 12, 12, 12, 9, 10, 9, 9, 7, 6, 9, 4, 0, 10, 4, 8, 12, 6, 10, 10, 10, 10, 14, 18, 14 },
                { 11, 18, 18, 16, 16, 14, 16, 14, 10, 14, 12, 10, 12, 7, 8, 12, 10, 5, 7, 9, 7, 0, 8, 8, 10, 10, 8, 12, 8, 12, 10, 20, 12 },
                { 10, 18, 18, 18, 16, 18, 18, 18, 13, 16, 14, 12, 13, 12, 11, 10, 9, 9, 8, 6, 8, 10, 0, 8, 10, 8, 10, 6, 6, 10, 10, 20, 10 },
                { 13, 22, 18, 18, 18, 18, 16, 16, 16, 14, 14, 14, 16, 14, 11, 14, 12, 8, 12, 10, 8, 10, 10, 0, 10, 11, 8, 6, 8, 12, 12, 22, 12 },
                { 12, 22, 20, 20, 18, 20, 18, 18, 15, 16, 14, 12, 15, 14, 11, 13, 11, 8, 13, 8, 8, 14, 8, 6, 0, 6, 10, 8, 8, 10, 12, 22, 8 },
                { 12, 22, 20, 20, 18, 20, 20, 20, 15, 18, 16, 16, 14, 14, 13, 13, 11, 10, 10, 8, 10, 10, 6, 8, 10, 0, 10, 8, 9, 4, 12, 22, 12 },
                { 14, 22, 22, 20, 18, 20, 18, 20, 16, 16, 16, 16, 16, 13, 13, 12, 12, 10, 8, 10, 9, 8, 11, 6, 6, 8, 0, 10, 6, 12, 8, 24, 10 },
                { 8, 20, 22, 18, 18, 18, 20, 20, 14, 16, 14, 14, 12, 15, 13, 13, 11, 10, 10, 8, 10, 8, 8, 6, 6, 8, 6, 0, 6, 10, 8, 24, 10 },
                { 11, 22, 22, 20, 20, 22, 22, 20, 17, 20, 16, 16, 16, 16, 13, 15, 13, 10, 12, 10, 8, 12, 10, 9, 8, 6, 10, 4, 0, 6, 8, 24, 4 },
                { 14, 24, 24, 22, 22, 22, 24, 22, 19, 22, 18, 16, 18, 16, 15, 14, 15, 14, 13, 12, 11, 14, 8, 8, 12, 6, 12, 8, 9, 0, 12, 26, 12 },
                { 14, 24, 24, 22, 22, 20, 22, 20, 19, 20, 18, 16, 17, 16, 14, 15, 13, 11, 14, 10, 10, 10, 9, 6, 6, 10, 8, 8, 8, 14, 0, 26, 10 },
                { 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999 },
                { 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999}
        };

        assertArrayEquals(expectedDistances, distances);
    }

    @Test
    void coldFusionReactorDistancesBoost() {
        emulator.loadLevel(8);

        ArrayList<TSPGUI.ListNode> inputNodes = tspgui.getAllChips();
        ArrayList<TSPGUI.ListNode> exitNodes = tspgui.getAllExits();
        ArrayList<TSPGUI.RestrictionNode> restrictionNodes = new ArrayList<>();

        TSPSolver solver = new TSPSolver(emulator, tspgui, inputNodes, exitNodes, restrictionNodes,
                simulatedAnnealingParameters, actingWallParameters, output);
        solver.gatherBoost();
        int[][] distancesBoost = solver.getDistancesBoost();

        int[][] expectedDistancesBoost = {
                { 0, 13, 13, 13, 11, 11, 13, 13, 8, 9, 7, 7, 5, 7, 8, 8, 6, 7, 9, 3, 9, 7, 9, 9, 15, 13, 13, 12, 13, 15, 11, 15, 17 },
                { 13, 0, 7, 7, 7, 5, 5, 13, 8, 11, 8, 13, 12, 13, 13, 15, 18, 16, 17, 15, 14, 17, 15, 14, 13, 11, 13, 11, 8, 9, 15, 13, 11 },
                { 14, 9, 0, 5, 7, 9, 5, 9, 10, 6, 11, 7, 13, 11, 11, 13, 17, 15, 17, 14, 13, 17, 15, 14, 13, 11, 13, 11, 9, 11, 15, 5, 11 },
                { 11, 5, 5, 0, 7, 7, 5, 9, 5, 9, 7, 11, 9, 11, 13, 13, 17, 15, 17, 15, 14, 15, 17, 14, 13, 13, 13, 11, 8, 11, 15, 11, 11 },
                { 10, 7, 7, 2, 0, 7, 5, 9, 6, 9, 8, 9, 6, 9, 9, 11, 13, 13, 13, 13, 14, 13, 15, 10, 9, 11, 9, 13, 10, 11, 15, 13, 11 },
                { 11, 7, 9, 6, 7, 0, 7, 11, 6, 8, 4, 9, 10, 11, 11, 11, 15, 14, 13, 14, 16, 15, 17, 12, 11, 13, 11, 15, 12, 13, 17, 9, 13 },
                { 12, 11, 5, 5, 5, 7, 0, 7, 7, 6, 6, 7, 8, 11, 10, 9, 15, 11, 13, 14, 15, 15, 17, 14, 13, 13, 13, 13, 11, 13, 17, 9, 13 },
                { 11, 11, 11, 8, 7, 7, 7, 0, 5, 7, 10, 5, 9, 9, 8, 13, 15, 9, 15, 12, 13, 15, 16, 16, 15, 15, 15, 17, 15, 15, 19, 15, 15 },
                { 5, 9, 7, 7, 7, 7, 5, 7, 0, 5, 8, 7, 4, 5, 9, 9, 12, 11, 13, 9, 15, 11, 13, 13, 17, 15, 17, 17, 14, 15, 17, 11, 15 },
                { 10, 9, 9, 9, 7, 9, 7, 7, 10, 0, 9, 3, 9, 7, 7, 7, 11, 11, 15, 10, 15, 13, 15, 17, 17, 13, 17, 15, 13, 13, 17, 11, 13 },
                { 12, 11, 11, 10, 9, 5, 9, 9, 8, 7, 0, 9, 7, 11, 8, 7, 11, 11, 9, 14, 15, 11, 15, 16, 15, 17, 15, 19, 16, 17, 19, 15, 17 },
                { 7, 13, 13, 9, 7, 9, 7, 5, 7, 8, 9, 0, 5, 5, 3, 10, 9, 7, 13, 6, 11, 11, 11, 13, 17, 15, 15, 14, 16, 17, 17, 15, 17 },
                { 4, 11, 9, 9, 9, 9, 7, 7, 7, 3, 5, 5, 0, 7, 7, 7, 9, 7, 9, 7, 11, 7, 13, 11, 15, 15, 13, 16, 15, 15, 13, 11, 15 },
                { 5, 11, 11, 11, 11, 9, 9, 11, 3, 9, 7, 9, 5, 0, 7, 8, 9, 9, 11, 6, 13, 11, 11, 13, 15, 15, 13, 14, 16, 17, 17, 13, 19 },
                { 5, 13, 13, 11, 11, 11, 11, 9, 6, 9, 7, 3, 5, 3, 0, 8, 9, 3, 9, 6, 9, 11, 11, 13, 17, 13, 15, 14, 16, 15, 15, 15, 19 },
                { 7, 15, 11, 10, 9, 13, 9, 9, 8, 7, 9, 5, 7, 5, 1, 0, 9, 5, 11, 6, 7, 11, 11, 15, 17, 13, 15, 14, 16, 17, 17, 15, 19 },
                { 9, 15, 13, 12, 11, 9, 11, 11, 10, 9, 7, 7, 9, 7, 3, 2, 0, 7, 11, 8, 7, 7, 11, 13, 13, 13, 15, 15, 15, 17, 15, 17, 19 },
                { 7, 15, 13, 11, 11, 13, 11, 9, 8, 11, 11, 5, 7, 7, 3, 9, 7, 0, 10, 4, 5, 11, 8, 11, 15, 11, 13, 11, 12, 15, 15, 15, 15 },
                { 10, 15, 15, 15, 13, 15, 13, 13, 11, 11, 11, 11, 11, 8, 8, 7, 9, 5, 0, 9, 9, 5, 9, 11, 9, 11, 7, 13, 11, 13, 13, 19, 15 },
                { 7, 13, 15, 15, 15, 13, 13, 13, 9, 11, 11, 9, 7, 9, 6, 5, 3, 6, 9, 0, 9, 7, 9, 9, 11, 13, 9, 8, 13, 15, 13, 17, 15 },
                { 5, 15, 15, 13, 15, 15, 15, 15, 10, 11, 11, 11, 8, 9, 8, 9, 7, 5, 8, 5, 0, 11, 3, 7, 11, 5, 11, 9, 9, 9, 13, 17, 13 },
                { 10, 17, 17, 15, 15, 13, 15, 13, 9, 13, 11, 9, 11, 6, 7, 11, 9, 4, 6, 8, 6, 0, 7, 7, 9, 9, 7, 11, 7, 11, 11, 19, 11 },
                { 9, 17, 17, 17, 15, 17, 17, 17, 12, 15, 13, 11, 12, 11, 10, 9, 8, 8, 7, 5, 7, 9, 0, 7, 9, 7, 9, 6, 7, 9, 9, 19, 9 },
                { 13, 21, 17, 17, 17, 17, 15, 15, 15, 13, 13, 13, 15, 13, 10, 13, 13, 7, 11, 11, 7, 11, 9, 0, 9, 11, 7, 7, 9, 11, 11, 21, 13 },
                { 13, 21, 19, 21, 19, 19, 19, 19, 14, 17, 15, 13, 14, 13, 12, 12, 10, 9, 13, 7, 9, 13, 8, 5, 0, 5, 9, 7, 7, 9, 11, 21, 7 },
                { 11, 21, 19, 19, 17, 19, 19, 19, 14, 17, 15, 15, 13, 13, 12, 12, 10, 9, 9, 7, 9, 9, 5, 9, 9, 0, 9, 7, 8, 3, 13, 21, 11 },
                { 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999 },
                { 8, 21, 21, 19, 17, 19, 19, 19, 14, 17, 13, 13, 13, 15, 14, 13, 12, 11, 10, 9, 9, 9, 7, 7, 5, 8, 5, 0, 5, 11, 9, 23, 9 },
                { 10, 23, 23, 19, 19, 21, 21, 19, 16, 19, 15, 15, 15, 15, 14, 15, 13, 11, 13, 11, 8, 11, 10, 8, 7, 7, 9, 3, 0, 7, 7, 25, 5 },
                { 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999 },
                { 13, 23, 23, 21, 21, 21, 21, 19, 18, 19, 17, 17, 16, 15, 14, 14, 12, 11, 13, 9, 9, 11, 8, 5, 7, 11, 7, 7, 9, 15, 0, 25, 9 },
                { 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999 },
                { 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999 }
        };

        assertArrayEquals(expectedDistancesBoost, distancesBoost);
    }

    @Test
    void solveWithSimulatedAnnealing() {
        emulator.loadLevel(1);

        ArrayList<TSPGUI.ListNode> inputNodes = tspgui.getAllChips();
        ArrayList<TSPGUI.ListNode> exitNodes = tspgui.getAllExits();
        ArrayList<TSPGUI.RestrictionNode> restrictionNodes = new ArrayList<>();

        TSPSolver solver = new TSPSolver(emulator, tspgui, inputNodes, exitNodes, restrictionNodes,
                simulatedAnnealingParameters, actingWallParameters, output);
        try {
            solver.solve();
        } catch (Exception e) {
            fail();
        }

        assertTrue(emulator.getLevel().isCompleted());
    }

    @Test
    void handleActingWalls() {
        emulator.loadLevel(9);

        ArrayList<TSPGUI.ListNode> inputNodes = tspgui.getAllChips();
        ArrayList<TSPGUI.ListNode> exitNodes = tspgui.getAllExits();
        ArrayList<TSPGUI.RestrictionNode> restrictionNodes = new ArrayList<>();

        TSPSolver solver = new TSPSolver(emulator, tspgui, inputNodes, exitNodes, restrictionNodes,
                simulatedAnnealingParameters, new ActingWallParameters(true, true, true, true, true), output);
        solver.gatherNormal();
        try {
            solver.solve();
        } catch (Exception e) {
            fail();
        }
        int[][] distances = solver.getDistances();

        int[][] expectedDistances = {
                {0, 68},
                {9999, 9999}
        };

        assertArrayEquals(expectedDistances, distances);
        assertTrue(emulator.getLevel().isCompleted());
    }

    @Test
    void solveStartingOnChip() {
        emulator.loadLevel(10);

        ArrayList<TSPGUI.ListNode> inputNodes = tspgui.getAllChips();
        ArrayList<TSPGUI.ListNode> exitNodes = tspgui.getAllExits();
        ArrayList<TSPGUI.RestrictionNode> restrictionNodes = new ArrayList<>();

        TSPSolver solver = new TSPSolver(emulator, tspgui, inputNodes, exitNodes, restrictionNodes,
                simulatedAnnealingParameters, actingWallParameters, output);
        try {
            solver.solve();
        } catch (Exception e) {
            fail();
        }
        int[][] distances = solver.getDistances();

        assertEquals(4, distances[0][1]);
        assertTrue(emulator.getLevel().isCompleted());
    }

    @Test
    void solveStartingOnExit() {
        emulator.loadLevel(11);

        ArrayList<TSPGUI.ListNode> inputNodes = tspgui.getAllChips();
        ArrayList<TSPGUI.ListNode> exitNodes = tspgui.getAllExits();
        ArrayList<TSPGUI.RestrictionNode> restrictionNodes = new ArrayList<>();

        TSPSolver solver = new TSPSolver(emulator, tspgui, inputNodes, exitNodes, restrictionNodes,
                simulatedAnnealingParameters, actingWallParameters, output);
        try {
            solver.solve();
        } catch (Exception e) {
            fail();
        }
        int[][] distances = solver.getDistances();

        assertEquals(12, distances[1][2]);
        assertTrue(emulator.getLevel().isCompleted());
    }
}