package ksn.imgusage.tabs.catalano;

import java.awt.Cursor;
import java.awt.image.BufferedImage;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BernsenThreshold;
import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.utils.UiHelper;

public class BernsenThresholdTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(BernsenThresholdTab.class);

    private static final int    MIN_RADIUS = 0;
    private static final int    MAX_RADIUS = 100;
    private static final double MIN_CONTRAST_THRESHOLD = 0;
    private static final double MAX_CONTRAST_THRESHOLD = 300;

    private final ITabHandler tabHandler;
    private ITab source;
    private BufferedImage image;
    private boolean boosting = true;
    private Runnable imagePanelInvalidate;
    private SliderIntModel    modelRadius            = new SliderIntModel   (15, 0, MIN_RADIUS            , MAX_RADIUS);
    private SliderDoubleModel modelContrastThreshold = new SliderDoubleModel(15, 0, MIN_CONTRAST_THRESHOLD, MAX_CONTRAST_THRESHOLD);
    private Timer timer;

    public BernsenThresholdTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;

        makeTab();
    }

    public BernsenThresholdTab(ITabHandler tabHandler, ITab source, boolean boosting, int radius, double contrastThreshold) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.boosting = boosting;
        this.modelRadius.setValue(radius);
        this.modelContrastThreshold.setValue(contrastThreshold);

        makeTab();
    }

    @Override
    public BufferedImage getImage() {
        if (image != null)
            return image;

        BufferedImage src = source.getImage();
        if (src == null)
            return null;

        JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(tabHandler.getTabPanel());
        try {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            FastBitmap bmp = new FastBitmap(src);
            if (boosting)
                bmp = UiHelper.boostImage(bmp, logger);
            if (!bmp.isGrayscale())
                bmp.toGrayscale();

            BernsenThreshold bernsenThreshold = new BernsenThreshold(modelRadius.getValue(), modelContrastThreshold.getValue());
            bernsenThreshold.applyInPlace(bmp);

            image = bmp.toBufferedImage();
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
             BernsenThreshold.class.getSimpleName(),
             true,
             this::makeFilterOptions
         );
    }

    public void makeFilterOptions(JPanel imagePanel, Box boxCenterLeft) {
        imagePanelInvalidate = imagePanel::repaint;

        boxCenterLeft.add(UiHelper.makeAsBoostCheckBox(() -> boosting, b -> boosting = b, this::resetImage));

        {
            Box boxOptions = Box.createHorizontalBox();
            boxOptions.setBorder(BorderFactory.createTitledBorder("Adaptive contrast"));

            boxOptions.add(Box.createHorizontalGlue());
            UiHelper.makeSliderVert(boxOptions, modelRadius, "Radius", "Radius");
            boxOptions.add(Box.createHorizontalStrut(8));
            UiHelper.makeSliderVert(boxOptions, modelContrastThreshold, "Contrast Threshold", "Contrast Threshold");
            boxOptions.add(Box.createHorizontalGlue());

            boxCenterLeft.add(boxOptions);

            modelRadius.getWrapped().addChangeListener(ev -> {
                logger.trace("modelRadius: value={}", modelRadius.getFormatedText());
                debounceResetImage();
            });
            modelContrastThreshold.getWrapped().addChangeListener(ev -> {
                logger.trace("modelContrastThreshold: value={}", modelContrastThreshold.getFormatedText());
                debounceResetImage();
            });
        }
    }

    private void debounceResetImage() {
        UiHelper.debounceExecutor(() -> timer, t -> timer = t, 300, this::resetImage, logger);
    }

    @Override
    public void printParams() {
        logger.info("radius={}, contrastThreshold={}",
                modelRadius           .getFormatedText(),
                modelContrastThreshold.getFormatedText());
    }

}
