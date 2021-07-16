package tools;

import emulator.SuperCC;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.util.ArrayList;

public class VerifyTWS {
    private JPanel mainPanel;
    private JList list1;
    private JList list2;
    private JTable table1;
    
    public VerifyTWS(SuperCC emulator) {
        int lastLevel = emulator.lastLevelNumber();
        ArrayList<String> titles = new ArrayList<>(lastLevel);
        ArrayList<String> results = new ArrayList<>(lastLevel);
        for (int i = 1; i < lastLevel; i++) {
            emulator.loadLevel(i);
            titles.add(i + "   " + emulator.getLevel().getTitle());
            try {
                results.add(emulator.twsReader.readSolution(emulator.getLevel()).efficiency > 0.9 ? "Tile World" : "SuCC");
            }
            catch (Exception e) {
                results.add("Could not read tws");
            }
        }
    
        list1.setListData(titles.toArray());
        list2.setListData(results.toArray());
        list1.setBorder(new BevelBorder(BevelBorder.LOWERED));
        list2.setBorder(new BevelBorder(BevelBorder.LOWERED));
        
        JFrame frame = new JFrame("Verification");
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(emulator.getMainWindow());
        frame.setVisible(true);
        
    }
    
    private void createUIComponents() {
        list1 = new JList(new String[] {});
        list2 = new JList(new String[] {});
    }
}
