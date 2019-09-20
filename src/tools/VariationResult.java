package tools;

import emulator.Solution;
import emulator.SuperCC;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;

public class VariationResult {
    private JPanel mainPanel;
    private JLabel labelVariations;
    private JScrollPane scroll;
    private JPanel solutionsPanel;
    private JLabel labelSolutions;

    VariationResult(SuperCC emulator, long count, ArrayList<Solution> solutions) {
        labelVariations.setText("Variations tested: " + String.format("%,d", count));
        labelSolutions.setText("Solutions found: " + String.format("%,d", solutions.size()));
        solutionsPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        GridBagConstraints labelC = new GridBagConstraints();
        labelC.anchor = GridBagConstraints.EAST;

        for(int i = 0; i < solutions.size(); i++) {
            int index = i;
            JPanel container = new JPanel();
            container.setLayout(new GridBagLayout());

            JLabel labelNumber = new JLabel();
            labelNumber.setText(Integer.toString(i + 1));
            labelNumber.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            labelNumber.setHorizontalAlignment(SwingConstants.RIGHT);
            labelNumber.setPreferredSize(new Dimension(30, 25));

            JButton loadButton = new JButton();
            loadButton.setText("Load");
            loadButton.addActionListener(e -> {
                solutions.get(index).load(emulator);
            });

            JButton copyButton = new JButton();
            copyButton.setText("Copy");
            copyButton.addActionListener(e -> {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(solutions.get(index).toString()), null);
            });

            c.gridy = i;

            container.add(labelNumber, labelC);
            container.add(loadButton);
            container.add(copyButton);

            solutionsPanel.add(container, c);
        }

        JFrame frame = new JFrame("Variation Results");
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setVisible(true);
    }
}
