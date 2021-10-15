package io;

import game.*;
import game.Lynx.LynxCreature;
import game.Lynx.LynxCreatureList;
import game.Lynx.LynxLevel;
import game.MS.*;
import game.button.BlueButton;
import game.button.BrownButton;
import game.button.GreenButton;
import game.button.RedButton;
import util.MultiHashMap;

import java.util.*;

/**
 * A class for turning creating a Level object using only the data from the
 * .dat file.
 */
public class LevelFactory {

    // Various helper functions for processing parts of the .dat file.
    private static Position[] getToggleDoors(Layer layerFG, Layer layerBG){
        List<Position> toggleDoors = new ArrayList<>();
        for (short i = 0; i < 32*32; i++){
            Tile tileFG = layerFG.get(i);
            if (tileFG == Tile.TOGGLE_CLOSED || tileFG == Tile.TOGGLE_OPEN){
                toggleDoors.add(new Position(i));
            }
            Tile tileBG = layerBG.get(i);
            if ((tileBG == Tile.TOGGLE_CLOSED || tileBG == Tile.TOGGLE_OPEN) && tileFG.isMonster()){
                toggleDoors.add(new Position(i));
            }
        }
        return toggleDoors.toArray(new Position[0]);
    }
    private static Position[] getTeleports(Layer layerFG, Layer layerBG){
        int l = 0;
        for (int i = 0; i < 32*32; i++){
            if (layerFG.get(i) == Tile.TELEPORT || (layerFG.get(i).isMonster() && layerBG.get(i) == Tile.TELEPORT)) l++;
        }
        Position[] teleports = new Position[l];
        l = 0;
        for (short i = 0; i < 32*32; i++){
            if (layerFG.get(i) == Tile.TELEPORT || (layerFG.get(i).isMonster() && layerBG.get(i) == Tile.TELEPORT)){
                teleports[l++] = new Position(i);
            }
        }
        return teleports;
    }
    private static MSCreatureList getMSMonsterList(int[][] monsterPositions, Layer layerFG, Layer layerBG){ //Probably not the best solution for this, but it does work
        if (monsterPositions == null) return new MSCreatureList(new MSCreature[] {}, layerFG, layerBG);
        int l = 0;
        for (int i = 0; i < monsterPositions.length; i++){
            int x = monsterPositions[i][0];
            int y = monsterPositions[i][1];
            int position = 32*y+x;
            if (layerFG.get(position).isMonster() && (layerBG.get(position) != Tile.CLONE_MACHINE)) {
                l++;
            }
        }
        MSCreature[] monsterList = new MSCreature[l];
        l = 0;
        for (int[] monsterPosition : monsterPositions) {
            int x = monsterPosition[0];
            int y = monsterPosition[1];
            Position position = new Position(x, y);
            if (!position.isValid())
                continue;
            if (layerFG.get(position).isMonster() && (layerBG.get(position) != Tile.CLONE_MACHINE)) {
                monsterList[l++] = new MSCreature(position, layerFG.get(position));
            }
        }
        return new MSCreatureList(monsterList, layerFG, layerBG);
    }
    private static LynxCreatureList getLynxMonsterList(Layer layerFG, Layer layerBG){ //Probably not the best solution for this, but it does work
        List<LynxCreature> creatures = new ArrayList<>();
        int numChips = 0;
        int index = 0;
        for (Tile tile : layerFG) {
            if (tile.isCreature() || tile == Tile.BLOCK || (tile.isChip() && numChips == 0) || tile.isSwimmingChip()) {
                creatures.add(new LynxCreature(new Position(index), tile));
                if (tile.isChip())
                    numChips++;

                layerFG.set(index, layerBG.get(index)); //Lynx doesn't have a lower layer, so every creature's tile needs to be popped
                layerBG.set(index, Tile.FLOOR);
            }
            index++;
        }
        if (numChips == 0)
            creatures.add(new LynxCreature(new Position(0, 0), Tile.CHIP_DOWN));
            //the idea is that instead of making levels invalid we instead "legalize" them
        for (int i=0; i < creatures.size(); i++) {
            LynxCreature c = creatures.get(i);
            if (c.getCreatureType() == CreatureID.CHIP) {
                Collections.swap(creatures, 0, i);
                break;
            }
        }
        for (int i = 0; i < 1024; i++) {
            Tile tileFG = layerBG.get(i);
            if (tileFG.isChip() || tileFG.isCreature() || tileFG == Tile.BLOCK)
                layerFG.set(i, Tile.FLOOR); //clear out illegal tiles
        }
        return new LynxCreatureList(creatures.toArray(new LynxCreature[0]));
    }
    private static MSCreature findMSPlayer(Layer layerFG){
        for (int i = 32*32-1; i >= 0; i--){
            Tile tile = layerFG.get(i);
            if (Tile.CHIP_UP.ordinal() <= tile.ordinal()) return new MSCreature(new Position(i), tile);
        }
        return new MSCreature(new Position(0), Tile.CHIP_DOWN);
    }
    private static int getTimer(int timeLimit, int startingDecimalTimesTen){
        if (timeLimit == 0) return -2;
        return (timeLimit*100 + startingDecimalTimesTen);
    }
    private static int getSliplistCapacity(Layer layerFG, Layer layerBG){
        int counter = 0;
        for (Tile t : layerBG) if (t.isSliding()) counter++;
        for (Tile t : layerFG) if (t.isSliding()) counter++;
        return counter;
    }
    private static MultiHashMap<Position, GreenButton> getGreenButtons(Layer layerFG, Layer layerBG) {
        MultiHashMap<Position, GreenButton> buttons = new MultiHashMap<>();
        for (short i = 0; i < 32*32; i++){
            if (layerFG.get(i) == Tile.BUTTON_GREEN || layerBG.get(i) == Tile.BUTTON_GREEN){
                Position p = new Position(i);
                buttons.put(p, new GreenButton(p));
            }
        }
        return buttons;
    }
    private static MultiHashMap<Position, BlueButton> getBlueButtons(Layer layerFG, Layer layerBG) {
        MultiHashMap<Position, BlueButton> buttons = new MultiHashMap<>();
        for (short i = 0; i < 32*32; i++){
            if (layerFG.get(i) == Tile.BUTTON_BLUE || layerBG.get(i) == Tile.BUTTON_BLUE){
                Position p = new Position(i);
                buttons.put(p, new BlueButton(p));
            }
        }
        return buttons;
    }
    private static MultiHashMap<Position, BrownButton> getBrownButtons(int[][] trapConnections) {
        MultiHashMap<Position, BrownButton> buttons = new MultiHashMap<>(trapConnections.length);
        for (int[] trapConnection : trapConnections) {
            Position buttonPos = new Position(trapConnection[0]);
            buttons.put(buttonPos, new BrownButton(buttonPos, new Position(trapConnection[1])));
        }
        return buttons;
    }
    private static MultiHashMap<Position, RedButton> getRedButtons(int[][] cloneConnections) {
        MultiHashMap<Position, RedButton> buttons = new MultiHashMap<>(cloneConnections.length);
        for (int[] cloneConnection : cloneConnections) {
            Position buttonPos = new Position(cloneConnection[0]);
            buttons.put(buttonPos, new RedButton(buttonPos, new Position(cloneConnection[1])));
        }
        return buttons;
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
     * @param lastLevel The last level number in the set.
     * @param rngSeed The starting RNG seed to use.
     * @param step The starting step to use.
     * @param rules The ruleset to use.
     * @return A Level
     */
    static Level makeLevel(int levelNumber, int timeLimit, int chips, byte[] byteLayerFG, byte[] byteLayerBG,
                             String title, int[][] trapConnections, int[][] cloneConnections, String password,
                             String hint, String author, int[][] monsterPositions, int rngSeed, Step step,
                             int lastLevel, Ruleset rules, Direction initialSlide) {

        Layer layerBG = new ByteLayer(byteLayerBG);
        Layer layerFG = new ByteLayer(byteLayerFG);

        if (rules == Ruleset.MS) {
            return new MSLevel(
                levelNumber,
                title,
                password,
                hint,
                author,
                getToggleDoors(layerFG, layerBG),
                getTeleports(layerFG, layerBG),
                getGreenButtons(layerFG, layerBG),
                getRedButtons(cloneConnections),
                getBrownButtons(trapConnections),
                getBlueButtons(layerFG, layerBG),
                new BitSet(trapConnections.length),
                layerBG,
                layerFG,
                getMSMonsterList(monsterPositions, layerFG, layerBG),
                new SlipList(),
                findMSPlayer(layerFG),
                getTimer(timeLimit, 90),
                chips,
                new RNG(rngSeed, 0, 0),
                rngSeed,
                step,
                lastLevel
            );
        }
        else {
            LynxCreatureList creatures;
            creatures = getLynxMonsterList(layerFG, layerBG);
            return new LynxLevel(
                    levelNumber,
                    title,
                    password,
                    hint,
                    author,
                    getToggleDoors(layerFG, layerBG),
                    getTeleports(layerFG, layerBG),
                    getGreenButtons(layerFG, layerBG),
                    getRedButtons(cloneConnections),
                    getBrownButtons(trapConnections),
                    getBlueButtons(layerFG, layerBG),
                    layerFG,
                    creatures,
                    creatures.get(0),
                    getTimer(timeLimit, 95),
                    chips,
                    new RNG(rngSeed, 0, 0),
                    rngSeed,
                    step,
                    lastLevel,
                    initialSlide
            );
        }
    }
}