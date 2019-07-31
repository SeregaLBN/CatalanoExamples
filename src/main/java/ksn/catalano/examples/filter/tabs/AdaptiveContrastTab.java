package ksn.catalano.examples.filter.tabs;

import java.awt.Cursor;
import java.util.Locale;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.AdaptiveContrastEnhancement;

public class AdaptiveContrastTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(AdaptiveContrastTab.class);

    private static final double    K_COEF = 0.01;
    private static final double GAIN_COEF = 0.01;
    private static final int MIN_WINDOW_SIZE = 1;                       // Size of window (should be an odd number).
    private static final int MAX_WINDOW_SIZE = 201;
    private static final int MIN_K1          = (int)( 0 /    K_COEF);   // Local gain factor, between 0 and 1.
    private static final int MAX_K1          = (int)( 1 /    K_COEF);
    private static final int MIN_K2          = (int)( 0 /    K_COEF);   // Local mean constant, between 0 and 1.
    private static final int MAX_K2          = (int)( 1 /    K_COEF);
    private static final int MIN_GAIN        = (int)( 0 / GAIN_COEF);   // The minimum gain factor
    private static final int MAX_GAIN        = (int)(20 / GAIN_COEF);   // The maximum gain factor

    private final ITabHandler tabHandler;
    private ITab source;
    private FastBitmap image;
    private boolean boosting = true;
    private Runnable imagePanelInvalidate;
    private DefaultBoundedRangeModel modelWinSize = new DefaultBoundedRangeModel( 20, 0, MIN_WINDOW_SIZE, MAX_WINDOW_SIZE);
    private DefaultBoundedRangeModel modelK1      = new DefaultBoundedRangeModel( 30, 0, MIN_K1         , MAX_K1);
    private DefaultBoundedRangeModel modelK2      = new DefaultBoundedRangeModel( 60, 0, MIN_K2         , MAX_K2);
    private DefaultBoundedRangeModel modelMinGain = new DefaultBoundedRangeModel( 10, 0, MIN_GAIN       , MAX_GAIN);
    private DefaultBoundedRangeModel modelMaxGain = new DefaultBoundedRangeModel(100, 0, MIN_GAIN       , MAX_GAIN);
    private Timer timer;

    public AdaptiveContrastTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;

        makeTab();
    }

    public AdaptiveContrastTab(ITabHandler tabHandler, ITab source, boolean boosting, int windowSize, double k1, double k2, double minGain, double maxGain) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.modelWinSize.setValue(windowSize);
        this.modelK1     .setValue((int)(k1 / K_COEF));
        this.modelK2     .setValue((int)(k2 / K_COEF));
        this.modelMinGain.setValue((int)(minGain / GAIN_COEF));
        this.modelMaxGain.setValue((int)(maxGain / GAIN_COEF));
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

            AdaptiveContrastEnhancement adaptiveContrastEnhancement = new AdaptiveContrastEnhancement(
                        modelWinSize.getValue(),
                        modelK1     .getValue() * K_COEF,
                        modelK2     .getValue() * K_COEF,
                        modelMinGain.getValue() * GAIN_COEF,
                        modelMaxGain.getValue() * GAIN_COEF
                    );
            adaptiveContrastEnhancement.applyInPlace(image);
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
             AdaptiveContrastEnhancement.class.getSimpleName(),
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
            UiHelper.makeSliderVert(boxOptions, "WinSize", modelWinSize, null                                                                          , "Size of window");
            boxOptions.add(Box.createHorizontalStrut(2));
            UiHelper.makeSliderVert(boxOptions, "K1"     , modelK1     , v -> String.format(Locale.US, "%.2f", v * K_COEF)                             , "Local gain factor");
            boxOptions.add(Box.createHorizontalStrut(2));
            UiHelper.makeSliderVert(boxOptions, "K2"     , modelK2     , v -> String.format(Locale.US, "%.2f", v * K_COEF)                             , "Local mean constant");
            boxOptions.add(Box.createHorizontalStrut(2));
            JSlider sliderMinGain = UiHelper.makeSliderVert(boxOptions, "MinGain", modelMinGain, v -> String.format(Locale.US, "%.2f", v * GAIN_COEF)  , "The minimum gain factor");
            boxOptions.add(Box.createHorizontalStrut(2));
            JSlider sliderMaxGain = UiHelper.makeSliderVert(boxOptions, "MaxGain", modelMaxGain, v -> String.format(Locale.US, "%.2f", v * GAIN_COEF)  , "The maximum gain factor");
            boxOptions.add(Box.createHorizontalGlue());

            boxCenterLeft.add(boxOptions);

            modelWinSize.addChangeListener(ev -> {
                int valWinSize = modelWinSize.getValue();
                logger.trace("modelWinSize: value={}", valWinSize);
                debounceResetImage();
            });
            modelK1.addChangeListener(ev -> {
                int valK1 = modelK1.getValue();
                logger.trace("modelK1: value={}", valK1 * K_COEF);
                debounceResetImage();
            });
            modelK2.addChangeListener(ev -> {
                int valK2 = modelK2.getValue();
                logger.trace("modelK2: value={}", valK2 * K_COEF);
                debounceResetImage();
            });
            modelMinGain.addChangeListener(ev -> {
                int valMinGain = modelMinGain.getValue();
                logger.trace("modelMinGain: value={}", valMinGain * GAIN_COEF);
                if (valMinGain > sliderMaxGain.getValue())
                    sliderMaxGain.setValue(valMinGain);
                debounceResetImage();
            });
            modelMaxGain.addChangeListener(ev -> {
                int valMaxGain = modelMaxGain.getValue();
                logger.trace("modelMaxGain: value={}", valMaxGain * GAIN_COEF);
                if (valMaxGain < sliderMinGain.getValue())
                    sliderMinGain.setValue(valMaxGain);
                debounceResetImage();
            });
        }
    }

    private void debounceResetImage() {
        UiHelper.debounceExecutor(() -> timer, t -> timer = t, 300, this::resetImage, logger);
    }

}
