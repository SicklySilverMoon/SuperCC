package io;

import java.awt.event.KeyEvent;
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
        try (PrintWriter writer = new PrintWriter(settingsFile, "UTF-8")) {
            writer.println("[Paths]"); //todo: refactor this into loops lol
            writer.printf("%s = %s\n", "Levelset", settingsMap.get("Paths:Levelset"));
            writer.printf("%s = %s\n", "TWS", settingsMap.get("Paths:TWS"));
            writer.printf("%s = %s\n\n", "succ", settingsMap.get("Paths:succ"));

            writer.println("[Controls]");
            writer.printf("%s = %s\n", "Up", settingsMap.get("Controls:Up"));
            writer.printf("%s = %s\n", "Left", settingsMap.get("Controls:Left"));
            writer.printf("%s = %s\n", "Down", settingsMap.get("Controls:Down"));
            writer.printf("%s = %s\n", "Right", settingsMap.get("Controls:Right"));
            writer.printf("%s = %s\n", "HalfWait", settingsMap.get("Controls:HalfWait"));
            writer.printf("%s = %s\n", "FullWait", settingsMap.get("Controls:FullWait"));
            writer.printf("%s = %s\n", "UpLeft", settingsMap.get("Controls:UpLeft"));
            writer.printf("%s = %s\n", "DownLeft", settingsMap.get("Controls:DownLeft"));
            writer.printf("%s = %s\n", "DownRight", settingsMap.get("Controls:DownRight"));
            writer.printf("%s = %s\n", "UpRight", settingsMap.get("Controls:UpRight"));
            writer.printf("%s = %s\n", "Rewind", settingsMap.get("Controls:Rewind"));
            writer.printf("%s = %s\n\n", "Play", settingsMap.get("Controls:Play"));

            writer.println("[Graphics]");
            writer.printf("%s = %s\n", "TilesheetNum", settingsMap.get("Graphics:TilesheetNum"));
            writer.printf("%s = %s\n", "TileWidth", settingsMap.get("Graphics:TileWidth"));
            writer.printf("%s = %s\n", "TileHeight", settingsMap.get("Graphics:TileHeight"));
            writer.printf("%s = %s", "TWSNotate", settingsMap.get("Graphics:TWSNotate"));
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
    public String getTWSPath() {
        String tws = settingsMap.get("Paths:TWS");
        if (tws != null) return tws;
        else {
            setTWSPath("");
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
        String[] mapKeys = new String[] {"Controls:Up", "Controls:Left", "Controls:Down", "Controls:Right",
                "Controls:UpLeft", "Controls:DownLeft", "Controls:DownRight", "Controls:UpRight",
                "Controls:HalfWait", "Controls:FullWait", "Controls:Rewind", "Controls:Play"};
        int[] controls = new int[mapKeys.length];
        int[] defaultControls = new int[] {KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT,
                KeyEvent.VK_U, KeyEvent.VK_J, KeyEvent.VK_K, KeyEvent.VK_I, KeyEvent.VK_SPACE, KeyEvent.VK_ESCAPE,
                KeyEvent.VK_BACK_SPACE, KeyEvent.VK_ENTER};
        boolean error = false;

        for (int i=0; i < mapKeys.length; i++) {
            try {
                controls[i] = Integer.parseInt(settingsMap.get(mapKeys[i]));
            }
            catch (NumberFormatException e) {
                controls[i] = defaultControls[i];
                error = true;
            }
        }
        if (error)
            setControls(controls);
        return controls;
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
    public boolean getTWSNotation() {
        if (settingsMap.get("Graphics:TWSNotate") != null) return Boolean.parseBoolean(settingsMap.get("Graphics:TWSNotate"));
        else {
            setTWSNotation(false);
            return false;
        }
    }
    public String getJSONPath(String levelsetName, int levelNumber, String levelName, String ruleset) {
        String json = getSuccPath();
        new File(Paths.get(json, levelsetName).toString()).mkdirs();
        return Paths.get(json, levelsetName, levelNumber +"_"+levelName+"-"+ruleset+".json").toString();
    }
    public String getSccPath(String levelsetName, int levelNumber, String levelName) {
        return Paths.get(getSuccPath(), levelsetName, Integer.toString(levelNumber), levelName+".scc").toString();
    }

    public void setLevelsetPath(String levelsetPath) {
        settingsMap.put("Paths:Levelset", levelsetPath);
        updateSettingsFile();
    }
    public void setTWSPath(String twsPath) {
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
        settingsMap.put("Controls:UpLeft", String.valueOf(controls[4]));
        settingsMap.put("Controls:DownLeft", String.valueOf(controls[5]));
        settingsMap.put("Controls:DownRight", String.valueOf(controls[6]));
        settingsMap.put("Controls:UpRight", String.valueOf(controls[7]));
        settingsMap.put("Controls:HalfWait", String.valueOf(controls[8]));
        settingsMap.put("Controls:FullWait", String.valueOf(controls[9]));
        settingsMap.put("Controls:Rewind", String.valueOf(controls[10]));
        settingsMap.put("Controls:Play", String.valueOf(controls[11]));
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
    public void setTWSNotation(boolean twsNotation) {
        settingsMap.put("Graphics:TWSNotate", String.valueOf(twsNotation));
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
