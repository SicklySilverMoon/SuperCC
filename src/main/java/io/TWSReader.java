package io;

import emulator.Solution;
import game.Direction;
import game.Level;
import game.Ruleset;
import game.Step;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static emulator.SuperCC.*;

public class TWSReader{

    private HashMap<Long, Long> lPassLevelOffsets = new HashMap<>();
    private HashMap<Long, Long> passLevelOffsets = new HashMap<>();

    private final File twsFile;
    private final Ruleset ruleset;

    public Solution readSolution(Level level) throws IOException{
        byte[] password = level.getPassword().getBytes("Windows-1252");
        Long pass = Integer.toUnsignedLong(
                password[0] + 0x100 * password[1] + 0x10000 * password[2] + 0x1000000 * password[3]
        );
        long lpass = pass + (Integer.toUnsignedLong(level.getLevelNumber()) << 32);
        long solutionOffset;
        if (lPassLevelOffsets.containsKey(lpass)) solutionOffset = lPassLevelOffsets.get(lpass);
        else if (passLevelOffsets.containsKey(pass)) solutionOffset = passLevelOffsets.get(pass);
        else throw new IOException("Level not found in tws");

        twsInputStream reader = new twsInputStream(twsFile);
        reader.skip(solutionOffset);

        int solutionLength = reader.readInt();
        reader.bytesRead = 0; //cancel out the length being read
        if (solutionLength == 6) throw new IOException("No solution recorded"); //If the offset is equal to 6 it means that the only thing the TWS file stores for that level is its level number, and its password
        reader.readShort();                     // Level number
        reader.readInt();                       // Password
        reader.readByte();                      // Other Flags (always 0)

        int stepSlideValue = reader.readByte();
        Step step = Step.fromTWS(stepSlideValue);
        Direction initialSlide = Direction.fromTWS(stepSlideValue);

        int rngSeed = reader.readInt();
        int solutionTime = reader.readInt();

        reader.counter = 0;
        CharArrayWriter writer = new CharArrayWriter();
        while (reader.bytesRead < solutionLength){
            int b = reader.readByte();
            try {
                switch (b & 0b11) {
                    case 0:
                        reader.readFormat3(b, writer);
                        break;
                    case 1:
                    case 2:
                        reader.readFormat1(b, writer);
                        break;
                    case 3:
                        if ((b & 0b10000) == 0) reader.readFormat2(b, writer);
                        else reader.readFormat4(b, writer);
                        break;
                }
            }
            catch (Exception e){                    // Some solution files are too long - seems to be caused by long slides at the end of a level
                e.printStackTrace();
                //System.out.println("TWS file too long on level: "+level.getLevelNumber()+" "+Arrays.toString(level.getTitle()));
                break;
            }
        }

        for (int i = writer.size() + reader.solutionLengthOffset; i <= solutionTime; i++)
            writer.write('~');

        Solution s = new Solution(writer.toCharArray(), rngSeed, step, Solution.QUARTER_MOVES, ruleset, initialSlide);
        s.efficiency = 1 - (double) reader.ineffiencies / solutionTime;
        return s;
    }

    public TWSReader (File twsFile) throws IOException{
        this.twsFile = twsFile;
        twsInputStream reader = new twsInputStream(twsFile);
        try{
            if (reader.readInt() != -1717882059) throw new IOException("Invalid signature");
            if (reader.readByte() == 2) ruleset = Ruleset.MS;
            else ruleset = Ruleset.LYNX;
            reader.readByte();
            reader.readByte();
            int length = reader.readByte();
            reader.skip(length);
            long offset = 8 + length;
            int levelOffset;

            while (offset < twsFile.length()){
                levelOffset = reader.readInt();
                int levelNumber = reader.readShort();
                int password = reader.readInt();
                passLevelOffsets.put(Integer.toUnsignedLong(password), offset);
                lPassLevelOffsets.put(Integer.toUnsignedLong(password) + (Integer.toUnsignedLong(levelNumber) << 32), offset);
                reader.skip(levelOffset - 6);       // 10: bytes read since levelOffset
                offset += levelOffset + 4;              // 4: length of levelOffset
            }
        }
        catch (IOException e){
            reader.close();
            throw e;
        }
        reader.close();
    }

    private class twsInputStream extends FileInputStream{
        private final char[] DIRECTIONS = new char[] {UP, LEFT, DOWN, RIGHT, UP_LEFT, DOWN_LEFT, UP_RIGHT, DOWN_RIGHT};
        
        public int solutionLengthOffset = 0;

        public int ineffiencies = 0;

        public int bytesRead = 0;
        public int counter;
        public void readFormat1(int b, Writer writer) throws IOException{
            int length = b & 0b11;
            counter += length;
            int time;
            char direction;
            if (length == 1){
                direction = DIRECTIONS[(b & 0b11100) >>> 2];
                time = (b & 0b11100000) >>> 5;
            }
            else{
                direction = DIRECTIONS[(b & 0b11100) >>> 2];
                time = ((b & 0b11100000) >>> 5 | readByte() << 3);
            }
            for (int i = 0; i < time; i++) writer.write('~');
            writer.write(direction);
        }
        public void readFormat2(int b, Writer writer) throws IOException{
            counter += 4;
            char direction = DIRECTIONS[(b & 0b1100) >>> 2];
            int time = ((b & 0b11100000) >> 5) | readByte() << 3 | readByte() << 11 | (readByte() & 0xf) << 19;
            if (time < 2047) ineffiencies += 1;
            for (int i = 0; i < time; i++) writer.write('~');
            writer.write(direction);
        }
        public void readFormat3(int b, Writer writer) throws IOException{
            counter += 1;
            char[] waits = new char[] {'~', '~', '~'};
            writer.write(waits);
            writer.write(DIRECTIONS[(b >>> 2) & 0b11]);
            writer.write(waits);
            writer.write(DIRECTIONS[(b >>> 4) & 0b11]);
            writer.write(waits);
            writer.write(DIRECTIONS[(b >>> 6) & 0b11]);
        }
        public void readFormat4(int b, Writer writer) throws IOException { //format 4 DOES NOT use the format given in the docs
            final int NORTH = 1, WEST = 2, SOUTH = 4, EAST = 8,
                    NORTHWEST = NORTH | WEST, SOUTHWEST = SOUTH | WEST, SOUTHEAST = SOUTH | EAST,
                    NORTHEAST = NORTH | EAST;
            int length = ((b >>> 2) & 0b11) + 2;
            counter += length;
            int b2 = readByte();
            int d = (b >>> 5) | ((b2 & 0b00111111) << 3);
            int time = (b2 & 0b11000000) >> 6;
            for (int i = 0; i < length - 2; i++) time |= ((readByte() & (i == 2 ? 0x1f : 0xff)) << (2 + 8*i));
            for (int i = 0; i < time; i++) writer.write('~');
            char direction = switch (d) {
                case NORTH -> UP;
                case WEST -> LEFT;
                case SOUTH -> DOWN;
                case EAST -> RIGHT;
                case NORTHWEST -> UP_LEFT;
                case SOUTHWEST -> DOWN_LEFT;
                case SOUTHEAST -> DOWN_RIGHT;
                case NORTHEAST -> UP_RIGHT;
                default -> Character.MAX_VALUE; //mouse moves
            };
            if (direction != Character.MAX_VALUE)
                writer.write(direction);
            else { //mouse moves
                d -= 16;
                int x9 = d % 19;
                int y9 = (d - x9) / 19;
                writer.write(CHIP_RELATIVE_CLICK);
                writer.write(x9);
                writer.write(y9);
                solutionLengthOffset -= 2;
            }
        }

        public twsInputStream (File file) throws FileNotFoundException{
            super(file);
        }
        int readByte() throws IOException{
            bytesRead++;
            return read();
        }
        int readShort() throws IOException{
            bytesRead += 2;
            return read() + (read() << 8);
        }
        int readInt() throws IOException{
            bytesRead += 4;
            return read() + (read() << 8) + (read() << 16) + (read() << 24);
        }
        byte[] readAscii(int length) throws IOException{
            bytesRead += length;
            byte[] asciiBytes = new byte[length];
            read(asciiBytes);
            return asciiBytes;
        }
        byte[] readEncodedAscii(int length) throws IOException{
            bytesRead += length;
            byte[] asciiBytes = new byte[length];
            read(asciiBytes);
            for (int i = 0; i < length; i++) asciiBytes[i] = (byte) ((int) asciiBytes[i] ^ 0x99b);
            return asciiBytes;
        }
    }

}
