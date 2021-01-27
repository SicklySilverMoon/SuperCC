package game.MS;

import game.Creature;
import game.Level;

import java.util.ArrayList;

public class SlipList extends ArrayList<Creature> { //This should only ever be used for MS
    protected Level level;

    public void tick(){
        // Iterating like this causes slide delay.
        for (int i = size(); i > 0; i--){
            MSCreature monster = (MSCreature) get(size()-i);
            monster.tick(monster.getSlideDirectionPriority(level.getLayerBG().get(monster.getPosition()), level.getRNG(), false), true);
        }
    }

    public void setLevel(Level level){
        this.level = level;
    }

    Level getLevel(){
        return level;
    }

    public void setSliplist(Creature[] slidingCreatures){
        clear();
        for (Creature slider : slidingCreatures){
            Creature c = level.getMonsterList().creatureAt(slider.getPosition(), false);
            if (c == null) c = slider;
            add(c);                // Blocks are not in the monster list, so they are added separately
        }

        for (Creature c : this)
            c.setLevel(level);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); i++){
            sb.append(i+1);
            sb.append('\t');
            sb.append(get(i));
            sb.append('\n');
        }
        return sb.toString();
    }
}
