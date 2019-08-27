package emulator;

import game.Level;
import game.Position;
import game.Step;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import util.ByteList;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import static emulator.SuperCC.*;

public class Solution{

    public static final String STEP = "Step", SEED = "Seed", MOVES = "Moves";
    
    public static final int QUARTER_MOVES = 0,
                            HALF_MOVES = 1,
                            SUCC_MOVES = 2;

    public byte[] halfMoves;
    public int rngSeed;
    public Step step;
    
    public double efficiency = -1;
    
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put(STEP, step.toString());
        json.put(SEED, Integer.toString(rngSeed));
        json.put(MOVES, new String(halfMoves, StandardCharsets.ISO_8859_1));
        return json;
    }
    
    public void load(SuperCC emulator){
        load(emulator, TickFlags.PRELOADING);
    }
    
    public void load(SuperCC emulator, TickFlags tickFlags){
        emulator.loadLevel(emulator.getLevel().getLevelNumber(), rngSeed, step, false);
        Level level = emulator.getLevel();
        try{
            for (int move = 0; move < halfMoves.length; move++){
                byte b = halfMoves[move];
                if (b == CHIP_RELATIVE_CLICK){
                    int x = halfMoves[++move] - 9;
                    int y = halfMoves[++move] - 9;
                    if (x == 0 && y == 0){                      // idk about this but it fixes thief street
                        b = '-';
                    }
                    else {
                        Position chipPosition = level.getChip().getPosition();
                        Position clickPosition = chipPosition.add(x, y);
                        level.setClick(clickPosition.getIndex());
                        b = clickPosition.clickByte(chipPosition);
                    }
                }
                boolean tickedTwice = emulator.tick(b, tickFlags);
                if (tickedTwice) move++;
                if (level.getChip().isDead()) {
                    break;
                }
            }
        }
        catch (Exception e){
            emulator.throwError("Something went wrong:\n"+e.getMessage());
        }
        emulator.getMainWindow().repaint(level, true);
    }
    
    public void loadMoves(SuperCC emulator, TickFlags tickFlags, boolean repaint){
        Level level = emulator.getLevel();
        try{
            for (int move = 0; move < halfMoves.length; move++){
                byte b = halfMoves[move];
                if (b == CHIP_RELATIVE_CLICK){
                    int x = halfMoves[++move] - 9;
                    int y = halfMoves[++move] - 9;
                    if (x == 0 && y == 0){                      // idk about this but it fixes thief street
                        b = '-';
                    }
                    else {
                        Position chipPosition = level.getChip().getPosition();
                        Position clickPosition = chipPosition.add(x, y);
                        level.setClick(clickPosition.getIndex());
                        b = clickPosition.clickByte(chipPosition);
                    }
                }
                boolean tickedTwice = emulator.tick(b, tickFlags);
                if (tickedTwice) move++;
                if (level.getChip().isDead()) {
                    break;
                }
            }
        }
        catch (Exception e){
            emulator.throwError("Something went wrong:\n"+e.getMessage());
        }
        if (repaint) emulator.getMainWindow().repaint(level, true);
    }
    
    private static byte[] succToHalfMoves(byte[] succMoves){
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        for (byte b : succMoves){
            if (b == 'U'){
                writer.write('u');
                writer.write('-');
            }
            else if (b == 'L'){
                writer.write('l');
                writer.write('-');
            }
            else if (b == 'D'){
                writer.write('d');
                writer.write('-');
            }
            else if (b == 'R'){
                writer.write('r');
                writer.write('-');
            }
            else writer.write(b);
        }
        return writer.toByteArray();
    }
    private static byte[] succToHalfMoves(ByteList succMoves){
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        for (byte b : succMoves){
            if (b == 'U'){
                writer.write('u');
                writer.write('-');
            }
            else if (b == 'L'){
                writer.write('l');
                writer.write('-');
            }
            else if (b == 'D'){
                writer.write('d');
                writer.write('-');
            }
            else if (b == 'R'){
                writer.write('r');
                writer.write('-');
            }
            else writer.write(b);
        }
        return writer.toByteArray();
    }

    private static byte[] quarterToHalfMoves(byte[] quarterMoves) {
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        //System.out.println(Arrays.toString(quarterMoves));

        Set<Byte> cardinals = new HashSet<>(); //Makes it so that i have to write out 8 equality checks
        cardinals.add(UP);
        cardinals.add(DOWN);
        cardinals.add(LEFT);
        cardinals.add(RIGHT);

        for (int i = 0; i < quarterMoves.length; i += 2) {
            byte a = quarterMoves[i];
            byte b = 0;
            int j = i+1;

            if (a == CHIP_RELATIVE_CLICK && j+2 < quarterMoves.length) j += 2; //Since mouse moves take 3 bytes this just makes sure that b is always the intended move/wait and not part of the mouse bytes
            if (j < quarterMoves.length) b = quarterMoves[j];

            if (a == '~' && b == '~') { //It should only write a half wait if BOTH values read are quarter waits
                writer.write('-');
            }
            else { //Input priority things
                if (cardinals.contains(a)) {
                    writer.write(a);
                    continue;
                }
                if (cardinals.contains(b) || b == CHIP_RELATIVE_CLICK) {
                    writer.write(b);
                    if (cardinals.contains(b)) { //Keyboard input check
                        continue;
                    }
                    else if (b == CHIP_RELATIVE_CLICK) {
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
        //System.out.println(Arrays.toString(writer.toByteArray()));
        return writer.toByteArray();
    }
    
    public static Solution fromJSON(String s){
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(s);
            Step step = Step.valueOf((String) json.get(STEP));
            int rngSeed = Integer.parseInt((String) json.get(SEED));
            byte[] halfMoves = ((String) json.get(MOVES)).getBytes(StandardCharsets.ISO_8859_1);
            return new Solution(halfMoves, rngSeed, step, HALF_MOVES);
        }
        catch (Exception e){
            throw new IllegalArgumentException("Invalid solution file:\n" + s);
        }
    }
    
    @Override
    public String toString(){
        return toJSON().toJSONString();
    }
    
    public Solution(byte[] moves, int rngSeed, Step step, int format){
        if (format == QUARTER_MOVES) this.halfMoves = quarterToHalfMoves(moves);
        else if (format == SUCC_MOVES) this.halfMoves = succToHalfMoves(moves);
        else if (format == HALF_MOVES) this.halfMoves = moves;
        this.rngSeed = rngSeed;
        this.step = step;
    }
    
    public Solution(ByteList moves, int rngSeed, Step step){
        this.halfMoves = succToHalfMoves(moves);
        this.rngSeed = rngSeed;
        this.step = step;
        //for (int move = 0; move < halfMoves.length; move++) System.out.println(halfMoves[move]);
    }

}
