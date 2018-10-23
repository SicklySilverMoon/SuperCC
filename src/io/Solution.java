package io;

import game.Level;
import game.Step;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class Solution{

    public static final int QUARTER_MOVES = 0,
                            HALF_MOVES = 1,
                            SUCC_MOVES = 2;

    public byte[] halfMoves;
    public int rngSeed;
    public Step step;

    private static byte[] succToHalfMoves(byte[] mixedMoves){
        String moves = new String(mixedMoves);
        moves = moves.replaceAll("U", "u-")
                .replaceAll("L", "l-")
                .replaceAll("D", "d-")
                .replaceAll("R", "r-")
                .replaceAll("_", "--");
        return moves.getBytes();
    }
    private static byte[] quarterToHalfMoves(byte[] quarterMoves){
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        for (int i = 0; i < quarterMoves.length; i += 2){
            byte b = quarterMoves[i];
            if (b == '-' && i+1 < quarterMoves.length){
                b = quarterMoves[i+1];
            }
            writer.write(b);
        }
        return writer.toByteArray();
    }
    
    public Solution(String s){
        try {
            String[] lines = s.split("\n");
            step = Step.valueOf(lines[0].substring(5));
            rngSeed = Integer.valueOf(lines[1].substring(5));
            if (lines.length > 2) halfMoves = lines[2].getBytes();
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
            + new String(halfMoves);
    }

    public Solution(byte[] moves, int rngSeed, Step step, int format){
        if (format == QUARTER_MOVES) this.halfMoves = quarterToHalfMoves(moves);
        else if (format == SUCC_MOVES) this.halfMoves = succToHalfMoves(moves);
        else if (format == HALF_MOVES) this.halfMoves = moves;
        this.rngSeed = rngSeed;
        this.step = step;
    }

}
