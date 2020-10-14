package game.MS;

import game.*;

import static game.CreatureID.*;
import static game.Tile.*;

public class MSCreatureList extends game.CreatureList {
    private int numDeadMonsters;

    @Override
    public void initialise() {
        newClones.clear();
        numDeadMonsters = 0;
        teethStep = (level.getStep().isEven()) != (level.getTickNumber() % 4 == 2);
    }

    @Override
    public void tick(){

        direction = null;
        for(Creature m : list){

            MSCreature monster = (MSCreature) m;

            if (monster.getCreatureType().isBlock()){
                numDeadMonsters++;
                continue;
            }
            if (!teethStep && (monster.getCreatureType() == TEETH || monster.getCreatureType() == BLOB)){
                continue;
            }

            if (!monster.isSliding()){
                if (monster.getNextMoveDirectionCheat() != null) direction = monster.getNextMoveDirectionCheat();
                else if (!monster.getCreatureType().isAffectedByCB()) direction = monster.getDirection();
                Tile bgTile = level.getLayerBG().get(monster.getPosition());
                if (bgTile == CLONE_MACHINE) tickClonedMonster(monster);
                else if (bgTile == TRAP) tickTrappedMonster(monster);
                else tickFreeMonster(monster);
            }
        }
    }

    private void tickClonedMonster(MSCreature monster){
        Position clonerPosition = monster.getPosition().clone();
        Tile tile = monster.toTile();
        if (monster.getCreatureType().isBlock()) tile = Tile.fromOrdinal(BLOCK_UP.ordinal() + monster.getDirection().ordinal());
        if (!monster.getCreatureType().isAffectedByCB() && monster.getCreatureType() != CreatureID.ICE_BLOCK) direction = monster.getDirection();
        if (direction == null) return;
        if (monster.getCreatureType() == BLOB){
            Position p = monster.getPosition().clone();
            Direction[] directions = monster.getDirectionPriority(level.getChip(), level.getRNG());
            monster.tick(directions, false);
            if (!monster.getPosition().equals(p)) level.insertTile(clonerPosition, tile);
        }
        else if (monster.canEnter(direction, level.getLayerFG().get(monster.getPosition().move(direction)))){
            if (monster.tick(new Direction[] {direction}, false)) level.insertTile(clonerPosition, tile);

            if (monster.getCreatureType() == CreatureID.BLOCK && level.getLayerBG().get(clonerPosition) != CLONE_MACHINE) {
                level.popTile(clonerPosition);
            }
        }
    }

    private void tickTrappedMonster(MSCreature monster){
        if (!monster.getCreatureType().isAffectedByCB()) direction = monster.getDirection();
        if (direction == null) return;
        if (monster.getCreatureType() == TANK_STATIONARY) monster.setCreatureType(TANK_MOVING);
        if (monster.getCreatureType() == BLOB){
            Direction[] directions = monster.getDirectionPriority(level.getChip(), level.getRNG());
            monster.tick(directions, false);
        }
        else monster.tick(new Direction[] {direction}, false);
    }

    private void tickFreeMonster(MSCreature monster){
        Direction[] directionPriorities = monster.getDirectionPriority(level.getChip(), level.getRNG());
        boolean success = monster.tick(directionPriorities, false);
        if (!success && monster.getCreatureType() == TEETH && !monster.isSliding()){
            monster.setDirection(directionPriorities[0]);
            direction = directionPriorities[0];
            level.getLayerFG().set(monster.getPosition(), monster.toTile());
        }
    }

    public void incrementDeadMonsters() {
        numDeadMonsters++;
    }

    @Override
    public void addClone(Position position){

        for (Creature c: list){
            if (c.getPosition().equals(position)) return;
        }
        for (Creature c: newClones){
            if (c.getPosition().equals(position)) return;
        }

        MSCreature clone;

        //Data resetting
        if (position.y == 32) {
            Position row0Position = new Position(position.x, 0);
            if (level.getLayerBG().get(row0Position).isCreature()) {
                clone = new MSCreature(new Position(position.x, 32), level.getLayerBG().get(row0Position));
                Tile newTile = level.getLayerFG().get(new Position(position.x, 31));
                if (clone.getDirection() == Direction.UP && clone.canEnter(clone.getDirection(), newTile)) {
                    System.out.println("fug");
                    ((MSLevel) level).resetData(row0Position.x);
                }
                direction = clone.getDirection();
            }
            else
                return;
        }

        else {
                Tile tile = level.getLayerFG().get(position);
                Tile tilebg = level.getLayerBG().get(position);
                if (!tile.isCreature() ^ (tile == Tile.ICE_BLOCK && tilebg.isCloneBlock()))
                    return;
                clone = new MSCreature(position, tile);
                if (tile == Tile.ICE_BLOCK)
                    direction = Direction.fromOrdinal((tilebg.ordinal() + 2) % 4);
                else
                    direction = clone.getDirection();
            }

            Position newPosition = clone.getPosition().move(direction);
            Tile newTile = level.getLayerFG().get(newPosition);

            if (clone.canEnter(direction, newTile) || newTile == clone.toTile()) {
                if (clone.getCreatureType().isBlock()) tickClonedMonster(clone);
                else newClones.add(clone);

                if (clone.getCreatureType() == CreatureID.ICE_BLOCK) level.getLayerFG().set(position, Tile.ICE_BLOCK);
        }
    }

    @Override
    public void springTrappedCreature(Position position) { //ideally shouldn't be used as this will mess up monster order stuff for ms
        if (level.getLayerBG().get(position) != TRAP || creatureAt(position) == null
        || !level.isTrapOpen(position))
            return;

        tickTrappedMonster((MSCreature) creatureAt(position));
    }

    @Override
    public boolean tickCreature(Creature creature, Direction direction) {
        for (Creature c : list) {
            if (c == creature)
                return creature.tick(direction);
        }
        return false;
    }

    @Override
    public void finalise(){

        if (numDeadMonsters == 0 && newClones.size() == 0) return;

        int length = list.length - numDeadMonsters + newClones.size();
        Creature[] newMonsterList = new MSCreature[length];

        // Re-add everything except dead monsters. Non-sliding blocks count as dead.
        int index = 0;
        for (Creature m : list){
            MSCreature monster = (MSCreature) m;
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

    @Override
    public int numCreaturesAt(Position position) {
        if (!position.isValid())
            return 0;
        if (level.getLayerFG().get(position).isCreature() || level.getLayerFG().get(position).isChip())
            return 1;
        return 0;
    }

    @Override
    public Creature animationAt(Position position) {
        return null; //only useful for Lynx-like rulesets
    }

    public MSCreatureList(MSCreature[] monsters, Layer layerFG, Layer layerBG){
        super(monsters);
    }

}
