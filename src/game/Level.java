package game;

import java.util.BitSet;

import static game.Tile.*;

public class Level extends SaveState {
    
    public final int levelNumber;
    public final byte[] title, password, hint;
    public final short[] toggleDoors, portals;
    public final int[][] trapConnections, cloneConnections;

    protected int tickN;
    private int rngSeed;
    private Step step;

    public Level(int levelNumber, byte[] title, byte[] password, byte[] hint, short[] toggleDoors, short[] portals,
                 int[][] trapConnections, BitSet traps, int[][] cloneConnections,
                 Tile[] layerBG, Tile[] layerFG, CreatureList monsterList, SlipList slipList,
                 Creature chip, int time, int chips, RNG rng, int rngSeed, Step step){

        super(layerBG, layerFG, monsterList, slipList, chip, new byte[0],
                time, chips, new short[4], new short[4], rng, 0, traps);

        this.levelNumber = levelNumber;
        this.title = title;
        this.password = password;
        this.hint = hint;
        this.toggleDoors = toggleDoors;
        this.portals = portals;
        this.trapConnections = trapConnections;
        this.cloneConnections = cloneConnections;
        this.rngSeed = rngSeed;
        this.step = step;

        this.slipList.setLevel(this);
        this.monsterList.setLevel(this);
    }

    public Tile[] getLayerBG() {
        return layerBG;
    }
    public Tile[] getLayerFG() {
        return layerFG;
    }
    public int getTimer(){
        return timer;
    }
    public int getChipsLeft(){
        return chipsLeft;
    }
    public Creature getChip(){
        return chip;
    }
    public byte[] getMoves(){
        return moves;
    }
    public int getRngSeed(){
        return rngSeed;
    }
    public Step getStep(){
        return step;
    }
    public short[] getKeys(){
        return keys;
    }
    public short[] getBoots(){
        return boots;
    }
    public CreatureList getMonsterList(){
        return monsterList;
    }
    public SlipList getSlipList(){
        return slipList;
    }
    public int[][] getTrapConnections(){
        return trapConnections;
    }
    public int[][] getCloneConnections(){
        return cloneConnections;
    }
    
    public static boolean isKeyMovement(char c){
        return c != '?';
    }

    protected Tile popTile(int position){
        layerFG[position] = layerBG[position];
        layerBG[position] = FLOOR;
        return layerFG[position];
    }
    protected void insertTile(int position, Tile tile){
        layerBG[position] = layerFG[position];
        layerFG[position] = tile;
    }

    public void tickTimer(int t){
        timer -= t;
        while (timer < -10) timer += 4;
    }

    private void moveChipVoluntary(int direction){
        if (direction == -1) return;
        if (chip.isSliding()){
            if (!layerBG[chip.getPosition()].isFF()) return;
            if (direction == chip.getDirection()) return;
        }
        chip.tick(new int[] {direction}, this);
        mouseClick = -1;
    }
    
    private void moveChipClick(){
        if (chip.isSliding()){
            // if (!layerBG[chip.getPosition()].isFF()) return;
            // if (direction == chip.getDirection()) return;
            return;
        }
        chip.tick(chip.seekPosition(mouseClick), this);
    }

    private void moveChipSliding(){
        int direction = chip.getDirection();
        if (layerBG[chip.getPosition()].isFF()) chip.tick(new int[] {direction}, this);
        else chip.tick(new int[] {direction, Creature.turnFromDir(direction, Creature.TURN_AROUND)}, this);
    }

    private void addMove(char c){
        byte[] newMoves = new byte[moves.length+1];
        System.arraycopy(moves, 0, newMoves, 0, moves.length);
        newMoves[moves.length] = (byte) c;
        moves = newMoves;
    }

    private void finaliseTraps(){
        for (int i = 0; i < traps.length(); i++){
            if (layerBG[trapConnections[i][0]] != BUTTON_BROWN && layerBG[trapConnections[i][1]] != TRAP){
                traps.set(i, false);
            }
        }
    }

    private void initialiseSlidingMonsters(){
        for (Creature m : monsterList.list) m.setSliding(false);
        for (Creature m : slipList) m.setSliding(true);
    }

    private static char longChar(char c){
        if (c == '-') return '_';
        return Character.toUpperCase(c);
    }

    private int numTicks(char newMove){
        int t = 0;
        for (byte c : moves){
            if (Character.isUpperCase(c)) t += 2;
            else t++;
        }
        if (Character.isUpperCase(newMove)) t += 2;
        else t++;
        return t;
    }

    // return: did it tick twice?
    public boolean tick(char c, int direction, int click){

        initialiseSlidingMonsters();

        tickN = numTicks(c);
        boolean isHalfMove = tickN % 2 == 0;
        
        if (click >= 0) mouseClick = click;
        boolean isKey = isKeyMovement(c);

        if (tickN > 2){
            tickTimer(1);
            if (!isHalfMove) monsterList.tick();
        }
        
        if (chip.isDead()) return false;
        
        if (!isKey && !chip.isDead()) moveChipClick();
        if (chip.isSliding()) moveChipSliding();
        if (chip.isSliding() && !layerBG[chip.getPosition()].isFF()) c = '-';
        slipList.tick();
        if (isKey && !chip.isDead()) moveChipVoluntary(direction);

        monsterList.finalise();
        finaliseTraps();

        if (!isHalfMove && c != '-' && c != '?' && !chip.isSliding()){
            tick(longChar(c), (short) -1, -1);
            return true;
        }
        addMove(c);
        return false;
    }

}
