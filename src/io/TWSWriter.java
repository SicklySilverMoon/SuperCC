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

    public void readSolution(Level level) throws IOException{
        byte[] password = level.getPassword();
        Long pass = Integer.toUnsignedLong(
                password[0] + 256 * password[1] + 65536 * password[2] + 16777216 * password[3]
        );
        long lpass = pass + (Integer.toUnsignedLong(level.getLevelNumber()) << 32);
        long solutionOffset;
        if (lPassLevelOffsets.containsKey(lpass)) solutionOffset = lPassLevelOffsets.get(lpass);
        else if (passLevelOffsets.containsKey(pass)) solutionOffset = passLevelOffsets.get(pass);
        else throw new IOException("Level not found in tws");

        twsInputStream reader = new twsInputStream(twsFile);
        reader.skip(solutionOffset);

        int offset = reader.readInt();
        reader.readInt();                       // Level number
        reader.readInt();                       // Password
        reader.readByte();                      // Other Flags (always 0)
        reader.readByte();                      // Random slide direction
        int prng = reader.readInt();
        int solutionLength = reader.readInt();
    }
    
    public static void write(File twsFile, Level level, Solution solution) {
        try(TWSOutputStream writer = new TWSOutputStream(twsFile)) {
            writer.writeTWSHeader(level);
            writer.writeInt(writer.solutionLength(solution));
            writer.writeLevelHeader(level, solution);
            for (byte b : solution.halfMoves) writer.writeMove(b, null);
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
        WAIT:                      01011000 10011111
        UP:                        01000000 00011111
        LEFT:                      01000000 00111111
        DOWN:                      01000000 01011111
        RIGHT:                     01000000 01111111
         */
        private final byte[] WAIT = new byte[] {-97, 88, 0, 0, 0};
        private final byte[] UP = new byte[] {31, 64, 0, 0, 0};
        private final byte[] LEFT = new byte[] {63, 64, 0, 0, 0};
        private final byte[] DOWN = new byte[] {95, 64, 0, 0, 0};
        private final byte[] RIGHT = new byte[] {127, 64, 0, 0, 0};
        
        void writeMove (byte b, Position chipPosition) throws IOException {
            switch (b) {
                case '-': write(WAIT); break;
                case 'u': write(UP); break;
                case 'l': write(LEFT); break;
                case 'd': write(DOWN); break;
                case 'r': write(RIGHT); break;
                default:
                    Position clickPosition = Position.clickPosition(Position.screenPosition(chipPosition), b);
                    int x = chipPosition.getX() - clickPosition.getX();
                    int y = chipPosition.getY() - clickPosition.getY();
                    int value = 16 + ((y + 9) * 19) + (x + 9);
                    write((value << 6) | 0b11111);
                    write(0b01000000 | (value >>> 3));
                    write(0);
                    write(0);
                    write(0);
            }
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
            writeInt(solution.halfMoves.length << 2 - 1);
        }
    
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
            return 16 + s.halfMoves.length * 5;
        }
    }

    private class twsInputStream extends FileInputStream{
        public twsInputStream (File file) throws FileNotFoundException{
            super(file);
        }
        int readByte() throws IOException{
            return read();
        }
        int readShort() throws IOException{
            return read() + 256*read();
        }
        int readInt() throws IOException{
            return read() + 256 * read() + 65536 * read() + 16777216 * read();
        }
        byte[] readAscii(int length) throws IOException{
            byte[] asciiBytes = new byte[length];
            read(asciiBytes);
            return asciiBytes;
        }
        byte[] readEncodedAscii(int length) throws IOException{
            byte[] asciiBytes = new byte[length];
            read(asciiBytes);
            for (int i = 0; i < length; i++) asciiBytes[i] = (byte) ((int) asciiBytes[i] ^ 0x99b);
            return asciiBytes;
        }
    }

}
