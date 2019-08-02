package ksn.catalano.examples.filter;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.Filters.*;

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

        Arrays.asList(
            new Object[] { BrightnessCorrection       .class, "Brightness adjusting in RGB color space" },
            new Object[] { AdaptiveContrastEnhancement.class, "Adaptive Contrast Enhancement is modification of the gray level values based on some criterion that adjusts its parameters as local image characteristics change" },
            new Object[] { FrequencyFilter            .class, "Filtering of frequencies outside of specified range in complex Fourier transformed image" },
            new Object[] { Rotate                     .class, "Rotate image" },
            new Object[] { ArtifactsRemoval           .class, "Remove artifacts caused by uneven lightning" },
            new Object[] { BernsenThreshold           .class, "The method uses a user-provided contrast threshold" },
            new Object[] { Blur                       .class, "Blur filter" },
            new Object[] { BradleyLocalThreshold      .class, "Adaptive thresholding using the integral image"}
        ).forEach(arr -> {
            String className = ((Class<?>)arr[0]).getSimpleName();
            String description = (String)arr[1];
            JRadioButton radioFilter = new JRadioButton(className + ": " + description);
            radioFilter.setActionCommand(className);
            panel4Radio.add(radioFilter);
            radioGroup.add(radioFilter);
        });

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
