package io;

import emulator.Solution;
import game.Level;
import game.Step;

import java.io.*;
import java.util.HashMap;

import static emulator.SuperCC.CHIP_RELATIVE_CLICK;

public class TWSReader{

    private HashMap<Long, Long> lPassLevelOffsets = new HashMap<>();
    private HashMap<Long, Long> passLevelOffsets = new HashMap<>();

    private final File twsFile;

    public Solution readSolution(Level level) throws IOException{
        byte[] password = level.getPassword();
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

        int offset = reader.readInt();
        reader.readShort();                     // Level number
        reader.readInt();                       // Password
        reader.readByte();                      // Other Flags (always 0)

        Step step = Step.fromTWS(reader.readByte());

        int rngSeed = reader.readInt();
        int solutionLength = reader.readInt();

        reader.counter = 0;
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        while (writer.size() + reader.solutionLengthOffset < solutionLength){
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
                        if ((b & 0x10) == 0) reader.readFormat2(b, writer);
                        else reader.readFormat4(b, writer);
                        break;
                }
            }
            catch (Exception e){                    // Some solution files are too long - seems to be caused by long
                break;                              // slides at the end of a level
            }
        }
        Solution s = new Solution(writer.toByteArray(), rngSeed, step, Solution.QUARTER_MOVES);
        s.efficiency = (double) solutionLength / offset;
        return s;
    }

    public TWSReader (File twsFile) throws IOException{
        this.twsFile = twsFile;
        twsInputStream reader = new twsInputStream(twsFile);
        try{
            if (reader.readInt() != -1717882059) throw new IOException("Invalid signature");
            if (reader.readByte() != 2) throw new IOException("Incorrect ruleset");
            reader.readByte();
            reader.readByte();
            int length = reader.readByte();
            reader.skip(length);
            long offset = 8 + length;
            int levelOffset;

            while (offset != twsFile.length()){
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
        private final byte[] DIRECTIONS = new byte[] {'u', 'l', 'd', 'r'};
        
        public int solutionLengthOffset = 0;

        public int counter;
        public void readFormat1(int b, ByteArrayOutputStream writer) throws IOException{
            int length = b & 0b11;
            counter += length;
            int time;
            byte direction;
            if (length == 1){
                direction = DIRECTIONS[(b & 0b11100) >>> 2];
                time = (b & 0b11100000) >>> 5;
            }
            else{
                direction = DIRECTIONS[(b & 0b11100) >>> 2];
                time = ((b & 0b11100000) >>> 5 | readByte() << 3);
            }
            for (int i = 0; i < time; i++) writer.write('-');
            writer.write(direction);
        }
        public void readFormat2(int b, ByteArrayOutputStream writer) throws IOException{
            counter += 4;
            byte direction = DIRECTIONS[(b & 0b1100) >>> 2];
            int time = ((b & 0b11100000) >> 5) | readByte() << 3 | readByte() << 11 | readByte() << 19;

            for (int i = 0; i < time; i++) writer.write('-');
            writer.write(direction);
        }
        public void readFormat3(int b, ByteArrayOutputStream writer) throws IOException{
            counter += 1;
            byte[] waits = new byte[] {'-', '-', '-'};
            writer.write(waits);
            writer.write(DIRECTIONS[(b >>> 2) & 0b11]);
            writer.write(waits);
            writer.write(DIRECTIONS[(b >>> 4) & 0b11]);
            writer.write(waits);
            writer.write(DIRECTIONS[(b >>> 6) & 0b11]);
        }
        public void readFormat4(int b, ByteArrayOutputStream writer) throws IOException{
            int length = ((b >>> 2) & 0b11) + 2;
            counter += length;
            int b2 = readByte();
            int d = (b >>> 5) | ((b2 & 0b00111111) << 3);
            int time = (b2 & 0b11000000) >> 6;
            for (int i = 2; i < length; i++) time = time | readByte() << (2 + 8*i);
            for (int i = 0; i < time; i++) writer.write('-');
            if (d < 4){
                byte direction = DIRECTIONS[d];
                writer.write(direction);
            }
            else{
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
