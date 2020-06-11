package io;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SuccPaths {

    private final File settingsFile;
    private Map<String, String> settingsMap;
    
    private void updateSettingsFile() {
        try (PrintWriter writer = new PrintWriter(settingsFile, "ISO_8859_1")) {
            writer.println("[Paths]"); //todo: refactor this into loops lol
            writer.printf("%s = %s", "Levelset", settingsMap.get("Paths:Levelset"));
            writer.println();
            writer.printf("%s = %s", "TWS", settingsMap.get("Paths:TWS"));
            writer.println();
            writer.printf("%s = %s", "succ", settingsMap.get("Paths:succ"));
            writer.println();
            writer.println();

            writer.println("[Controls]");
            writer.printf("%s = %s", "Up", settingsMap.get("Controls:Up"));
            writer.println();
            writer.printf("%s = %s", "Left", settingsMap.get("Controls:Left"));
            writer.println();
            writer.printf("%s = %s", "Down", settingsMap.get("Controls:Down"));
            writer.println();
            writer.printf("%s = %s", "Right", settingsMap.get("Controls:Right"));
            writer.println();
            writer.printf("%s = %s", "HalfWait", settingsMap.get("Controls:HalfWait"));
            writer.println();
            writer.printf("%s = %s", "FullWait", settingsMap.get("Controls:FullWait"));
            writer.println();
            writer.printf("%s = %s", "Rewind", settingsMap.get("Controls:Rewind"));
            writer.println();
            writer.printf("%s = %s", "Play", settingsMap.get("Controls:Play"));
            writer.println();
            writer.println();

            writer.println("[Graphics]");
            writer.printf("%s = %s", "TilesheetNum", settingsMap.get("Graphics:TilesheetNum"));
            writer.println();
            writer.printf("%s = %s", "TileWidth", settingsMap.get("Graphics:TileWidth"));
            writer.println();
            writer.printf("%s = %s", "TileHeight", settingsMap.get("Graphics:TileHeight"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLevelsetPath() {
        String levelset = settingsMap.get("Paths:Levelset");
        if (levelset != null) return levelset;
        else {
            setLevelsetPath("");
            return "";
        }
    }
    public String getTwsPath() {
        String tws = settingsMap.get("Paths:TWS");
        if (tws != null) return tws;
        else {
            setTwsPath("");
            return "";
        }
    }
    public String getSuccPath() {
        String succ = settingsMap.get("Paths:succ");
        if (succ != null) return succ;
        else {
            setSuccPath("");
            return "";
        }
    }
    public int[] getControls() {
        try {
            int up = Integer.parseInt(settingsMap.get("Controls:Up"));
            int left = Integer.parseInt(settingsMap.get("Controls:Left"));
            int down = Integer.parseInt(settingsMap.get("Controls:Down"));
            int right = Integer.parseInt(settingsMap.get("Controls:Right"));
            int halfWait = Integer.parseInt(settingsMap.get("Controls:HalfWait"));
            int fullWait = Integer.parseInt(settingsMap.get("Controls:FullWait"));
            int rewind = Integer.parseInt(settingsMap.get("Controls:Rewind"));
            int play = Integer.parseInt(settingsMap.get("Controls:Play"));
        return new int[]{up, left, down, right, halfWait, fullWait, rewind, play};
        }
        catch (NumberFormatException e) {
            int[] controls = new int[] {38, 37, 40, 39, 32, 27, 8, 10};
            setControls(controls);
            return controls;
        }
    }
    public int getTilesetNum() {
        try {
            return Integer.parseInt(settingsMap.get("Graphics:TilesheetNum"));
        }
        catch (NumberFormatException e) {
            setTilesetNum(0);
            return 0;
        }
    }
    public int[] getTileSizes() {
        try {
            return new int[]{Integer.parseInt(settingsMap.get("Graphics:TileWidth")),
                    Integer.parseInt(settingsMap.get("Graphics:TileHeight"))};
        }
        catch (NumberFormatException e) {
            int[] tileSizes = new int[] {20, 20};
            setTileSizes(tileSizes);
            return tileSizes;
        }
    }
    public String getJSONPath(String levelsetName, int levelNumber, String levelName) {
        String json = getSuccPath();
        new File(Paths.get(json, levelsetName).toString()).mkdirs();
        return Paths.get(json, levelsetName, Integer.toString(levelNumber)+"_"+levelName+".json").toString();
    }
    public String getSccPath(String levelsetName, int levelNumber, String levelName) {
        return Paths.get(getSuccPath(), levelsetName, Integer.toString(levelNumber), levelName+".scc").toString();
    }

    public void setLevelsetPath(String levelsetPath) {
        settingsMap.put("Paths:Levelset", levelsetPath);
        updateSettingsFile();
    }
    public void setTwsPath(String twsPath) {
        settingsMap.put("Paths:TWS", twsPath);
        updateSettingsFile();
    }
    public void setSuccPath(String succPath) {
        settingsMap.put("Paths:succ", succPath);
        updateSettingsFile();
    }
    public void setControls(int[] controls) {
        settingsMap.put("Controls:Up", String.valueOf(controls[0]));
        settingsMap.put("Controls:Left", String.valueOf(controls[1]));
        settingsMap.put("Controls:Down", String.valueOf(controls[2]));
        settingsMap.put("Controls:Right", String.valueOf(controls[3]));
        settingsMap.put("Controls:HalfWait", String.valueOf(controls[4]));
        settingsMap.put("Controls:FullWait", String.valueOf(controls[5]));
        settingsMap.put("Controls:Rewind", String.valueOf(controls[6]));
        settingsMap.put("Controls:Play", String.valueOf(controls[7]));
        updateSettingsFile();
    }
    public void setTilesetNum(int tilesetNum) {
        settingsMap.put("Graphics:TilesheetNum", String.valueOf(tilesetNum));
        updateSettingsFile();
    }
    public void setTileSizes(int[] tileSizes) {
        settingsMap.put("Graphics:TileWidth", String.valueOf(tileSizes[0]));
        settingsMap.put("Graphics:TileHeight", String.valueOf(tileSizes[1]));
        updateSettingsFile();
    }

    public SuccPaths(File settingsFile) throws IOException {
        this.settingsFile = settingsFile;
        settingsMap = new HashMap<>();
        parseSettings(settingsMap, new BufferedReader(new FileReader(settingsFile)));
    }

    private static void parseSettings(Map<String, String> settingsMap, Reader source)
    throws IOException
        {
            BufferedReader reader = new BufferedReader(source);
            String
                    section = "",
                    line = null;
            Pattern headerPattern = null;
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if (line.isEmpty() || line.charAt(0) == ';')
                {
                    continue;
                }
                else if (line.charAt(0) == '[')
                {
                    if (headerPattern == null)
                        headerPattern = Pattern.compile("\\[\\W*(\\w*)\\W*\\]?");
                    Matcher matcher = headerPattern.matcher(line);
                    if (matcher.find()) section = matcher.group(1);
                }
                else
                {
                    int pivot = line.indexOf('=');
                    if (pivot > 0)
                    {
                        String name = line.substring(0, pivot - 1).trim(),
                                value = line.substring(pivot + 1).trim();
                        if (!name.isEmpty())
                            settingsMap.put(section + ':' + name, value);
                        /*`[View]
                          Zoom = 1.0000`
                          would get put into the map as:
                          ["View:Zoom": "1.0000"]
                         */
                    }
                }
            }
        }

    public static void createSettingsFile() {
        try {
            FileWriter fw = new FileWriter("settings.ini");
            fw.write("[Paths]\n" +
                    "Levelset = \n" +
                    "TWS = \n" +
                    "succ = succsave\n" +
                    "\n" +
                    "[Controls]\n" +
                    "Up = 38\n" +
                    "Left = 37\n" +
                    "Down = 40\n" +
                    "Right = 39\n" +
                    "HalfWait = 32\n" +
                    "FullWait = 27\n" +
                    "Rewind = 8\n" +
                    "Play = 10\n" +
                    "\n" +
                    "[Graphics]\n" +
                    "TilesheetNum = 0\n" +
                    "TileWidth = 20\n" +
                    "TileHeight = 20");
            fw.close();
        }
        catch(Exception g) {
            g.printStackTrace();
        }
    }
}
