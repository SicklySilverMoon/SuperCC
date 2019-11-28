package tools.tsp;

import emulator.Solution;
import emulator.SuperCC;
import emulator.TickFlags;
import game.Direction;
import game.Level;
import game.Position;
import game.Tile;
import util.ByteList;

import java.util.ArrayList;
import java.util.LinkedList;

public class TSPSolver {
    private SuperCC emulator;
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

    public TSPSolver(SuperCC emulator) {
        this.emulator = emulator;
        this.level = emulator.getLevel();
        this.startState = level.save();
        emulator.tick(SuperCC.WAIT, TickFlags.LIGHT); // Full wait
        emulator.tick(SuperCC.WAIT, TickFlags.LIGHT);
        this.directions = new byte[]{ SuperCC.UP, SuperCC.RIGHT, SuperCC.DOWN, SuperCC.LEFT };
        this.initialState = normalizeState();
        this.boostNodes = new boolean[nodes.size()];
        this.distances = new int[nodes.size()][nodes.size()];
        for(int i = 0 ; i < nodes.size(); i++) {
            for(int j = 0; j < nodes.size(); j++) {
                distances[i][j] = 9999;
            }
        }
        this.paths = new PathNode[nodes.size()][nodes.size()];
        this.startTime = level.getTimer();
    }

    public void solve() {
        for(int i = 0; i < nodes.size(); i++) {
            level.load(initialState);
            level.cheats.moveChip(new Position(nodes.get(i)));

            LinkedList<PathNode> states = new LinkedList<>();
            states.add(new PathNode(level.save(), new ByteList(), startTime));

            int[] visited = new int[32 * 32];
            int[] visitedCount = new int[32 * 32];
            int statesExplored = 0;

            while (!states.isEmpty() && statesExplored < 50000) {
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

                if(visitedCount[index] >= 2 && !t.isIce() && !t.isFF() && t != Tile.TELEPORT) {
                    continue;
                }
                else if(visitedCount[index] >= 20) {
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

                /*if(index == 451 - 32 + 1) {
                    for (int j = 0; j < 32 * 32; j++) {
                        if (j % 32 == 0) System.out.print("\n");
                        if (level.getChip().getPosition().getIndex() == j) System.out.print("P");
                        else System.out.print(visited[j] > 0 ? "#" : ".");
                    }
                }

                /*if(i == 3) {
                    int x = 1;
                }*/
            }
            System.out.println(statesExplored);
        }

        for(int i = 0; i < nodes.size(); i++) {
            for(int j = 0; j < nodes.size(); j++) {
                System.out.format("%4d", distances[i][j]);
            }
            System.out.print("\n");
        }
        System.out.print("\n");

        System.out.println("Starting SA");
        SimulatedAnnealing sa = new SimulatedAnnealing(100, 0.05, 0.998, 50000, distances);
        int[] solution = sa.start();
        System.out.println("Finished SA");

        for(int i = 0; i < solution.length; i++) {
            //System.out.format("%3d", solution[i]);
            System.out.println(new Position(nodes.get(solution[i])).toString());
        }

        complete(solution);
        //level.load(startState);
    }

    private byte[] normalizeState() {
        int start = 0;
        int end = 0;
        for(int i = 0; i < 32 * 32; i++) {
            Tile t = level.getLayerFG().get(i);
            if(t.isChip()) {
                start = i;
            }
            if(t == Tile.EXIT) {
                end = i;
            }
            if(!isTSPTile(t)) {
                level.getLayerFG().set(i, Tile.FLOOR);
            }
            else if(isNodeTile(t)) {
                nodes.add(i);
            }
            level.getLayerBG().set(i, Tile.FLOOR);
        }

        int startIndex = nodes.indexOf(start);
        if(startIndex != 0) {
            nodes.set(startIndex, nodes.get(0));
            nodes.set(0, start);
        }

        int endIndex = nodes.indexOf(end);
        if(endIndex != nodes.size() - 1) {
            nodes.set(endIndex, nodes.get(nodes.size() - 1));
            nodes.set(nodes.size() - 1, end);
        }

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
        boolean ret = true; //visited[newPosition] < level.getTimer();
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
                //visited[newPosition] = level.getTimer();
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

    private Boolean checkSlidingPath(byte d, int position, int[] visited, int[] visitedCount, int to, Tile on) {
        int delta = 0;
        Direction dir = level.getChip().getDirection();

        if(dir == Direction.UP) delta = -32;
        else if(dir == Direction.RIGHT) delta = 1;
        else if(dir == Direction.DOWN) delta = 32;
        else if(dir == Direction.LEFT) delta = -1;

        int newPosition = position + delta;

        Tile t = level.getLayerFG().get(newPosition);
        if(!t.isIce() && !t.isFF() && t != Tile.TELEPORT && canEnter(d, newPosition)) {
            if (newPosition == to) {
                return true;
            }
            if(visitedCount[newPosition] >= 2) {
                return null;
            }
        }
        else if(!directionEquals(level.getChip().getDirection(), d) && (on.isIce() || on == Tile.TELEPORT)) {
            return null;
        }
        return false;
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
        level.load(startState);

        ArrayList<ByteList> partialSolutions = new ArrayList<>();

        /*partialSolutions.add(findPath(nodes.get(0), nodes.get(solution[0]), false));
        for(int i = 0; i < solution.length - 1; i++) {
            partialSolutions.add(findPath(nodes.get(solution[i]), nodes.get(solution[i + 1]), boostNodes[solution[i]]));
        }
        partialSolutions.add(findPath(nodes.get(solution[solution.length - 1]), nodes.get(nodes.size() - 1), boostNodes[solution[solution.length - 1]]));
        */

        partialSolutions.add(paths[0][solution[0]].moves);
        for(int i = 0; i < solution.length - 1; i++) {
            partialSolutions.add(paths[solution[i]][solution[i + 1]].moves);
        }
        partialSolutions.add(paths[solution[solution.length - 1]][nodes.size() - 1].moves);

        emulator.getSavestates().restart();
        level.load(emulator.getSavestates().getSavestate());

        for(ByteList partialSolution : partialSolutions) {
            for(Byte b : partialSolution) {
                emulator.tick(b, TickFlags.PRELOADING);
            }
        }

        Solution tspSolution = new Solution(emulator.getSavestates().getMoveList(), level.getRngSeed(), level.getStep());
        tspSolution.load(emulator);

        int x = 2;
    }

    private ByteList findPath(int from, int to, boolean boost) {
        level.load(initialState);
        level.cheats.moveChip(new Position(from));

        if(boost) {
            emulator.tick(SuperCC.WAIT, TickFlags.LIGHT);
        }

        LinkedList<PathNode> nodes = new LinkedList<>();
        nodes.add(new PathNode(level.save(), new ByteList(), startTime));

        int[] visited = new int[32 * 32];
        int[] visitedCount = new int[32 * 32];
        int statesExplored = 0;

        PathNode bestPath = null;

        while (!nodes.isEmpty() && statesExplored < 50000) {
            statesExplored++;
            PathNode node = nodes.poll();
            byte[] state = node.state;
            level.load(state);

            int index = level.getChip().getPosition().getIndex();
            Tile t = level.getLayerBG().get(index);
            if(visitedCount[index] >= 2 && !t.isIce() && !t.isFF() && t != Tile.TELEPORT) {
                continue;
            }
            else if(visitedCount[index] >= 20) {
                continue;
            }

            visitedCount[index]++;

            if(visited[index] < level.getTimer()) {
                visited[index] = level.getTimer();
                visitedCount[index] = 0;
            }

            if (index == to) {
                if(bestPath == null || bestPath.time < node.time) {
                    bestPath = node;
                }
            }

            for (int d = 0; d < directions.length; d++) {
                if (d > 0) {
                    level.load(state);
                }
                Boolean result = false;
                if(level.getChip().isSliding()) {
                    result = checkSlidingPath(directions[d], index, visited, visitedCount, to, t);
                    if(result != null && result) {
                        if(bestPath == null || bestPath.time < node.time) {
                            bestPath = node;
                        }
                    }
                }
                if(result != null && !result) {
                    emulator.tick(directions[d], TickFlags.LIGHT);
                    ByteList newMoves = node.moves.clone();
                    newMoves.add(directions[d]);
                    nodes.add(new PathNode(level.save(), newMoves, level.getTimer()));
                }
            }
        }
        return bestPath.moves;
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
