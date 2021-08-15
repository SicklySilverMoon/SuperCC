package io;

import emulator.SavestateManager;
import emulator.Solution;
import emulator.SuperCC;
import game.Direction;
import game.Level;
import game.Position;
import game.Ruleset;
import util.CharList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Set;

public class TWSWriter{
    
    public static byte[] write(Level level, Solution solution, SavestateManager savestates) {
        //Goes over the level and transforms all mouse moves into a form TW can handle
        savestates.addSavestate(-1); //shouldn't be possible for a user to save a savestate to this key
        CharList mouseMoves = new CharList();
        savestates.restart();
        ListIterator<Character> itr = savestates.getMoveList().listIterator(false);
        while (itr.hasNext()) {
            char c = itr.next();
            if (SuperCC.isClick(c)) { //Use to math out the relative click position for TWS writing with mouse moves
                Position screenPosition = Position.screenPosition(level.getChip().getPosition());
                Position clickedPosition = Position.clickPosition(screenPosition, c);
                int relativeClickX = clickedPosition.getX() - level.getChip().getPosition().getX(); //down and to the right of Chip are positive, this just quickly gets the relative position following that
                int relativeClickY = clickedPosition.getY() - level.getChip().getPosition().getY();

                mouseMoves.add(relativeClickX);
                mouseMoves.add(relativeClickY);
            }
            savestates.replay();
        }
        savestates.load(-1, level);

        try(TWSOutputStream writer = new TWSOutputStream()) {
            final int ticksPerMove = level.getRuleset().ticksPerMove;

            writer.writeTWSHeader(level, solution);
            writer.writeInt(writer.solutionLength(solution));
            writer.writeLevelHeader(level, solution);
            int timeBetween = 0;
            boolean firstMove = true;
            int i = 0;
            for (char c : solution.basicMoves) {
                if (c == SuperCC.WAIT) {
                    if (ticksPerMove == 2)
                        timeBetween += 2;
                    else
                        timeBetween += 1;
                }
                else {
                    int relativeClickX;
                    int relativeClickY;
                    int twsRelativeClick = 0;
                    if (SuperCC.isClick(c)) {
                        relativeClickX = mouseMoves.get(i++);
                        relativeClickY = mouseMoves.get(i++);

                        twsRelativeClick = 16 + ((relativeClickY + 9) * 19) + (relativeClickX + 9);
                    }
                    writer.writeMove(c, timeBetween, firstMove, twsRelativeClick);
                    if (ticksPerMove == 2)
                        timeBetween = 2;
                    else
                        timeBetween = 1;
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

        private final byte NORTH = 0, WEST = 1, SOUTH = 2, EAST = 3,
                NORTHWEST = -3, SOUTHWEST = -6, NORTHEAST = -9, SOUTHEAST = -12,
                MOUSE = -1;
        private final Set<Byte> CARDINALS = Set.of(NORTH, WEST, SOUTH, EAST);

        //all cardinal directions (u, l, d, r) use format 2 on this page http://www.muppetlabs.com/~breadbox/software/tworld/tworldff.html#3
        void writeMove(char c, int time, boolean firstMove, int relativeClick) throws IOException {
            if (!firstMove)
                time -= 1;
            byte twsMoveByte = switch (c) {
                case SuperCC.UP -> NORTH;
                case SuperCC.LEFT -> WEST;
                case SuperCC.DOWN -> SOUTH;
                case SuperCC.RIGHT -> EAST;
                case SuperCC.UP_LEFT -> NORTHWEST;
                case SuperCC.DOWN_LEFT -> SOUTHWEST;
                case SuperCC.UP_RIGHT -> NORTHEAST;
                case SuperCC.DOWN_RIGHT -> SOUTHEAST;
                default -> MOUSE;
            };
            if (CARDINALS.contains(twsMoveByte))
                writeFormat2(twsMoveByte, time);
            else {
                if (twsMoveByte == MOUSE) { //mouse moves
                    writeFormat4(relativeClick, time);
                }
                else
                    writeFormat4(-twsMoveByte, time);
                /* for some reason format 4 doesn't use the move type mentioned in the docs, it uses the moves as they
                appear in TW's source, which overlap with the ones given in the docs, so we resolve that by assigning
                them negative values and then inverting it here */
            }
        }
        void writeTWSHeader(Level level, Solution solution) throws IOException {
            writeInt(0x999B3335);                        // Signature
            if (level.getRuleset() == Ruleset.MS) write(2);
            else write(1); //Lynx Ruleset
            writeShort(level.getLevelNumber());
            write(5);
            writeInt(Arrays.hashCode(solution.basicMoves)); //sig
            write(0); //in case of further extensions
        }
        void writeLevelHeader(Level level, Solution solution) throws IOException {
            writeShort(level.getLevelNumber());
            byte[] password = level.getPassword().getBytes("Windows-1252");
            for (int i = 0; i < 4; i++)
                write(password[i]);
            write(0x83);                                   // Other flags
            write(solution.step.toTWS() | solution.initialSlide.turn(Direction.LEFT).toTWS()); //Compensation for TW's turning of the RFF on load
            writeInt(solution.rngSeed);
            if (level.getRuleset().ticksPerMove == 2)
                writeInt(2 * solution.basicMoves.length - 2);
            /* minus 2 because the time value is always 2 extra for unknown reasons
            (likely tick counting differences between TW and SuCC).
            there's actually an issue here in that if Chip slides into the exit in MS Mode
            SuCC writes a time value one higher than it should be,
            this however can't be helped without introducing potential side effects */
            else if (level.getRuleset().ticksPerMove == 4)
                writeInt(solution.basicMoves.length - 1);
            //solutions are always 1 extra for unknown reasons, likely TW stopping the counter early or something
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
            for (char c : s.basicMoves) {
                if (c != SuperCC.WAIT)
                    length += 4;
            }
            return length;
        }
        void writeFormat2(byte twsMoveByte, int time) throws IOException {
            write(twsMoveByte << 2 | 0b11 | (time & 0b111) << 5);
            write((time >> 3) & 0xFF);
            write((time >> 11) & 0xFF);
            write(((time >> 19) & 0b00001111) | 011 << 01104);
        }
        void writeFormat4(int direction, int time) throws IOException {
            System.out.println(direction);
            // First byte DDD1NN11
            //int numBytes = measureTime(time);
            write(0b11011 | (direction & 0b111) << 5); //(2 << 2), the first 2 used to be the result of measureTime however as all bytes have to be 4 long its forced to being 2 now

            // Second byte TTDDDDDD
            write(((time & 0b11) << 6) | ((direction & 0b11_1111_000) >> 3));

            // Third byte TTTTTTTT
            //if (numBytes > 0) {
                write((time & 0b1111_1111_00) >> 2);
            //}

            // Fourth byte TTTTTTTT
            //if (numBytes > 1) {
                write((time & 0b1111_1111_0000_0000_00) >> 10);
            //}

            // Fifth byte 000TTTTT
//            if (numBytes > 2) {
//                write((time & 0b1_1111_0000_0000_0000_0000_00) >> 18);
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
