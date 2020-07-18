package game.Lynx;

import game.*;

import static game.CreatureID.*;
import static game.Direction.*;
import static game.Direction.TURN_AROUND;
import static game.Tile.*;

public class LynxCreature extends Creature {

    private int timeTraveled;
    private int animationTimer; //Exists primarily for death effect timings, but adds future ability to implement actual animations

    @Override
    public boolean isSliding() {
        return sliding;
    }

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
    public int bits() {
        return direction.getBits() | creatureType.getBits() | position.getIndex();
    }

    @Override
    public String toString() {
        if (creatureType == DEAD) return "Dead monster at position " + position;
        return creatureType+" facing "+direction+" at position "+position;
    }


    @Override
    public int getTimeTraveled() {
        return timeTraveled & 0b111; //timeTraveled should always be between 0 and 7 anyways but just to be safe
    }

    @Override
    public boolean tick() {
        timeTraveled = (timeTraveled + 2) & 0b111; //Mod 8
        //Needs to be changed to include blobs only moving by 1 and sliding moving by an extra 2

        if (timeTraveled != 0) return true;

        Tile newTile = level.getLayerFG().get(position);
        switch (newTile) {
            case WATER:
                if (creatureType != GLIDER) kill(); //todo: splash
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
                direction = applySlidingTile(direction, newTile, null);
                sliding = true;
                break;
            case BUTTON_GREEN:
            case BUTTON_RED:
            case BUTTON_BROWN:
            case BUTTON_BLUE:
                level.getButton(position).press(level);
                break;
            case TELEPORT:
                //todo: oh christ
                break;
            case BOMB:
                kill(); //TODO: splash but hot
                break;
        }

        return false;
    }

    @Override
    public Direction[] getDirectionPriority(Creature chip, RNG rng) {
        if (isSliding()) return direction.turn(new Direction[] {TURN_FORWARD, TURN_AROUND});

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
                //todo
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
        creatureType = DEAD;
        animationTimer = 12; //todo: minus depending on current tick + step
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

    private boolean canLeave(Tile lower) {
        switch (lower){
            case THIN_WALL_UP: return direction != UP;
            case THIN_WALL_RIGHT: return direction != RIGHT;
            case THIN_WALL_DOWN: return direction != DOWN;
            case THIN_WALL_LEFT: return direction != LEFT;
            case THIN_WALL_DOWN_RIGHT: return direction != DOWN && direction != RIGHT;
            case TRAP: return level.isTrapOpen(position);
            default: return true;
        }
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
        if (creatureType == TANK_STATIONARY) creatureType = TANK_MOVING;
    }

    public LynxCreature(int bitMonster) {
        direction = Direction.fromOrdinal(bitMonster >>> 14);
        creatureType = CreatureID.fromOrdinal((bitMonster >>> 10) & 0b1111);
        if (creatureType == CHIP_SLIDING) sliding = true; //TODO: CHIP_SLIDING probably doesn't need to exist for lynx, nor do the 2 types of tanks
        position = new Position(bitMonster & 0b00_0000_1111111111);
    }
}
