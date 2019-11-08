package io;

import java.io.*;
import java.nio.file.Paths;

public class SuccPaths {
    
    private String tilesetNum;
    private String levelsetPath;
    private String twsPath;
    private String succPath;
    private int[] controls;
    private int[] tileSizes;
    private final File settingsFile;
    
    private void updateSettingsFile() {
        try (PrintWriter writer = new PrintWriter(settingsFile, "ISO_8859_1")) {
            String[] allPaths = new String[] {tilesetNum, levelsetPath, twsPath, succPath};
            for (String path : allPaths) {
                writer.println(path);
            }
            for (int b : controls) {
                writer.println(b);
            }
            for (int b : tileSizes) {
                writer.println(b);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public int getTilesetNum() {
        try {
            return Integer.parseInt(tilesetNum);
        } catch (NumberFormatException e) {
            setTilesetNum("0"); //Sets the line to be this so this error doesn't happen in future
            return 0; //The value for the TW CCEdit sheet
        }
    }
    public String getLevelsetPath() {
        return levelsetPath;
    }
    public String getTwsPath() {
        return twsPath;
    }
    public String getSuccPath() {
        return succPath;
    }
    public int[] getControls() {
        return controls;
    }
    public int[] getTileSizes() {
        return tileSizes;
    }
    public String getJSONPath(String levelsetName, int levelNumber, String levelName) {
        new File(Paths.get(succPath, levelsetName).toString()).mkdirs();
        return Paths.get(succPath, levelsetName, Integer.toString(levelNumber)+"_"+levelName+".json").toString();
    }
    public String getSccPath(String levelsetName, int levelNumber, String levelName) {
        return Paths.get(succPath, levelsetName, Integer.toString(levelNumber), levelName+".scc").toString();
    }
    public void setTilesetNum(String tilesetNum) {
        this.tilesetNum = tilesetNum;
        updateSettingsFile();
    }
    public void setLevelsetPath(String levelsetPath) {
        this.levelsetPath = levelsetPath;
        updateSettingsFile();
    }
    public void setTwsPath(String twsPath) {
        this.twsPath = twsPath;
        updateSettingsFile();
    }
    public void setSuccPath(String succPath) {
        this.succPath = succPath;
        updateSettingsFile();
    }
    public void setControls(int[] controls) {
        this.controls = controls;
        updateSettingsFile();
    }
    public void setTileSizes(int[] tileSizes) {
        this.tileSizes = tileSizes;
        updateSettingsFile();
    }
    
    public SuccPaths(File settingsFile) throws IOException {
        this.settingsFile = settingsFile;
        try (BufferedReader reader = new BufferedReader(new FileReader(settingsFile))) {
    
            tilesetNum = reader.readLine();
    
            levelsetPath = reader.readLine();
            if (!new File(levelsetPath).exists()) {
                try {
                    new File(levelsetPath).mkdirs();
                }
                catch (SecurityException e) {
                    levelsetPath = "";
                }
            }
            twsPath = reader.readLine();
            if (!new File(twsPath).exists()) {
                try {
                    new File(twsPath).mkdirs();
                }
                catch (SecurityException e) {
                    twsPath = "";
                }
            }
            succPath = reader.readLine();
            if (!new File(succPath).exists()) {
                try {
                    new File(succPath).mkdirs();
                }
                catch (SecurityException e) {
                    succPath = "";
                }
            }
            int[] controlsList = new int[8];
            String line;
            int i = 0;
            while (i <= 7 && (line = reader.readLine()) != null) {
                controlsList[i++] = Integer.parseInt(line);
            }
            controls = new int[i];
            System.arraycopy(controlsList, 0, controls, 0, i);

            int[] tileSizesList = new int[2];
            String line2;
            int j = 0;
            while (j <= 1) {
                line2 = reader.readLine();
                try {
                    tileSizesList[j++] = Integer.parseInt(line2);
                } catch (NumberFormatException e) {
                    tileSizesList[j-1] = 20; //The default value
                    setTileSizes(new int[]{20, 20}); //Write it to the file so the error doesn't happen again
                }
            }
            tileSizes = new int[j];
            System.arraycopy(tileSizesList, 0, tileSizes, 0, j);
            
        }
    }
    
}
