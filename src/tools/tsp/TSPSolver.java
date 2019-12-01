package tools.tsp;

import emulator.Solution;
import emulator.SuperCC;
import emulator.TickFlags;
import game.Direction;
import game.Level;
import game.Position;
import game.Tile;
import tools.TSPGUI;
import util.ByteList;

import javax.swing.*;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class TSPSolver {
    private SuperCC emulator;
    private TSPGUI gui;
    private Level level;
    private byte[] startState;
    private byte[] directions;
    private byte[] initialState;
    private ArrayList<Integer> nodes = new ArrayList<>();
    private boolean[] boostNodes;
    private int[][] distances;
    private PathNode[][] paths;
    private int startTime;

    private int toBeVisited = 0;
    private boolean shouldBeVisited = false;
    private int visitedTime = 0;

    private int inputNodeSize;
    private int exitNodeSize;

    private ArrayList<TSPGUI.RestrictionNode> restrictionNodes;

    private double startTemp;
    private double endTemp;
    private double cooling;
    private int iterations;

    private int chosenExit;

    private JTextPane output;

    private final int LIMIT = 200000; // Upper bound of exploration

    public TSPSolver(SuperCC emulator, TSPGUI gui, ArrayList<TSPGUI.ListNode> inputNodes, ArrayList<TSPGUI.ListNode> exitNodes,
                     ArrayList<TSPGUI.RestrictionNode> restrictionNodes,
                     double startTemp, double endTemp, double cooling, int iterations, JTextPane output) {
        this.emulator = emulator;
        this.gui = gui;
        this.level = emulator.getLevel();
        this.startState = level.save();
        emulator.tick(SuperCC.WAIT, TickFlags.LIGHT); // Full wait
        emulator.tick(SuperCC.WAIT, TickFlags.LIGHT);
        this.directions = new byte[]{ SuperCC.UP, SuperCC.RIGHT, SuperCC.DOWN, SuperCC.LEFT };
        this.initialState = normalizeState();

        for(int i = 0; i < inputNodes.size(); i++) {
            this.nodes.add(inputNodes.get(i).index);
        }
        for(int i = 0; i < exitNodes.size(); i++) {
            this.nodes.add(exitNodes.get(i).index);
        }

        this.boostNodes = new boolean[nodes.size()];
        this.distances = new int[nodes.size()][nodes.size()];
        for(int i = 0 ; i < nodes.size(); i++) {
            for(int j = 0; j < nodes.size(); j++) {
                distances[i][j] = Integer.MAX_VALUE;
            }
        }
        this.paths = new PathNode[nodes.size()][nodes.size()];
        this.startTime = level.getTimer();

        this.inputNodeSize = inputNodes.size();
        this.exitNodeSize = exitNodes.size();

        this.restrictionNodes = restrictionNodes;

        for(TSPGUI.RestrictionNode node : restrictionNodes) {
            node.beforeIndex = nodes.indexOf(node.before.index) - 1;
            node.afterIndex = nodes.indexOf(node.after.index) - 1;
        }

        this.startTemp = startTemp;
        this.endTemp = endTemp;
        this.cooling = cooling;
        this.iterations = iterations;

        this.output = output;
    }

    public void solve() {
        for(int i = 0; i < inputNodeSize + 1; i++) {
            output.setText("Finding distances... " + (i + 1) + "/" + (inputNodeSize + 1));
            level.load(initialState);
            level.cheats.moveChip(new Position(nodes.get(i)));

            PriorityQueue<PathNode> states = new PriorityQueue<>(100, (a,b) -> b.time - a.time);
            states.add(new PathNode(level.save(), new ByteList(), startTime));

            int[] visited = new int[32 * 32];
            int[] visitedCount = new int[32 * 32];
            int statesExplored = 0;

            while (!states.isEmpty() && statesExplored < LIMIT && !gui.killflag) {
                statesExplored++;
                PathNode node = states.poll();
                byte[] state = node.state;
                level.load(state);

                int index = level.getChip().getPosition().getIndex();
                Tile t = level.getLayerBG().get(index);

                if(visited[index] < level.getTimer()) {
                    visited[index] = level.getTimer();
                    visitedCount[index] = 0;
                }

                // Some strange limits because CC is too complex
                if(visitedCount[index] >= 2 && !t.isIce() && !t.isFF() && t != Tile.TELEPORT) {
                    continue;
                }
                else if(visitedCount[index] >= 10 && t.isFF()) {
                    continue;
                }
                else if(visitedCount[index] >= 100) {
                    continue;
                }

                visitedCount[index]++;

                if (nodes.contains(index)) {
                    if(distances[i][nodes.indexOf(index)] > startTime - level.getTimer()) {
                        distances[i][nodes.indexOf(index)] = startTime - level.getTimer();
                        paths[i][nodes.indexOf(index)] = node;
                    }
                }

                for (int d = 0; d < directions.length; d++) {
                    if (d > 0) {
                        level.load(state);
                    }
                    boolean can = true;
                    if(level.getChip().isSliding()) {
                        can = checkSliding(directions[d], index, visited, visitedCount, i, t, node);
                    }
                    if(can) {
                        emulator.tick(directions[d], TickFlags.LIGHT);
                        ByteList newMoves = node.moves.clone();
                        newMoves.add(directions[d]);
                        states.add(new PathNode(level.save(), newMoves, level.getTimer()));
                    }
                }

                if(shouldBeVisited) {
                    visitedCount[toBeVisited]++;
                    shouldBeVisited = false;
                    if(visited[toBeVisited] < level.getTimer()) {
                        visited[toBeVisited] = visitedTime;
                        visitedCount[toBeVisited] = 0;
                    }
                }
            }
        }

        SimulatedAnnealing sa = new SimulatedAnnealing(gui, startTemp, endTemp, cooling, iterations, distances,
                inputNodeSize, exitNodeSize, restrictionNodes, output);
        int[] solution = sa.start();
        chosenExit = sa.bestExit;

        complete(solution);
        output.setText("Finished!");
    }

    private byte[] normalizeState() {
        int start = 0;
        for(int i = 0; i < 32 * 32; i++) {
            Tile t = level.getLayerFG().get(i);
            if(t.isChip()) {
                start = i;
            }
            if(!isTSPTile(t)) {
                level.getLayerFG().set(i, Tile.FLOOR);
            }
            level.getLayerBG().set(i, Tile.FLOOR);
        }

        nodes.add(start);

        return level.save();
    }

    private boolean isTSPTile(Tile t) {
        if(t == Tile.FLOOR || t == Tile.WALL || t == Tile.CHIP || t == Tile.EXIT || t.isChip() ||
           t.isIce() || t.isFF() || t == Tile.TELEPORT || t == Tile.BLUEWALL_REAL ||
           t == Tile.HIDDENWALL_TEMP || t == Tile.INVISIBLE_WALL || t == Tile.CLONE_MACHINE ||
           t == Tile.THIN_WALL_LEFT || t == Tile.THIN_WALL_RIGHT || t == Tile.THIN_WALL_DOWN_RIGHT ||
           t == Tile.THIN_WALL_DOWN || t == Tile.THIN_WALL_UP) {
            return true;
        }
        return false;
    }

    private boolean isNodeTile(Tile t) {
        if (t.isChip() || t == Tile.CHIP || t == Tile.EXIT ) {
            return true;
        }
        return false;
    }

    private boolean checkSliding(byte d, int position, int[] visited, int[] visitedCount, int i, Tile on, PathNode node) {
        int delta = 0;
        Direction dir = level.getChip().getDirection();

        if(dir == Direction.UP) delta = -32;
        else if(dir == Direction.RIGHT) delta = 1;
        else if(dir == Direction.DOWN) delta = 32;
        else if(dir == Direction.LEFT) delta = -1;

        int newPosition = position + delta;

        Tile t = level.getLayerFG().get(newPosition);
        boolean ret = true;
        if(!t.isIce() && !t.isFF() && t != Tile.TELEPORT && canEnter(d, newPosition)) {
            if(visitedCount[newPosition] >= 2) {
                return false;
            }
            if (nodes.contains(newPosition)) {
                int deltaTime = (level.getTimer() % 2 == 0) ? 0 : -1;
                if(distances[i][nodes.indexOf(newPosition)] > startTime - level.getTimer() + deltaTime) {
                    distances[i][nodes.indexOf(newPosition)] = startTime - level.getTimer() + deltaTime;
                    paths[i][nodes.indexOf(newPosition)] = node;
                    if(deltaTime == -1) {
                        boostNodes[nodes.indexOf(newPosition)] = true;
                    }
                }
            }
            toBeVisited = newPosition;
            shouldBeVisited = true;
            visitedTime = level.getTimer();
        }
        else if(!directionEquals(level.getChip().getDirection(), d) && (on.isIce() || on == Tile.TELEPORT)) {
            ret = false;
        }
        return ret;
    }

    private boolean directionEquals(Direction dir, byte d) {
        if(dir == Direction.UP && d == 'u') return true;
        if(dir == Direction.RIGHT && d == 'r') return true;
        if(dir == Direction.DOWN && d == 'd') return true;
        if(dir == Direction.LEFT && d == 'l') return true;
        return false;
    }

    private boolean canEnter(byte d, int position) {
        Tile t = level.getLayerFG().get(position);

        switch(t) {
            case WALL:
            case HIDDENWALL_TEMP:
            case INVISIBLE_WALL:
            case BLUEWALL_REAL:
            case CLONE_MACHINE:
                return false;
            case THIN_WALL_DOWN: return d != 'u';
            case THIN_WALL_DOWN_RIGHT: return (d == 'd' || d == 'r');
            case THIN_WALL_LEFT: return d != 'r';
            case THIN_WALL_RIGHT: return d != 'l';
            case THIN_WALL_UP: return d != 'd';
            default: return true;
        }
    }

    private void complete(int[] solution) {
        output.setText("Reconstructing solution...");
        level.load(startState);

        ArrayList<ByteList> partialSolutions = new ArrayList<>();

        if(solution.length > 0) {
            partialSolutions.add(paths[0][solution[0]].moves);
            for (int i = 0; i < solution.length - 1; i++) {
                partialSolutions.add(paths[solution[i]][solution[i + 1]].moves);
            }
            partialSolutions.add(paths[solution[solution.length - 1]][1 + inputNodeSize + chosenExit].moves);
        }
        else {
            partialSolutions.add(paths[0][1].moves);
        }

        emulator.getSavestates().restart();
        level.load(emulator.getSavestates().getSavestate());

        for(ByteList partialSolution : partialSolutions) {
            for(Byte b : partialSolution) {
                emulator.tick(b, TickFlags.PRELOADING);
            }
        }

        Solution tspSolution = new Solution(emulator.getSavestates().getMoveList(), level.getRngSeed(), level.getStep());
        tspSolution.load(emulator);
    }

    private class PathNode {
        public byte[] state;
        public ByteList moves;
        public int time;

        public PathNode(byte[] state, ByteList moves, int time) {
            this.state = state;
            this.moves = moves;
            this.time = time;
        }
    }
}
