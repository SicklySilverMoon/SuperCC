package game;

import java.util.ArrayList;
import java.util.List;

import static game.Creature.TEETH;
import static game.Tile.*;

/**
 * The monster list. The list attribute is the actual list.
 */
public class CreatureList{

    private static int NO_DIRECTION = -1;
    
    private Level level;

    public Creature[] list;
    int numDeadMonsters;
    private List<Creature> newClones;
    public static int direction;
    boolean blobStep;
    
    public Creature creatureAt(Position position){
        int index = position.getIndex();
        for (Creature c : list) if (c.getIndex() == index) return c;
        return null;
    }

    void tick(){

        blobStep = (level.getStep() == Step.EVEN) != (level.tickNumber % 4 == 3);

        direction = NO_DIRECTION;
        for(Creature monster : list){

            if (monster.isBlock()){
                numDeadMonsters++;
                continue;
            }
            if (!blobStep && (monster.getMonsterType() == TEETH || monster.getMonsterType() == Creature.BLOB)){
                continue;
            }

            if (!monster.isSliding()){
                if (!monster.isAffectedByCB()) direction = monster.getDirection();
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
        if (monster.isBlock()) tile = Tile.fromOrdinal(BLOCK_UP.ordinal() + (monster.getDirection() >>> 14));
        if (!monster.isAffectedByCB()) direction = monster.getDirection();
        if (direction == NO_DIRECTION) return;
        if (monster.getMonsterType() == Creature.BLOB){
            Position p = monster.getPosition().clone();
            int[] directions = monster.getDirectionPriority(level.getChip(), level.rng);
            monster.tick(directions, level, false);
            if (!monster.getPosition().equals(p)) level.insertTile(clonerPosition, tile);
        }
        else if (monster.canEnter(direction, level.layerFG.get(monster.move(direction)), level)){
            if (monster.tick(new int[] {direction}, level, false)) level.insertTile(clonerPosition, tile);
        }
    }

    private void tickTrappedMonster(Creature monster){
        if (!monster.isAffectedByCB()) direction = monster.getDirection();
        if (direction == NO_DIRECTION) return;
        if (monster.getMonsterType() == Creature.TANK_STATIONARY) monster.setMonsterType(Creature.TANK_MOVING);
        if (monster.getMonsterType() == Creature.BLOB){
            int[] directions = monster.getDirectionPriority(level.getChip(), level.rng);
            monster.tick(directions, level, false);
        }
        else monster.tick(new int[] {direction}, level, false);
    }

    private void tickFreeMonster(Creature monster){
        int[] directionPriorities = monster.getDirectionPriority(level.getChip(), level.rng);
        boolean success = monster.tick(directionPriorities, level, false);
        if (!success && monster.getMonsterType() == TEETH && !monster.isSliding()){
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
        Position newPosition = clone.move(direction);
        Tile newTile = level.layerFG.get(newPosition);

        if (clone.canEnter(direction, newTile, level) || newTile == clone.toTile()){
            if (clone.isBlock()) tickClonedMonster(clone);
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
            if (!monster.isDead() && !(monster.isBlock() && !monster.isSliding())) newMonsterList[index++] = monster;
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
            if (!list[i].isDead()) sb.append(list[i]);
            sb.append('\n');
        }
        return sb.toString();
    }

}
