package ksn.catalano.examples.filter.tabs;

import java.awt.Cursor;
import java.awt.event.ItemEvent;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Rotate;
import Catalano.Imaging.Filters.Rotate.Algorithm;
import ksn.catalano.examples.filter.model.SliderDoubleModel;
import ksn.catalano.examples.filter.util.UiHelper;

public class RotateTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(RotateTab.class);
    private static final double MIN =   0;
    private static final double MAX = 360;

    private final ITabHandler tabHandler;
    private ITab source;
    private FastBitmap image;
    private boolean boosting = true;
    private boolean keepSize = true;
    private Rotate.Algorithm algorithm = Algorithm.BICUBIC;
    private Runnable imagePanelInvalidate;
    private SliderDoubleModel modelAngle = new SliderDoubleModel(100, 0, MIN, MAX);
    private Timer timer;

    public RotateTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;

        makeTab();
    }

    public RotateTab(ITabHandler tabHandler, ITab source, boolean boosting, double angle, boolean keepSize, Rotate.Algorithm algorithm) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.modelAngle.setValue(angle);
        this.keepSize = keepSize;
        this.algorithm = algorithm;
        this.boosting = boosting;

        makeTab();
    }

    @Override
    public FastBitmap getImage() {
        if (image != null)
            return image;
        if (source == null)
            return null;

        image = source.getImage();
        if (image == null)
            return null;

        JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(tabHandler.getTabPanel());
        try {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            image = new FastBitmap(image);
            if (boosting)
                image = UiHelper.boostImage(image, logger);

            Rotate rotate = new Rotate(modelAngle.getValue(), keepSize, algorithm);
            rotate.applyInPlace(image);
        } finally {
            frame.setCursor(Cursor.getDefaultCursor());
        }
        return image;
    }


    @Override
    public void resetImage() {
        if (image == null)
            return;

        image = null;
        imagePanelInvalidate.run();
        SwingUtilities.invokeLater(() -> tabHandler.onImageChanged(this));
    }

    @Override
    public void updateSource(ITab newSource) {
        this.source = newSource;
        resetImage();
    }

    private void makeTab() {
        UiHelper.makeTab(
             tabHandler,
             this,
             Rotate.class.getSimpleName(),
             true,
             this::makeFilterOptions
         );
    }

    public void makeFilterOptions(JPanel imagePanel, Box boxCenterLeft) {
        imagePanelInvalidate = imagePanel::repaint;

        boxCenterLeft.add(UiHelper.makeAsBoostCheckBox(() -> boosting, b -> boosting = b, this::resetImage));

        {
            Box boxOptions = Box.createHorizontalBox();
            boxOptions.setBorder(BorderFactory.createTitledBorder("Rotate options"));

            Box box2 = Box.createVerticalBox();
            box2.setBorder(BorderFactory.createTitledBorder(""));

            JCheckBox btnKeepSize = new JCheckBox("Keep size", keepSize);
            btnKeepSize.setToolTipText("Keep the original size");
            btnKeepSize.addActionListener(ev -> {
                keepSize  = btnKeepSize.isSelected();
                resetImage();
            });
            box2.add(btnKeepSize);

            Box box4Alg = Box.createVerticalBox();
            box4Alg.setBorder(BorderFactory.createTitledBorder("Algorithm"));
            box4Alg.setToolTipText("Interpolation algorithm");
            ButtonGroup radioGroup = new ButtonGroup();
            for (Rotate.Algorithm alg : Rotate.Algorithm.values()) {
                JRadioButton radioBtnAlg = new JRadioButton(alg.name(), alg == this.algorithm);
                radioBtnAlg.setActionCommand(alg.name());
                radioBtnAlg.setToolTipText("Interpolation algorithm");
                radioBtnAlg.addItemListener(ev -> {
                    if (ev.getStateChange() == ItemEvent.SELECTED) {
                        this.algorithm = alg;
                        logger.trace("algorithm changed to {}", alg);
                        resetImage();
                    }
                });
                box4Alg.add(radioBtnAlg);
                radioGroup.add(radioBtnAlg);
            }
            box2.add(box4Alg);

            boxOptions.add(Box.createHorizontalGlue());
            UiHelper.makeSliderVert(boxOptions, modelAngle, "Angle", "Angle");
            boxOptions.add(Box.createHorizontalStrut(8));
            boxOptions.add(box2);
            boxOptions.add(Box.createHorizontalGlue());

            boxCenterLeft.add(boxOptions);

            modelAngle.getWrapped().addChangeListener(ev -> {
                logger.trace("modelAngle: value={}", modelAngle.getFormatedText());
                debounceResetImage();
            });
        }
    }

    private void debounceResetImage() {
        UiHelper.debounceExecutor(() -> timer, t -> timer = t, 300, this::resetImage, logger);
    }

}
