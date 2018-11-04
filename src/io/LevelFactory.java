package io;

import game.*;

import java.util.BitSet;

/**
 * A class for turning creating a Level object using only the data from the
 * .dat file.
 */
public class LevelFactory {

    // Various helper functions for processing parts of the .dat file.
    private static short[] getToggleDoors(Layer layerFG, Layer layerBG){
        int l = 0;
        for (int i = 0; i < 32*32; i++){
            Tile tile = layerFG.get(i);
            if (tile == Tile.TOGGLE_CLOSED || tile == Tile.TOGGLE_OPEN) l++;
            tile = layerBG.get(i);
            if (tile == Tile.TOGGLE_CLOSED || tile == Tile.TOGGLE_OPEN) l++;
        }
        short[] toggleDoors = new short[l];
        l = 0;
        for (short i = 0; i < 32*32; i++){
            Tile tile = layerFG.get(i);
            if (tile == Tile.TOGGLE_CLOSED || tile == Tile.TOGGLE_OPEN){
                toggleDoors[l] = i;
                l++;
            }
            tile = layerBG.get(i);
            if (tile == Tile.TOGGLE_CLOSED || tile == Tile.TOGGLE_OPEN){
                toggleDoors[l] = i;
                l++;
            }
        }
        return toggleDoors;
    }
    private static short[] getPortals(Layer layerFG, Layer layerBG){
        int l = 0;
        for (int i = 0; i < 32*32; i++){
            if (layerFG.get(i) == Tile.TELEPORT || layerBG.get(i) == Tile.TELEPORT) l++;
        }
        short[] portals = new short[l];
        l = 0;
        for (short i = 0; i < 32*32; i++){
            if (layerFG.get(i) == Tile.TELEPORT || layerBG.get(i) == Tile.TELEPORT){
                portals[l++] = i;
            }
        }
        return portals;
    }
    private static CreatureList getMonsterList(int[][] monsterPositions, Layer layerFG, Layer layerBG){
        if (monsterPositions == null) return new CreatureList(new Creature[] {});
        int l = 0;
        for (int i = 0; i < monsterPositions.length; i++){
            int x = monsterPositions[i][0];
            int y = monsterPositions[i][1];
            int position = 32*y+x;
            if (layerFG.get(position).isMonster() && (layerBG.get(position) != Tile.CLONE_MACHINE)) {
                l++;
            }
        }
        Creature[] monsterList = new Creature[l];
        l = 0;
        for (int i = 0; i < monsterPositions.length; i++){
            int x = monsterPositions[i][0];
            int y = monsterPositions[i][1];
            Position position = new Position(x, y);
            if (layerFG.get(position).isMonster() && (layerBG.get(position) != Tile.CLONE_MACHINE)) {
                monsterList[l++] = new Creature(position, layerFG.get(position));
            }
        }
        return new CreatureList(monsterList);
    }
    private static Creature findPlayer(Layer layerFG){
        for (int i = 32*32-1; i >= 0; i--){
            Tile tile = layerFG.get(i);
            if (Tile.CHIP_UP.ordinal() <= tile.ordinal()) return new Creature(new Position(i), tile);
            if (Tile.CHIP_SWIMMING_NORTH.ordinal() <= tile.ordinal() && tile.ordinal() <= Tile.CHIP_SWIMMING_EAST.ordinal())
                return new Creature(new Position(i), tile);
        }
        return new Creature(new Position(0), Tile.CHIP_DOWN);
    }
    private static int getTimer(int timeLimit){
        if (timeLimit == 0) return -2;
        return (timeLimit*10+8);
    }
    private static int getSliplistCapacity(Layer layerFG, Layer layerBG){
        int counter = 0;
        for (Tile t : layerBG) if (t.isSliding()) counter++;
        for (Tile t : layerFG) if (t.isSliding()) counter++;
        return counter;
    }

    /**
     * Convert the raw data of the .dat file into a level
     * @param levelNumber The level number
     * @param timeLimit The time limit of the level - this does not include the
     *                  decimal.
     * @param chips The number of chips to collect in the level before the
     *              socket opens
     * @param byteLayerFG The foreground layer, as a 32*32 byte[] in C order
     * @param byteLayerBG The background layer, as a 32*32 byte[] in C order
     * @param title The level title, as a byte[] representing ASCII characters
     * @param trapConnections Trap connections, such that trapConnections[i][0]
     *                        is a button connected to trapConnections[i][1]
     * @param cloneConnections Clone machine connections, such that
     *                         cloneConnections[i][0] is a button connected to
     *                         cloneConnections[i][1]
     * @param password The level password, as a byte[] representing ASCII
     *                 characters, not encoded
     * @param hint The level hint, as a byte[] representing ASCII characters
     * @param monsterPositions The x and y coordinates of all monsters that
     *                         will be in the initial monster list. Buried
     *                         monsters and monsters on clone machines may
     *                         be included. They will be ignored.
     * @return A Level
     */
    static Level makeLevel(int levelNumber, int timeLimit, int chips, byte[] byteLayerFG, byte[] byteLayerBG,
                           byte[] title, int[][] trapConnections, int[][] cloneConnections, byte[] password,
                           byte[] hint, int[][] monsterPositions, int rngSeed, Step step){

        Layer layerBG = new Layer(byteLayerBG);
        Layer layerFG = new Layer(byteLayerFG);
        short[] toggleDoors = getToggleDoors(layerFG, layerBG);
        short[] portals = getPortals(layerFG, layerBG);
        CreatureList monsterList = getMonsterList(monsterPositions, layerFG, layerBG);
        SlipList slipList = new SlipList(getSliplistCapacity(layerFG, layerBG));
        if (trapConnections == null) trapConnections = new int[][] {};
        BitSet traps = new BitSet(trapConnections.length);
        if (cloneConnections == null) cloneConnections = new int[][] {};

        Creature chip = findPlayer(layerFG);
        int timer = getTimer(timeLimit);
        RNG rng = new RNG();
        rng.setRNG(rngSeed);

        return new Level(levelNumber, title, password, hint, toggleDoors, portals, trapConnections, traps,
                cloneConnections, layerBG, layerFG, monsterList, slipList, chip, timer, chips, rng,
                rngSeed, step);
    }

}
