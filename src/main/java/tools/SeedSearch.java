package tools;

import emulator.Solution;
import emulator.SuperCC;
import emulator.TickFlags;
import game.Position;
import game.Ruleset;

import javax.swing.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

public class SeedSearch {

    private SuperCC emulator;
    private Solution solution;
    private JPanel panel1;
    private JButton startStopButton;
    private JLabel resultsLabel;
    private JLabel exampleSeedLabel;
    private JLabel startLabel;
    private JTextField startField;
    private JLabel currentSeedLabel;
    private JRadioButton untilExitRadioButton;
    private JRadioButton untilPositionRadioButton;
    private JLabel searchTypeLabel;
    private JTextField positionField;

    private static final int UPDATE_VALUE_RATE = 1000;
    private static final int UPDATE_GUI_RATE = UPDATE_VALUE_RATE * 2;

    private final byte[] startingState;
    private int seed;
    private static boolean killFlag = false;
    private static boolean running = false;
    private static AtomicInteger numAlive = new AtomicInteger(0);
    private DecimalFormat df;

    private AtomicInteger globalAttempts = new AtomicInteger(0);
    private AtomicInteger globalSuccesses = new AtomicInteger(0);
    private AtomicInteger globalLastSuccess = new AtomicInteger(-1);
    private boolean untilPosition = false;
    private Position endPosition = new Position(0, 0);

    public SeedSearch(SuperCC emulator, Solution solution) {

        emulator.loadLevel(emulator.getLevel().getLevelNumber(), seed, solution.step, false,
                Ruleset.CURRENT, solution.initialSlide);
        startingState = emulator.getLevel().save();
    
        resultsLabel.setText("Successes: 0/0 (0%)");
        df = new DecimalFormat("##.####");
    
        startStopButton.addActionListener((e) -> {
            if (running) {
                startStopButton.setText("Resume");
                killFlag = true;
            }
            else {
                if (seed == 0) seed = Integer.parseInt(startField.getText());
                if (untilPosition) {
                    String positionString = positionField.getText().replaceAll("\\s+",""); //Remove whitespace
                    String[] positionStrings = positionString.split(",");
                    int[] positions = {Integer.parseInt(positionStrings[0]), Integer.parseInt(positionStrings[1])};
                    endPosition = new Position(
                            positions[0] < 32 ? positions[0] : 31, //Just a little ternary check to make sure the position are within bounds, and to put them in bounds if they aren't
                            positions[1] < 32 ? positions[1] : 31);
                }
                searchTypeLabel.setVisible(false);
                untilExitRadioButton.setVisible(false);
                untilPositionRadioButton.setVisible(false);
                positionField.setVisible(false);
                startLabel.setVisible(false);
                startField.setVisible(false);
                startStopButton.setText("Pause");
                new SeedSearchThread().start();
            }
        });
        untilPositionRadioButton.addActionListener(e -> {
            positionField.setEnabled(true);
            untilPosition = true;
        });
        untilExitRadioButton.addActionListener(e -> {
            positionField.setEnabled(false);
            untilPosition = false;
        });

        JFrame frame = new JFrame("Seed Search");
        frame.setContentPane(panel1);
        frame.pack();
        frame.setLocationRelativeTo(emulator.getMainWindow());
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent windowEvent) { killFlag = true; }
        });
    }

    private void createUIComponents() {
        resultsLabel = new JLabel();
        exampleSeedLabel = new JLabel("Example seed:");
    }

    private void updateValues(int attemps, int successes, int exampleSuccess) {
        globalAttempts.addAndGet(attemps);
        globalSuccesses.addAndGet(successes);
        if (exampleSuccess >= 0)
            globalLastSuccess.set(exampleSuccess);
        if (attemps % UPDATE_GUI_RATE == 0)
            updateText();
    }

    private void updateText() {
        int lastSuccessNA = globalLastSuccess.get();
        int globalSuccessesNA = globalSuccesses.get();
        int globalAttemptsNA = globalAttempts.get();
        resultsLabel.setText("Successes: "+ globalSuccessesNA +"/"+globalAttemptsNA+" ("+df.format(100.0 * globalSuccessesNA / globalAttemptsNA)+"%)");
        resultsLabel.repaint();
        currentSeedLabel.setText("Current Seed: "+seed);
        if (lastSuccess >= 0) exampleSeedLabel.setText("Example seed: " + lastSuccess);
        exampleSeedLabel.repaint();
    }

    private boolean verifySeed(int seed, Solution solution, SuperCC emulator) {
        solution.rngSeed = seed;
        emulator.getLevel().load(startingState);
        emulator.getLevel().getCheats().setRng(seed);
        solution.loadMoves(emulator, TickFlags.LIGHT, false);
        if (!untilPosition)
            return emulator.getLevel().isCompleted();
        else
            return emulator.getLevel().getChip().getPosition().equals(endPosition) && !emulator.getLevel().getChip().isDead();
    }

    private class SeedSearchThread extends Thread {
        public void run(){
            while (!killFlag && currentSeed <= endSeed) {
                if (verifySeed(currentSeed, solution, emulator)) {
                    successes++;
                    lastSuccess = seed;
                }
                attempts++;
                seed++;
                if (seed % UPDATE_RATE == 0) updateText();
            }
            updateValues(attempts, successes, lastSuccess);
            if (numAlive.decrementAndGet() == 0) { //last one to die should do some cleanup
                updateText();
                killFlag = false;
                running = false;
            }
        }
    }

    public static boolean isRunning() {
        return running;
    }

}
