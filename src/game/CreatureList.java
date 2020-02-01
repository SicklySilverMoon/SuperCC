package game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import game.button.*;

import static game.CreatureID.*;
import static game.Tile.*;

/**
 * The monster list. The list attribute is the actual list.
 */
public class CreatureList implements Iterable<Creature> {
    
    private Level level;

    private Creature[] list;
    int numDeadMonsters;
    private List<Creature> newClones;
    public static Direction direction;
    private boolean blobStep;
    
    public Creature creatureAt(Position position){
        for (Creature c : list) if (c.getPosition().equals(position)) return c;
        return null;
    }
    
    public int size() {
        return list.length;
    }
    
    public Creature get(int i) {
        return list[i];
    }
    
    public Creature[] getCreatures() {
        return list;
    }
    
    public void setCreatures(Creature[] creatures) {
        list = creatures;
    }

    public List<Creature> getNewClones() {
        return newClones;
    }
    
    void initialise() {
        newClones.clear();
        numDeadMonsters = 0;
        blobStep = (level.getStep() == Step.EVEN) != (level.tickNumber % 4 == 2);
    }

    void tick(){

        direction = null;
        for(Creature monster : list){

            if (monster.getCreatureType().isBlock()){
                numDeadMonsters++;
                continue;
            }
            if (!blobStep && (monster.getCreatureType() == TEETH || monster.getCreatureType() == BLOB)){
                continue;
            }

            if (!monster.isSliding()){
                if (monster.getNextMoveDirectionCheat() != null) direction = monster.getNextMoveDirectionCheat();
                else if (!monster.getCreatureType().isAffectedByCB()) direction = monster.getDirection();
                Tile bgTile = level.layerBG.get(monster.getPosition());
                if (bgTile == CLONE_MACHINE) tickClonedMonster(monster);
                else if (bgTile == TRAP) tickTrappedMonster(monster);
                else tickFreeMonster(monster);
            }
        }
    }

    private void tickClonedMonster(Creature monster){
        Position clonerPosition = monster.getPosition().clone();
        Tile tile = monster.toTile();
        if (monster.getCreatureType().isBlock()) tile = Tile.fromOrdinal(BLOCK_UP.ordinal() + monster.getDirection().ordinal());
        if (!monster.getCreatureType().isAffectedByCB() && !monster.getCreatureType().isIceBlock()) direction = monster.getDirection();
        if (direction == null) return;
        if (monster.getCreatureType() == BLOB){
            Position p = monster.getPosition().clone();
            Direction[] directions = monster.getDirectionPriority(level.getChip(), level.rng);
            monster.tick(directions, level, false);
            if (!monster.getPosition().equals(p)) level.insertTile(clonerPosition, tile);
        }
        else if (monster.canEnter(direction, level.layerFG.get(monster.getPosition().move(direction)), level)){
            if (monster.tick(new Direction[] {direction}, level, false)) level.insertTile(clonerPosition, tile);

            if (monster.getCreatureType().isDirtBlock() && level.getLayerBG().get(clonerPosition) != CLONE_MACHINE) {
                level.popTile(clonerPosition);
            }
        }
    }

    private void tickTrappedMonster(Creature monster){
        if (!monster.getCreatureType().isAffectedByCB()) direction = monster.getDirection();
        if (direction == null) return;
        if (monster.getCreatureType() == TANK_STATIONARY) monster.setCreatureType(TANK_MOVING);
        if (monster.getCreatureType() == BLOB){
            Direction[] directions = monster.getDirectionPriority(level.getChip(), level.rng);
            monster.tick(directions, level, false);
        }
        else monster.tick(new Direction[] {direction}, level, false);
    }

    private void tickFreeMonster(Creature monster){
        Direction[] directionPriorities = monster.getDirectionPriority(level.getChip(), level.rng);
        boolean success = monster.tick(directionPriorities, level, false);
        if (!success && monster.getCreatureType() == TEETH && !monster.isSliding()){
            monster.setDirection(directionPriorities[0]);
            direction = directionPriorities[0];
            level.layerFG.set(monster.getPosition(), monster.toTile());
        }
    }

    public void addClone(Position position){

        for (Creature c: list){
            if (c.getPosition().equals(position)) return;
        }
        for (Creature c: newClones){
            if (c.getPosition().equals(position)) return;
        }

        //Data resetting right here
        if (position.y == 32) { //If the clone button's Y target is row 32 take over from normal code
            Position row0Position = new Position(position.x, 0);
            if (level.getLayerBG().get(row0Position).isCreature()) { //if the background (buried) layer is a creature
                Creature resetClone = new Creature(row0Position, level.layerBG.get(row0Position)); //Create a new variable for the creature
                if (resetClone.getDirection()==Direction.UP) { //If the creature is facing up
                    Position row31Position = new Position(position.x, 31); //Create a new variable for the creature's position
                    Tile resetNewTile = level.layerFG.get(row31Position); //Makes it so that the next section checks X, 31 and not X, 0
                    if (resetClone.canEnter(direction, resetNewTile, level)) { //If the creature can clone to X, 31
                        Tile tile = resetClone.toTile(); //Needed to not cause tile erasure
                        resetClone.setPosition(row31Position); //Sets the clone's position to be on row 31
                        boolean SpecialTileInteraction = false;
                        if (level.getChip().getPosition().equals(row31Position)|| resetNewTile.isSwimmingChip()) { //Swimming Chip is now checked along side normal Chip
                            level.chip.kill();
                        }
                        if (resetNewTile.isButton()) { //Buttons now push properly
                            Button button = null; //The IDE screams at me if i don't do this
                            if (resetNewTile == BUTTON_GREEN) button = level.getButton(row31Position, GreenButton.class); //This is... disturbing in its inefficiency
                            if (resetNewTile == BUTTON_RED) button = level.getButton(row31Position, RedButton.class);
                            if (resetNewTile == BUTTON_BROWN) button = level.getButton(row31Position, BrownButton.class);
                            if (resetNewTile == BUTTON_BLUE) button = level.getButton(row31Position, BlueButton.class);
                            if (button != null) {
                                button.press(level); //If this thing gives you warnings about null pointers, ignore it, i handle all 4 button types
                            }
                        }
                        if (resetNewTile == WATER || resetNewTile == BOMB || resetNewTile == FIRE) { //Special interactions
                            if (resetClone.getCreatureType().isBlock() && resetNewTile == WATER) level.layerFG.set(row31Position, DIRT);
                            if (resetNewTile == BOMB) level.layerFG.set(row31Position, FLOOR);
                            SpecialTileInteraction = true; //Literally just so you don't add dead monsters to lists they shouldn't be in
                        }
                        else level.insertTile(row31Position, tile); //Clones them
                        if (level.getLayerBG().get(row31Position).isSliding()) { //Bunch of stuff to make things slide correctly
                            resetClone.setSliding(true);
                            resetClone.tick(new Direction[]{Direction.DOWN}, level, false); //Some fancy stuff to actually make them slide
                        } //Fun fact: not having else here causes a crash when a sliding creature steps off a sliding force floor and hits a resetclone button the same turn a normal clone button is hit, BUT only if that's the first normal button hit. However the game not adding resetclones that started on sliding tiles to the monster list is a bigger issue
                        if (!SpecialTileInteraction && !(resetClone.getCreatureType().isBlock())) newClones.add(resetClone); //the above error is caused by accidentally adding blocks to the monsterlist, if you handle it so that doesn't happen there's no error
                        level.ResetData(row0Position, level); //passes the position of the reset to a new method to handle data resets
                    }
                }
            }
        }

        else {
            Tile tile = level.layerFG.get(position);
            Tile tilebg = level.layerBG.get(position);
            if (!tile.isCreature() ^ (tile == Tile.ICE_BLOCK && tilebg.isCloneBlock())) return;
            Creature clone = new Creature(position, tile);
            if (tile == Tile.ICE_BLOCK) direction = Direction.fromOrdinal((tilebg.ordinal() + 2) % 4);
            else direction = clone.getDirection();


            Position newPosition = clone.getPosition().move(direction);
            Tile newTile = level.layerFG.get(newPosition);

            if (clone.canEnter(direction, newTile, level) || newTile == clone.toTile()) {
                if (clone.getCreatureType().isBlock()) tickClonedMonster(clone);
                else newClones.add(clone);

                if (clone.getCreatureType().isIceBlock()) level.layerFG.set(position, Tile.ICE_BLOCK);
            }
        }
    }

    void finalise(){
        
        if (numDeadMonsters == 0 && newClones.size() == 0) return;

        int length = list.length - numDeadMonsters + newClones.size();
        Creature[] newMonsterList = new Creature[length];

        // Re-add everything except dead monsters. Non-sliding blocks count as dead.
        int index = 0;
        for (Creature monster : list){
            if (!monster.isDead() && !(monster.getCreatureType().isBlock() && !monster.isSliding())) newMonsterList[index++] = monster;
        }

        // Add all cloned monsters
        for (Creature clone : newClones){
            newMonsterList[index++] = clone;
        }

        list = newMonsterList;
        newClones.clear();
        numDeadMonsters = 0;

    }

    void setLevel(Level level){
        this.level = level;
        newClones = new ArrayList<>();
    }

    public CreatureList(Creature[] monsters){
        list = monsters;
        numDeadMonsters = 0;
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.length; i++){
            sb.append(i+1);
            sb.append('\t');
            sb.append(list[i]);
            sb.append('\n');
        }
        return sb.toString();
    }
    
    @Override
    public Iterator<Creature> iterator() {
        return new Iterator<Creature>() {
            
            private int i = 0;
            
            @Override
            public boolean hasNext() {
                return i < list.length;
            }
    
            @Override
            public Creature next() {
                return list[i++];
            }
        };
    }
    
    @Override
    public void forEach(Consumer<? super Creature> action) {
        for (Creature c : list) action.accept(c);
    }
    
    @Override
    public Spliterator<Creature> spliterator() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
