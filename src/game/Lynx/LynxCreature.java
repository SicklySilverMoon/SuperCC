package game.Lynx;

import game.*;
import game.button.BrownButton;

import static game.CreatureID.*;
import static game.Direction.*;
import static game.Direction.TURN_AROUND;
import static game.Tile.*;

/**
 * Lynx creatures are encoded as follows:
 *
 * |    0    |  0 0 0 0  |    0 0 0    |    0 0    | 0 0 0 0 | 0 0 0 0 0 | 0 0 0 0 0 |
 * | SLIDING | ANIMATION | TIME TRAVEL | DIRECTION | MONSTER |    ROW    |    COL    |
 */
public class LynxCreature extends Creature {

    private int timeTraveled;
    private int animationTimer; //Exists primarily for death effect and Chip in exit timing, but adds future ability to implement actual animations

    private final int defaultMoveSpeed = (creatureType != BLOB ? 2 : 1); //Blobs take 2 turns to move between tiles

    @Override
    public Tile toTile() { //Used exclusively for drawing creatures, please do not use for anything else
        switch (creatureType) {
            case BLOCK:
                return Tile.BLOCK;
            case CHIP:
                return Tile.fromOrdinal(Tile.CHIP_UP.ordinal() | direction.ordinal());
            case TANK_MOVING:
            case TANK_STATIONARY:
                return Tile.fromOrdinal(TANK_UP.ordinal() | direction.ordinal());
            case CHIP_SWIMMING:
                return Tile.fromOrdinal(CHIP_SWIMMING_UP.ordinal() | direction.ordinal());
            default:
                return Tile.fromOrdinal((creatureType.ordinal() << 2) + 0x40 | direction.ordinal());
        }
    }

    @Override
    public int getTimeTraveled() {
        return timeTraveled & 0b111; //timeTraveled should always be between 0 and 7 anyways but just to be safe
    }

    @Override
    public int getAnimationTimer() {
        return animationTimer % 13; //it should always be between 0 and 12 but safety
    }

    @Override
    public boolean tick(Direction direction) {
        if (animationTimer != 0) {
            animationTimer--;
            if (creatureType == CreatureID.CHIP && level.getLayerFG().get(position.index) == EXIT) {
                level.setLevelWon(true);
                level.getLayerFG().set(position, EXITED_CHIP);
                kill(); //kill and get rid of the animation as well
                kill();
            }
            return false;
        }

        if (creatureType == DEAD) return false;

        if (timeTraveled == 0) {
            if ((creatureType == CreatureID.BLOCK || creatureType == CHIP_SWIMMING) && direction == null && !sliding)
                return false;
            /* yeah so these 2 creatures don't move on their own, represented by passing null,
            however they do need to move if sliding (blocks also have tick manually called when pushed)
            so this little messy if takes care of that odd situation */
            if (direction == null) direction = this.direction;

            Position newPosition = position.move(direction);
            boolean canMove = canEnter(direction, level.getLayerFG().get(newPosition))
                    && canLeave(direction, level.getLayerFG().get(position), position);
            boolean blockedByCreature = false;
            if (creatureType != CreatureID.CHIP)
                blockedByCreature = monsterList.getCreaturesAtPosition(newPosition) != 0;
            else if (monsterList.getCreaturesAtPosition(newPosition) != 0 && monsterList.creatureAt(newPosition).getCreatureType() == CreatureID.BLOCK)
                blockedByCreature = true;

            if (!canMove || blockedByCreature || !newPosition.isValid()) {
                    Tile currentTile = level.getLayerFG().get(position);
                    if (sliding && (currentTile.isIce() || currentTile == FF_RANDOM)) {
                        direction = direction.turn(TURN_AROUND);
                        if (currentTile.isIceCorner() || currentTile == FF_RANDOM)
                            direction = applySlidingTile(direction, currentTile, null);
                        this.direction = direction; //the passed direction is (usually) formed from the creature's direction, therefore setting this.direction here and down a bit is correct
                    }
                    return false;
            }
            finishLeaving(position);
            position = newPosition;
            sliding = level.getLayerFG().get(newPosition).isSliding(); //todo: oh god trap sliding
        }
        Tile newTile = level.getLayerFG().get(position);

        timeTraveled += defaultMoveSpeed;
        if (newTile.isFF() || newTile.isIce()) timeTraveled += defaultMoveSpeed; //Sliding on ice or FFs doubles move speed
        timeTraveled &= 0b111; //Mod 8

        if (timeTraveled != 0) return true;

        switch (newTile) {
            case WATER:
                if (creatureType == CreatureID.BLOCK)
                    level.getLayerFG().set(position, DIRT);
                if (creatureType != GLIDER && !(creatureType == CreatureID.CHIP && level.getBoots()[0] != 0))
                    kill();
                break;
            case ICE:
            case FF_DOWN:
            case FF_UP:
            case FF_RIGHT:
            case FF_LEFT:
            case ICE_SLIDE_SOUTHEAST:
            case ICE_SLIDE_SOUTHWEST:
            case ICE_SLIDE_NORTHWEST:
            case ICE_SLIDE_NORTHEAST:
            case FF_RANDOM:
                this.direction = applySlidingTile(this.direction, newTile, null);
                sliding = true;
                break;
            case BUTTON_GREEN:
            case BUTTON_RED:
            case BUTTON_BROWN:
            case BUTTON_BLUE:
                level.getButton(position).press(level);
                break;
            case FIRE:
                if (creatureType != FIREBALL && !(creatureType == CreatureID.CHIP && level.getBoots()[1] != 0))
                    kill();
                break;
            case BOMB:
                kill();
                level.getLayerFG().set(position, FLOOR);
                break;
            case POP_UP_WALL:
                level.getLayerFG().set(position, WALL);
                break;
            case BLUEWALL_FAKE:
            case SOCKET:
            case DIRT:
                level.getLayerFG().set(position, FLOOR);
                break;
            case CHIP:
                if (creatureType == CreatureID.CHIP) {
                    level.setChipsLeft(level.getChipsLeft() - 1);
                    level.getLayerFG().set(position, FLOOR);
                }
                break;
            case KEY_BLUE:
                if (creatureType == CreatureID.CHIP)
                    level.getKeys()[0]++;
                level.getLayerFG().set(position, FLOOR);
                break;
            case KEY_RED:
                if (creatureType == CreatureID.CHIP) {
                    level.getKeys()[1]++;
                    level.getLayerFG().set(position, FLOOR);
                }
                break;
            case KEY_GREEN:
                if (creatureType == CreatureID.CHIP) {
                    level.getKeys()[2]++;
                    level.getLayerFG().set(position, FLOOR);
                }
                break;
            case KEY_YELLOW:
                if (creatureType == CreatureID.CHIP) {
                    level.getKeys()[3]++;
                    level.getLayerFG().set(position, FLOOR);
                }
                break;
            case DOOR_BLUE:
                level.getKeys()[0]--;
                level.getLayerFG().set(position, FLOOR);
                break;
            case DOOR_RED:
                level.getKeys()[1]--;
                level.getLayerFG().set(position, FLOOR);
                break;
            case DOOR_GREEN:
                level.getLayerFG().set(position, FLOOR);
                break;
            case DOOR_YELLOW:
                level.getKeys()[3]--;
                level.getLayerFG().set(position, FLOOR);
                break;
            case BOOTS_WATER:
                if (creatureType == CreatureID.CHIP) {
                    level.getBoots()[0] = 1;
                    level.getLayerFG().set(position, FLOOR);
                }
                break;
            case BOOTS_FIRE:
                if (creatureType == CreatureID.CHIP) {
                    level.getBoots()[1] = 1;
                    level.getLayerFG().set(position, FLOOR);
                }
                break;
            case BOOTS_ICE:
                if (creatureType == CreatureID.CHIP) {
                    level.getBoots()[2] = 1;
                    level.getLayerFG().set(position, FLOOR);
                }
                break;
            case BOOTS_SLIDE:
                if (creatureType == CreatureID.CHIP) {
                    level.getBoots()[3] = 1;
                    level.getLayerFG().set(position, FLOOR);
                }
                break;
            case EXIT:
                if (creatureType == CreatureID.CHIP)
                    animationTimer = 4;
                break;
        }
        return true;
    }

    @Override
    public Direction[] getDirectionPriority(Creature chip, RNG rng) {
        if (isSliding()) return direction.turn(new Direction[] {TURN_FORWARD});
        if (directions != null) return directions;

        switch (creatureType) {
            case BUG:
                return direction.turn(new Direction[] {TURN_LEFT, TURN_FORWARD, TURN_RIGHT, TURN_AROUND});
            case FIREBALL:
                return direction.turn(new Direction[] {TURN_FORWARD, TURN_RIGHT, TURN_LEFT, TURN_AROUND});
            case PINK_BALL:
                return direction.turn(new Direction[] {TURN_FORWARD, TURN_AROUND});
            case TANK_MOVING:
                return new Direction[] {getDirection()};
            case GLIDER:
                return direction.turn(new Direction[] {TURN_FORWARD, TURN_LEFT, TURN_RIGHT, TURN_AROUND});
            case TEETH:
                return position.seek(chip.getPosition());
            case WALKER:
                break; //TODO
            case BLOB:
                break;
            case PARAMECIUM:
                return direction.turn(new Direction[] {TURN_RIGHT, TURN_FORWARD, TURN_LEFT, TURN_AROUND});
            default:
                return new Direction[] {};
        }
        return new Direction[] {};
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
                return level.getAndCycleRFFDirection();
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

    @Override
    public void kill() {
        if (creatureType != DEAD) {
            creatureType = DEAD;
            animationTimer = 12; //todo: minus depending on current tick + step
            timeTraveled = 0;
            switch (level.getLayerFG().get(position)) {
                case WATER: //direction is used for determining which graphic to draw
                case DIRT:
                    direction = UP;
                    break;
                case FIRE:
                case BOMB:
                    direction = LEFT;
                    break;
                default:
                    direction = DOWN;
            }
        }
        else
            animationTimer = 0;
    }

    @Override
    public boolean canEnter(Direction direction, Tile tile) {
        boolean isChip = creatureType.isChip();

        switch (tile){
            case FLOOR:
                return true;
            case WALL:
                return false;
            case CHIP:
                return isChip;
            case WATER:
                return true;
            case FIRE:
                return creatureType == FIREBALL || isChip;
            case INVISIBLE_WALL:
                return false;
            case THIN_WALL_UP:
                return direction != DOWN;
            case THIN_WALL_LEFT:
                return direction != RIGHT;
            case THIN_WALL_DOWN:
                return direction != UP;
            case THIN_WALL_RIGHT:
                return direction != LEFT;
            case DIRT:
                return isChip;
            case ICE:
            case FF_DOWN:
            case FF_UP:
            case FF_RIGHT:
            case FF_LEFT:
                return true;
            case EXIT:
                return isChip;
            case DOOR_BLUE:
                return isChip && level.getKeys()[0] > 0;
            case DOOR_RED:
                return isChip && level.getKeys()[1] > 0;
            case DOOR_GREEN:
                return isChip && level.getKeys()[2] > 0;
            case DOOR_YELLOW:
                return isChip && level.getKeys()[3] > 0;
            case ICE_SLIDE_SOUTHEAST:
                return direction != DOWN && direction != RIGHT;
            case ICE_SLIDE_SOUTHWEST:
                return direction != DOWN && direction != LEFT;
            case ICE_SLIDE_NORTHWEST:
                return direction != UP && direction != LEFT;
            case ICE_SLIDE_NORTHEAST:
                return direction != UP && direction != RIGHT;
            case BLUEWALL_FAKE:
                return isChip;
            case BLUEWALL_REAL:
                return false;
            case OVERLAY_BUFFER:
                return false;
            case THIEF:
                return isChip;
            case SOCKET:
                return isChip && level.getChipsLeft() <= 0;
            case BUTTON_GREEN:
            case BUTTON_RED:
                return true;
            case TOGGLE_CLOSED:
                return false;
            case TOGGLE_OPEN:
            case BUTTON_BROWN:
            case BUTTON_BLUE:
            case TELEPORT:
            case BOMB:
            case TRAP:
                return true;
            case HIDDENWALL_TEMP:
                return false;
            case GRAVEL:
                return isChip;
            case POP_UP_WALL:
            case HINT:
                return isChip;
            case THIN_WALL_DOWN_RIGHT:
                return direction != UP && direction != LEFT;
            case CLONE_MACHINE:
                return false;
            case FF_RANDOM:
                return true;
            case DROWNED_CHIP:
            case BURNED_CHIP:
            case BOMBED_CHIP:
            case UNUSED_36:
            case UNUSED_37:
            case ICE_BLOCK: //Doesn't exist in lynx
            case EXITED_CHIP:
            case EXIT_EXTRA_1:
            case EXIT_EXTRA_2:
                return false;
            case KEY_BLUE:
            case KEY_RED:
                return true;
            case KEY_GREEN:
            case KEY_YELLOW:
            case BOOTS_WATER:
            case BOOTS_FIRE:
            case BOOTS_ICE:
            case BOOTS_SLIDE:
                return isChip;
            case CHIP_UP: //Probably shouldn't exist as a tile
            case CHIP_LEFT:
            case CHIP_DOWN:
            case CHIP_RIGHT:
                return !isChip;
            default:
                return false;
        }
    }

    @Override
    public boolean canLeave(Direction direction, Tile tile, Position position) {
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
    public int bits() {
        return (sliding ? 1 : 0) << 22 | (animationTimer << 19) | (timeTraveled << 16)
                | direction.getBits() | creatureType.getBits() | position.getIndex();
    }

    private void finishLeaving(Position position) {
        Tile tile = level.getLayerFG().get(position);
        if (tile == BUTTON_BROWN) {
            for (BrownButton b : level.getBrownButtons()) {
                //todo: refactor this later to have a releaseBrownButton(Position buttonPos) in Level or something so as to avoid having this loop in multiple places
                if (b.getButtonPosition().equals(position))
                    b.release(level);
            }
        }
    }

    @Override
    public Creature clone() {
        return new LynxCreature(bits());
    }

    public LynxCreature(Position position, Tile tile) {
        this.position = position;

        if (BLOCK_UP.ordinal() <= tile.ordinal() && tile.ordinal() <= BLOCK_RIGHT.ordinal()){
            direction = Direction.fromOrdinal((tile.ordinal() + 2) % 4);
            creatureType = CreatureID.BLOCK;
        }
        else {
            direction = Direction.fromOrdinal(tile.ordinal() % 4);
            switch (tile) {
                case BLOCK:
                    direction = UP;
                    creatureType = CreatureID.BLOCK;
                    break;
                case CHIP_SWIMMING_UP:
                case CHIP_SWIMMING_LEFT:
                case CHIP_SWIMMING_DOWN:
                case CHIP_SWIMMING_RIGHT:
                    creatureType = CHIP_SWIMMING;
                    break;
                default:
                    creatureType = CreatureID.fromOrdinal((tile.ordinal() - 0x40) >>> 2);
                    break;
            }
        }
        if (creatureType == TANK_STATIONARY)
            creatureType = TANK_MOVING;
    }

    public LynxCreature(int bitMonster) {
        sliding = ((bitMonster >>> 22) & 0b1) == 1;
        animationTimer = (bitMonster >>> 19) & 0b111;
        timeTraveled = (bitMonster >>> 16) & 0b111;
        direction = Direction.fromOrdinal((bitMonster >>> 14) & 0b11);
        creatureType = CreatureID.fromOrdinal((bitMonster >>> 10) & 0b1111);
        if (creatureType == CHIP_SLIDING) {
            sliding = true;
            creatureType = CreatureID.CHIP;
        }
        if (creatureType == TANK_STATIONARY)
            creatureType = TANK_MOVING;
        position = new Position(bitMonster & 0b00_0000_1111111111);
    }
}
