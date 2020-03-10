package tools.tsp;

import emulator.Solution;
import emulator.SuperCC;
import emulator.TickFlags;
import game.*;
import tools.TSPGUI;
import util.ByteList;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

public class TSPSolver {
    public final int INFINITE_DISTANCE = 9999;
    private SuperCC emulator;
    private TSPGUI gui;
    private Level level;
    private byte[] startState;
    private byte[] directions = { SuperCC.UP, SuperCC.RIGHT, SuperCC.DOWN, SuperCC.LEFT };
    private byte[] initialState;
    private ArrayList<Integer> nodes = new ArrayList<>();
    private boolean[][] boostNodes;
    private boolean[][] boostNodesBoost;
    private int[][] distances;
    private int[][] distancesBoost;
    private PathNode[][] paths;
    private PathNode[][] pathsBoost;
    private int startTime;

    private int inputNodeSize;
    private int exitNodeSize;

    private ArrayList<TSPGUI.RestrictionNode> restrictionNodes;

    private SimulatedAnnealingParameters simulatedAnnealingParameters;

    private int chosenExit;

    private JTextPane output;

    private Creature[] monsterList;

    private int[][] currentDistances;
    private PathNode[][] currentPaths;
    private boolean[][] currentBoostNodes;
    private int deltaTime;

    private final int LIMIT = 500000; // Upper bound of exploration

    public TSPSolver(SuperCC emulator, TSPGUI gui, ArrayList<TSPGUI.ListNode> inputNodes, ArrayList<TSPGUI.ListNode> exitNodes,
                     ArrayList<TSPGUI.RestrictionNode> restrictionNodes, SimulatedAnnealingParameters simulatedAnnealingParameters,
                     ActingWallParameters actingWallParameters, JTextPane output) {
        this.emulator = emulator;
        this.gui = gui;
        this.level = emulator.getLevel();
        setupState();
        this.initialState = normalizeState(actingWallParameters);
        setupNodes(inputNodes, exitNodes);
        this.boostNodes = new boolean[nodes.size()][nodes.size()];
        this.boostNodesBoost = new boolean[nodes.size()][nodes.size()];
        this.distances = new int[nodes.size()][nodes.size()];
        this.distancesBoost = new int[nodes.size()][nodes.size()];
        setupDistances();
        this.paths = new PathNode[nodes.size()][nodes.size()];
        this.pathsBoost = new PathNode[nodes.size()][nodes.size()];
        this.startTime = level.getTChipTime();
        this.inputNodeSize = inputNodes.size();
        this.exitNodeSize = exitNodes.size();
        this.restrictionNodes = restrictionNodes;
        setupRestrictionNodes();
        this.simulatedAnnealingParameters = simulatedAnnealingParameters;
        this.output = output;
    }

    private void setupState() {
        emulator.getSavestates().restart();
        level.load(emulator.getSavestates().getSavestate());
        this.monsterList = level.getMonsterList().getCreatures().clone();
        level.getMonsterList().setCreatures(new Creature[0]);
        this.startState = level.save();
        emulator.tick(SuperCC.WAIT, TickFlags.LIGHT); // Full wait
        emulator.tick(SuperCC.WAIT, TickFlags.LIGHT);
    }

    private void setupNodes(ArrayList<TSPGUI.ListNode> inputNodes, ArrayList<TSPGUI.ListNode> exitNodes) {
        for(TSPGUI.ListNode node : inputNodes) {
            this.nodes.add(node.index);
        }
        for(TSPGUI.ListNode node : exitNodes) {
            this.nodes.add(node.index);
        }
    }

    private void setupDistances() {
        for(int i = 0 ; i < nodes.size(); i++) {
            Arrays.fill(distances[i], INFINITE_DISTANCE);
            Arrays.fill(distancesBoost[i], INFINITE_DISTANCE);
        }
    }

    private void setupRestrictionNodes() {
        for(TSPGUI.RestrictionNode node : restrictionNodes) {
            node.beforeIndex = nodes.indexOf(node.before.index) - 1;
            node.afterIndex = nodes.indexOf(node.after.index) - 1;
        }
    }

    public void solve() throws Exception {
        gatherNormal();
        gatherBoost();
        level.load(startState);
        validate();
        if(!gui.killFlag) {
            solveWithSA();
        } else {
            output.setText("Stopped");
        }
    }

    public void gatherNormal() {
        currentDistances = distances;
        currentPaths = paths;
        currentBoostNodes = boostNodes;
        deltaTime = 0;
        search();
    }

    public void gatherBoost() {
        currentDistances = distancesBoost;
        currentPaths = pathsBoost;
        currentBoostNodes = boostNodesBoost;
        deltaTime = -1;
        search();
    }

    private void search() {
        boolean isBoost = deltaTime == -1;
        for(int from = 0; from < inputNodeSize + 1; from++) {
            if(isBoost && !canBoost(nodes.get(from))) {
                continue;
            }
            outputDistanceProgress(from, isBoost);
            level.load(initialState);
            level.cheats.moveChip(new Position(nodes.get(from)));
            if(isBoost) {
                emulator.tick(SuperCC.WAIT, TickFlags.LIGHT); // Half wait
            }
            searchBFS(from);
        }
    }

    private void outputDistanceProgress(int from, boolean isBoost) {
        int current = from + 1;
        if(isBoost) current += inputNodeSize + 1;
        int total = (inputNodeSize + 1) * 2;
        output.setText("Finding distances... " + current + "/" + total);
    }

    private boolean canBoost(int position) {
        for(Direction dir : new Direction[] {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT}) {
            int delta = getDelta(dir);
            int newPosition = position + delta;
            if(newPosition >= 0 && newPosition < 1024 && level.getLayerFG().get(newPosition).isSliding()) {
                return true;
            }
        }
        return false;
    }

    private void validate() throws Exception {
        String errorString = validateInputNodeAccessibility() + validateExitAccessibility();

        if(!errorString.equals("")) {
            throw new Exception(errorString);
        }
    }

    private String validateInputNodeAccessibility() {
        String errorString = "";
        for(int i = 1; i < inputNodeSize + 1; i++) {
            if(distances[0][i] == INFINITE_DISTANCE) {
                errorString += "    " +
                        emulator.getLevel().getLayerFG().get(nodes.get(i)).toString() +
                        " at " +
                        new Position(nodes.get(i)).toString() +
                        " inaccessible!\n";
            }
        }
        return errorString;
    }

    private String validateExitAccessibility() {
        boolean isExitAccessible = false;
        for(int i = inputNodeSize + 1; i < nodes.size(); i++) {
            if(distances[0][i] != INFINITE_DISTANCE) {
                isExitAccessible = true;
                break;
            }
        }
        if(!isExitAccessible) {
            return "    No exit accessible!";
        }
        return "";
    }

    private void solveWithSA() {
        level.getMonsterList().setCreatures(monsterList);
        SimulatedAnnealing sa = new SimulatedAnnealing(gui, level.getStartTime(), simulatedAnnealingParameters, distances, distancesBoost,
                boostNodes, boostNodesBoost, inputNodeSize, exitNodeSize, restrictionNodes, output);
        int[] solution = sa.start();
        chosenExit = sa.bestExit;

        createSolution(solution);
        output.setText("Finished!");
    }

    private void searchBFS(int from) {
        PriorityQueue<PathNode> states = new PriorityQueue<>(100, (a, b) -> b.time - a.time);
        states.add(new PathNode(level.save(), new ByteList(), startTime, (byte) 'u'));

        int[] visitedAt = new int[1024 * 4];
        int[] visitedCount = new int[1024 * 4];
        int statesExplored = 0;

        while (!states.isEmpty() && statesExplored < LIMIT && !gui.killFlag) {
            statesExplored++;
            PathNode node = states.poll();
            byte[] state = node.state;
            level.load(state);

            int index = level.getChip().getPosition().getIndex() + 1024 * getDirectionIndex(node.lastMove);
            Tile onTile = level.getLayerBG().get(index % 1024);
            if(!onTile.isSliding()) {
                index %= 1024;
            }

            if (visitedAt[index] < level.getTChipTime()) {
                visitedAt[index] = level.getTChipTime();
                visitedCount[index] = 0;
            }

            if (visitedCount[index] > 0) {
                continue;
            }

            visitedCount[index]++;

            getToNode(from, index, node, false);

            for (int direction = 0; direction < directions.length; direction++) {
                if (direction > 0) {
                    level.load(state);
                }
                boolean can = true;
                if (level.getChip().isSliding()) {
                    can = handleSliding(directions[direction], index, visitedAt, visitedCount, from, onTile, node);
                }
                if (can) {
                    emulator.tick(directions[direction], TickFlags.LIGHT);
                    ByteList newMoves = node.moves.clone();
                    newMoves.add(directions[direction]);
                    states.add(new PathNode(level.save(), newMoves, level.getTChipTime(), directions[direction]));
                }
            }
        }
    }

    private boolean getToNode(int from, int to, PathNode node, boolean isBoost) {
        if (nodes.contains(to % 1024)) {
            to = nodes.indexOf(to % 1024);
            if (currentDistances[from][to] > startTime - level.getTChipTime() + deltaTime) {
                currentDistances[from][to] = startTime - level.getTChipTime() + deltaTime;
                currentPaths[from][to] = node;
                currentBoostNodes[from][to] = isBoost;
            }
            return true;
        }
        return false;
    }

    private int getDirectionIndex(byte d) {
        if(d == 'u') return 0;
        if(d == 'r') return 1;
        if(d == 'd') return 2;
        if(d == 'l') return 3;
        return 0;
    }

    private byte[] normalizeState(ActingWallParameters actingWallParameters) {
        int start = 0;
        for(int i = 0; i < 1024; i++) {
            Tile t = level.getLayerFG().get(i);
            if(t.isChip()) {
                start = i;
            }
            if(!isTSPTile(t)) {
                level.getLayerFG().set(i, Tile.FLOOR);
            }
            if(isActingWall(t, actingWallParameters)) {
                level.getLayerFG().set(i, Tile.BOMB);
            }
            if(t == Tile.BLOCK || t.isTransparent()) {
                Tile bgT = level.getLayerBG().get(i);
                if(isActingWall(bgT, actingWallParameters)) {
                    level.getLayerFG().set(i, Tile.WALL);
                }
            }
            level.getLayerBG().set(i, Tile.FLOOR);
        }

        nodes.add(start);

        return level.save();
    }

    private boolean isActingWall(Tile t, ActingWallParameters actingWallParameters) {
        if(t == Tile.WATER && actingWallParameters.isWaterWall) {
            return true;
        }
        if(t == Tile.FIRE && actingWallParameters.isFireWall) {
            return true;
        }
        if(t == Tile.BOMB && actingWallParameters.isBombWall) {
            return true;
        }
        if(t == Tile.THIEF && actingWallParameters.isThiefWall) {
            return true;
        }
        if(t == Tile.TRAP && actingWallParameters.isTrapWall) {
            return true;
        }
        return false;
    }

    private boolean isTSPTile(Tile t) {
        return (t == Tile.FLOOR || t == Tile.WALL || t.isChip() ||
           t.isIce() || t.isFF() || t == Tile.TELEPORT || t == Tile.BLUEWALL_REAL ||
           t == Tile.HIDDENWALL_TEMP || t == Tile.INVISIBLE_WALL || t == Tile.CLONE_MACHINE ||
           t == Tile.THIN_WALL_LEFT || t == Tile.THIN_WALL_RIGHT || t == Tile.THIN_WALL_DOWN_RIGHT ||
           t == Tile.THIN_WALL_DOWN || t == Tile.THIN_WALL_UP);
    }

    private boolean handleSliding(byte direction, int position, int[] visitedAt, int[] visitedCount, int from, Tile onTile, PathNode node) {
        Direction dir = level.getChip().getDirection();
        int delta = getDelta(dir);
        int newPosition = ((position % 1024) + delta) + 1024 * getDirectionIndex(direction);
        Tile newTile = level.getLayerFG().get(newPosition % 1024);

        if(!canEnter(direction, newPosition) && !isThinWall(newTile)) {
            return false;
        }

        if(!canEnter(dirToByte(dir), newPosition)) {
            return !onTile.isIce();
        }

        if(!newTile.isSliding()) {
            boolean isBoost = level.getTChipTime() % 2 == 1;
            boolean gotTo = getToNode(from, newPosition, node, isBoost);
            if(!gotTo && visitedCount[newPosition] >= 1) {
                return false;
            }
            return true;
        }
        return canOverride(direction, newTile);
    }

    private int getDelta(Direction dir) {
        if(dir == Direction.UP) return -32;
        if(dir == Direction.RIGHT) return 1;
        if(dir == Direction.DOWN) return 32;
        return -1;
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

    private boolean canOverride(byte d, Tile t) {
        return directionEquals(level.getChip().getDirection(), d) || t.isFF();
    }

    private void createSolution(int[] solution) {
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

    public int[][] getDistances() {
        return distances;
    }

    public int[][] getDistancesBoost() {
        return distancesBoost;
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
