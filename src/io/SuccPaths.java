package io;

import java.io.*;
import java.nio.file.Path;

public class SuccPaths {
    
    private String tilesetPath;
    private String levelsetPath;
    private String twsPath;
    private String succPath;
    private final File settingsFile;
    
    private void updateSettingsFile() {
        try (PrintWriter writer = new PrintWriter(settingsFile, "UTF-8")) {
            String[] allPaths = new String[] {tilesetPath, levelsetPath, twsPath, succPath};
            for (String path : allPaths) {
                writer.println(path);
                System.out.println(path);
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
            
        }
    }
    
}
