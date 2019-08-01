package ksn.catalano.examples.filter.tabs;

import java.awt.Cursor;
import java.util.Locale;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BernsenThreshold;

public class BernsenThresholdTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(BernsenThresholdTab.class);

    private static final int MIN_RADIUS = 0;                       // Size of window (should be an odd number).
    private static final int MAX_RADIUS = 100;

    private static final double CONTRAST_THRESHOLD_COEF = 0.01;
    private static final int MIN_CONTRAST_THRESHOLD = (int)( 0 /CONTRAST_THRESHOLD_COEF);
    private static final int MAX_CONTRAST_THRESHOLD = (int)(300/CONTRAST_THRESHOLD_COEF);

    private final ITabHandler tabHandler;
    private ITab source;
    private FastBitmap image;
    private boolean boosting = true;
    private Runnable imagePanelInvalidate;
    private DefaultBoundedRangeModel modelRadius            = new DefaultBoundedRangeModel(15, 0, MIN_RADIUS            , MAX_RADIUS);
    private DefaultBoundedRangeModel modelСontrastThreshold = new DefaultBoundedRangeModel(15, 0, MIN_CONTRAST_THRESHOLD, MAX_CONTRAST_THRESHOLD);
    private Timer timer;

    public BernsenThresholdTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;

        makeTab();
    }

    public BernsenThresholdTab(ITabHandler tabHandler, ITab source, boolean boosting, int radius, double contrastThreshold) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.modelRadius.setValue(radius);
        this.modelСontrastThreshold.setValue((int)(contrastThreshold / CONTRAST_THRESHOLD_COEF));
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
            if (!image.isGrayscale())
                image.toGrayscale();

            BernsenThreshold bernsenThreshold = new BernsenThreshold(modelRadius.getValue(), modelСontrastThreshold.getValue() * CONTRAST_THRESHOLD_COEF);
            bernsenThreshold.applyInPlace(image);
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
            UiHelper.makeSliderVert(boxOptions, "Radius", modelRadius, null, "Radius");
            boxOptions.add(Box.createHorizontalStrut(8));
            UiHelper.makeSliderVert(boxOptions, "Contrast Threshold", modelСontrastThreshold, v -> String.format(Locale.US, "%.2f", v * CONTRAST_THRESHOLD_COEF), "Contrast Threshold");
            boxOptions.add(Box.createHorizontalGlue());

            boxCenterLeft.add(boxOptions);

            modelRadius.addChangeListener(ev -> {
                int valRadius = modelRadius.getValue();
                logger.trace("modelRadius: value={}", valRadius);
                debounceResetImage();
            });
        }
    }

    private void debounceResetImage() {
        UiHelper.debounceExecutor(() -> timer, t -> timer = t, 300, this::resetImage, logger);
    }

}
