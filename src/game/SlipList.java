package game;

import java.util.ArrayList;

public class SlipList extends ArrayList<Creature>{

    private Level level;

    void tick(){
        // Iterating like this causes slide delay.
        for (int i = size(); i > 0; i--){
            Creature monster = get(size()-i);
            monster.tick(monster.getSlideDirectionPriority(level.layerBG[monster.getPosition()], level.rng), level);
        }
    }

    void setLevel(Level level){
        this.level = level;
    }
    Level getLevel(){
        return level;
    }

    SlipList(Creature[] monsters, Level level){
        super();
        sliders: for (Creature slider : monsters){
            for (Creature monster : level.monsterList.list){
                if (slider.getPosition() == monster.getPosition()){
                    add(monster);
                    continue sliders;
                }
            }
            add(slider);                // Blocks are not in the monster list, so they are added separately
        }
        this.level = level;
    }
    public SlipList() {}

    @Override
    public String toString(){
        String str = "";
        for (int i = 0; i < size(); i++){
            str += i + "\t" + get(i)+"\n";
        }
        return str;
    }

}
