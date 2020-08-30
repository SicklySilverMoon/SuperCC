package emulator;

import game.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import util.CharList;

import java.io.CharArrayWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import static emulator.SuperCC.*;

public class Solution{

    public static final String STEP = "Step", SEED = "Seed", MOVES = "Moves", ENCODE = "Encode", RULE = "Rule", SLIDE = "Initial Slide";
    
    public static final int QUARTER_MOVES = 0,
                            HALF_MOVES = 1,
                            SUCC_MOVES = 2;

    public char[] halfMoves;
    public int rngSeed;
    public Step step;
    public Ruleset ruleset;
    public Direction initialSlide;
    public final String encoding = "UTF-8";
    
    public double efficiency = -1;
    
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put(STEP, step.toString());
        json.put(SEED, Integer.toString(rngSeed));
        json.put(SLIDE, initialSlide.toString());
        json.put(RULE, ruleset.toString());
        json.put(ENCODE, encoding);
        json.put(MOVES, new String(halfMoves));
        return json;
    }

    public static Solution fromJSON(String s){
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(s);
            Step step = Step.valueOf((String) json.get(STEP));
            int rngSeed = Integer.parseInt((String) json.get(SEED));
            char[] halfMoves = ((String) json.get(MOVES)).toCharArray();
            String ruleString = (String) json.get(RULE);
            String slideString = (String) json.get(SLIDE);
            Ruleset ruleset = Ruleset.valueOf(ruleString == null ? "MS" : ruleString);
            Direction slidingDirection = Direction.valueOf(slideString == null ? "UP" : slideString);
            return new Solution(halfMoves, rngSeed, step, HALF_MOVES, ruleset, slidingDirection);
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
            if (!emulator.throwQuestion("Solution has a different ruleset than currently selected, change rulesets?")) return;

        emulator.loadLevel(emulator.getLevel().getLevelNumber(), rngSeed, step, false, ruleset, initialSlide);
        tickHalfMoves(emulator, tickFlags);
        if(emulator.hasGui) {
            emulator.getMainWindow().repaint(true);
        }
    }
    
    public void loadMoves(SuperCC emulator, TickFlags tickFlags, boolean repaint){
        tickHalfMoves(emulator, tickFlags);
        if (repaint) emulator.getMainWindow().repaint(true);
    }

    private void tickHalfMoves(SuperCC emulator, TickFlags tickFlags) {
        Level level = emulator.getLevel();
        try{
            for (int move = 0; move < halfMoves.length; move++){
                char c = halfMoves[move];
                if (c == CHIP_RELATIVE_CLICK){
                    int x = halfMoves[++move] - 9;
                    int y = halfMoves[++move] - 9;
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
                boolean tickedTwice = emulator.tick(c, tickFlags);
                if (tickedTwice && (!isClick(c) && c != WAIT)) move++; //todo: switch to a system where its not constantly passed between byte and char, so that clicks and waits can be properly capitalized so as to avoid this shit
                if (level.getChip().isDead()) {
                    break;
                }
            }
        }
        catch (Exception e){
            emulator.throwError("Something went wrong:\n"+e.getMessage());
        }
    }
    
    private static char[] succToHalfMoves(char[] succMoves){
        CharArrayWriter writer = new CharArrayWriter();
        for (char c : succMoves){
            if (c != WAIT) for (char l : SuperCC.lowerCase(c)) writer.write(l);
            else writer.write(c);
        }
        return writer.toCharArray();
    }
    private static char[] succToHalfMoves(CharList succMoves){
        return succToHalfMoves(succMoves.toArray());
    }

    private static char[] quarterToHalfMoves(char[] quarterMoves) {
        CharArrayWriter writer = new CharArrayWriter();
//        System.out.println(Arrays.toString(quarterMoves));

        Set<Character> directions = new HashSet<>(); //Makes it so that i don't have to write out 8 equality checks
        Collections.addAll(directions, UP, DOWN, LEFT, RIGHT, UP_LEFT, DOWN_LEFT, UP_RIGHT, DOWN_RIGHT);

        for (int i = 0; i < quarterMoves.length; i += 2) {
            char a = quarterMoves[i];
            char c = 0;
            int j = i+1;

            if (a == CHIP_RELATIVE_CLICK && j+2 < quarterMoves.length) j += 2; //Since mouse moves take 3 bytes this just makes sure that c is always the intended move/wait and not part of the mouse bytes
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
                        ++i; //Puts the reader right into the first direction so that the i += 2 at the start jumps to the next pair of quarter moves
                        continue;
                    }
                }
                if (a == CHIP_RELATIVE_CLICK) {
                    writer.write(a);
                    writer.write(quarterMoves[++i]);
                    writer.write(quarterMoves[++i]); //Puts the reader right into the second direction so that the i += 2 at the start jumps to the next pair of quarter moves
                    continue;
                }
            }
        }
//        System.out.println(Arrays.toString(writer.toCharArray()));
        return writer.toCharArray();
    }
    
    @Override
    public String toString(){
        return toJSON().toJSONString();
    }
    
    public Solution(char[] moves, int rngSeed, Step step, int format, Ruleset ruleset, Direction initialSlide){
        if (format == QUARTER_MOVES) this.halfMoves = quarterToHalfMoves(moves);
        else if (format == SUCC_MOVES) this.halfMoves = succToHalfMoves(moves);
        else if (format == HALF_MOVES) this.halfMoves = moves;
        this.rngSeed = rngSeed;
        this.step = step;
        this.ruleset = ruleset;
        this.initialSlide = initialSlide;
    }
    
    public Solution(CharList moves, int rngSeed, Step step, Ruleset ruleset, Direction initialSlide){
        this.halfMoves = succToHalfMoves(moves);
        this.rngSeed = rngSeed;
        this.step = step;
        this.ruleset = ruleset;
        this.initialSlide = initialSlide;
        //for (int move = 0; move < halfMoves.length; move++) System.out.println(halfMoves[move]);
    }

}
