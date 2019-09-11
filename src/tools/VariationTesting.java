package tools;

import emulator.SuperCC;
import tools.variation.Token;
import tools.variation.TokenType;
import tools.variation.Tokenizer;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;

public class VariationTesting {
    private JTextPane codeEditor;
    private JTextPane codeOutput;
    private JPanel mainPanel;
    private JPanel menuPanel;
    private JButton runButton;
    private JPanel editorPanel;
    private JLayeredPane editorArea;
    private static final HashMap<TokenType, Color> colors;

    static {
        colors = new HashMap<>();
        colors.put(TokenType.COMMENT, new Color(0, 204, 0));
        colors.put(TokenType.NUMBER, new Color(255, 153, 255));
        colors.put(TokenType.IDENTIFIER, new Color(68, 221, 255));
        colors.put(TokenType.MOVE, new Color(221, 51, 221));

        colors.put(TokenType.START, new Color(68, 221, 255));
        colors.put(TokenType.BEFORE_MOVE, new Color(68, 221, 255));
        colors.put(TokenType.AFTER_MOVE, new Color(68, 221, 255));

        colors.put(TokenType.IF, new Color(0, 153, 255));
        colors.put(TokenType.ELSE, new Color(0, 153, 255));
        colors.put(TokenType.FOR, new Color(0, 153, 255));
        colors.put(TokenType.TRUE, new Color(0, 153, 255));
        colors.put(TokenType.FALSE, new Color(0, 153, 255));
        colors.put(TokenType.NULL, new Color(0, 153, 255));
        colors.put(TokenType.VAR, new Color(0, 153, 255));
        colors.put(TokenType.AND, new Color(0, 153, 255));
        colors.put(TokenType.OR, new Color(0, 153, 255));
        colors.put(TokenType.NOT, new Color(0, 153, 255));
        colors.put(TokenType.RETURN, new Color(0, 153, 255));
        colors.put(TokenType.CONTINUE, new Color(0, 153, 255));
        colors.put(TokenType.TERMINATE, new Color(0, 153, 255));
    }

    private SuperCC emulator;

    public VariationTesting(SuperCC emulator) {
        this.emulator = emulator;

        editorArea = new JLayeredPane();
        editorArea.setPreferredSize(new Dimension(600, 600));

        codeEditor = new JTextPane();
        codeOutput = new JTextPane();

        codeOutput.setEditable(false);
        codeEditor.setOpaque(false);

        codeEditor.setSize(600, 600);
        codeOutput.setSize(600, 600);
        codeEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        codeOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));

        codeEditor.setForeground(new Color(0, 0, 0, 0));
        codeEditor.setCaretColor(new Color(255, 255, 255));

        codeOutput.setBackground(new Color(50, 50, 50));
        codeOutput.setForeground(new Color(255, 255, 255));

        editorArea.add(codeEditor, new Integer(1));
        editorArea.add(codeOutput, new Integer(1));

        editorPanel.setLayout(new GridBagLayout());
        editorPanel.add(editorArea);

        JFrame frame = new JFrame("Variation testing");
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setVisible(true);

        runButton.addActionListener(e -> {
            //Tokenizer tokenizer = new Tokenizer(codeEditor.getText());
            //ArrayList<Token> tokens = tokenizer.tokenize();
        });

        codeEditor.addKeyListener(new EditorKeyListener());
    }

    private class EditorKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
            codeOutput.setText("");
            StyledDocument doc = codeOutput.getStyledDocument();
            Style style = codeOutput.addStyle("style", null);
            Tokenizer tokenizer = new Tokenizer(codeEditor.getText() + e.getKeyChar());
            ArrayList<Token> tokens = tokenizer.tokenize();

            for(Token token : tokens) {
                Color c = colors.get(token.type);
                if(c == null) {
                    c = new Color(255, 255, 255);
                }
                StyleConstants.setForeground(style, c);
                try {
                    doc.insertString(doc.getLength(), token.lexeme, style);
                } catch(BadLocationException ex) {}
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }
}
