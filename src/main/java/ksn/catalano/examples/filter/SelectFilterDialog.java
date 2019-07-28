package ksn.catalano.examples.filter;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.Filters.FrequencyFilter;

public class SelectFilterDialog {

    private static final Logger logger = LoggerFactory.getLogger(SelectFilterDialog.class);

    private final Frame owner;

    public SelectFilterDialog(Frame owner) {
        this.owner = owner;
    }

    public String getFilterClassName() {
        logger.trace("getFilterClassName");

        JDialog dlg = new JDialog(owner, "Select filter...", true);

        Object keyBind = "CloseDialog";
        dlg.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), keyBind);
        dlg.getRootPane().getActionMap().put(keyBind, new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) { dlg.dispose(); }
        });

        JPanel panel4Radio = new JPanel(new GridLayout(0, 1, 0, 5));
        panel4Radio.setBorder(BorderFactory.createTitledBorder("Filters"));
        ButtonGroup radioGroup = new ButtonGroup();

        JRadioButton radioFilter1 = new JRadioButton(FrequencyFilter.class.getSimpleName());
        radioFilter1.setActionCommand(FrequencyFilter.class.getSimpleName());

        JRadioButton radioFilter2 = new JRadioButton(FrequencyFilter.class.getSimpleName());
        radioFilter2.setActionCommand(FrequencyFilter.class.getSimpleName());

        panel4Radio.add(radioFilter1);
        panel4Radio.add(radioFilter2);
        radioGroup.add(radioFilter1);
        radioGroup.add(radioFilter2);

        dlg.add(panel4Radio);

        JButton btnOk = new JButton("Ok");
        String[] filterClassName = { null };
        btnOk.addActionListener(ev -> {
            dlg.dispose();

            ButtonModel bm = radioGroup.getSelection();
            if (bm == null)
                return;
            filterClassName[0] = bm.getActionCommand();
        });

        dlg.add(panel4Radio, BorderLayout.CENTER);
        dlg.add(btnOk, BorderLayout.SOUTH);

        dlg.setResizable(false);
        dlg.pack();
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);

        return filterClassName[0];
    }

}
