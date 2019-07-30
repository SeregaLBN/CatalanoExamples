package ksn.catalano.examples.filter.tabs;

import java.awt.Cursor;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.AdaptiveContrastEnhancement;
import Catalano.Imaging.Filters.Resize;

public class AdaptiveContrastEnhancementTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(AdaptiveContrastEnhancementTab.class);
    private static final int MIN_WINDOW_SIZE = 0;
    private static final int MAX_WINDOW_SIZE = 200;
    private static final int MIN_K           = 0;
    private static final int MAX_K           = 2000;
    private static final int MIN_GAIN        = 0;
    private static final int MAX_GAIN        = 2000;
    private static final double    K_COEF = 0.01;
    private static final double GAIN_COEF = 0.01;

    private final ITabHandler tabHandler;
    private ITab source;
    private FastBitmap image;
    private boolean boosting = true;
    private Runnable imagePanelInvalidate;
    private DefaultBoundedRangeModel modelWinSize = new DefaultBoundedRangeModel( 20, 0, MIN_WINDOW_SIZE, MAX_WINDOW_SIZE);
    private DefaultBoundedRangeModel modelK1      = new DefaultBoundedRangeModel( 30, 0, MIN_K          , MAX_K);
    private DefaultBoundedRangeModel modelK2      = new DefaultBoundedRangeModel( 60, 0, MIN_K          , MAX_K);
    private DefaultBoundedRangeModel modelMinGain = new DefaultBoundedRangeModel( 10, 0, MIN_GAIN       , MAX_GAIN);
    private DefaultBoundedRangeModel modelMaxGain = new DefaultBoundedRangeModel(100, 0, MIN_GAIN       , MAX_GAIN);
    private Timer timer;

    public AdaptiveContrastEnhancementTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;
    }

    public AdaptiveContrastEnhancementTab(ITabHandler tabHandler, ITab source, int windowSize, double k1, double k2, double minGain, double maxGain, boolean boosting) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.modelWinSize.setValue(windowSize);
        this.modelK1     .setValue((int)(k1 / K_COEF));
        this.modelK2     .setValue((int)(k2 / K_COEF));
        this.modelMinGain.setValue((int)(minGain / GAIN_COEF));
        this.modelMaxGain.setValue((int)(maxGain / GAIN_COEF));
        this.boosting = boosting;
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
            if (boosting) {
                double zoomX = 400 / (double)image.getWidth();
                double zoomY = 250 / (double)image.getHeight();
                double zoom = Math.min(zoomX, zoomY);
                logger.trace("zoom={}", zoom);
                if (zoom < 1) {
                    int newWidth  = (int)(zoom * image.getWidth());
                    int newHeight = (int)(zoom * image.getHeight());

                    Resize resize = new Resize(newWidth, newHeight);
                    image = resize.apply(image);
                }
            }
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

    public void makeTab() {
        FirstTab.makeTab(
             tabHandler,
             this,
             AdaptiveContrastEnhancement.class.getSimpleName(),
             true,
             this::makeFrequencyFilterOptions
         );
    }

    public void makeFrequencyFilterOptions(JPanel imagePanel, Box boxCenterLeft) {
        imagePanelInvalidate = imagePanel::repaint;

        {
            JCheckBox btnAsBoost = new JCheckBox("Boosting", boosting);
            btnAsBoost.addActionListener(ev -> {
                boosting = btnAsBoost.isSelected();
                resetImage();
            });
            boxCenterLeft.add(btnAsBoost);
        }

        {
            Box boxOptions = Box.createHorizontalBox();
            boxOptions.setBorder(BorderFactory.createTitledBorder("Adaptive contrast"));

            boxOptions.add(Box.createHorizontalGlue());
            FrequencyFilterTab.makeSliderVert(boxOptions, "Window size", modelWinSize, null);
            boxOptions.add(Box.createHorizontalStrut(8));
            FrequencyFilterTab.makeSliderVert(boxOptions, "K1"         , modelK1     , v -> String.format("%.2f", v * K_COEF));
            boxOptions.add(Box.createHorizontalStrut(8));
            FrequencyFilterTab.makeSliderVert(boxOptions, "K2"         , modelK2     , v -> String.format("%.2f", v * K_COEF));
            boxOptions.add(Box.createHorizontalStrut(8));
            JSlider sliderMinGain = FrequencyFilterTab.makeSliderVert(boxOptions, "Min gain", modelMinGain, v -> String.format("%.2f", v * GAIN_COEF));
            boxOptions.add(Box.createHorizontalStrut(8));
            JSlider sliderMaxGain = FrequencyFilterTab.makeSliderVert(boxOptions, "Max gain", modelMaxGain, v -> String.format("%.2f", v * GAIN_COEF));
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
        if (timer == null)
            timer = new Timer(300, ev -> {
                timer.stop();
                logger.info("debounce: call resetImage");
                resetImage();
            });

        if (timer.isRunning())
            timer.restart();
        else
            timer.start();
    }

}
