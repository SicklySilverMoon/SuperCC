package io;

import emulator.Solution;
import emulator.SuperCC;
import game.Level;
import game.Ruleset;
import util.CharList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TWSWriter{
    
    public static byte[] write(Level level, Solution solution, CharList mouseMoves) {
        try(TWSOutputStream writer = new TWSOutputStream()) {
            writer.writeTWSHeader(level);
            writer.writeInt(writer.solutionLength(solution));
            writer.writeLevelHeader(level, solution);
            int timeBetween = 0;
            boolean firstMove = true;
            int i = 0;
            for (char c : solution.halfMoves) {
                if (c == SuperCC.WAIT) timeBetween += 2;
                else {
                    int relativeClickX;
                    int relativeClickY;
                    int twsRelativeClick = 0;
                    if (SuperCC.isClick(c)) {
                        relativeClickX = mouseMoves.get(i++); //Postfix ++ returns the current value of i then increments it
                        relativeClickY = mouseMoves.get(i++);

                        twsRelativeClick = 16 + ((relativeClickY + 9) * 19) + (relativeClickX + 9);
                    }
                    writer.writeMove(c, timeBetween, firstMove, twsRelativeClick);
                    timeBetween = 2;
                    firstMove = false;
                }
            }
            return writer.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class TWSOutputStream extends ByteArrayOutputStream{

        private final byte UP = 3, LEFT = 7, DOWN = 11, RIGHT = 15;

        //all key directions (u, l, d, r) use format 2 on this page http://www.muppetlabs.com/~breadbox/software/tworld/tworldff.html#3

        void writeMove(char c, int time, boolean firstMove, int relativeClick) throws IOException {
            if (!firstMove) time -= 1;
            byte twsMoveByte;
            boolean useFormat4 = false;
            switch (c) {
                case SuperCC.UP: twsMoveByte = UP; break;
                case SuperCC.LEFT: twsMoveByte = LEFT; break;
                case SuperCC.DOWN: twsMoveByte = DOWN; break;
                case SuperCC.RIGHT: twsMoveByte = RIGHT; break;
                default:
                    twsMoveByte = 0; //turns out if you don't have this it won't compile due to twsMoveByte "may not have been initialized" despite that it'll never get used if this code block activates!
                    useFormat4 = true;
                    //Do all the things involving the type 4 mouse moves either here or next
            }
            if (!useFormat4) { //This is all format 2 which is all SuCC supports using for key moves
                writeFormat2(twsMoveByte, time);
            }
            else {
                writeFormat4(time, relativeClick);
            }
        }
        void writeTWSHeader(Level level) throws IOException {
            writeInt(0x999B3335);                        // Signature
            if (level.getRuleset() == Ruleset.MS) write(2);
            else write(1); //Lynx Ruleset
            writeShort(level.getLevelNumber());
            write(0);
        }
        void writeLevelHeader (Level level, Solution solution) throws IOException {
            writeShort(level.getLevelNumber());
            byte[] password = level.getPassword();
            for (int i = 0; i < 4; i++) write(password[i]);
            write(0);                                   // Other flags
            write(solution.step.toTWS());
            writeInt(solution.rngSeed);
            writeInt(2 * solution.halfMoves.length - 2);
            /* minus 2 because the time value is always 2 extra for unknown reasons
            (likely tick counting differences between TW and SuCC).
            there's actually an issue here in that if Chip slides into the exit in MS Mode
            SuCC writes a time value one higher than it should be,
            this however can't be helped without introducing potential side effects */
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
        public int solutionLength(Solution s) {
            int length = LEVEL_HEADER_SIZE;
            for (char c : s.halfMoves) if (c != SuperCC.WAIT) length += 4;
            return length;
        }
        void writeFormat2(byte twsMoveByte, int time) throws IOException {
            write(twsMoveByte | (time & 0b111) << 5);
            write((time >> 3) & 0xFF);
            write((time >> 11) & 0xFF);
            write((time >> 19) & 0xFF);
        }
        void writeFormat4(int time, int direction) throws IOException {
            // First byte DDD1NN11
            //int numBytes = measureTime(time);
            write(0b1_0011 | (2 << 2) | (direction & 0b111) << 5); //(2 << 2), the first 2 used to be the result of measureTime however as all bytes have to be 4 long its forced to being 2 now

            // Second byte TTDDDDDD
            write (((time & 0b11) << 6) | ((direction & 0b11_1111_000) >> 3));

            // Third byte TTTTTTTT
            //if (numBytes > 0) {
                write ((time & 0b1111_1111_00) >> 2);
            //}

            // Fourth byte TTTTTTTT
            //if (numBytes > 1) {
                write ((time & 0b1111_1111_0000_0000_00) >> 10);
            //}

            // Fifth byte 000TTTTT
//            if (numBytes > 2) {
//                write ((time & 0b1_1111_0000_0000_0000_0000_00) >> 18);
//            }
        }
//        int measureTime(int time) {
//            if ((time & 0b11) == time) {
//                return 0;
//            }
//            else if ((time & 0b11_1111_1111) == time) {
//                return 1;
//            }
//            else if ((time & 0b11_1111_1111_1111_1111) == time) {
//                return 2;
//            }
//            else {
//                return 3;
//            }
//        }
    }
}
