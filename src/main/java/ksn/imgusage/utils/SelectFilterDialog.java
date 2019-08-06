package ksn.imgusage.utils;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.Filters.*;

public class SelectFilterDialog {

    private static final Logger logger = LoggerFactory.getLogger(SelectFilterDialog.class);
    public static final String CATALANO_TAB_PREFIX = "Catalano:";
    public static final String   OPENCV_TAB_PREFIX = "OpenCV:";

    private final Frame owner;

    public SelectFilterDialog(Frame owner) {
        this.owner = owner;
    }

    private static class FilterTabs {
        public final String filterName;
        public final String description;
        public FilterTabs(Class<?> filterClass, String description) {
            this.filterName  = filterClass.getSimpleName();
            this.description = description;
        }
        public FilterTabs(String filterName, String description) {
            this.filterName  = filterName;
            this.description = description;
        }
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

        Box boxCatalanoFilters = Box.createVerticalBox();
        boxCatalanoFilters.setBorder(BorderFactory.createTitledBorder("Catalano filters"));
        ButtonGroup radioGroup = new ButtonGroup();

        Arrays.asList( // alphabetical sort
            new FilterTabs(AdaptiveContrastEnhancement.class, "Adaptive Contrast Enhancement is modification of the gray level values based on some criterion that adjusts its parameters as local image characteristics change"),
            new FilterTabs(ArtifactsRemoval           .class, "Remove artifacts caused by uneven lightning"),
            new FilterTabs(BernsenThreshold           .class, "The method uses a user-provided contrast threshold"),
            new FilterTabs(Blur                       .class, "Blur filter"),
            new FilterTabs(BradleyLocalThreshold      .class, "Adaptive thresholding using the integral image"),
            new FilterTabs(BrightnessCorrection       .class, "Brightness adjusting in RGB color space"),
            new FilterTabs(FrequencyFilter            .class, "Filtering of frequencies outside of specified range in complex Fourier transformed image"),
            new FilterTabs(Rotate                     .class, "Rotate image")
        ).forEach(tab -> {
            JRadioButton radioFilter = new JRadioButton(tab.filterName + ": " + tab.description);
            radioFilter.setActionCommand(CATALANO_TAB_PREFIX + tab.filterName);
            boxCatalanoFilters.add(radioFilter);
            radioGroup.add(radioFilter);
        });

        Box boxOpenCvFilters = Box.createVerticalBox();
        boxOpenCvFilters.setBorder(BorderFactory.createTitledBorder("OpenCV filters"));

        Arrays.<FilterTabs>asList( // alphabetical sort
            new FilterTabs("AsIs"        , "As is"),
            new FilterTabs("GaussianBlur", "Blurs an image using a Gaussian filter")
        ).forEach(tab -> {
            JRadioButton radioFilter = new JRadioButton(tab.filterName + ": " + tab.description);
            radioFilter.setActionCommand(OPENCV_TAB_PREFIX + tab.filterName);
            boxOpenCvFilters.add(radioFilter);
            radioGroup.add(radioFilter);
        });


        JButton btnOk = new JButton("Ok");
        String[] filterClassName = { null };
        btnOk.addActionListener(ev -> {
            dlg.dispose();

            ButtonModel bm = radioGroup.getSelection();
            if (bm == null)
                return;
            filterClassName[0] = bm.getActionCommand();
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

        return filterClassName[0];
    }

}
