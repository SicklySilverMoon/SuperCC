package game.MS;

import game.*;
import game.button.*;

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
        if (!monster.getCreatureType().isAffectedByCB() && !monster.getCreatureType().isIceBlock()) direction = monster.getDirection();
        if (direction == null) return;
        if (monster.getCreatureType() == BLOB){
            Position p = monster.getPosition().clone();
            Direction[] directions = monster.getDirectionPriority(level.getChip(), level.getRNG());
            monster.tick(directions, false);
            if (!monster.getPosition().equals(p)) level.insertTile(clonerPosition, tile);
        }
        else if (monster.canEnter(direction, level.getLayerFG().get(monster.getPosition().move(direction)))){
            if (monster.tick(new Direction[] {direction}, false)) level.insertTile(clonerPosition, tile);

            if (monster.getCreatureType().isDirtBlock() && level.getLayerBG().get(clonerPosition) != CLONE_MACHINE) {
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

        //Data resetting right here
        if (position.y == 32) { //If the clone button's Y target is row 32 take over from normal code
            Position row0Position = new Position(position.x, 0);
            if (level.getLayerBG().get(row0Position).isCreature()) { //if the background (buried) layer is a creature
                MSCreature resetClone = new MSCreature(row0Position, level.getLayerBG().get(row0Position)); //Create a new variable for the creature
                if (resetClone.getDirection()==Direction.UP) { //If the creature is facing up
                    Position row31Position = new Position(position.x, 31); //Create a new variable for the creature's position
                    Tile resetNewTile = level.getLayerFG().get(row31Position); //Makes it so that the next section checks X, 31 and not X, 0
                    if (resetClone.canEnter(direction, resetNewTile)) { //If the creature can clone to X, 31
                        Tile tile = resetClone.toTile(); //Needed to not cause tile erasure
                        resetClone.setPosition(row31Position); //Sets the clone's position to be on row 31
                        boolean SpecialTileInteraction = false;
                        if (level.getChip().getPosition().equals(row31Position)|| resetNewTile.isChip() || resetNewTile.isSwimmingChip()) { //Swimming Chip is now checked along side normal Chip
                            level.getChip().kill();
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
                            if (resetClone.getCreatureType().isBlock() && resetNewTile == WATER) level.getLayerFG().set(row31Position, DIRT);
                            if (resetNewTile == BOMB) level.getLayerFG().set(row31Position, FLOOR);
                            SpecialTileInteraction = true; //Literally just so you don't add dead monsters to lists they shouldn't be in
                        }
                        else level.insertTile(row31Position, tile); //Clones them
                        if (level.getLayerBG().get(row31Position).isSliding()) { //Bunch of stuff to make things slide correctly
                            resetClone.setSliding(true);
                            resetClone.tick(new Direction[]{Direction.DOWN}, false); //Some fancy stuff to actually make them slide
                        } //Fun fact: not having else here causes a crash when a sliding creature steps off a sliding force floor and hits a resetclone button the same turn a normal clone button is hit, BUT only if that's the first normal button hit. However the game not adding resetclones that started on sliding tiles to the monster list is a bigger issue
                        if (!SpecialTileInteraction && !(resetClone.getCreatureType().isBlock())) newClones.add(resetClone); //the above error is caused by accidentally adding blocks to the monsterlist, if you handle it so that doesn't happen there's no error
                        ((MSLevel) level).ResetData(row0Position); //passes the position of the reset to a new method to handle data resets
                    }
                }
            }
        }

        else {
            Tile tile = level.getLayerFG().get(position);
            Tile tilebg = level.getLayerBG().get(position);
            if (!tile.isCreature() ^ (tile == Tile.ICE_BLOCK && tilebg.isCloneBlock())) return;
            MSCreature clone = new MSCreature(position, tile);
            if (tile == Tile.ICE_BLOCK) direction = Direction.fromOrdinal((tilebg.ordinal() + 2) % 4);
            else direction = clone.getDirection();


            Position newPosition = clone.getPosition().move(direction);
            Tile newTile = level.getLayerFG().get(newPosition);

            if (clone.canEnter(direction, newTile) || newTile == clone.toTile()) {
                if (clone.getCreatureType().isBlock()) tickClonedMonster(clone);
                else newClones.add(clone);

                if (clone.getCreatureType().isIceBlock()) level.getLayerFG().set(position, Tile.ICE_BLOCK);
            }
        }
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
    public int getCreaturesAtPosition(Position position) {
        if (!position.isValid())
            return -1;
        if (level.getLayerFG().get(position).isCreature() || level.getLayerFG().get(position).isChip())
            return 1;
        return 0;
    }

    public MSCreatureList(MSCreature[] monsters){
        super(monsters);
    }

}
