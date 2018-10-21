package game;

import java.util.BitSet;

import static game.Tile.*;

/**
 * The monster list. The list attribute is the actual list.
 */
public class CreatureList{

    private Level level;

    public Creature[] list;
    int numDeadMonsters;
    private int numClones;
    private Creature[] newClones;
    private int direction;
    boolean blobStep;

    void tick(){

        blobStep = (level.getStep() == Step.EVEN) != (level.tickN % 4 == 3);

        direction = -1;
        for(Creature monster : list){

            if (monster.isBlock()){
                numDeadMonsters++;
                continue;
            }
            if (!blobStep && (monster.getMonsterType() == Creature.TEETH || monster.getMonsterType() == Creature.BLOB)){
                continue;
            }

            if (!monster.isSliding()){
                if (!monster.isAffectedByCB()) direction = monster.getDirection();
                Tile bgTile = level.layerBG[monster.getPosition()];
                if (bgTile == CLONE_MACHINE) tickClonedMonster(monster);
                else if (bgTile == TRAP) tickTrappedMonster(monster);
                else tickFreeMonster(monster);
            }

            direction = monster.getDirection();
        }
    }

    private void tickClonedMonster(Creature monster){
        int clonerPosition = monster.getPosition();
        Tile tile = monster.toTile();
        if (monster.isBlock()) tile = Tile.fromOrdinal(BLOCK_UP.ordinal() + (monster.getDirection() >>> 14));
        if (!monster.isAffectedByCB()) direction = monster.getDirection();
        if (monster.canEnter(direction, level.layerFG[Creature.moveInDirection(monster.getPosition(), direction)], level)){
            monster.tick(new int[] {direction}, level);
            level.insertTile(clonerPosition, tile);
        }
    }

    private void tickTrappedMonster(Creature monster){
        if (!monster.isAffectedByCB()) direction = monster.getDirection();
        if (monster.getMonsterType() == Creature.TANK_STATIONARY) monster.setMonsterType(Creature.TANK_MOVING);
        monster.tick(new int[] {direction}, level);
    }

    private void tickFreeMonster(Creature monster){
        int[] directionPriorities = monster.getDirectionPriority(level.chip.getPosition(), level.rng);
        monster.tick(directionPriorities, level);
    }

    void addClone(int position){

        for (Creature c: list){
            if (c.getPosition() == position) return;
        }
        Tile tile = level.layerFG[position];
        if (!tile.isCreature()) return;
        Creature clone = new Creature(position, tile);
        direction = clone.getDirection();
        int newPosition = Creature.moveInDirection(clone.getPosition(), direction);
        Tile newTile = level.layerFG[newPosition];

        if (clone.canEnter(direction, level.layerFG[newPosition], level) || newTile == clone.toTile()){
            if (clone.isBlock()) tickClonedMonster(clone);
            else newClones[numClones++] = clone;
        }

    }

    void finalise(){

        int length = list.length - numDeadMonsters + numClones;
        Creature[] newMonsterList = new Creature[length];

        // Re-add everything except dead monsters. Non-sliding blocks count as dead.
        int index = 0;
        for (Creature monster : list){
            if (!monster.isDead() && !(monster.isBlock() && !monster.isSliding())) newMonsterList[index++] = monster;
        }

        // Add all cloned monsters
        for (int i = 0; i < numClones; i++){
            newMonsterList[index++] = newClones[i];
        }

        list = newMonsterList;
        numClones = 0;
        numDeadMonsters = 0;

    }

    void setLevel(Level level){
        this.level = level;
        newClones = new Creature[this.level.cloneConnections.length];
    }

    public CreatureList(Creature[] monsters){
        list = monsters;
        numClones = 0;
        numDeadMonsters = 0;
    }

}
