package tools;

import emulator.SuperCC;
import tools.variation.Token;
import tools.variation.Tokenizer;

import javax.swing.*;
import java.util.ArrayList;

public class VariationTesting {
    private JTextArea codeEditor;
    private JPanel mainPanel;
    private JPanel menuPanel;
    private JButton runButton;

    private SuperCC emulator;

    public VariationTesting(SuperCC emulator) {
        this.emulator = emulator;

        JFrame frame = new JFrame("Variation testing");
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setVisible(true);

        runButton.addActionListener(e -> {
            Tokenizer tokenizer = new Tokenizer(codeEditor.getText());
            ArrayList<Token> tokens = tokenizer.tokenize();
            System.out.println(codeEditor.getText());
        });
    }
}
