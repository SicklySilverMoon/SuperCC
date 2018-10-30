package io;

import emulator.SuperCC;
import game.Level;
import game.Position;
import game.Step;
import util.ByteList;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static emulator.SuperCC.CHIP_RELATIVE_CLICK;

public class Solution{

    public static final int QUARTER_MOVES = 0,
                            HALF_MOVES = 1,
                            SUCC_MOVES = 2;

    public byte[] halfMoves;
    public int rngSeed;
    public Step step;
    
    public void play(SuperCC emulator){
        emulator.loadLevel(emulator.getLevel().levelNumber, rngSeed, step);
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
                boolean tickedTwice = emulator.tick(b, false);
                if (tickedTwice) move++;
                if (level.getChip().isDead()){
                    break;
                }
            }
            while (level.getChip().isSliding()){
                emulator.tick((byte) '-', new int[] {-1}, false);
                if (level.getChip().isDead()){
                    break;
                }
            }
        }
        catch (Exception e){
            emulator.throwError("Something went wrong:\n"+e.getMessage());
        }
        emulator.getMainWindow().repaint(level, true);
    }
    
    public void play(SuperCC emulator, long waitTime){
        emulator.loadLevel(emulator.getLevel().levelNumber, rngSeed, step);
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
                boolean tickedTwice = emulator.tick(b, true);
                if (tickedTwice){
                    move++;
                    Thread.sleep(waitTime*2);
                }
                else Thread.sleep(waitTime);
                if (level.getChip().isDead()){
                    break;
                }
            }
        }
        catch (Exception e){
            emulator.throwError("Something went wrong:\n"+e.getMessage());
        }
        emulator.getMainWindow().repaint(level, true);
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
    private static byte[] quarterToHalfMoves(byte[] quarterMoves){
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        for (int i = 0; i < quarterMoves.length; i += 2){
            int j = i;
            byte b = quarterMoves[i];
            if (b == '-' && i+1 < quarterMoves.length){
                b = quarterMoves[++j];
            }
            writer.write(b);
            if (b == CHIP_RELATIVE_CLICK){
                writer.write(quarterMoves[++j]);
                writer.write(quarterMoves[++j]);
            }
        }
        return writer.toByteArray();
    }
    
    public Solution(String s){
        try {
            String[] lines = s.split("\n");
            step = Step.valueOf(lines[0].substring(5));
            rngSeed = Integer.valueOf(lines[1].substring(5));
            if (lines.length > 2) halfMoves = lines[2].getBytes(StandardCharsets.ISO_8859_1);
            else halfMoves = new byte[0];
        }
        catch (Exception e){
            throw new IllegalArgumentException("Invalid solution file:\n" + s);
        }
    }
    
    @Override
    public String toString(){
        return "Step " + step + "\n"
            + "Seed " + rngSeed + "\n"
            + new String(halfMoves, StandardCharsets.ISO_8859_1);
    }
    
    public Solution(byte[] moves, int rngSeed, Step step, int format){
        if (format == QUARTER_MOVES) this.halfMoves = quarterToHalfMoves(moves);
        else if (format == SUCC_MOVES) this.halfMoves = succToHalfMoves(moves);
        else if (format == HALF_MOVES) this.halfMoves = moves;
        this.rngSeed = rngSeed;
        this.step = step;
        //for (int move = 0; move < halfMoves.length; move++) System.out.println(halfMoves[move]);
    }
    
    public Solution(ByteList moves, int rngSeed, Step step){
        this.halfMoves = succToHalfMoves(moves);
        this.rngSeed = rngSeed;
        this.step = step;
        //for (int move = 0; move < halfMoves.length; move++) System.out.println(halfMoves[move]);
    }

}
