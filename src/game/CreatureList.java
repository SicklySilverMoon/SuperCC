package game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

import static game.CreatureID.*;
import static game.Tile.*;

/**
 * The monster list. The list attribute is the actual list.
 */
public class CreatureList implements Iterable<Creature> {
    
    private Level level;

    public Creature[] list;
    int numDeadMonsters;
    private List<Creature> newClones;
    public static Direction direction;
    boolean blobStep;
    
    public Creature creatureAt(Position position){
        for (Creature c : list) if (c.getPosition().equals(position)) return c;
        return null;
    }

    void tick(){

        blobStep = (level.getStep() == Step.EVEN) != (level.tickNumber % 4 == 3);

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
                if (!monster.getCreatureType().isAffectedByCB()) direction = monster.getDirection();
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
        if (!monster.getCreatureType().isAffectedByCB()) direction = monster.getDirection();
        if (direction == null) return;
        if (monster.getCreatureType() == BLOB){
            Position p = monster.getPosition().clone();
            Direction[] directions = monster.getDirectionPriority(level.getChip(), level.rng);
            monster.tick(directions, level, false);
            if (!monster.getPosition().equals(p)) level.insertTile(clonerPosition, tile);
        }
        else if (monster.canEnter(direction, level.layerFG.get(monster.getPosition().move(direction)), level)){
            if (monster.tick(new Direction[] {direction}, level, false)) level.insertTile(clonerPosition, tile);
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
        Tile tile = level.layerFG.get(position);
        if (!tile.isCreature()) return;
        Creature clone = new Creature(position, tile);
        direction = clone.getDirection();
        Position newPosition = clone.getPosition().move(direction);
        Tile newTile = level.layerFG.get(newPosition);

        if (clone.canEnter(direction, newTile, level) || newTile == clone.toTile()){
            if (clone.getCreatureType().isBlock()) tickClonedMonster(clone);
            else newClones.add(clone);
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
