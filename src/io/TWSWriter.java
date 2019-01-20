package io;

import emulator.Solution;
import game.Level;
import game.Position;

import java.io.*;
import java.util.HashMap;

public class TWSWriter{

    private HashMap<Long, Long> lPassLevelOffsets = new HashMap<Long, Long>();
    private HashMap<Long, Long> passLevelOffsets = new HashMap<Long, Long>();

    private File twsFile;
    
    public static void write(File twsFile, Level level, Solution solution) {
        try(TWSOutputStream writer = new TWSOutputStream(twsFile)) {
            writer.writeTWSHeader(level);
            writer.writeInt(writer.solutionLength(solution));
            writer.writeLevelHeader(level, solution);
            int timeBetween = 0;
            boolean firstMove = true;
            for (byte b : solution.halfMoves) {
                if (b == '-') timeBetween += 2;
                else {
                    writer.writeMove(b, timeBetween, firstMove);
                    timeBetween = 2;
                    firstMove = false;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class TWSOutputStream extends FileOutputStream{
        // direction = 196
        /*
        98765432 10987654 32109876 54321098 76543210
        000TTTTT TTTTTTTT TTTTTTTT TTDDDDDD DDD1NN11
        WAIT:                      00011000 10011111
        UP:                        00000000 00011111
        LEFT:                      00000000 00111111
        DOWN:                      00000000 01011111
        RIGHT:                     00000000 01111111
         */
        private final byte[] WAIT = new byte[] {-97, 24, 0, 0, 0};
        /*private final byte[] UP = new byte[] {31, 0, 0, 0, 0};
        private final byte[] LEFT = new byte[] {63, 0, 0, 0, 0};
        private final byte[] DOWN = new byte[] {95, 0, 0, 0, 0};
        private final byte[] RIGHT = new byte[] {127, 0, 0, 0, 0};*/
        private final byte UP = 3, LEFT = 7, DOWN = 11, RIGHT = 15;
        
        void writeMove (byte b, int time, boolean firstMove) throws IOException {
            if (!firstMove) time -= 1;
            byte twsMoveByte;
            switch (b) {
                case 'u': twsMoveByte = UP; break;
                case 'l': twsMoveByte = LEFT; break;
                case 'd': twsMoveByte = DOWN; break;
                case 'r': twsMoveByte = RIGHT; break;
                default:
                    throw new RuntimeException("Solutions with clicks are not supported!");
            }
            write(twsMoveByte | (time & 0b111) << 5);
            write((time >> 3) & 0xFF);
            write((time >> 11) & 0xFF);
            write((time >> 19) & 0xFF);
        }
        void writeTWSHeader (Level level) throws IOException {
            writeInt(0x999B3335);                        // Signature
            write(2);                                   // Ruleset
            writeShort(level.getLevelNumber());
            write(0);
        }
        void writeLevelHeader (Level level, Solution solution) throws IOException {
            writeShort(level.getLevelNumber());
            for (int i = 0; i < 4; i++) write(level.getPassword()[i]);
            write(0);                                   // Other flags
            write(solution.step.toTWS());
            writeInt(solution.rngSeed);
            writeInt(2 * solution.halfMoves.length);
        }
        private static final int LEVEL_HEADER_SIZE = 16;
    
        void writeShort(int i) throws IOException{
            write(i);
            write(i >> 8);
        }
        void writeInt(int i) throws IOException{
            write(i);
            write(i >> 8);
            write(i >> 16);
            write(i >> 24);
        }
        public TWSOutputStream(File file) throws IOException {
            super(file);
        }
        public int solutionLength(Solution s) {
            int c = LEVEL_HEADER_SIZE;
            for (byte b : s.halfMoves) if (b != '-') c += 4;
            return c;
        }
    }

}
