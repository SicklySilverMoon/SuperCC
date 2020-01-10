package tools.tsp;

import emulator.Solution;
import emulator.SuperCC;
import emulator.TickFlags;
import game.*;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
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
    private boolean[][] boostNodes;
    private boolean[][] boostNodesBoost;
    private int[][] distances;
    private int[][] distancesBoost;
    private PathNode[][] paths;
    private PathNode[][] pathsBoost;
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

    Creature[] monsterList;

    private final int LIMIT = 500000; // Upper bound of exploration

    public TSPSolver(SuperCC emulator, TSPGUI gui, ArrayList<TSPGUI.ListNode> inputNodes, ArrayList<TSPGUI.ListNode> exitNodes,
                     ArrayList<TSPGUI.RestrictionNode> restrictionNodes,
                     double startTemp, double endTemp, double cooling, int iterations,
                     boolean isWaterWall, boolean isFireWall, boolean isBombWall, boolean isThiefWall, boolean isTrapWall,
                     JTextPane output) {
        this.emulator = emulator;
        this.gui = gui;
        this.level = emulator.getLevel();
        emulator.getSavestates().restart();
        level.load(emulator.getSavestates().getSavestate());
        this.monsterList = level.getMonsterList().getCreatures().clone();
        level.getMonsterList().setCreatures(new Creature[0]);
        this.startState = level.save();
        emulator.tick(SuperCC.WAIT, TickFlags.LIGHT); // Full wait
        emulator.tick(SuperCC.WAIT, TickFlags.LIGHT);
        this.directions = new byte[]{ SuperCC.UP, SuperCC.RIGHT, SuperCC.DOWN, SuperCC.LEFT };
        this.initialState = normalizeState(isWaterWall, isFireWall, isBombWall, isThiefWall, isTrapWall);

        for(int i = 0; i < inputNodes.size(); i++) {
            this.nodes.add(inputNodes.get(i).index);
        }
        for(int i = 0; i < exitNodes.size(); i++) {
            this.nodes.add(exitNodes.get(i).index);
        }

        this.boostNodes = new boolean[nodes.size()][nodes.size()];
        this.boostNodesBoost = new boolean[nodes.size()][nodes.size()];
        this.distances = new int[nodes.size()][nodes.size()];
        this.distancesBoost = new int[nodes.size()][nodes.size()];
        for(int i = 0 ; i < nodes.size(); i++) {
            for(int j = 0; j < nodes.size(); j++) {
                distances[i][j] = 9999;
                distancesBoost[i][j] = 9999;
            }
        }
        this.paths = new PathNode[nodes.size()][nodes.size()];
        this.pathsBoost = new PathNode[nodes.size()][nodes.size()];
        this.startTime = level.getTChipTime();

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
            for(int j = 0; j < 2; j++) {
                output.setText("Finding distances... " + (i + 1) + "/" + (inputNodeSize + 1));
                level.load(initialState);
                level.cheats.moveChip(new Position(nodes.get(i)));
                if(j == 1) {
                    emulator.tick(SuperCC.WAIT, TickFlags.LIGHT); // Half wait
                }

                int[][] currentDistances = (j == 0) ? distances : distancesBoost;
                PathNode[][] currentPaths = (j == 0) ? paths : pathsBoost;
                boolean[][] currentBoostNodes = (j == 0) ? boostNodes : boostNodesBoost;

                PriorityQueue<PathNode> states = new PriorityQueue<>(100, (a, b) -> b.time - a.time);
                states.add(new PathNode(level.save(), new ByteList(), startTime, (byte) 'u'));

                int[] visited = new int[32 * 32 * 4];
                int[] visitedCount = new int[32 * 32 * 4];
                int statesExplored = 0;

                while (!states.isEmpty() && statesExplored < LIMIT && !gui.killFlag) {
                    statesExplored++;
                    PathNode node = states.poll();
                    byte[] state = node.state;
                    level.load(state);

                    int index = level.getChip().getPosition().getIndex() + 1024 * getDirectionIndex(node.lastMove);
                    Tile t = level.getLayerBG().get(index % 1024);

                    if (visited[index] < level.getTChipTime()) {
                        visited[index] = level.getTChipTime();
                        visitedCount[index] = 0;
                    }

                    if (visitedCount[index] >= 2) {
                        continue;
                    }

                    visitedCount[index]++;

                    if (nodes.contains(index % 1024)) {
                        int deltaTime = (j == 0) ? 0 : -1;
                        if (currentDistances[i][nodes.indexOf(index % 1024)] > startTime - level.getTChipTime() + deltaTime) {
                            currentDistances[i][nodes.indexOf(index % 1024)] = startTime - level.getTChipTime() + deltaTime;
                            currentPaths[i][nodes.indexOf(index % 1024)] = node;
                            currentBoostNodes[i][nodes.indexOf(index % 1024)] = false;
                        }
                    }

                    for (int d = 0; d < directions.length; d++) {
                        if (d > 0) {
                            level.load(state);
                        }
                        boolean can = true;
                        if (level.getChip().isSliding()) {
                            can = checkSliding(directions[d], index, visited, visitedCount, i, t, node, j);
                        }
                        if (can) {
                            emulator.tick(directions[d], TickFlags.LIGHT);
                            ByteList newMoves = node.moves.clone();
                            newMoves.add(directions[d]);
                            states.add(new PathNode(level.save(), newMoves, level.getTChipTime(), directions[d]));
                        }
                    }

                    if (shouldBeVisited) {
                        visitedCount[toBeVisited]++;
                        shouldBeVisited = false;
                        if (visited[toBeVisited] < level.getTChipTime()) {
                            visited[toBeVisited] = visitedTime;
                            visitedCount[toBeVisited] = 0;
                        }
                    }
                }
            }
        }

        level.getMonsterList().setCreatures(monsterList);
        SimulatedAnnealing sa = new SimulatedAnnealing(gui, startTemp, endTemp, cooling, iterations, distances,
                distancesBoost, boostNodes, boostNodesBoost, inputNodeSize, exitNodeSize, restrictionNodes, output);
        int[] solution = sa.start();
        chosenExit = sa.bestExit;

        complete(solution);
        output.setText("Finished!");
    }

    private int getDirectionIndex(byte d) {
        if(d == 'u') return 0;
        if(d == 'r') return 1;
        if(d == 'd') return 2;
        if(d == 'l') return 3;
        return 0;
    }

    private byte[] normalizeState(boolean isWaterWall, boolean isFireWall, boolean isBombWall, boolean isThiefWall, boolean isTrapWall) {
        int start = 0;
        for(int i = 0; i < 32 * 32; i++) {
            Tile t = level.getLayerFG().get(i);
            if(t.isChip()) {
                start = i;
            }
            if(!isTSPTile(t)) {
                level.getLayerFG().set(i, Tile.FLOOR);
            }
            if(isActingWall(t, isWaterWall, isFireWall, isBombWall, isThiefWall, isTrapWall)) {
                level.getLayerFG().set(i, Tile.BOMB);
            }
            if(t == Tile.BLOCK || t.isTransparent()) {
                Tile bgT = level.getLayerBG().get(i);
                if(isActingWall(bgT, isWaterWall, isFireWall, isBombWall, isThiefWall, isTrapWall)) {
                    level.getLayerFG().set(i, Tile.WALL);
                }
            }
            level.getLayerBG().set(i, Tile.FLOOR);
        }

        nodes.add(start);

        return level.save();
    }

    private boolean isActingWall(Tile t, boolean isWaterWall, boolean isFireWall, boolean isBombWall, boolean isThiefWall, boolean isTrapWall) {
        if(t == Tile.WATER && isWaterWall) {
            return true;
        }
        if(t == Tile.FIRE && isFireWall) {
            return true;
        }
        if(t == Tile.BOMB && isBombWall) {
            return true;
        }
        if(t == Tile.THIEF && isThiefWall) {
            return true;
        }
        if(t == Tile.TRAP && isTrapWall) {
            return true;
        }
        return false;
    }

    private boolean isTSPTile(Tile t) {
        if(t == Tile.FLOOR || t == Tile.WALL || t.isChip() ||
           t.isIce() || t.isFF() || t == Tile.TELEPORT || t == Tile.BLUEWALL_REAL ||
           t == Tile.HIDDENWALL_TEMP || t == Tile.INVISIBLE_WALL || t == Tile.CLONE_MACHINE ||
           t == Tile.THIN_WALL_LEFT || t == Tile.THIN_WALL_RIGHT || t == Tile.THIN_WALL_DOWN_RIGHT ||
           t == Tile.THIN_WALL_DOWN || t == Tile.THIN_WALL_UP) {
            return true;
        }
        return false;
    }

    private boolean checkSliding(byte d, int position, int[] visited, int[] visitedCount, int i, Tile on, PathNode node, int j) {
        int delta = 0;
        Direction dir = level.getChip().getDirection();

        if(dir == Direction.UP) delta = -32;
        else if(dir == Direction.RIGHT) delta = 1;
        else if(dir == Direction.DOWN) delta = 32;
        else if(dir == Direction.LEFT) delta = -1;

        int newPosition = ((position % 1024) + delta) + 1024 * getDirectionIndex(d);
        Tile t = level.getLayerFG().get(newPosition % 1024);

        if(!canEnter(d, newPosition) && !isThinWall(t)) {
            return false;
        }

        if(!canEnter(dirToByte(dir), newPosition)) {
            return !on.isIce();
        }

        int[][] currentDistances = (j == 0) ? distances : distancesBoost;
        PathNode[][] currentPaths = (j == 0) ? paths : pathsBoost;
        boolean[][] currentBoostNodes = (j == 0) ? boostNodes : boostNodesBoost;

        boolean ret = true;
        if(!t.isIce() && !t.isFF() && t != Tile.TELEPORT) {
            if (nodes.contains(newPosition % 1024)) {
                int deltaTime = (j == 0) ? 0 : -1;
                if(currentDistances[i][nodes.indexOf(newPosition % 1024)] > startTime - level.getTChipTime() + deltaTime) {
                    currentDistances[i][nodes.indexOf(newPosition % 1024)] = startTime - level.getTChipTime() + deltaTime;
                    currentPaths[i][nodes.indexOf(newPosition % 1024)] = node;
                    if(level.getTChipTime() % 2 == 1) {
                        currentBoostNodes[i][nodes.indexOf(newPosition % 1024)] = true;
                    }
                    else {
                        currentBoostNodes[i][nodes.indexOf(newPosition % 1024)] = false;
                    }
                }
            }
            else if(visitedCount[newPosition] >= 2) {
                return false;
            }
            toBeVisited = newPosition;
            shouldBeVisited = true;
            visitedTime = level.getTChipTime();
        }
        else if(!directionEquals(level.getChip().getDirection(), d) && (t.isIce() || t == Tile.TELEPORT)) {
            ret = false;
        }
        return ret;
    }

    private byte dirToByte(Direction dir) {
        switch(dir) {
            case UP: return 'u';
            case RIGHT: return 'r';
            case DOWN: return 'd';
            case LEFT: return 'l';
            default: return '-';
        }
    }

    private boolean isThinWall(Tile tile) {
        switch(tile) {
            case THIN_WALL_DOWN:
            case THIN_WALL_DOWN_RIGHT:
            case THIN_WALL_LEFT:
            case THIN_WALL_RIGHT:
            case THIN_WALL_UP:
                return true;
            default: return false;
        }
    }

    private boolean directionEquals(Direction dir, byte d) {
        if(dir == Direction.UP && d == 'u') return true;
        if(dir == Direction.RIGHT && d == 'r') return true;
        if(dir == Direction.DOWN && d == 'd') return true;
        if(dir == Direction.LEFT && d == 'l') return true;
        return false;
    }

    private boolean canEnter(byte d, int position) {
        Tile t = level.getLayerFG().get(position % 1024);

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
            case ICE_SLIDE_NORTHEAST: return (d == 'd' || d == 'l');
            case ICE_SLIDE_SOUTHEAST: return (d == 'u' || d == 'l');
            case ICE_SLIDE_NORTHWEST: return (d == 'd' || d == 'r');
            case ICE_SLIDE_SOUTHWEST: return (d == 'u' || d == 'r');
            default: return true;
        }
    }

    private void complete(int[] solution) {
        output.setText("Reconstructing solution...");
        level.load(startState);

        ArrayList<ByteList> partialSolutions = new ArrayList<>();

        if(solution.length > 0) {
            partialSolutions.add(paths[0][solution[0]].moves);
            boolean boosted = boostNodes[0][solution[0]];
            for (int i = 0; i < solution.length - 1; i++) {
                if(boosted) {
                    partialSolutions.add(pathsBoost[solution[i]][solution[i + 1]].moves);
                }
                else {
                    partialSolutions.add(paths[solution[i]][solution[i + 1]].moves);
                }
                boosted = boosted ?
                        boostNodesBoost[solution[i]][solution[i + 1]] : boostNodes[solution[i]][solution[i + 1]];
            }
            if(boosted) {
                partialSolutions.add(pathsBoost[solution[solution.length - 1]][1 + inputNodeSize + chosenExit].moves);
            }
            else {
                partialSolutions.add(paths[solution[solution.length - 1]][1 + inputNodeSize + chosenExit].moves);
            }
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
        byte lastMove;

        public PathNode(byte[] state, ByteList moves, int time, byte lastMove) {
            this.state = state;
            this.moves = moves;
            this.time = time;
            this.lastMove = lastMove;
        }
    }
}
