package io;

import game.Level;
import game.Ruleset;
import game.Step;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A class for reading the .dat format.
 * For documentation on the dat format itself please read http://www.seasip.info/ccfile.html
 */
public class DatParser{

    private final static int MSCC_SIGNATURE = 0x0002AAAC;
    private final static int MSCC_PG_SIGNATURE = 0x0003AAAC;
    private final static int TWORLD_LYNX_SIGNATURE = 0x0102AAAC;
    private final static Set<Integer> SIGNATURES = new HashSet<>(Arrays.asList(MSCC_SIGNATURE, MSCC_PG_SIGNATURE, TWORLD_LYNX_SIGNATURE));

    private final File file;
    private long[] levelStart;

    private Ruleset rules;

    public int lastLevel() {
        return levelStart.length;
    }
    
    public String getLevelsetName() {
        return file.getName().replaceFirst("[.][^.]+$", "");
    }

    /**
     * Reads either layer 1 or layer 2 of the .dat file. Only call this if the
     * FileInputStream stream is pointing at the layer data, so that the next
     * word is the "Number of bytes in X layer".
     *
     * @return A 1028 element byte array containing the layer, row by row.
     */
    private byte[] readLayer(DatReader reader) throws IOException{
        byte[] layer = new byte[32*32];
        int bytesInLayer = reader.readWord();
        int i = 0;
        while (i < 1024){
            int b = reader.readUnsignedByte();
            if (b == 0xFF){
                int copies = reader.readUnsignedByte();
                int objectCode = reader.read();
                for (int k = 0; k < copies; k++){
                    layer[i] = (byte) objectCode;
                    i++;
                }
            }
            else{
                layer[i] = (byte) b;
                i++;
            }
        }
        return layer;
    }

    /**
     * Reads a set of button connections from the .dat file. Only call this if
     * the FileInputStream stream is pointing at the connection data, so that
     * the next word is the "Button X position".
     *
     * @param length Length of this field
     * @param trapConnections if true, handles trap connections; else, handles
     *                        clone machine connections
     * @return an nx2 array where each row is {buttonPosition, targetPosition},
     *         where a position is 32*y+x
     */
    private int[][] readConnections(DatReader reader, int length, boolean trapConnections) throws IOException{
        int[][] connections = new int[length][2];
        for (int j = 0; j < length; j++){
            int buttonX = reader.readWord();
            int buttonY = reader.readWord();
            int targetX = reader.readWord();
            int targetY = reader.readWord();
            connections[j][0] = 32*buttonY+buttonX;
            connections[j][1] = 32*targetY+targetX;
            if (trapConnections) reader.readWord();
        }
        return connections;
    }

    /**
     * Load a level from the .dat file.
     * @param level The level number
     * @param rngSeed The starting rng seed
     * @param step The starting step of the level. Either Step.ODD or Step.EVEN
     * @param rules The ruleset to use, a value of CURRENT means to keep the currently selected ruleset (defaulting to file signature)
     * @return a Level object
     */
    public Level parseLevel(int level, int rngSeed, Step step, Ruleset rules) throws IOException{
        DatReader reader = new DatReader(file);
        if (rules != Ruleset.CURRENT) this.rules = rules;
        try {
            reader.skip(levelStart[level]);
            final int levelNumber = reader.readWord();
            int timeLimit = reader.readWord();
            int chips = reader.readWord();
            final int mapDetail = reader.readWord();
            final byte[] layerFG = readLayer(reader);
            final byte[] layerBG = readLayer(reader);
            byte[] title = null;
            int[][] trapConnections = new int[][] {};
            int[][] cloneConnections = new int[][] {};
            byte[] password = null;
            byte[] hint = null;
            int[][] monsterPositions = null;
            int optionalFieldsLength = reader.readWord();
            while (optionalFieldsLength > 0) {
                final int fieldType = reader.readUnsignedByte();
                optionalFieldsLength--;
                final int fieldLength = reader.readUnsignedByte();
                optionalFieldsLength--;
                switch (fieldType) {
                    case 1:
                        timeLimit = reader.readWord();
                        break;
                    case 2:
                        chips = reader.readWord();
                        break;
                    case 3:
                        title = reader.readAscii(fieldLength);
                        break;
                    case 4:
                        trapConnections = readConnections(reader, fieldLength / 10, true);
                        break;
                    case 5:
                        cloneConnections = readConnections(reader, fieldLength / 8, false);
                        break;
                    case 6:
                        password = reader.readEncodedAscii(fieldLength);
                        break;
                    case 7:
                        hint = reader.readAscii(fieldLength);
                        break;
                    case 8:
                        password = reader.readAscii(fieldLength);
                        break;
                    case 9:
                        for (int j = 0; j < fieldLength; j++) reader.readUnsignedByte();
                        break;
                    case 10:
                        int numMonsters = fieldLength / 2;
                        monsterPositions = new int[numMonsters][2];
                        for (int j = 0; j < numMonsters; j++) {
                            monsterPositions[j][0] = reader.readUnsignedByte();
                            monsterPositions[j][1] = reader.readUnsignedByte();
                        }
                        break;
                }
                optionalFieldsLength -= fieldLength;
            }

            reader.close();
            return LevelFactory.makeLevel(levelNumber, timeLimit, chips, layerFG, layerBG, title, trapConnections,
                    cloneConnections, password, hint, monsterPositions, rngSeed, step, lastLevel(), this.rules);
        }
        catch (IOException e){
            reader.close();
            throw(e);
        }
    }

    /**
     * DatParser constructor. The .dat file is skimmed in order to create an
     * array of pointers to each individual level. No levels get loaded in
     * this constructor.
     * @param file The .dat file
     */
    public DatParser(File file) throws IOException {
        this.file = file;
        DatReader reader = new DatReader(file);
        try {
            int signature = reader.readInt32();
            if (!SIGNATURES.contains(signature)) {
                throw new IOException("Invalid signature");
            }
            if (signature == MSCC_SIGNATURE || signature == MSCC_PG_SIGNATURE) rules = Ruleset.MS;
            else rules = Ruleset.LYNX;
            final int levels = reader.readWord();
            long byteN = 4+2;
            levelStart = new long[levels+1];  // +1 because we skip level #0
            for (int i = 1; i <= levels; i++) {
                long bytesInLevel = reader.readWord();
                byteN += 2;
                levelStart[i] = byteN;
                byteN += bytesInLevel;
                reader.skip(bytesInLevel);
            }
            reader.close();
        }
        catch (IOException e){
            reader.close();
            throw e;
        }
    }
    
    private static class DatReader extends FileInputStream{
        private int readUnsignedByte() throws IOException{
            return read() & 0xFF;
        }
        private int readWord() throws IOException{
            return readUnsignedByte() + 256*readUnsignedByte();
        }
        private int readInt32() throws IOException{
            return readUnsignedByte() + 256*readUnsignedByte() + 65536*readUnsignedByte() + 16777216*readUnsignedByte();
        }
        private byte[] readAscii(int length) throws IOException{
            byte[] asciiBytes = new byte[length];
            read(asciiBytes, 0, length-1);
            read();                                         // trailing '\0'
            return asciiBytes;
        }
        private byte[] readEncodedAscii(int length) throws IOException{
            byte[] asciiBytes = new byte[length];
            read(asciiBytes, 0, length-1);
            read();                                         // trailing '\0'
            for (int i = 0; i < length; i++) asciiBytes[i] = (byte) ((int) asciiBytes[i] ^ 0x99);
            return asciiBytes;
        }
        DatReader (File datFile) throws IOException{
            super(datFile);
        }
    }

}
