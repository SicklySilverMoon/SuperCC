package tools;

import emulator.SuperCC;
import tools.variation.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class VariationTesting {
    private JTextPane codeEditor;
    private JTextPane codeOutput;
    private JPanel mainPanel;
    private JPanel menuPanel;
    private JButton runButton;
    private JPanel editorPanel;
    private JPanel consolePanel;
    private JProgressBar progressBar;
    private JLabel solutionsFoundLabel;
    private JTextPane console;
    private JLayeredPane editorArea;
    private JScrollPane editor;
    private JScrollPane output;
    private ArrayList<JLabel> lineNumbers = new ArrayList<>();
    private Interpreter interpreter;
    public boolean killFlag = false;
    public static boolean running = false;
    private static final HashMap<TokenType, Color> colors;
    public boolean hasGui = true;

    static {
        colors = new HashMap<>();
        colors.put(TokenType.COMMENT, new Color(0, 204, 0));
        colors.put(TokenType.NUMBER, new Color(255, 153, 255));
        colors.put(TokenType.IDENTIFIER, new Color(68, 221, 255));
        colors.put(TokenType.MOVE, new Color(221, 51, 221));
        colors.put(TokenType.FUNCTION, new Color(221, 170, 0));
        colors.put(TokenType.TILE, new Color(221, 221, 0));

        colors.put(TokenType.START, new Color(68, 221, 255));
        colors.put(TokenType.BEFORE_MOVE, new Color(68, 221, 255));
        colors.put(TokenType.AFTER_MOVE, new Color(68, 221, 255));
        colors.put(TokenType.BEFORE_STEP, new Color(68, 221, 255));
        colors.put(TokenType.AFTER_STEP, new Color(68, 221, 255));
        colors.put(TokenType.END, new Color(68, 221, 255));

        colors.put(TokenType.IF, new Color(0, 153, 255));
        colors.put(TokenType.ELSE, new Color(0, 153, 255));
        colors.put(TokenType.FOR, new Color(0, 153, 255));
        colors.put(TokenType.TRUE, new Color(0, 153, 255));
        colors.put(TokenType.FALSE, new Color(0, 153, 255));
        colors.put(TokenType.NULL, new Color(0, 153, 255));
        colors.put(TokenType.VAR, new Color(0, 153, 255));
        colors.put(TokenType.PRINT, new Color(0, 153, 255));
        colors.put(TokenType.BREAK, new Color(0, 153, 255));
        colors.put(TokenType.AND, new Color(0, 153, 255));
        colors.put(TokenType.OR, new Color(0, 153, 255));
        colors.put(TokenType.NOT, new Color(0, 153, 255));
        colors.put(TokenType.RETURN, new Color(0, 153, 255));
        colors.put(TokenType.CONTINUE, new Color(0, 153, 255));
        colors.put(TokenType.TERMINATE, new Color(0, 153, 255));
        colors.put(TokenType.LEXICOGRAPHIC, new Color(0, 153, 255));
        colors.put(TokenType.ALL, new Color(0, 153, 255));

        colors.put(TokenType.OTHER, new Color(221, 0, 0));
    }

    private SuperCC emulator;

    public VariationTesting(SuperCC emulator) {
        this.emulator = emulator;

        setGUI();
    }

    public VariationTesting(SuperCC emulator, boolean hasGui) {
        this.emulator = emulator;
        this.console = new JTextPane();
        this.hasGui = hasGui;
        if(hasGui) {
            setGUI();
        }
    }

    private void setGUI() {
        setTextPanes();
        setScrollPanes();
        setAdjustmentListeners();
        setEditorArea();
        setEditorPanel();
        setConsole();
        setFrame();
        highlight();

        runButton.addActionListener(e -> {
            if(running) {
                killFlag = true;
                return;
            }
            interpreter = new Interpreter(emulator, this, console, codeEditor.getText());
            this.progressBar.setMaximum(Integer.MAX_VALUE);
            new VariationTestingThread().start();
            Timer timer = new Timer(true);
            timer.scheduleAtFixedRate(new VariationTestingProgressTask(), 0, 100);
        });
    }

    private void setTextPanes() {
        codeEditor = new JTextPane();
        codeEditor.setOpaque(false);
        codeEditor.setSize(new Dimension(6000, 600));
        codeEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        codeEditor.setForeground(new Color(0, 0, 0, 0));
        codeEditor.setCaretColor(new Color(255, 255, 255));
        codeEditor.getStyledDocument().addDocumentListener(new EditorDocumentListener());
        codeEditor.setMargin(new Insets(0, 60, 0, 0));

        codeOutput = new JTextPane();
        codeOutput.setEditable(false);
        codeOutput.setSize(new Dimension(6000, 600));
        codeOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        codeOutput.setBackground(new Color(50, 50, 50));
        codeOutput.setForeground(new Color(255, 255, 255));
        codeOutput.setMargin(new Insets(0, 60, 0, 0));

        console = new JTextPane();
        console.setEditable(false);
        console.setSize(new Dimension(6000, 200));
        console.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        console.setBackground(new Color(50, 50, 50));
    }

    private void setScrollPanes() {
        JPanel outputNoWrapPanel = new JPanel(new BorderLayout());
        outputNoWrapPanel.add(codeOutput);

        output = new JScrollPane(outputNoWrapPanel);
        output.setSize(new Dimension(600, 600));

        JPanel editorNoWrapPanel = new JPanel(new BorderLayout());
        editorNoWrapPanel.setOpaque(false);
        editorNoWrapPanel.add(codeEditor);
        editorNoWrapPanel.setSize(new Dimension(600, 600));

        editor = new JScrollPane(editorNoWrapPanel);
        editor.setOpaque(false);
        editor.getViewport().setOpaque(false);
        editor.setSize(new Dimension(600, 600));
    }

    private void setAdjustmentListeners() {
        editor.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                output.getVerticalScrollBar().setValue(e.getValue());
                updateLineNumbers();
            }
        });

        editor.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                output.getHorizontalScrollBar().setValue(e.getValue());
                updateLineNumbers();
            }
        });

        output.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                output.getVerticalScrollBar().setValue(editor.getVerticalScrollBar().getValue());
                updateLineNumbers();
            }
        });

        output.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                output.getHorizontalScrollBar().setValue(editor.getHorizontalScrollBar().getValue());
                updateLineNumbers();
            }
        });
    }

    private void setEditorArea() {
        editorArea = new JLayeredPane();

        editorArea.add(editor, new Integer(1));
        editorArea.add(output, new Integer(1));
    }

    private void setEditorPanel() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        editorPanel.add(new JScrollPane(editorArea), gbc);
    }

    private void setConsole() {
        JPanel noWrapPanel = new JPanel(new BorderLayout());
        noWrapPanel.add(console);

        consolePanel.setLayout(new BorderLayout());
        consolePanel.add(new JScrollPane(noWrapPanel), BorderLayout.CENTER);
    }

    private void setFrame() {
        JFrame frame = new JFrame("Variation testing");
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(new WindowListener() {
            @Override public void windowClosing(WindowEvent windowEvent) { killFlag = true; }

            //None of these are useful but the code requires them to be here so i shoved them all into one line
            @Override public void windowOpened(WindowEvent windowEvent) {}@Override public void windowClosed(WindowEvent windowEvent) {}@Override public void windowIconified(WindowEvent windowEvent) {}@Override public void windowDeiconified(WindowEvent windowEvent) {}@Override public void windowActivated(WindowEvent windowEvent) { }@Override public void windowDeactivated(WindowEvent windowEvent) { }
        });

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                editor.setSize(new Dimension(editorArea.getWidth(), editorArea.getHeight()));
                output.setSize(new Dimension(editorArea.getWidth(), editorArea.getHeight()));
            }
        });
    }

    private class EditorDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            highlight();
        }

        public void removeUpdate(DocumentEvent e) {
            highlight();
        }

        public void changedUpdate(DocumentEvent e) {

        }
    }

    private void highlight() {
        codeOutput.setText("");
        StyledDocument doc = codeOutput.getStyledDocument();
        Style style = codeOutput.addStyle("style", null);
        Tokenizer tokenizer = new Tokenizer(codeEditor.getText());
        ArrayList<Token> tokens = tokenizer.getAllTokens();
        int lines = 1;

        for(Token token : tokens) {
            Color c = colors.get(token.type);
            if(c == null) {
                c = new Color(255, 255, 255);
            }
            StyleConstants.setForeground(style, c);
            if(token.type == TokenType.NEW_LINE) {
                lines++;
            }
            try {
                doc.insertString(doc.getLength(), token.lexeme, style);
            } catch(BadLocationException ex) {}
        }

        for(int i = 0; i < lines; i++) {
            if(lineNumbers.size() <= i) {
                JLabel lineNumber = new JLabel();
                lineNumber.setText(Integer.toString(i + 1));
                lineNumber.setBounds(0, 22 * i, 40, 24);
                lineNumber.setForeground(new Color(153, 153, 153));
                lineNumber.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
                lineNumber.setHorizontalAlignment(SwingConstants.RIGHT);
                lineNumbers.add(lineNumber);
                editorArea.add(lineNumber, new Integer(2));
            }
            else {
                lineNumbers.get(i).setVisible(true);
            }
        }

        for(int i = lines; i < lineNumbers.size(); i++) {
            lineNumbers.get(i).setVisible(false);
        }
    }

    private void updateLineNumbers() {
        int offsetX = editor.getHorizontalScrollBar().getValue();
        int offsetY = editor.getVerticalScrollBar().getValue();
        for(int i = 0; i < lineNumbers.size(); i++) {
            lineNumbers.get(i).setBounds(0 - offsetX, i * 22 - offsetY, 40, 24);
        }
    }

    public JTextPane getConsole() {
        return console;
    }

    public void clearConsole() {
        console.setText("");
    }

    public static boolean isRunning() {
        return running;
    }

    private class VariationTestingThread extends Thread {
        public void run() {
            running = true;
            killFlag = false;
            runButton.setText("Stop");
            interpreter.interpret();
            running = false;
            killFlag = false;
            runButton.setText("Run");

            if(hasGui && interpreter.solutions.size() > 0) {
                VariationResult result = new VariationResult(emulator, interpreter.variationCount, interpreter.solutions);
            }
        }
    }

    private class VariationTestingProgressTask extends TimerTask {
        public void run() {
            solutionsFoundLabel.setText("Solutions found: " + interpreter.solutions.size());
            double normalization = Integer.MAX_VALUE/interpreter.totalPermutationCount;
            double permutationIndex = interpreter.getPermutationIndex() * normalization;
            progressBar.setValue((int)permutationIndex);
            double percentage = permutationIndex/Integer.MAX_VALUE * 100;
            progressBar.setString(String.format("%.3f", percentage) + "%");
            if(killFlag || !running) {
                progressBar.setValue(0);
                progressBar.setString("0.000%");
                cancel();
            }
        }
    }
}
