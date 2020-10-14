package emulator;

import game.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import util.CharList;

import java.io.CharArrayWriter;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import static emulator.SuperCC.*;

public class Solution{

    public static final String STEP = "Step", SEED = "Seed", MOVES = "Moves", ENCODE = "Encode", RULE = "Rule", SLIDE = "Initial Slide";
    
    public static final int QUARTER_MOVES = 0,
                            BASIC_MOVES = 1,
                            SUCC_MOVES = 2;

    public char[] basicMoves;
    public int rngSeed;
    public Step step;
    public Ruleset ruleset;
    public Direction initialSlide;
    public final String encoding = "UTF-8";
    
    public double efficiency = -1;
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put(STEP, step.name());
        json.put(SEED, Integer.toString(rngSeed));
        json.put(SLIDE, initialSlide.toString());
        json.put(RULE, ruleset.toString());
        json.put(ENCODE, encoding);
        json.put(MOVES, new String(basicMoves));
        return json;
    }

    public static Solution fromJSON(String s){
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(s);
            Step step = Step.valueOf((String) json.get(STEP));
            int rngSeed = Integer.parseInt((String) json.get(SEED));
            char[] basicMoves = ((String) json.get(MOVES)).toCharArray();
            String ruleString = (String) json.get(RULE);
            String slideString = (String) json.get(SLIDE);
            Ruleset ruleset = Ruleset.valueOf(ruleString == null ? "MS" : ruleString);
            Direction slidingDirection = Direction.valueOf(slideString == null ? "UP" : slideString);
            return new Solution(basicMoves, rngSeed, step, BASIC_MOVES, ruleset, slidingDirection);
        }
        catch (Exception e){
            throw new IllegalArgumentException("Invalid solution file:\n" + s);
        }
    }
    
    public void load(SuperCC emulator){
        load(emulator, TickFlags.PRELOADING);
    }
    
    public void load(SuperCC emulator, TickFlags tickFlags){
        if (emulator.getLevel().getRuleset() != ruleset)
            if (!emulator.throwQuestion("Solution has a different ruleset than currently selected, change rulesets?"))
                return;

        emulator.loadLevel(emulator.getLevel().getLevelNumber(), rngSeed, step, false, ruleset, initialSlide);
        tickBasicMoves(emulator, tickFlags);
        if(emulator.hasGui) {
            emulator.getMainWindow().repaint(true);
        }
    }
    
    public void loadMoves(SuperCC emulator, TickFlags tickFlags, boolean repaint){
        tickBasicMoves(emulator, tickFlags);
        if (repaint) emulator.getMainWindow().repaint(true);
    }

    private void tickBasicMoves(SuperCC emulator, TickFlags tickFlags) {
        Level level = emulator.getLevel();
        try{
            for (int move = 0; move < basicMoves.length; move++){
                char c = basicMoves[move];
                if (c == CHIP_RELATIVE_CLICK){
                    int x = basicMoves[++move] - 9;
                    int y = basicMoves[++move] - 9;
                    if (x == 0 && y == 0){                      // idk about this but it fixes thief street
                        c = WAIT;
                    }
                    else {
                        Position chipPosition = level.getChip().getPosition();
                        Position clickPosition = chipPosition.add(x, y);
                        level.setClick(clickPosition.getIndex());
                        c = clickPosition.clickChar(chipPosition);
                    }
                }
                boolean tickedMulti = emulator.tick(c, tickFlags);
                if (tickedMulti)
                    move += level.ticksPerMove() - 1;

                if (level.getChip().isDead()) {
                    break;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            emulator.throwError("Something went wrong:\n"+e.getMessage());
        }
    }
    
    private static char[] succToBasicMoves(char[] succMoves, Ruleset ruleset){
        CharArrayWriter writer = new CharArrayWriter();
        for (char c : succMoves){
            if (SuperCC.isUppercase(c)) {
                writer.write(SuperCC.lowerCase(c));
                for (int i=0; i < ruleset.ticksPerMove - 1; i++)
                    writer.write(WAIT);
            }
            else writer.write(c);
        }
        return writer.toCharArray();
    }
    private static char[] succToBasicMoves(CharList succMoves, Ruleset ruleset){
        return succToBasicMoves(succMoves.toArray(), ruleset);
    }

    private static char[] quarterToBasicMoves(char[] quarterMoves, Ruleset ruleset) {
        switch (ruleset.ticksPerMove) {
            case 2:
                return quarterMovesToHalfMoves(quarterMoves);
            case 4:
                return quarterMovesToQuarterBasicMoves(quarterMoves);
            default:
                System.err.println("ENCOUNTERED BAD TICKS PER MOVE VALUES OF: " + ruleset.ticksPerMove);
                return new char[]{};
        }
    }

    private static char[] quarterMovesToHalfMoves(char[] quarterMoves) {
        CharArrayWriter writer = new CharArrayWriter();
//        System.out.println(Arrays.toString(quarterMoves));

        Set<Character> directions = new HashSet<>(); //Makes it so that i don't have to write out 8 equality checks
        Collections.addAll(directions, UP, DOWN, LEFT, RIGHT, UP_LEFT, DOWN_LEFT, UP_RIGHT, DOWN_RIGHT);

        for (int i = 0; i < quarterMoves.length; i += 2) {
            char a = quarterMoves[i];
            char c = 0;
            int j = i+1;

            if (a == CHIP_RELATIVE_CLICK && j+2 < quarterMoves.length) j += 2;
            //Since mouse moves take 3 bytes this just makes sure that c is always the intended move/wait
            //and not part of the mouse bytes
            if (j < quarterMoves.length) c = quarterMoves[j];

            if (a == '~' && c == '~') { //It should only write a half wait if BOTH values read are quarter waits
                writer.write(WAIT);
            }
            else { //Input priority things
                if (directions.contains(a)) {
                    writer.write(a);
                    continue;
                }
                if (directions.contains(c) || c == CHIP_RELATIVE_CLICK) {
                    writer.write(c);
                    if (directions.contains(c)) { //Keyboard input check
                        continue;
                    }
                    else if (c == CHIP_RELATIVE_CLICK) {
                        i = j;
                        writer.write(quarterMoves[++j]);
                        writer.write(quarterMoves[++j]);
                        ++i; //Puts the reader right into the first direction so that the i += 2 at the start jumps to
                            //the next pair of quarter moves
                        continue;
                    }
                }
                if (a == CHIP_RELATIVE_CLICK) {
                    writer.write(a);
                    writer.write(quarterMoves[++i]);
                    writer.write(quarterMoves[++i]);
                    continue;
                }
            }
        }
        writer.write(WAIT);
        //Sometimes solutions end on slides into the exit which TW somehow handles magically even when no waits
        //occur at solution end, so sticking an extra wait onto the end here should be enough to fix that

//        System.out.println(Arrays.toString(writer.toCharArray()));
        return writer.toCharArray();
    }

    private static char[] quarterMovesToQuarterBasicMoves(char[] quarterMoves) {
        CharArrayWriter writer = new CharArrayWriter();
        for (int i=0; i < quarterMoves.length; i++) {
            char c = quarterMoves[i];
            if (c != CHIP_RELATIVE_CLICK)
                writer.write(c == '~' ? '-' : c);
            else {
                writer.write(c);
                writer.write(++i);
                writer.write(++i);
            }
        }
        return writer.toCharArray();
    }

    @Override
    public String toString(){
        return toJSON().toJSONString();
    }
    
    public Solution(char[] moves, int rngSeed, Step step, int format, Ruleset ruleset, Direction initialSlide){
        if (format == QUARTER_MOVES) this.basicMoves = quarterToBasicMoves(moves, ruleset);
        else if (format == SUCC_MOVES) this.basicMoves = succToBasicMoves(moves, ruleset);
        else if (format == BASIC_MOVES) this.basicMoves = moves;
        this.rngSeed = rngSeed;
        this.step = step;
        this.ruleset = ruleset;
        this.initialSlide = initialSlide;
    }
    
    public Solution(CharList moves, int rngSeed, Step step, Ruleset ruleset, Direction initialSlide){
        this.basicMoves = succToBasicMoves(moves, ruleset);
        this.rngSeed = rngSeed;
        this.step = step;
        this.ruleset = ruleset;
        this.initialSlide = initialSlide;
        //for (int move = 0; move < basicMoves.length; move++) System.out.println(basicMoves[move]);
    }

}
