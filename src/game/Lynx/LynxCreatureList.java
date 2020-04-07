package game.Lynx;

import game.CreatureList;
import game.Level;
import game.Position;

public class LynxCreatureList extends CreatureList {

    @Override
    public void initialise() {

    }

    @Override
    public void finalise() {

    }

    @Override
    public void tick() {
        for (int i = list.length - 1; i >= 0; i--) {
            //todo: https://wiki.bitbusters.club/Release_desynchronization#Explanation, see this section
        }
    }

    @Override
    public void addClone(Position position) {

    }

    @Override
    public void setLevel(Level level) {
        this.level = level;
    }

    public LynxCreatureList(LynxCreature[] creatures) {
        super(creatures);
    }
}
