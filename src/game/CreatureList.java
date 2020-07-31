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
    public int numDeadMonsters;

    protected Level level;
    protected Creature[] list;
    protected List<Creature> newClones;
    protected boolean blobStep;

    public CreatureList(Creature[] monsters) {
        list = monsters;
        numDeadMonsters = 0;
    }

    public Creature creatureAt(Position position){
        for (Creature c : list) {
            if (c.getPosition().equals(position)) return c;
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

    public List<Creature> getNewClones() {
        return newClones;
    }

    public void setCreatures(Creature[] creatures) {
        list = creatures;
    }

    /** Returns the number of creatures currently occupying the specified position
     *
     * @param position The position to check (must be a valid position between 0 and 31 on x and y).
     * @return Number of creatures located within the given position.
     */
    public abstract int getCreaturesAtPosition(Position position);

    public abstract void initialise();

    public abstract void finalise();

    public abstract void tick();

    public abstract void addClone(Position position);

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
