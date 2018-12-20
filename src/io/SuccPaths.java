package io;

import util.ByteList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SuccPaths {
    
    private String tilesetPath;
    private String levelsetPath;
    private String twsPath;
    private String succPath;
    private int[] controls;
    private final File settingsFile;
    
    private void updateSettingsFile() {
        try (PrintWriter writer = new PrintWriter(settingsFile, "ISO_8859_1")) {
            String[] allPaths = new String[] {tilesetPath, levelsetPath, twsPath, succPath};
            for (String path : allPaths) {
                writer.println(path);
            }
            for (int b : controls) {
                writer.println(b);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getGetTilesetPath() {
        return tilesetPath;
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
    public String getJSONPath(String levelsetName, int levelNumber, String levelName) {
        new File(Paths.get(succPath, levelsetName).toString()).mkdirs();
        return Paths.get(succPath, levelsetName, Integer.toString(levelNumber)+"_"+levelName+".json").toString();
    }
    public String getSccPath(String levelsetName, int levelNumber, String levelName) {
        return Paths.get(succPath, levelsetName, Integer.toString(levelNumber), levelName+".scc").toString();
    }
    public void setTilesetPath(String tilesetPath) {
        this.tilesetPath = tilesetPath;
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
    
    public SuccPaths(File settingsFile) throws IOException {
        this.settingsFile = settingsFile;
        try (BufferedReader reader = new BufferedReader(new FileReader(settingsFile))) {
    
            tilesetPath = reader.readLine();
    
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
            int[] controlsList = new int[16];
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                controlsList[i++] = Integer.parseInt(line);
            }
            controls = new int[i];
            System.arraycopy(controlsList, 0, controls, 0, i);
            
        }
    }
    
}
