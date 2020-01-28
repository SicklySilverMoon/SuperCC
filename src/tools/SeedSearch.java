package tools;

import emulator.Solution;
import emulator.SuperCC;
import emulator.TickFlags;
import game.Position;

import javax.swing.*;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.DecimalFormat;

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

    private static final int UPDATE_RATE = 1000;
    
    private final byte[] startingState;
    private int seed;
    private static boolean killFlag = false;
    private static boolean running = false;
    private DecimalFormat df;
    
    private int successes = 0;
    private int attempts = 0;
    private int lastSuccess = -1;
    private boolean untilPosition = false;
    private Position endPosition = new Position(0, 0);

    public SeedSearch(SuperCC emulator, Solution solution) {

        emulator.loadLevel(emulator.getLevel().getLevelNumber(), seed, solution.step, false);
        startingState = emulator.getLevel().save();
        
        this.emulator = emulator;
        this.solution = solution;
    
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
                    System.out.println(endPosition);
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
        frame.addWindowListener(new WindowListener() {
            @Override public void windowClosing(WindowEvent windowEvent) { killFlag = true; }

            //None of these are useful but the code requires them to be here so i shoved them all into one line
            @Override public void windowOpened(WindowEvent windowEvent) {}@Override public void windowClosed(WindowEvent windowEvent) {}@Override public void windowIconified(WindowEvent windowEvent) {}@Override public void windowDeiconified(WindowEvent windowEvent) {}@Override public void windowActivated(WindowEvent windowEvent) { }@Override public void windowDeactivated(WindowEvent windowEvent) { }
        });
    }

    private void createUIComponents() {
        resultsLabel = new JLabel();
        exampleSeedLabel = new JLabel("Example seed:");
    }

    private void updateText() {
        resultsLabel.setText("Successes: "+successes+"/"+attempts+" ("+df.format(100.0 * (double) successes / (double) attempts)+"%)");
        resultsLabel.repaint();
        currentSeedLabel.setText("Current Seed: "+seed);
        if (lastSuccess >= 0) exampleSeedLabel.setText("Example seed: " + lastSuccess);
        exampleSeedLabel.repaint();
    }
    
    private class SeedSearchThread extends Thread {
        public void run(){
            running = true;
            killFlag = false;
            while (!killFlag && seed >= 0) {
                if (verifySeed(seed)) {
                    successes++;
                    lastSuccess = seed;
                }
                attempts++;
                seed++;
                if (seed % UPDATE_RATE == 0) updateText();
            }
            running = false;
            killFlag = false;
            updateText(); //just have it update with the last result, in case a success is found before an update and it gets canceled
                emulator.getSavestates().restart();
                emulator.getLevel().load(emulator.getSavestates().getSavestate());
                emulator.showAction("Restarted Level");
                emulator.getMainWindow().repaint(false);
        }
    }
    
    public boolean verifySeed(int seed) {
        solution.rngSeed = seed;
        emulator.getLevel().load(startingState);
        emulator.getLevel().cheats.setRng(seed);
        solution.loadMoves(emulator, TickFlags.LIGHT, false);
        if (!untilPosition) return emulator.getLevel().isCompleted();
        else return emulator.getLevel().getChip().getPosition().equals(endPosition) && !emulator.getLevel().getChip().isDead();
    }

    public static boolean isRunning() {
        return running;
    }

    public static void kill() {
        killFlag = true;
    }

}
