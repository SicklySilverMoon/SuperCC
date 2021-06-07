package game.Lynx;

import game.*;
import game.button.BrownButton;
import game.button.Button;

import java.util.Objects;

import static game.CreatureID.*;
import static game.Direction.*;
import static game.Direction.TURN_AROUND;
import static game.Tile.*;

/**
 * Lynx creatures are encoded as follows:
 *
 *      0    |    0    |  0 0 0 0  |    0 0 0    |  0 0 0 0  | 0 0 0 0 | 0 0 0 0 0 | 0 0 0 0 0 |
 *  OVERRIDE | SLIDING | ANIMATION | TIME TRAVEL | DIRECTION | MONSTER |    ROW    |    COL    |
 */
public class LynxCreature extends Creature {

    private int timeTraveled;
    private int animationTimer; //Exists primarily for death effect and Chip in exit timing, but adds future ability to implement actual animations
    protected boolean overrideToken;

    @Override
    public Tile toTile() { //Used exclusively for drawing creatures, please do not use for anything else
        return switch (creatureType) {
            case BLOCK -> Tile.BLOCK;
            case CHIP -> Tile.fromOrdinal(Tile.CHIP_UP.ordinal() | direction.ordinal());
            case TANK_MOVING, TANK_STATIONARY -> Tile.fromOrdinal(TANK_UP.ordinal() | direction.ordinal());
            case CHIP_SWIMMING -> Tile.fromOrdinal(CHIP_SWIMMING_UP.ordinal() | direction.ordinal());
            default -> Tile.fromOrdinal((creatureType.ordinal() << 2) + 0x40 | direction.ordinal());
        };
    }

    @Override
    public int getTimeTraveled() {
        return timeTraveled % 9; //timeTraveled should always be between 0 and 8 anyways but just to be safe
    }

    @Override
    public int getAnimationTimer() {
        return animationTimer % 13; //it should always be between 0 and 12 but safety
    }

    @Override
    public boolean tick(boolean releasing) {
        Direction direction;
        Direction tdir;

        if (animationTimer != 0) {
            animationTimer--;
            return false;
        }

        if (timeTraveled == 0) { //analog to TW's startmovement()
            sliding = false;
            if (releasing) {
                tdir = this.tDirection;
                this.tDirection = this.direction;
            }
            //equiv. to TW's startmovement
            if (tDirection != NONE)
                direction = tDirection;
            else if (fDirection != NONE) {
                direction = fDirection;
                sliding = true;
            }
            else
                return false;
            this.direction = direction;

            Position from = position;
            Position to = position.move(direction);

            Tile tileFrom = level.getLayerFG().get(from);
            if (creatureType == CreatureID.CHIP) { //todo: make this actually work please
                if (level.getBoots()[3] == 0) {
                    if (tileFrom.isFF() && tDirection == NONE)
                        overrideToken = true;
                    else if (!tileFrom.isIce() || level.getBoots()[2] != 0)
                        overrideToken = false;
                }
            }

            boolean isChip = creatureType == CreatureID.CHIP;
            if (!canMakeMove(direction, to, !isChip, isChip, isChip, releasing)) {
                if (level.getLayerFG().get(from).isIce()) {
                    direction = direction.turn(TURN_AROUND);
                    this.direction = getSlideDirection(direction, level.getLayerFG().get(from), null, false);
                }
                return false;
            }

            if (creatureType != CreatureID.CHIP)
                level.getMonsterList().adjustClaim(from, false);

            position = to;
            if (creatureType != CreatureID.CHIP)
                level.getMonsterList().adjustClaim(to, true);

            timeTraveled = 8;
            if (creatureType != CreatureID.CHIP && level.getChip().getPosition() == position) {
                level.getChip().kill();
                return false;
            }
            else if (creatureType == CreatureID.CHIP && level.getMonsterList().claimed(position)) {
                kill();
                return false;
            }
        }

        if (creatureType != DEAD) { //analog to TW's continue movement
            int speed = creatureType == BLOB ? 1 : 2;
            Tile tile = level.getLayerFG().get(position);
            if (tile.isIce() && (creatureType != CreatureID.CHIP || level.getBoots()[2] == 0))
                speed *= 2;
            else if (tile.isFF() && (creatureType != CreatureID.CHIP || level.getBoots()[3] == 0))
                speed *= 2;
            timeTraveled -= speed;
        }
        if (timeTraveled > 0)
            return true;

        Tile newTile = level.getLayerFG().get(position);
        switch (newTile) {
            case WATER:
                if (creatureType == CreatureID.BLOCK)
                    level.getLayerFG().set(position, DIRT);
                if (creatureType != GLIDER && !(creatureType == CreatureID.CHIP && level.getBoots()[0] != 0))
                    kill();
                break;
//            case ICE:
            case ICE_SLIDE_SOUTHEAST:
            case ICE_SLIDE_SOUTHWEST:
            case ICE_SLIDE_NORTHWEST:
            case ICE_SLIDE_NORTHEAST:
                if (creatureType != CreatureID.CHIP || level.getBoots()[2] == 0) {
                    this.direction = getSlideDirection(this.direction, newTile, null, false);
                }
                break;
//            case FF_DOWN:
//            case FF_UP:
//            case FF_RIGHT:
//            case FF_LEFT:
//            case FF_RANDOM:
//                if (creatureType != CreatureID.CHIP) {
//                    this.direction = getSlideDirection(this.direction, newTile, null, true);
//                }
//                break;
            case BUTTON_GREEN:
            case BUTTON_RED:
//            case BUTTON_BROWN: //todo: handle like TW does
            case BUTTON_BLUE:
                Button button = level.getButton(position);
                if (button != null) {
                    button.press(level);
//                    if (button instanceof BrownButton) {
//                        level.getMonsterList().springTrappedCreature(((BrownButton) button).getTargetPosition());
//                    }
                }
                break;
            case FIRE:
                if (creatureType == CreatureID.CHIP && level.getBoots()[1] == 0)
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
            case BOOTS_FF:
                if (creatureType == CreatureID.CHIP) {
                    level.getBoots()[3] = 1;
                    level.getLayerFG().set(position, FLOOR);
                }
                break;
            case EXIT:
                if (creatureType == CreatureID.CHIP)
                    animationTimer = 1;
                break;
            case THIEF:
                if (creatureType == CreatureID.CHIP)
                    level.setBoots(new byte[]{0, 0, 0, 0});
                break;
        }
        return true;
    }

    @Override
    public Direction[] getDirectionPriority(Creature chip, RNG rng) {
        if (tDirection != NONE)
            return new Direction[] {NONE};

        if (nextMoveDirectionCheat != null) {
            Direction[] directions = new Direction[] {nextMoveDirectionCheat};
            nextMoveDirectionCheat = null;
            if (creatureType == BLOB)
                rng.random4();
            if (creatureType == WALKER)
                rng.pseudoRandom4();
            return directions;
        }

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
                int turns = rng.pseudoRandom4();
                Direction walkerDirection = direction;
                while(turns-- != 0)
                    walkerDirection = walkerDirection.turn(TURN_RIGHT);
                return new Direction[] {direction, walkerDirection};
            case BLOB:
                Direction[] blobDirs = new Direction[] {UP, RIGHT, DOWN, LEFT};
                return new Direction[] { blobDirs[rng.random4()] };
            case PARAMECIUM:
                return direction.turn(new Direction[] {TURN_RIGHT, TURN_FORWARD, TURN_LEFT, TURN_AROUND});
            default:
                return new Direction[] {NONE};
        }
    }

    @Override
    public Direction getSlideDirection(Direction direction, Tile tile, RNG rng, boolean advanceRFF){
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
                if (advanceRFF)
                    return level.getAndCycleRFFDirection();
                else
                    return level.getRFFDirection();
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
    public boolean getForcedMove(Tile tile) {
        fDirection = NONE;
        if (tile.isSliding()) { //replication of TW's getforcedmove(), todo: check how TW uses and sets the teleport flag
            Direction slideDir = getSlideDirection(direction, tile, null, true);
            if (tile.isIce()) {
                if (direction == Direction.NONE)
                    return false;
                if (creatureType == CreatureID.CHIP && level.getBoots()[2] != 0)
                    return false;
                fDirection = slideDir;
                return true;
            }
            else if (tile.isFF()) {
                if (creatureType == CreatureID.CHIP && level.getBoots()[3] != 0)
                    return false;
                fDirection = slideDir;
                return !overrideToken;
            }
            else if (tile == Tile.TELEPORT) { //please find how the CS_TELEPORTED flag works in TW
                fDirection = slideDir;
                return true;
            }
        }
        return false;
    }

    @Override
    public void kill() {
        if (creatureType != DEAD) {
            if (creatureType != CreatureID.CHIP)
                level.getMonsterList().adjustClaim(position, false);
            creatureType = DEAD;
            animationTimer = ((level.getTickNumber() + level.getStep().ordinal()) & 1) == 0 ? 11 : 10; //basically copied out of TW's source
            timeTraveled = 0;
            switch (level.getLayerFG().get(position)) { //direction is used for determining which graphic to draw
                case WATER, DIRT -> direction = UP;
                case FIRE, BOMB -> direction = LEFT;
                default -> direction = DOWN;
            }
        }
        else
            animationTimer = 0;
    }

    @Override
    public boolean canMakeMove(Direction direction, Position position, boolean clearAnims, boolean pushBlocks, boolean pushBlocksNow, boolean releasing) {
        if (timeTraveled != 0) //this is more defensive than TW which only checks for Blocks, see if that's an issue anywhere
            return false;

        if (!canLeave(direction, this.position, releasing) || !position.isValid())
            return false;

        Tile enteringTile = level.getLayerFG().get(position);
        if (!canEnter(direction, enteringTile))
            return false;
        if (creatureType == CreatureID.CHIP) {
            if (level.getMonsterList().animationAt(position) != null)
                return false;

            Creature other = level.getMonsterList().creatureAt(position, false);
            if (other != null && other.getCreatureType() == CreatureID.BLOCK) {
                Position blockPos = other.getPosition(); //equiv. to TW's canpushblock()
                if (!other.canMakeMove(direction, blockPos.move(direction), true, false, false, false)) {
                    if (other.getTimeTraveled() == 0 && (pushBlocks || pushBlocksNow))
                        other.setDirection(direction);
                    return false;
                }
                if (pushBlocks || pushBlocksNow) {
                    other.setDirection(direction);
                    other.setTDirection(direction);
                    if (pushBlocksNow)
                        other.tick(false);
                }
            }

            if (enteringTile == HIDDENWALL_TEMP || enteringTile == BLUEWALL_REAL) {
                if (timeTraveled == 0)
                    level.getLayerFG().set(position, WALL);
                return false;
            }
        }
        else {
            if (level.getMonsterList().claimed(position))
                return false;
            Creature anim = level.getMonsterList().animationAt(position);
            if (anim != null && clearAnims)
                anim.kill();
        }

        return true;
    }

    private boolean canEnter(Direction direction, Tile tile) {
        boolean isChip = creatureType.isChip();

        return switch (tile) {
            case FLOOR -> true;
            case WALL -> false;
            case CHIP -> isChip;
            case WATER -> true;
            case FIRE -> creatureType == FIREBALL || isChip || creatureType == CreatureID.BLOCK;
            case INVISIBLE_WALL -> false;
            case THIN_WALL_UP -> direction != DOWN;
            case THIN_WALL_LEFT -> direction != RIGHT;
            case THIN_WALL_DOWN -> direction != UP;
            case THIN_WALL_RIGHT -> direction != LEFT;
            case DIRT -> isChip;
            case ICE, FF_DOWN, FF_UP, FF_RIGHT, FF_LEFT -> true;
            case EXIT -> isChip;
            case DOOR_BLUE -> isChip && level.getKeys()[0] > 0;
            case DOOR_RED -> isChip && level.getKeys()[1] > 0;
            case DOOR_GREEN -> isChip && level.getKeys()[2] > 0;
            case DOOR_YELLOW -> isChip && level.getKeys()[3] > 0;
            case ICE_SLIDE_SOUTHEAST -> direction != DOWN && direction != RIGHT;
            case ICE_SLIDE_SOUTHWEST -> direction != DOWN && direction != LEFT;
            case ICE_SLIDE_NORTHWEST -> direction != UP && direction != LEFT;
            case ICE_SLIDE_NORTHEAST -> direction != UP && direction != RIGHT;
            case BLUEWALL_FAKE -> isChip;
            case BLUEWALL_REAL -> isChip;
            case OVERLAY_BUFFER -> false;
            case THIEF -> isChip;
            case SOCKET -> isChip && level.getChipsLeft() <= 0;
            case BUTTON_GREEN, BUTTON_RED -> true;
            case TOGGLE_CLOSED -> false;
            case TOGGLE_OPEN, BUTTON_BROWN, BUTTON_BLUE, TELEPORT, BOMB, TRAP -> true;
            case HIDDENWALL_TEMP -> isChip;
            case GRAVEL -> isChip || creatureType == CreatureID.BLOCK;
            case POP_UP_WALL, HINT -> isChip;
            case THIN_WALL_DOWN_RIGHT -> direction != UP && direction != LEFT;
            case CLONE_MACHINE -> false;
            case FF_RANDOM -> true;
            case DROWNED_CHIP, BURNED_CHIP, BOMBED_CHIP, UNUSED_36, UNUSED_37, ICE_BLOCK, EXITED_CHIP, EXIT_EXTRA_1, EXIT_EXTRA_2 -> false; //Doesn't exist in lynx
            case KEY_BLUE, KEY_RED -> true;
            case KEY_GREEN, KEY_YELLOW, BOOTS_WATER, BOOTS_FIRE, BOOTS_ICE, BOOTS_FF -> isChip; //Probably shouldn't exist as a tile
            case CHIP_UP, CHIP_LEFT, CHIP_DOWN, CHIP_RIGHT -> !isChip;
            default -> false;
        };
    }

    private boolean canLeave(Direction direction, Position position, boolean releasing) {
        Tile tile = level.getLayerFG().get(position);
        switch (tile){
            case THIN_WALL_UP: return direction != UP;
            case THIN_WALL_RIGHT: return direction != RIGHT;
            case THIN_WALL_DOWN: return direction != DOWN;
            case THIN_WALL_LEFT: return direction != LEFT;
            case THIN_WALL_DOWN_RIGHT: return direction != DOWN && direction != RIGHT;
            case TRAP: return releasing;
        }
        if (tile.isFF()) {
            if (creatureType != CreatureID.CHIP || level.getBoots()[3] == 0)
                return direction.turn(TURN_AROUND) != getSlideDirection(NONE, tile, null, false);
        }
        return true;
    }

    @Override
    public boolean canOverride() {
        return overrideToken;
    }

    @Override
    public int bits() {
        return ((overrideToken ? 1 : 0) << 25) | ((sliding ? 1 : 0) << 24) | (animationTimer << 21) | (timeTraveled << 18)
                | (direction.getBits() << 14) | creatureType.getBits() | position.getIndex();
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
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        LynxCreature that = (LynxCreature) o;

        if (position != that.position)
            return false;
        if (creatureType != that.creatureType)
            return false;
        if (direction != that.direction)
            return false;
        if (sliding != that.sliding)
            return false;
        if (timeTraveled != that.timeTraveled)
            return false;
        return animationTimer == that.animationTimer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, creatureType, direction, sliding, timeTraveled, animationTimer);
    }

    @Override
    public Creature clone() {
        LynxCreature c = new LynxCreature(bits());
        c.setLevel(level);
        return c;
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
                case BLOCK -> {
                    direction = UP;
                    creatureType = CreatureID.BLOCK;
                }
                case CHIP_SWIMMING_UP, CHIP_SWIMMING_LEFT, CHIP_SWIMMING_DOWN, CHIP_SWIMMING_RIGHT -> creatureType = CHIP_SWIMMING;
                default -> creatureType = CreatureID.fromOrdinal((tile.ordinal() - 0x40) >>> 2);
            }
        }
        if (creatureType == TANK_STATIONARY)
            creatureType = TANK_MOVING;
    }

    public LynxCreature(int bitMonster) {
        overrideToken = ((bitMonster >>> 25) & 0b1) == 1;
        sliding = ((bitMonster >>> 24) & 0b1) == 1;
        animationTimer = (bitMonster >>> 21) & 0b111;
        timeTraveled = (bitMonster >>> 18) & 0b111;
        direction = Direction.fromOrdinal((bitMonster >>> 14) & 0b1111);
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
