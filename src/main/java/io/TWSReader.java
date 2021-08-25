package io;

import emulator.Solution;
import game.Direction;
import game.Level;
import game.Ruleset;
import game.Step;

import java.io.*;
import java.util.Set;

import static emulator.SuperCC.*;

public class TWSReader{

    private int headerLength;
    private Ruleset ruleset;

    private final File twsFile;

    public void verifyAndInit() throws IOException {
        twsInputStream reader = new twsInputStream(twsFile);
        try{
            if (reader.readInt() != -1717882059)
                throw new IOException("Invalid signature");
            if (reader.readByte() == 2)
                ruleset = Ruleset.MS;
            else
                ruleset = Ruleset.LYNX;
            reader.skip(2);
            int len = reader.readByte();
            headerLength = 8 + len;
            reader.skip(len);
            reader.close();
        }
        catch (IOException e){
            reader.close();
            throw e;
        }
    }

    public Solution readSolution(Level level) throws IOException{
        verifyAndInit();
        twsInputStream reader = new twsInputStream(twsFile);
        reader.skip(headerLength);

        long offset = headerLength;
        int recordLength = 0;

        while (offset < twsFile.length()){
            recordLength = reader.readInt();
            int levelNumber = reader.readShort();
            int twsPassword = reader.readInt();

            byte[] levelPass = level.getPassword().getBytes("Windows-1252");
            long pass = Integer.toUnsignedLong(levelPass[0] + (levelPass[1] << 8) + (levelPass[2] << 16) + (levelPass[3] << 24));

            if (levelNumber == level.getLevelNumber() && twsPassword == pass)
                break;
            offset += recordLength + 4;              //4: length of recordLength
            reader.skip(recordLength - 6);        //6: bytes read since recordLength
        }
        if (offset >= twsFile.length())
            throw new IOException("Level not found in tws");

        if (recordLength == 6)
            throw new IOException("No solution recorded"); //If the offset is equal to 6 it means that the only thing the TWS file stores for that level is its level number, and its password
        reader.bytesRead = 6; //cancel out earlier readings and add len of level number and pass
        reader.readByte();    //Other Flags

        int stepSlideValue = reader.readByte();
        Step step = Step.fromTWS(stepSlideValue);
        Direction initialSlide = Direction.fromTWS(stepSlideValue);

        int rngSeed = reader.readInt();
        int solutionTime = reader.readInt();

        reader.counter = 0;
        CharArrayWriter writer = new CharArrayWriter();
        while (reader.bytesRead < recordLength){
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
        s.melindaRouterGenerated = reader.melindaRouter;
        return s;
    }

    public TWSReader (File twsFile) throws IOException{
        this.twsFile = twsFile;
        verifyAndInit();
    }

    private static class twsInputStream extends FileInputStream{
        private final char[] DIRECTIONS = new char[] {UP, LEFT, DOWN, RIGHT, UP_LEFT, DOWN_LEFT, UP_RIGHT, DOWN_RIGHT};
        private final Set<Character> cardinalSet = Set.of(UP, LEFT, DOWN, RIGHT);

        public int solutionLengthOffset = 0;

        public int ineffiencies = 0;
        public int numCardinals = 0; //These are used for MR detection, counting if 3 ortho moves are in a row with 3 tick between
        public int num3TickTime = 0;
        public boolean melindaRouter = false;

        public int bytesRead = 0;
        public int counter;
        public void readFormat1(int b, Writer writer) throws IOException{
            int length = b & 0b11;
            counter += length;
            int time;
            char direction = DIRECTIONS[(b & 0b11100) >>> 2];
            if (length == 1){
                time = (b & 0b11100000) >>> 5;
            }
            else{
                time = ((b & 0b11100000) >>> 5 | readByte() << 3);
                if (time < 8)
                    ineffiencies++;
            }
            for (int i = 0; i < time; i++)
                writer.write('~');
            writer.write(direction);

            if (cardinalSet.contains(direction) && time == 3) {
                numCardinals++;
                num3TickTime++;
            }
            else {
                numCardinals = 0;
                num3TickTime = 0;
            }
            if (numCardinals == 3 && num3TickTime == 3)
                melindaRouter = true;
        }
        public void readFormat2(int b, Writer writer) throws IOException{
            counter += 4;
            char direction = DIRECTIONS[(b & 0b1100) >>> 2];
            int time = ((b & 0b11100000) >> 5) | readByte() << 3 | readByte() << 11 | (readByte() & 0xf) << 19;
            if (time < 2047)
                ineffiencies++;
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
            for (int i = 0; i < length - 2; i++)
                time |= ((readByte() & (i == 2 ? 0x1f : 0xff)) << (2 + 8*i));
            for (int i = 0; i < time; i++)
                writer.write('~');
            if (length >= 4) {
                if (length == 4 && time < 0x400) //2^10
                    ineffiencies++;
                else if (time < 0x40000) //len 5 and 2^18
                    ineffiencies++;
            }
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
