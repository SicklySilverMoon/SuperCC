package game;

import java.util.ArrayList;

public abstract class SlipList extends ArrayList<Creature> {
    protected Level level;

    public abstract void tick();

    public void setLevel(Level level){
        this.level = level;
    }

    Level getLevel(){
        return level;
    }

    public abstract void setSliplist(Creature[] slidingCreatures);

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
