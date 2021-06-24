package emulator;

import game.Direction;
import game.Ruleset;
import game.Step;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class ArgumentParser {
    private final static String helpLong = "--help", helpShort = "-h", helpQuestion = "-?", twsLong = "--testtws",
            levelLong = "--level", levelShort =  "-l", stepLong = "--step", stepShort = "-s", rffLong = "-rff",
            rffShort = "-f", rngLong = "--rng", rngShort = "-r", rulesLong = "--rules", rulesShort = "-m";

    static void parseArguments(SuperCC emulator, String[] args) throws IllegalArgumentException {
        if (args.length != 0) {
            Set<String> arguments = new HashSet<>(Arrays.asList(helpLong, helpShort, helpQuestion, twsLong, levelLong,
                    levelShort, stepLong, stepShort, rngLong, rngShort, rulesLong, rulesShort));

            Ruleset rules = Ruleset.CURRENT;
            boolean testTWS = false;
            Path levelsetPath = null;
            Path twsPath = null;
            int levelNum = 1;
            Step step = Step.EVEN;
            Direction rffDir = Direction.UP;
            int rng = 0;

            for (int i=0; i < args.length; i++) {
                String s = args[i];
                if (arguments.contains(s.toLowerCase())) {
                    switch (s.toLowerCase()) {
                        case helpLong:
                        case helpShort:
                        case helpQuestion:
                            help();
                            continue;
                        case twsLong:
                            testTWS = true;
                            continue;
                        case levelLong:
                        case levelShort:
                            String levelString = args[++i];
                            try {
                                levelNum = Integer.parseInt(levelString);
                                continue;
                            }
                            catch (NumberFormatException e) {
                                numberError(levelLong+"/"+levelShort, s +" "+ levelString);
                            }
                            continue;
                        case stepLong:
                        case stepShort:
                            String stepString = args[++i];
                            try {
                                step = Step.valueOf(stepString.toUpperCase());
                                continue;
                            }
                            catch (IllegalArgumentException e) {
                                enumError(Arrays.toString(Step.values()), stepLong+"/"+stepShort, s +" "+ stepString);
                            }
                            continue;
                        case rffLong:
                        case rffShort:
                            String rffString = args[++i];
                            try {
                                rffDir = Direction.valueOf(rffString.toUpperCase());
                                continue;
                            }
                            catch (IllegalArgumentException e) {
                                enumError(Arrays.toString(Direction.CARDINALS), rffLong+"/"+rffShort, s +" "+ rffString);
                            }
                            continue;
                        case rulesLong:
                        case rulesShort:
                            String rulesString = args[++i];
                            try {
                                rules = Ruleset.valueOf(rulesString.toUpperCase());
                                continue;
                            }
                            catch (IllegalArgumentException e) {
                                enumError(Arrays.toString(Ruleset.PLAYABLES), rulesLong+"/"+rulesShort, s +" "+ rulesString);
                            }
                            continue;
                        case rngLong:
                        case rngShort:
                            String rngString = args[++i];
                            try {
                                rng = Integer.parseInt(rngString);
                                continue;
                            }
                            catch (NumberFormatException e) {
                                numberError(rngLong+"/"+rngShort, s +" "+ rngString);
                            }
                    }
                }

                Path p = Paths.get(s);
                if (p.toString().toLowerCase().endsWith(".dat") || p.toString().toLowerCase().endsWith(".ccl")) {
                    levelsetPath = p;
                    continue;
                }
                if (p.toString().toLowerCase().endsWith(".tws")) {
                    twsPath = p;
                    continue;
                }
            }

            if (levelsetPath != null) {
                emulator.openLevelset(levelsetPath.toFile());
                emulator.loadLevel(levelNum, rng, step, false, rules, rffDir);

                if (twsPath != null) {
                    emulator.setTWSFile(twsPath.toFile());
                    if (testTWS)
                        emulator.testTWS();
                }
            }
        }
    }

    private static void enumError(String values, String flag, String arg) throws IllegalArgumentException {
        System.err.println("The " + flag + " flag MUST be followed by one of the following: " +
                values);
        throw new IllegalArgumentException(arg);
    }

    private static void numberError(String flag, String arg) throws IllegalArgumentException {
        System.err.println("The " + flag + " flag MUST be followed by an integer number");
        throw new IllegalArgumentException(arg);
    }

    private static void help() {
        //exclude CURRENT and the non cardinal directions
        System.out.println(
                "usage: SuperCC.jar [-h] [LEVELSET [-lr N] [-s STEP] [-f DIR] [-m RULE] [TWS [--testtws]]]\n"+
                        "-h        Display this help and exit.\n"+
                        "-l        Load level number T.\n" +
                        "-r        Load level with starting RNG seed N.\n" +
                        "-s        Load level with a step parity of STEP.\n" +
                        "-f        Load level with an initial random force floor direction of DIR.\n" +
                        "-m        Load level with a ruleset of RULE.\n" +
                        "LEVELSET  Open the levelset given by this path.\n" +
                        "TWS       Set the TWS file to the one given by this path.\n" +
                        "--testtws Perform a unit test on the given TWS file with the given levelset.\n\n" +

                        "STEP must be one of: " + Arrays.toString(Step.values()) + ".\n" +
                        "DIR must be one of:  " + Arrays.toString(Direction.CARDINALS) + ".\n" +
                        "RULE must be one of: " + Arrays.toString(Ruleset.PLAYABLES) + ".\n\n" +

                        "Each flag has an alternate form of: [-h/--help/-?], [-l/--level], [-r/--rng],\n" +
                        "[-s/--step], [-f/--rff], [-m/--rules].");
        System.exit(0);
    }

    private ArgumentParser(){} //static class only, hide the default constructor
}
