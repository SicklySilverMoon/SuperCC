package tools;

import emulator.SuperCC;
import game.Creature;
import game.Level;

import javax.swing.*;
import java.awt.event.*;

public class MonsterlistRearrangeGUI {
    private SuperCC emulator;
    private JPanel mainPanel;
    private JList guiList;
    private DefaultListModel<Creature> listModel = new DefaultListModel<>();
    private JScrollPane scrollPane;
    private JButton upButton;
    private JButton downButton;

    public MonsterlistRearrangeGUI(SuperCC emulator) {
        this.emulator = emulator;
        for (Creature c : emulator.getLevel().getMonsterList().getCreatures()) {
            if (c.getCreatureType().isChip()) continue;
            listModel.addElement(c);
        }
        guiList.setModel(listModel);
        guiList.setVisibleRowCount(-1);

        JFrame frame = new JFrame("Change Monster List Positions");
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(emulator.getMainWindow());
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                updateChanges(emulator);
            }
        });

        upButton.addActionListener(e -> {
            int index = guiList.getSelectedIndex();
            Creature selected = (Creature) guiList.getSelectedValue();
            if (index > 0) {
                listModel.remove(index);
                listModel.add(index-1, selected);
                guiList.setSelectedIndex(index-1);
            }
        });
        downButton.addActionListener(e -> {
            int index = guiList.getSelectedIndex();
            Creature selected = (Creature) guiList.getSelectedValue();
            if (index < listModel.size()-1) {
                listModel.remove(index);
                listModel.add(index+1, selected);
                guiList.setSelectedIndex(index+1);
            }
        });
    }

    private void updateChanges(SuperCC emulator) {
        Level level = emulator.getLevel();
        int chipIndex = level.getMonsterList().getIndexOfCreature(level.getChip());
        if (level.chipInMonsterList()) listModel.add(chipIndex, level.getChip());

        Creature[] list = new Creature[listModel.size()];
        listModel.copyInto(list);
        level.getMonsterList().setCreatures(list, level.getLayerFG());
        emulator.getMainWindow().repaint(false);
    }
}
