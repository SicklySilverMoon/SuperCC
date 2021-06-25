package game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * The monster list. The list attribute is the actual list.
 */
public abstract class CreatureList implements Iterable<Creature> {
    public Direction direction;

    protected Level level;
    protected Creature[] list;
    protected List<Creature> newClones;
    protected boolean teethStep;
    protected Creature chipToCr; //not used for MS but eh

    public CreatureList(Creature[] monsters) {
        list = monsters;
    }

    public Creature creatureAt(Position position, boolean includeChip){
        for (Creature c : list) {
            if (((c.getCreatureType() == CreatureID.CHIP || c == level.getChip()) && !includeChip)
                    || c.getCreatureType() == CreatureID.DEAD) {
                continue;
            }
            if (c.getPosition().equals(position)) {
                return c;
            }
        }
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

    public int getIndexOfCreature(Creature creature) {
        for (int i=0; i < list.length; i++) {
            if (creature == list[i]) return i;
        }
        return -1;
    }

    public boolean getTeethStep() {
        return teethStep;
    }

    public List<Creature> getNewClones() {
        return newClones;
    }

    public Creature getChipToCr() {
        return chipToCr;
    }

    public void setChipToCr(Creature cr) {
        chipToCr = cr;
    }

    public void setCreatures(Creature[] creatures, Layer layerFG) {
        list = creatures;
        for (Creature c : list)
            c.setLevel(level);
    }

    /** Returns true if a position is claimed by a creature (usually meaning a creature occupies the tile)
     *
     * @param position The position to check (must be a valid position between 0 and 31 on x and y).
     * @return If the position is claimed.
     */
    public abstract boolean claimed(Position position);

    /** Attach or detach a claim to a position.
     *
     * @param position The position to claim.
     * @param claim the state to set the position claim to.
     */
    public abstract void adjustClaim(Position position, boolean claim);

    /**
     * @return the array of claimed positions.
     */
    public abstract boolean[] getClaimedArray();

    public abstract void setClaimedArray(boolean[] claimedArray);

    /** Returns the animation currently occurring at the given position (null if there is no animation).
     *
     * @param position The position to check.
     * @return A animation at the given position (null if there is no such creature).
     */
    public abstract Creature animationAt(Position position);

    public abstract void initialise();

    public abstract void finalise();

    public abstract void tick();

    public abstract void addClone(Position position);

    public abstract void springTrappedCreature(Position position);

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

    public void setLevel(Level level){
        this.level = level;
        newClones = new ArrayList<>();
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
