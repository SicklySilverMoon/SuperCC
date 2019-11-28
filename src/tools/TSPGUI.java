package tools;

import emulator.SuperCC;
import tools.tsp.TSPSolver;

import javax.swing.*;

public class TSPGUI {
    private JButton runTSP;
    private JPanel mainPanel;

    private SuperCC emulator;

    public TSPGUI(SuperCC emulator) {
        this.emulator = emulator;

        JFrame frame = new JFrame("Variation testing");
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setVisible(true);

        runTSP.addActionListener(e -> {
            TSPSolver solver = new TSPSolver(emulator);
            solver.solve();
        });
    }
}
