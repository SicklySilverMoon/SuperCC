package game;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public abstract class CreatureList implements Iterable<Creature> {
    public static Direction direction;
    public int numDeadMonsters;

    protected Level genLevel;
    protected Creature[] list;
    protected List<Creature> newClones;
    protected boolean blobStep;

    public CreatureList(Creature[] monsters) {
        list = monsters;
        numDeadMonsters = 0;
    }

    public Creature creatureAt(Position position){
        for (Creature c : list) if (c.getPosition().equals(position)) return c;
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

    public abstract void initialise();

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

    @Override
    public void forEach(Consumer<? super Creature> action) {
        for (Creature c : list) action.accept(c);
    }

    @Override
    public Spliterator<Creature> spliterator() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
