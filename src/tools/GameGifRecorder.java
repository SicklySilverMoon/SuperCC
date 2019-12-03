package tools;

import emulator.SavestateManager;
import emulator.SuperCC;
import graphics.Gui;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class GameGifRecorder {
    private JRadioButton a5FpsRadioButton;
    private JRadioButton a10FpsRadioButton;
    private JButton okButton;
    private JPanel mainPanel;
    private JSpinner spinner;
    private JProgressBar progressBar;
    
    private static final int GIF_RECORDING_STATE = -1;
    
    public GameGifRecorder(SuperCC emulator) {
        ButtonGroup bg = new ButtonGroup();
        bg.add(a5FpsRadioButton);
        bg.add(a10FpsRadioButton);
        a5FpsRadioButton.setSelected(true);
    
        okButton.addActionListener((e) -> {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                try {
                    Gui window = emulator.getMainWindow();
                    SavestateManager savestates = emulator.getSavestates();
                    int numHalfTicks = (int) (((Number) spinner.getValue()).doubleValue() * 10);

                    savestates.addSavestate(GIF_RECORDING_STATE);
                    emulator.showAction("Recording gif, please wait");
                    List<BufferedImage> images = savestates.play(emulator, numHalfTicks);
                    int i = 1;
                    File outFile = new File("out.gif");
                    while (outFile.exists()) outFile = new File("out" + (i++) + ".gif");
                    ImageOutputStream output = new FileImageOutputStream(outFile);
                    int imageSkip = 1;
                    if (a5FpsRadioButton.isSelected()) imageSkip = 2;
                    int timePerFrame = 100 * imageSkip;
                    GifSequenceWriter writer = new GifSequenceWriter(output, images.get(0).getType(), timePerFrame, true);

                    progressBar.setMinimum(0);
                    progressBar.setMaximum(numHalfTicks);
                    for (i = 0; i < numHalfTicks && i < images.size(); i += imageSkip) {
                        writer.writeToSequence(images.get(i));
                        progressBar.setValue(i);
                        progressBar.repaint();
                    }
                    writer.close();
                    output.close();
                    emulator.getSavestates().load(GIF_RECORDING_STATE, emulator.getLevel());
                    emulator.showAction("Recorded " + outFile.getName());
                }
                catch (IOException exc) {
                    exc.printStackTrace();
                }
                SwingUtilities.getWindowAncestor(mainPanel).dispose();
                emulator.repaint(false);
                return null;
                }
            };
            worker.execute();
        });
    
        JFrame frame = new JFrame("Gif Recorder");
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(emulator.getMainWindow());
        frame.setVisible(true);
        
    }
    
    private void createUIComponents() {
        SpinnerModel model = new SpinnerNumberModel(2.5, 0.2, Integer.MAX_VALUE, 0.1);
        spinner = new JSpinner(model);
        progressBar = new JProgressBar();
    }
}
