package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SuccPaths {
    
    private String tilesetPath;
    private String levelsetPath;
    private String twsPath;
    private String succPath;
    
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
    }
    public void setLevelsetPath(String levelsetPath) {
        this.levelsetPath = levelsetPath;
    }
    public void setTwsPath(String twsPath) {
        this.twsPath = twsPath;
    }
    public void setSuccPath(String succPath) {
        this.succPath = succPath;
    }
    
    public SuccPaths(File settingsFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(settingsFile))) {
    
            tilesetPath = reader.readLine();
            if (!new File(tilesetPath).exists()) throw new IOException("tilesetFile " + tilesetPath + " does not exist");
            
            String[] folders = new String[] {levelsetPath, twsPath, succPath};
            
            for (String folder : folders) {
                folder = reader.readLine();
                if (!new File(folder).exists()) {
                    try {
                        new File(folder).mkdirs();
                    }
                    catch (SecurityException e) {
                        folder = "";
                    }
                }
            }
            
        }
    }
    
}
