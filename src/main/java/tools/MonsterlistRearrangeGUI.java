package tools;

import emulator.SuperCC;
import game.Creature;
import game.Level;

import javax.swing.*;
import java.awt.event.*;

public class MonsterlistRearrangeGUI {
    private JPanel mainPanel;
    private JList<Creature> guiList;
    private DefaultListModel<Creature> listModel = new DefaultListModel<>();
    private JScrollPane scrollPane;
    private JButton upButton;
    private JButton downButton;

    public MonsterlistRearrangeGUI(SuperCC emulator, boolean useSliplist) {
        Creature[] list;
        if (useSliplist)
            list = emulator.getLevel().getSlipList().toArray(new Creature[0]);
        else
            list = emulator.getLevel().getMonsterList().getCreatures();

        for (Creature c : list) {
            if (c.getCreatureType().isChip())
                continue;
            listModel.addElement(c);
        }
        guiList.setModel(listModel);
        guiList.setVisibleRowCount(-1);

        JFrame frame = new JFrame("Change " + (useSliplist ? "Slip" : "Monster") + " List Positions");
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(emulator.getMainWindow());
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                updateChanges(emulator, useSliplist);
            }
        });

        upButton.addActionListener(e -> {
            int index = guiList.getSelectedIndex();
            Creature selected = guiList.getSelectedValue();
            if (index > 0) {
                listModel.remove(index);
                listModel.add(index-1, selected);
                guiList.setSelectedIndex(index-1);
            }
        });
        downButton.addActionListener(e -> {
            int index = guiList.getSelectedIndex();
            Creature selected = guiList.getSelectedValue();
            if (index < listModel.size()-1) {
                listModel.remove(index);
                listModel.add(index+1, selected);
                guiList.setSelectedIndex(index+1);
            }
        });
    }

    private void updateChanges(SuperCC emulator, boolean useSliplist) {
        Level level = emulator.getLevel();
        if (!useSliplist) {
            int chipIndex = level.getMonsterList().getIndexOfCreature(level.getChip());
            if (level.chipInMonsterList())
                listModel.add(chipIndex, level.getChip());
        }

        Creature[] list = new Creature[listModel.size()];
        listModel.copyInto(list);
        if (useSliplist)
            level.getSlipList().setSliplist(list);
        else
            level.getMonsterList().setCreatures(list, level.getLayerFG());
        emulator.getMainWindow().repaint(false);
    }
}
