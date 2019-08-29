package tools;

import emulator.Solution;
import emulator.SuperCC;
import emulator.TickFlags;

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
    
    private static final int UPDATE_RATE = 1000;
    
    private final byte[] startingState;
    private int seed = 0;
    private boolean killThreadFlag = false;
    private static boolean running = false;
    private DecimalFormat df;
    
    private int successes = 0;
    private int attempts = 0;
    private int lastSuccess = -1;

    public SeedSearch(SuperCC emulator, Solution solution) {

        emulator.loadLevel(emulator.getLevel().getLevelNumber(), seed, solution.step, false);
        startingState = emulator.getLevel().save();
        
        this.emulator = emulator;
        this.solution = solution;
    
        resultsLabel.setText("Successes: 0/0 (0%)");
        df = new DecimalFormat("##.####");
    
        startStopButton.addActionListener((e) -> {
            if (running) killThreadFlag = true;
            else new SeedSearchThread().start();
        });

        JFrame frame = new JFrame("Seed Search");
        frame.setContentPane(panel1);
        frame.pack();
        frame.setLocationRelativeTo(emulator.getMainWindow());
        frame.setVisible(true);
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent windowEvent) {

            }

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                killThreadFlag = true;
            }

            @Override
            public void windowClosed(WindowEvent windowEvent) { //this stuff doesn't work
//                emulator.getSavestates().restart();
//                emulator.getLevel().load(emulator.getSavestates().getSavestate());
//                emulator.showAction("Restarted Level");
//                emulator.getMainWindow().repaint(emulator.getLevel(), false);
            }

            @Override
            public void windowIconified(WindowEvent windowEvent) {

            }

            @Override
            public void windowDeiconified(WindowEvent windowEvent) {

            }

            @Override
            public void windowActivated(WindowEvent windowEvent) {

            }

            @Override
            public void windowDeactivated(WindowEvent windowEvent) {

            }
        });
    }

    private void createUIComponents() {
        resultsLabel = new JLabel();
        exampleSeedLabel = new JLabel("Example seed:");
    }

    private void updateText() {
        resultsLabel.setText("Successes: "+successes+"/"+attempts+" ("+df.format(100.0 * (double) successes / (double) attempts)+"%)");
        resultsLabel.repaint();
        if (lastSuccess >= 0) exampleSeedLabel.setText("Example seed: " + lastSuccess);
        exampleSeedLabel.repaint();
    }
    
    private class SeedSearchThread extends Thread {
        public void run(){
            running = true;
            killThreadFlag = false;
            while (!killThreadFlag && seed >= 0) {
                if (verifySeed(seed)) {
                    successes++;
                    lastSuccess = seed;
                }
                attempts++;
                seed++;
                if (seed % UPDATE_RATE == 0) updateText();
            }
            running = false;
            killThreadFlag = false;
                emulator.getSavestates().restart();
                emulator.getLevel().load(emulator.getSavestates().getSavestate());
                emulator.showAction("Restarted Level");
                emulator.getMainWindow().repaint(emulator.getLevel(), false);
        }
    }
    
    public boolean verifySeed(int seed) {
        solution.rngSeed = seed;
        emulator.getLevel().load(startingState);
        emulator.getLevel().cheats.setRng(seed);
        solution.loadMoves(emulator, TickFlags.LIGHT, false);
        return emulator.getLevel().isCompleted();
    }

    public static boolean isRunning() {
        return running;
    }

}
