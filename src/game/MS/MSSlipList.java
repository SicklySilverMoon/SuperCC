package game.MS;

import game.Creature;
import game.Level;

import java.util.ArrayList;

public class MSSlipList extends game.SlipList {

    @Override
    public void tick(){
        // Iterating like this causes slide delay.
        for (int i = size(); i > 0; i--){
            MSCreature monster = (MSCreature) get(size()-i);
            monster.tick(monster.getSlideDirectionPriority(level.getLayerBG().get(monster.getPosition()), level.getRNG(), false), level, true);
        }
    }

    @Override
    public void setSliplist(Creature[] slidingCreatures){
        clear();
        for (Creature slider : slidingCreatures){
            Creature c = level.getMonsterList().creatureAt(slider.getPosition());
            if (c == null) c = slider;
            add(c);                // Blocks are not in the monster list, so they are added separately
        }
    }

}
