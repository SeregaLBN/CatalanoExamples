package ksn.imgusage.utils;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ksn.imgusage.tabs.catalano.CatalanoFilterTab;
import ksn.imgusage.tabs.opencv.OpencvFilterTab;

public class SelectFilterDialog {

    private static final Logger logger = LoggerFactory.getLogger(SelectFilterDialog.class);

    private final Frame owner;

    public SelectFilterDialog(Frame owner) {
        this.owner = owner;
    }

    public String getFilterTabFullName() {
        logger.trace("getFilterTabName");

        JDialog dlg = new JDialog(owner, "Select filter...", true);

        Object keyBind = "CloseDialog";
        dlg.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), keyBind);
        dlg.getRootPane().getActionMap().put(keyBind, new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) { dlg.dispose(); }
        });

        Box boxCatalanoFilters = Box.createVerticalBox();
        boxCatalanoFilters.setBorder(BorderFactory.createTitledBorder("Catalano filters"));
        ButtonGroup radioGroup = new ButtonGroup();

        MapperFilter.getAllCatalanoTabsDescr().forEach(tab -> {
            JRadioButton radioFilter = new JRadioButton(tab.filterName + ": " + tab.description);
            radioFilter.setActionCommand(CatalanoFilterTab.TAB_PREFIX + tab.filterName);
            boxCatalanoFilters.add(radioFilter);
            radioGroup.add(radioFilter);
        });

        Box boxOpenCvFilters = Box.createVerticalBox();
        boxOpenCvFilters.setBorder(BorderFactory.createTitledBorder("OpenCV filters"));

        MapperFilter.getAllOpencvTabsDescr().forEach(tab -> {
            JRadioButton radioFilter = new JRadioButton(tab.filterName + ": " + tab.description);
            radioFilter.setActionCommand(OpencvFilterTab.TAB_PREFIX + tab.filterName);
            boxOpenCvFilters.add(radioFilter);
            radioGroup.add(radioFilter);
        });

        JButton btnOk = new JButton("Ok");
        String[] filterTabFullName = { null };
        btnOk.addActionListener(ev -> {
            dlg.dispose();

            ButtonModel bm = radioGroup.getSelection();
            if (bm == null)
                return;
            filterTabFullName[0] = bm.getActionCommand();
        });

        Box boxCenter = Box.createVerticalBox();
        boxCenter.add(boxOpenCvFilters);
        boxCenter.add(boxCatalanoFilters);

        dlg.add(boxCenter, BorderLayout.CENTER);
        dlg.add(btnOk, BorderLayout.SOUTH);

        dlg.setResizable(false);
        dlg.pack();
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);

        return filterTabFullName[0];
    }

    /** construct a relative path in Java from two absolute paths */
    public static File getRelativePath(File from, File basePath) {
        return basePath.toPath().relativize(from.toPath()).toFile();
    }

}
