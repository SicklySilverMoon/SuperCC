package game.MS;

import game.*;
import game.button.*;

import java.util.LinkedList;
import java.util.List;

import static game.CreatureID.BLOCK;
import static game.CreatureID.*;
import static game.CreatureID.CHIP;
import static game.Direction.*;
import static game.Tile.*;

public class MSCreature extends Creature {

    private Direction nextMoveDirectionCheat = null;

    // Direction-related methods
    protected void setPosition(Position position){ //So you can make a creature teleport 32 tiles at once to data reset properly
        this.position = position;
    }

    @Override
    public Direction[] getDirectionPriority(Creature chip, RNG rng){
        if (nextMoveDirectionCheat != null) {
            Direction[] directions = new Direction[] {nextMoveDirectionCheat};
            nextMoveDirectionCheat = null;
            if (creatureType == WALKER || creatureType == BLOB) rng.random4();
            return directions;
        }
        if (isSliding()) return direction.turn(new Direction[] {TURN_FORWARD, TURN_AROUND});
        switch (creatureType){
            case BUG: return direction.turn(new Direction[] {TURN_LEFT, TURN_FORWARD, TURN_RIGHT, TURN_AROUND});
            case FIREBALL: return direction.turn(new Direction[] {TURN_FORWARD, TURN_RIGHT, TURN_LEFT, TURN_AROUND});
            case PINK_BALL: return direction.turn(new Direction[] {TURN_FORWARD, TURN_AROUND});
            case TANK_STATIONARY: return new Direction[] {};
            case GLIDER: return direction.turn(new Direction[] {TURN_FORWARD, TURN_LEFT, TURN_RIGHT, TURN_AROUND});
            case TEETH: return position.seek(chip.getPosition());
            case WALKER:
                Direction[] directions = new Direction[] {TURN_LEFT, TURN_AROUND, TURN_RIGHT};
                rng.randomPermutation3(directions);
                return direction.turn(new Direction[] {TURN_FORWARD, directions[0], directions[1], directions[2]});
            case BLOB:
                directions = new Direction[] {TURN_FORWARD, TURN_LEFT, TURN_AROUND, TURN_RIGHT};
                rng.randomPermutation4(directions);
                return direction.turn(directions);
            case PARAMECIUM: return direction.turn(new Direction[] {TURN_RIGHT, TURN_FORWARD, TURN_LEFT, TURN_AROUND});
            case TANK_MOVING: return new Direction[] {getDirection()};
            default: return new Direction[] {};
        }
    }
    public Direction[] seek(Position position){
        return this.position.seek(position);
    }

    @Override
    protected Direction applySlidingTile(Direction direction, Tile tile, RNG rng){
        switch (tile){
            case FF_DOWN:
                return DOWN;
            case FF_UP:
                return UP;
            case FF_RIGHT:
                return RIGHT;
            case FF_LEFT:
                return LEFT;
            case FF_RANDOM:
                return Direction.fromOrdinal(rng.random4());
            case ICE_SLIDE_SOUTHEAST:
                if (direction == UP) return RIGHT;
                else if (direction == LEFT) return DOWN;
                else return direction;
            case ICE_SLIDE_NORTHEAST:
                if (direction == DOWN) return RIGHT;
                else if (direction == LEFT) return UP;
                else return direction;
            case ICE_SLIDE_NORTHWEST:
                if (direction == DOWN) return LEFT;
                else if (direction == RIGHT) return UP;
                else return direction;
            case ICE_SLIDE_SOUTHWEST:
                if (direction == UP) return LEFT;
                else if (direction == RIGHT) return DOWN;
                else return direction;
            case TRAP:
                return direction;
        }
        return direction;
    }
    Direction[] getSlideDirectionPriority(Tile tile, RNG rng, boolean changeOnRFF){
        if (nextMoveDirectionCheat != null) {
            Direction[] directions = new Direction[] {nextMoveDirectionCheat};
            nextMoveDirectionCheat = null;
            return directions;
        }
        if (tile.isIce() || (creatureType.isChip() && tile == TELEPORT)){
            Direction[] directions = direction.turn(new Direction[] {TURN_FORWARD, TURN_AROUND});
            directions[0] = applySlidingTile(directions[0], tile, rng);
            directions[1] = applySlidingTile(directions[1], tile, rng);
            return directions;
        }
        else if (tile == TELEPORT) return new Direction[] {direction};
        else if (tile == FF_RANDOM && !changeOnRFF) return new Direction[] {direction};
        else return new Direction[] {applySlidingTile(getDirection(), tile, rng)};
    }

    public Direction getNextMoveDirectionCheat() {
        return nextMoveDirectionCheat;
    }
    
    public void setNextMoveDirectionCheat(Direction nextMoveDirectionCheat) {
        this.nextMoveDirectionCheat = nextMoveDirectionCheat;
    }

    // Sliding-related functions

    @Override
    public boolean isSliding() {
        return creatureType == CHIP_SLIDING || sliding;
    }

    public void setSliding(boolean wasSliding, boolean isSliding) {
        setSliding(wasSliding, isSliding, false);
    }

    // The entered parameter is for checking if a block has just slid into a trap since in this case it shouldn't generate slide delay
    public void setSliding(boolean wasSliding, boolean isSliding, boolean entered) {
        if(!(wasSliding || isSliding)) {
            return;
        }
        MSLevel level = (MSLevel) Creature.level;
        if (wasSliding && !isSliding){
            if (!isDead() && creatureType.isChip()) setCreatureType(CHIP);
            else if(!(creatureType.isBlock() && level.getLayerBG().get(position) == TRAP)) level.slipList.remove(this);
            // Handles block colliding on trap
            else if (creatureType.isBlock() && level.getLayerBG().get(position) == TRAP && canLeave(direction, level.getLayerBG().get(position))) {
                level.slipList.remove(this);
                level.slipList.add(this);
            }
        }
        else if (!wasSliding && isSliding){
            if (creatureType.isChip()) setCreatureType(CHIP_SLIDING);
            else if (level.getSlipList().contains(this)) {
                new RuntimeException("adding block twice on level "+level.getLevelNumber()+" "+new String(level.getTitle())).printStackTrace();
            }
            else {
                level.getSlipList().add(this);
                if (!creatureType.isBlock()) {
                    direction = applySlidingTile(direction, level.getLayerBG().get(position), level.rng); //When a creature first enters a sliding tile its direction is updated to face whatever direction its going to move next after that tile takes effect
                    monsterList.direction = this.getDirection();
                }
            }
        }
        if (creatureType.isBlock() && isSliding && level.getLayerBG().get(position) == TRAP
            && (!canLeave(direction, level.getLayerBG().get(position))) && !entered){
            level.slipList.remove(this);
            level.slipList.add(this);
            this.sliding = true;
            return;
        }
        if (creatureType.isBlock() && wasSliding && level.getLayerBG().get(position) == TRAP && canLeave(direction, level.getLayerBG().get(position)))
            this.sliding = true; //This prevents errors about adding a block to the sliplist twice
        else this.sliding = isSliding;
    }

    /**
     * Get the Tile representation of this monster.
     * @return The Tile representation of this monster.
     */
    @Override
    public Tile toTile(){
        switch (creatureType){
            case BLOCK: return Tile.BLOCK;
            case ICE_BLOCK: return Tile.ICE_BLOCK;
            case CHIP_SLIDING: return Tile.fromOrdinal(Tile.CHIP_UP.ordinal() | direction.ordinal());
            case TANK_STATIONARY: return Tile.fromOrdinal(TANK_UP.ordinal() | direction.ordinal());
            default: return Tile.fromOrdinal((creatureType.ordinal() << 2) + 0x40 | direction.ordinal());
        }
    }

    @Override
    public int getTimeTraveled() {
        return 0; //See javadocs for this in Level
    }

    private void teleport(Direction direction, Position position, List<Button> pressedButtons) {
        Position chipPosition = level.getChip().getPosition();
        if (creatureType.isChip()) level.popTile(chipPosition);
        int portalIndex;
        for (portalIndex = 0; true; portalIndex++){
            if (portalIndex >= level.getTeleports().length) return;
            if (level.getTeleports()[portalIndex].equals(position)){
                break;
            }
        }
        int l = level.getTeleports().length;
        int i = portalIndex;
        do{
            i--;
            if (i < 0) i += l;
            position.setIndex(level.getTeleports()[i].getIndex());
            if (level.getLayerFG().get(position) != TELEPORT) {
                if (!creatureType.isChip()) { //Allows monsters  to still partial post off themselves
                    continue;
                } //Yeah weirdly monsters can partial post off themselves but Chip can't
                else {
                    if (!level.getLayerFG().get(position).isChip()) continue; //So Chip doesn't partial post off himself
                }
            }
            Position exitPosition = position.move(direction);
            if (exitPosition.getX() < 0 || exitPosition.getX() > 31 ||
                exitPosition.getY() < 0 || exitPosition.getY() > 31) continue;
            Tile exitTile = level.getLayerFG().get(exitPosition);
            if (level.getLayerBG().get(exitPosition) == CLONE_MACHINE && level.getLayerFG().get(exitPosition) != Tile.BLOCK) exitTile = level.getLayerBG().get(exitPosition);
            if (!creatureType.isChip() && exitTile.isChip()) exitTile = level.getLayerBG().get(exitPosition);
            if (creatureType.isChip() && exitTile.isTransparent()) exitTile = level.getLayerBG().get(exitPosition);
            if (creatureType.isChip() && exitTile == Tile.BLOCK){
                MSCreature block = new MSCreature(direction, BLOCK, exitPosition);
                for (Creature monster : level.getSlipList()) {
                    MSCreature m = (MSCreature) monster;
                    if (m.position.equals(exitPosition)){
                        block = m;
                        break;
                    }
                }

                if (canEnter(direction, level.getLayerBG().get(exitPosition)) && block.canLeave(direction, level.getLayerBG().get(exitPosition))) {
                    Position blockPushPosition = exitPosition.move(direction);
                    if (blockPushPosition.getX() < 0 || blockPushPosition.getX() > 31 ||
                        blockPushPosition.getY() < 0 || blockPushPosition.getY() > 31) continue;
                    if (block.canEnter(direction, level.getLayerFG().get(blockPushPosition))){
                        if (block.tryMove(direction, false, pressedButtons)) break;
                    }
                }

                if (level.getLayerBG().get(exitPosition) == Tile.BLOCK) {
                    block.tryMove(direction, false, pressedButtons);
                    break;
                }

                if (level.getLayerBG().get(exitPosition) == CLONE_MACHINE) {
                    block.tryMove(direction, false, pressedButtons);
                    level.getLayerFG().set(exitPosition, CLONE_MACHINE); //Sets the block/clone_machine back to the way it was
                    level.insertTile(exitPosition, Tile.BLOCK);
                    continue; //Continues through the teleport array like in MSCC
                }

                if (block.tryMove(direction, false, pressedButtons) && (canEnter(direction, exitTile))) {
                    break; //The loop shouldn't break if Chip can't enter the tile, instead he should move onto the next teleport, AFTER pushing the block however, and this should in fact be done multiple times in a row if the situation allows
                }
            }
            if (canEnter(direction, exitTile)) break;
            if((getCreatureType() == BUG || getCreatureType() == WALKER) && exitTile == FIRE) {
                break;
            }
        }
        while (i != portalIndex);
    }

    private boolean canLeave(Direction direction, Tile tile){
        switch (tile){
            case THIN_WALL_UP: return direction != UP;
            case THIN_WALL_RIGHT: return direction != RIGHT;
            case THIN_WALL_DOWN: return direction != DOWN;
            case THIN_WALL_LEFT: return direction != LEFT;
            case THIN_WALL_DOWN_RIGHT: return direction != DOWN && direction != RIGHT;
            case TRAP: return level.isTrapOpen(position);
            default: return true;
        }
    }
    @Override
    public boolean canEnter(Direction direction, Tile tile){
        switch (tile) {
            case FLOOR: return true;
            case WALL: return false;
            case CHIP: return creatureType.isChip();
            case WATER: return true;
            case FIRE: return getCreatureType() != BUG && getCreatureType() != WALKER;
            case INVISIBLE_WALL: return false;
            case THIN_WALL_UP: return direction != DOWN;
            case THIN_WALL_RIGHT: return direction != LEFT;
            case THIN_WALL_DOWN: return direction != UP;
            case THIN_WALL_LEFT: return direction != RIGHT;
            case BLOCK: return false;
            case DIRT: return creatureType.isChip();
            case ICE:
            case FF_DOWN: return true;
            case BLOCK_UP:
            case BLOCK_LEFT:
            case BLOCK_RIGHT:
            case BLOCK_DOWN: return false;
            case FF_UP:
            case FF_LEFT:
            case FF_RIGHT: return true;
            case EXIT: return !creatureType.isMonster();
            case DOOR_BLUE: return creatureType.isChip() && level.getKeys()[0] > 0;
            case DOOR_RED: return creatureType.isChip() && level.getKeys()[1] > 0;
            case DOOR_GREEN: return creatureType.isChip() && level.getKeys()[2] > 0;
            case DOOR_YELLOW: return creatureType.isChip() && level.getKeys()[3] > 0;
            case ICE_SLIDE_SOUTHEAST: return direction == UP || direction == LEFT;
            case ICE_SLIDE_NORTHEAST: return direction == DOWN || direction == LEFT;
            case ICE_SLIDE_NORTHWEST: return direction == DOWN || direction == RIGHT;
            case ICE_SLIDE_SOUTHWEST: return direction == UP || direction == RIGHT;
            case BLUEWALL_FAKE: return creatureType.isChip();
            case BLUEWALL_REAL: return false;
            case OVERLAY_BUFFER: return false;
            case THIEF: return creatureType.isChip();
            case SOCKET: return creatureType.isChip() && level.getChipsLeft() <= 0;
            case BUTTON_GREEN: return true;
            case BUTTON_RED: return true;
            case TOGGLE_CLOSED: return false;
            case TOGGLE_OPEN: return true;
            case BUTTON_BROWN:
            case BUTTON_BLUE:
            case TELEPORT:
            case BOMB:
            case TRAP: return true;
            case HIDDENWALL_TEMP: return false;
            case GRAVEL: return (!creatureType.isMonster());
            case POP_UP_WALL: return creatureType.isChip();
            case HINT: return true;
            case THIN_WALL_DOWN_RIGHT: return direction == DOWN || direction == RIGHT;
            case CLONE_MACHINE: return false;
            case FF_RANDOM: return !creatureType.isMonster();
            case DROWNED_CHIP:
            case BURNED_CHIP:
            case BOMBED_CHIP:
            case UNUSED_36:
            case UNUSED_37: return false;
            case ICE_BLOCK: return creatureType.isIceBlock();
            case EXITED_CHIP:
            case EXIT_EXTRA_1:
            case EXIT_EXTRA_2: return false;
            case CHIP_SWIMMING_UP:
            case CHIP_SWIMMING_LEFT:
            case CHIP_SWIMMING_DOWN:
            case CHIP_SWIMMING_RIGHT: return !creatureType.isChip();
            //monsters
            default: return creatureType.isChip();
            case KEY_BLUE:
            case KEY_RED:
            case KEY_GREEN:
            case KEY_YELLOW: return true;
            case BOOTS_WATER:
            case BOOTS_FIRE:
            case BOOTS_ICE:
            case BOOTS_SLIDE: return !creatureType.isMonster();
            case CHIP_UP:
            case CHIP_LEFT:
            case CHIP_RIGHT:
            case CHIP_DOWN: return !creatureType.isChip();
        }
    }
    private boolean tryEnter(Direction direction, Position newPosition, Tile tile, List<Button> pressedButtons){
        MSLevel level = (MSLevel) Creature.level; //A bunch of things in here require MSLevel, TODO: Refactor that later
        sliding = false;
        switch (tile) {
            case FLOOR: return true;
            case WALL: return false;
            case CHIP:
                if (creatureType.isChip()) {
                    level.chipsLeft--;
                    level.getLayerFG().set(newPosition, FLOOR);
                    return true;
                }
                return false;
            case WATER:
                if (creatureType.isChip()){
                    if (level.boots[0] == 0){
                        level.getLayerFG().set(newPosition, DROWNED_CHIP);
                        kill();
                    }
                }
                else if (creatureType.isBlock()) {
                    if (creatureType.isIceBlock()) level.getLayerFG().set(newPosition, ICE);
                    else level.getLayerFG().set(newPosition, DIRT);
                    kill();
                }
                else if (creatureType != GLIDER) kill();
                return true;
            case FIRE:
                if (creatureType.isChip()) {
                    if (level.boots[1] == 0){
                        level.getLayerFG().set(newPosition,  BURNED_CHIP);
                        kill();
                    }
                    return true;
                }
                switch (creatureType) {
                    case BLOCK:
                    case FIREBALL:
                        return true;
                    case BUG:
                    case WALKER:
                        return false;
                    case ICE_BLOCK:
                        level.getLayerFG().set(newPosition, WATER);
                        kill();
                    default:
                        kill();
                        return true;
                }
            case INVISIBLE_WALL: return false;
            case THIN_WALL_UP: return direction != DOWN;
            case THIN_WALL_RIGHT: return direction != LEFT;
            case THIN_WALL_DOWN: return direction != UP;
            case THIN_WALL_LEFT: return direction != RIGHT;
            case BLOCK:
                if (creatureType.isChip()) {
                    for (Creature monster : level.slipList) {
                        MSCreature m = (MSCreature) monster;
                        if (m.position.equals(newPosition)) {
                            if (m.direction == direction || m.direction.turn(TURN_AROUND) == direction) return false;
                            if (m.tryMove(direction, false, pressedButtons)){
                                return tryEnter(direction, newPosition, level.getLayerFG().get(newPosition), pressedButtons);
                            }
                            return false;
                        }
                    }
                    if (level.getLayerBG().get(newPosition) == Tile.BLOCK) {
                        level.popTile(newPosition);
                    }
                    MSCreature block = new MSCreature(newPosition, Tile.BLOCK);
                    if (level.getLayerBG().get(newPosition) == CLONE_MACHINE) { //The weird block/clone_machine push block to clone thing now works
                        block.tryMove(direction, false, pressedButtons);
                        level.getLayerFG().set(newPosition, CLONE_MACHINE); //Sets the block/clone_machine back to the way it was
                        level.insertTile(newPosition, Tile.BLOCK);
                        return false; //Normally you would check if Chip could enter the resulting tile but seeing as its always a clone machine on the bottom it'll always be false, therefore i always have this return false
                    }
                    if (block.tryMove(direction, false, pressedButtons)){
                        for (BrownButton b : level.getBrownButtons()) { //Ok so since blocks don't follow normal creature rules they don't get caught in the section of tick() further down that closes traps after creatures leave, so I had to manually do it here
                            if (b.getTargetPosition().equals(newPosition) && level.getLayerFG().get(b.getButtonPosition()) == BUTTON_BROWN) {
                                b.release(level);
                            }
                        }
                        return tryEnter(direction, newPosition, level.getLayerFG().get(newPosition), pressedButtons);
                    }
                }
                return false;
            case DIRT:
                if (creatureType.isChip() || creatureType.isIceBlock()) {
                    level.getLayerFG().set(newPosition, FLOOR);
                    return true;
                }
                return false;
            case ICE:
                if (!(creatureType.isChip() && level.getBoots()[2] > 0)) sliding = true;
                return true;
            case FF_DOWN:
                if (!(creatureType.isChip() && level.getBoots()[3] > 0)) sliding = true;
                return true;
            case BLOCK_UP:
            case BLOCK_LEFT:
            case BLOCK_RIGHT:
            case BLOCK_DOWN: return false;
            case FF_UP:
            case FF_LEFT:
            case FF_RIGHT:
                if (!(creatureType.isChip() && level.getBoots()[3] > 0)) sliding = true;
                return true;
            case EXIT:
                if (creatureType.isBlock()) return true;
                if (creatureType.isChip()){
                    level.getLayerFG().set(newPosition, EXITED_CHIP);
                    level.setLevelWon(true);
                    kill();
                    return true;
                }
                return false;
            case DOOR_BLUE:
                if (creatureType.isChip() && level.keys[0] > 0) {
                    level.keys[0] -= 1;
                    level.getLayerFG().set(newPosition, FLOOR);
                    return true;
                }
                return false;
            case DOOR_RED:
                if (creatureType.isChip() && level.keys[1] > 0) {
                    level.keys[1] -= 1;
                    level.getLayerFG().set(newPosition, FLOOR);
                    return true;
                }
                return false;
            case DOOR_GREEN:
                if (creatureType.isChip() && level.keys[2] > 0) {
                    level.getLayerFG().set(newPosition, FLOOR);
                    return true;
                }
                return false;
            case DOOR_YELLOW:
                if (creatureType.isChip() && level.keys[3] > 0) {
                    level.keys[3] -= 1;
                    level.getLayerFG().set(newPosition, FLOOR);
                    return true;
                }
                return false;
            case ICE_SLIDE_SOUTHEAST:
                if (direction == UP || direction == LEFT){
                    if (!(creatureType.isChip() && level.getBoots()[2] > 0)) sliding = true;
                    return true;
                }
                else return false;
            case ICE_SLIDE_NORTHEAST:
                if(direction == DOWN || direction == LEFT){
                    if (!(creatureType.isChip() && level.getBoots()[2] > 0)) sliding = true;
                    return true;
                }
                else return false;
            case ICE_SLIDE_NORTHWEST:
                if(direction == DOWN || direction == RIGHT){
                    if (!(creatureType.isChip() && level.getBoots()[2] > 0)) sliding = true;
                    return true;
                }
                else return false;
            case ICE_SLIDE_SOUTHWEST:
                if(direction == UP || direction == RIGHT){
                    if (!(creatureType.isChip() && level.getBoots()[2] > 0)) sliding = true;
                    return true;
                }
                else return false;
            case BLUEWALL_FAKE:
                if (creatureType.isChip()) {
                    level.getLayerFG().set(newPosition, FLOOR);
                    return true;
                }
                else return false;
            case BLUEWALL_REAL:
                if (creatureType.isChip()) level.getLayerFG().set(newPosition, WALL);
                return false;
            case OVERLAY_BUFFER: return false;
            case THIEF:
                if (creatureType.isChip()) {
                    level.boots = new byte[]{0, 0, 0, 0};
                    return true;
                }
                return false;
            case SOCKET:
                if (creatureType.isChip() && level.chipsLeft <= 0) {
                    level.getLayerFG().set(newPosition, FLOOR);
                    return true;
                }
                return false;
            case BUTTON_GREEN:
                pressedButtons.add(level.getButton(newPosition, GreenButton.class));
                return true;
            case BUTTON_RED:
                Button b = level.getButton(newPosition, RedButton.class);
                if (b != null) pressedButtons.add(b);
                return true;
            case TOGGLE_CLOSED: return false;
            case TOGGLE_OPEN: return true;
            case BUTTON_BROWN:
                Button b2 = level.getButton(newPosition, BrownButton.class);
                if (b2 != null) pressedButtons.add(b2);
                return true;
            case BUTTON_BLUE:
                pressedButtons.add(level.getButton(newPosition, BlueButton.class));
                return true;
            case TELEPORT:
                sliding = true;
                teleport(direction, newPosition, pressedButtons);
                return true;
            case BOMB:
                if (!creatureType.isChip()) {
                    level.getLayerFG().set(newPosition, FLOOR);
                }
                kill();
                return true;
            case TRAP: return true;
            case HIDDENWALL_TEMP:
                if (creatureType.isChip()) {
                    level.getLayerFG().set(newPosition, WALL);
                }
                return false;
            case GRAVEL: return !creatureType.isMonster();
            case POP_UP_WALL:
                if (creatureType.isChip()) {
                    level.getLayerFG().set(newPosition, WALL);
                    return true;
                }
                return false;
            case HINT: return true;
            case THIN_WALL_DOWN_RIGHT: return direction == DOWN || direction == RIGHT;
            case CLONE_MACHINE:
                return false;
            case FF_RANDOM:
                if (creatureType.isMonster()) return false;
                if (!(creatureType.isChip() && level.getBoots()[3] > 0)) sliding = true;
                return true;
            case DROWNED_CHIP:
            case BURNED_CHIP:
            case BOMBED_CHIP:
            case UNUSED_36:
            case UNUSED_37: return false;
            case ICE_BLOCK:
                if (level.getLayerBG().get(newPosition).isCloneBlock()) return false; //You know how if you have a clone machine under a monster you can't enter it? Well it's the same with ice blocks and the clone blocks under them
                if (creatureType.isChip() || creatureType.isTank() || creatureType == TEETH || creatureType.isIceBlock()){
                    for (Creature monster : level.slipList) {
                        MSCreature m = (MSCreature) monster;
                        if (m.position.equals(newPosition)) {
                            if (m.direction == direction || m.direction.turn(TURN_AROUND) == direction) return false;
                            if (m.tryMove(direction, false, pressedButtons)){
                                return tryEnter(direction, newPosition, level.getLayerFG().get(newPosition), pressedButtons);
                            }
                            return false;
                        }
                    }
                    if (level.getLayerBG().get(newPosition) == Tile.ICE_BLOCK) {
                        level.popTile(newPosition);
                    }
                    MSCreature block = new MSCreature(newPosition, Tile.ICE_BLOCK);
                    if (level.getLayerBG().get(newPosition) == CLONE_MACHINE) { //The weird block/clone_machine push block to clone thing now works
                        block.tryMove(direction, false, pressedButtons);
                        level.getLayerFG().set(newPosition, CLONE_MACHINE); //Sets the block/clone_machine back to the way it was
                        level.insertTile(newPosition, Tile.ICE_BLOCK);
                        return false; //Normally you would check if Chip could enter the resulting tile but seeing as its always a clone machine on the bottom it'll always be false, therefore i always have this return false
                    }
                    if (block.tryMove(direction, false, pressedButtons)){
                        for (BrownButton b3 : level.getBrownButtons()) { //Ok so since blocks don't follow normal creature rules they don't get caught in the section of tick() further down that closes traps after creatures leave, so I had to manually do it here
                            if (b3.getTargetPosition().equals(newPosition) && level.getLayerFG().get(b3.getButtonPosition()) == BUTTON_BROWN) {
                                b3.release(level);
                            }
                        }
                        return tryEnter(direction, newPosition, level.getLayerFG().get(newPosition), pressedButtons);
                    }
                }
                return false;
            case EXITED_CHIP:
            case EXIT_EXTRA_1:
            case EXIT_EXTRA_2: return false;
            case CHIP_SWIMMING_UP:
            case CHIP_SWIMMING_LEFT:
            case CHIP_SWIMMING_DOWN:
            case CHIP_SWIMMING_RIGHT:
                if (!creatureType.isChip()) {
                    level.chip.kill();
                    return true;
                }
                return false;
            default:                                    // Monsters
                if (creatureType.isChip()) {
                    kill();
                    return true;
                }
                return false;
            case KEY_BLUE:
                if (creatureType.isChip()) {
                    level.getLayerFG().set(newPosition, FLOOR);
                    level.keys[0]++;
                }
                return true;
            case KEY_RED:
                if (creatureType.isChip()) {
                    level.getLayerFG().set(newPosition, FLOOR);
                    level.keys[1]++;
                }
                return true;
            case KEY_GREEN:
                if (creatureType.isChip()) {
                    level.getLayerFG().set(newPosition, FLOOR);
                    level.keys[2]++;
                }
                return true;
            case KEY_YELLOW:
                if (creatureType.isChip()) {
                    level.getLayerFG().set(newPosition, FLOOR);
                    level.keys[3]++;
                }
                return true;
            case BOOTS_WATER:
                if (creatureType.isChip()) {
                    level.getLayerFG().set(newPosition, FLOOR);
                    level.boots[0] = 1;
                }
                return !creatureType.isMonster();
            case BOOTS_FIRE:
                if (creatureType.isChip()) {
                    level.getLayerFG().set(newPosition, FLOOR);
                    level.boots[1] = 1;
                }
                return !creatureType.isMonster();
            case BOOTS_ICE:
                if (creatureType.isChip()) {
                    level.getLayerFG().set(newPosition, FLOOR);
                    level.boots[2] = 1;
                }
                return !creatureType.isMonster();
            case BOOTS_SLIDE:
                if (creatureType.isChip()) {
                    level.getLayerFG().set(newPosition, FLOOR);
                    level.boots[3] = 1;
                }
                return !creatureType.isMonster();
            case CHIP_UP:
            case CHIP_LEFT:
            case CHIP_RIGHT:
            case CHIP_DOWN:
                if (!creatureType.isChip()) {
                    level.getChip().kill();
                    return true;
                }
                return false;
        }
    }
    private boolean tryMove(Direction direction, boolean slidingMove, List<Button> pressedButtons){
        if (direction == null) return false;
        if (!canLeave(direction, level.getLayerBG().get(position))) return false;
        Direction oldDirection = this.direction;
        boolean wasSliding = sliding;
        setDirection(direction);
        Position newPosition = position.move(direction);
        if (!newPosition.isValid()) newPosition.setIndex(-1);

        Tile newTileFG = level.getLayerFG().get(newPosition);
        Tile newTileBG = level.getLayerBG().get(newPosition);
        boolean isChip = creatureType.isChip();
        boolean isBlock = creatureType.isBlock();
        boolean isMonster = creatureType.isMonster();
        boolean pickupCheck = false;
        boolean blockMachineCheck = true;
        if (newTileBG == CLONE_MACHINE && !isBlock) blockMachineCheck = false;

        if ((creatureType.isMonster()) && newTileFG.isChip()) newTileFG = newTileBG;
        if (newTileFG.isPickup()) {
            if (isChip) {
                if (canEnter(direction, newTileBG) || newTileBG == Tile.BLOCK) pickupCheck = true;
            }
            else if (newTileFG.isKey()) pickupCheck = true;
        }

        if (canEnter(direction, newTileBG) ||
                (!newTileFG.isTransparent() && (isBlock || blockMachineCheck)) //Look at this if statement, this is all just to get transparency to work
                || (pickupCheck && blockMachineCheck)
                || (isBlock && (newTileFG.isBoot() || newTileFG.isChip() || newTileFG.isSwimmingChip()))) { //This right here can sometimes cause Mini Challenges (CCLP3 116) to hang if you mess with the mouse code

            if (newTileBG == CLONE_MACHINE && creatureType.isDirtBlock()) newTileFG = newTileBG; //Putting a check for clone machines on the lower layer with blocks in the if statement above causes massive slide delay issues, so i set newTile to be the clone machine here and those issues are gone and lower layer clone machines now work properly

            if (tryEnter(direction, newPosition, newTileFG, pressedButtons)) {
                if (newTileFG != TELEPORT) level.popTile(position);
                else if (!isChip) level.popTile(position); //You probably noticed that this works for every creature other than Chip, we handle this very specific case (Chip and teleport) over in the teleport method so we cancel it out here, and yes it does in fact cause some issues if we don't, possibly even crashes if you revert both this and the teleport method handle
                position = newPosition;

                //!!DIRTY HACK SECTION BEGINS!!//
                if (isChip && newTileBG == EXIT && level.getLayerFG().get(newPosition) == FLOOR && level.getChip().getPosition() == newPosition) {
                    tryEnter(direction, newPosition, newTileBG, pressedButtons); //Quick little hack to make having Chip reveal an Exit on the lower layer take effect
                    level.getLayerFG().set(newPosition, EXITED_CHIP); //Fixed cosmetics, else you have an EXITED_CHIP/EXIT tile that looks ugly (no gameplay effect however)
                    level.getLayerBG().set(newPosition, FLOOR);
                }

                if (isBlock && slidingMove && level.getLayerFG().get(newPosition) == TRAP)
                    sliding = true; //A block that slides into a trap should be properly added or maintained in the sliplist, however tryenter doesn't register traps as sliding so i have to manually add a check here
                //!!DIRTY HACK SECTION ENDS!!//

                if (sliding && !isMonster)
                    setDirection(applySlidingTile(direction, level.getLayerFG().get(position), level.getRNG()));

                if (!isDead()) level.insertTile(getPosition(), toTile());
                else if (isMonster) {
                    level.getMonsterList().numDeadMonsters++;
                }

                setSliding(wasSliding, sliding, true);
                return true;
            }
        }

        setSliding(wasSliding, sliding);

        if (wasSliding && !creatureType.isMonster()) {
            if (level.getLayerBG().get(this.position) == FF_RANDOM && !slidingMove) setDirection(oldDirection);
            else setDirection(applySlidingTile(direction, level.getLayerBG().get(position), level.getRNG()));
        }

        return false;
    }

    boolean tick(Direction[] directions, boolean slidingMove){
        MSLevel level = (MSLevel) Creature.level;
        MSCreature oldCreature = clone();
        if (!creatureType.isChip() && !isSliding()) monsterList.direction = direction;
        for (Direction newDirection : directions){

            LinkedList<Button> pressedButtons = new LinkedList<>();

            if (tryMove(newDirection, slidingMove, pressedButtons)){
                for(int i = pressedButtons.size() - 1; i >= 0; i--) {
                    pressedButtons.get(i).press(level);
                }
                if (level.getLayerFG().get(oldCreature.position) == BUTTON_BROWN){
                    BrownButton b = ((BrownButton) level.getButton(oldCreature.position, BrownButton.class));
                    if (b != null && level.getLayerBG().get(b.getTargetPosition()) != TRAP && !b.getTargetPosition().equals(position)) {
                        b.release(level);
                    }
                    if (b != null && b.getTargetPosition().equals(position)) {
                        b.release(level);
                    }
                }
                if (level.getLayerFG().get(oldCreature.position) == TRAP || level.getLayerBG().get(oldCreature.position) == TRAP){
                    for (BrownButton b : level.getBrownButtons()) {
                        if (b.getTargetPosition().equals(oldCreature.position) && level.getLayerFG().get(b.getButtonPosition()) == BUTTON_BROWN) {
                            b.release(level);
                        }
                    }
                }
                if (!creatureType.isChip()) {
                    if (level.getLayerBG().get(position).isChip()) level.getChip().kill();
                    if (!isSliding()) monsterList.direction = newDirection;
                }
                return true;
            }
            if (!creatureType.isChip() && !isSliding()) monsterList.direction = newDirection;

        }
        setSliding(this.sliding, oldCreature.sliding);
        if (creatureType.isTank() && !isSliding()) setCreatureType(TANK_STATIONARY);
        if (!creatureType.isChip() &&!(creatureType.isBlock() && level.getLayerBG().get(position) == FF_RANDOM)) setDirection(oldCreature.direction);
        else level.getLayerFG().set(position, toTile());
        return false;
    }

    @Override
    public boolean tick() { //Ideally shouldn't be used for MS, the method this calls should be used instead
        return tick(new Direction[]{direction}, sliding);
    }
    
    public MSCreature(Direction direction, CreatureID creatureType, Position position){
        this.direction = direction;
        this.creatureType = creatureType;
        this.position = position;
    }
    public MSCreature(Position position, Tile tile){
        this.position = position;
        if (BLOCK_UP.ordinal() <= tile.ordinal() && tile.ordinal() <= BLOCK_RIGHT.ordinal()){
            direction = Direction.fromOrdinal((tile.ordinal() + 2) % 4);
            creatureType = BLOCK;
        }
        else{
            direction = Direction.fromOrdinal(tile.ordinal() % 4);
            if (tile == Tile.BLOCK) creatureType = BLOCK;
            else {
                if (tile == Tile.ICE_BLOCK) creatureType = CreatureID.ICE_BLOCK;
                else creatureType = CreatureID.fromOrdinal((tile.ordinal() - 0x40) >>> 2);
            }
        }
        if (creatureType == TANK_STATIONARY) creatureType = TANK_MOVING;
    }
    public MSCreature(int bitMonster){
        direction = Direction.fromOrdinal(bitMonster >>> 14);
        creatureType = CreatureID.fromOrdinal((bitMonster >>> 10) & 0b1111);
        if (creatureType == CHIP_SLIDING) sliding = true;
        position = new Position(bitMonster & 0b00_0000_1111111111);
    }

    @Override
    public int bits(){
        return direction.getBits() | creatureType.getBits() | position.getIndex();
    }

    @Override
    public MSCreature clone(){
        MSCreature c = new MSCreature(direction, creatureType, position);
        c.sliding = sliding;
        return c;
    }

    @Override
    public String toString(){
        if (creatureType == DEAD) return "Dead monster at position " + position;
        return creatureType+" facing "+direction+" at position "+position;
    }

}
