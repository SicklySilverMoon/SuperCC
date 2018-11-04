package game;

import util.FixedCapacityList;

import java.util.Objects;

public class SlipList extends FixedCapacityList<Creature> {

    private Level level;
    
    void tick(){
        // Iterating like this causes slide delay.
        for (int i = size(); i > 0; i--){
            Creature monster = get(size()-i);
            monster.tick(monster.getSlideDirectionPriority(level.layerBG.get(monster.getPosition()), level.rng), level);
        }
    }

    void setLevel(Level level){
        this.level = level;
    }
    Level getLevel(){
        return level;
    }

    public void setSliplist(Creature[] slidingCreatures){
        clear();
        for (Creature slider : slidingCreatures){
            Creature c = level.monsterList.creatureAt(slider.getPosition());
            if (c == null) c = slider;
            add(c);                // Blocks are not in the monster list, so they are added separately
        }
    }
    public SlipList(int capacity) {
        super(capacity);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); i++){
            sb.append(i);
            sb.append('\t');
            sb.append(get(i));
            sb.append('\n');
        }
        return sb.toString();
    }

}
