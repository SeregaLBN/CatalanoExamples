package ksn.imgusage.tabs.catalano;

import java.awt.Cursor;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.AdaptiveContrastEnhancement;
import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.utils.ImgWrapper;
import ksn.imgusage.utils.UiHelper;

public class AdaptiveContrastTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(AdaptiveContrastTab.class);

    private static final int    MIN_WINDOW_SIZE =   1; // Size of window (should be an odd number).
    private static final int    MAX_WINDOW_SIZE = 201;
    private static final double MIN_K1          =   0; // Local gain factor, between 0 and 1.
    private static final double MAX_K1          =   1;
    private static final double MIN_K2          =   0; // Local mean constant, between 0 and 1.
    private static final double MAX_K2          =   1;
    private static final double MIN_GAIN        =   0; // The minimum gain factor
    private static final double MAX_GAIN        =  20; // The maximum gain factor

    private final ITabHandler tabHandler;
    private ITab source;
    private FastBitmap image;
    private boolean boosting = true;
    private Runnable imagePanelInvalidate;
    private SliderIntModel    modelWinSize = new SliderIntModel   (  20, 0, MIN_WINDOW_SIZE, MAX_WINDOW_SIZE);
    private SliderDoubleModel modelK1      = new SliderDoubleModel(0.30, 0, MIN_K1         , MAX_K1);
    private SliderDoubleModel modelK2      = new SliderDoubleModel(0.60, 0, MIN_K2         , MAX_K2);
    private SliderDoubleModel modelMinGain = new SliderDoubleModel(0.10, 0, MIN_GAIN       , MAX_GAIN);
    private SliderDoubleModel modelMaxGain = new SliderDoubleModel(1.00, 0, MIN_GAIN       , MAX_GAIN);
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
        this.modelK1     .setValue(k1);
        this.modelK2     .setValue(k2);
        this.modelMinGain.setValue(minGain);
        this.modelMaxGain.setValue(maxGain);
        this.boosting = boosting;

        makeTab();
    }

    @Override
    public ImgWrapper getImage() {
        if (image != null)
            return new ImgWrapper(image);
        if (source == null)
            return null;

        ImgWrapper wrp = source.getImage();
        if (wrp == null)
            return null;

        image = wrp.getFastBitmap();

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
                        modelK1     .getValue(),
                        modelK2     .getValue(),
                        modelMinGain.getValue(),
                        modelMaxGain.getValue()
                    );
            adaptiveContrastEnhancement.applyInPlace(image);
        } finally {
            frame.setCursor(Cursor.getDefaultCursor());
        }
        return new ImgWrapper(image);
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
            UiHelper.makeSliderVert(boxOptions, modelWinSize, "WinSize", "Size of window");
            boxOptions.add(Box.createHorizontalStrut(2));
            UiHelper.makeSliderVert(boxOptions, modelK1     , "K1"     , "Local gain factor");
            boxOptions.add(Box.createHorizontalStrut(2));
            UiHelper.makeSliderVert(boxOptions, modelK2     , "K2"     , "Local mean constant");
            boxOptions.add(Box.createHorizontalStrut(2));
            UiHelper.makeSliderVert(boxOptions, modelMinGain, "MinGain", "The minimum gain factor");
            boxOptions.add(Box.createHorizontalStrut(2));
            UiHelper.makeSliderVert(boxOptions, modelMaxGain, "MaxGain", "The maximum gain factor");
            boxOptions.add(Box.createHorizontalGlue());

            boxCenterLeft.add(boxOptions);

            modelWinSize.getWrapped().addChangeListener(ev -> {
                logger.trace("modelWinSize: value={}", modelWinSize.getFormatedText());
                debounceResetImage();
            });
            modelK1.getWrapped().addChangeListener(ev -> {
                logger.trace("modelK1: value={}", modelK1.getFormatedText());
                debounceResetImage();
            });
            modelK2.getWrapped().addChangeListener(ev -> {
                logger.trace("modelK2: value={}", modelK2.getFormatedText());
                debounceResetImage();
            });
            modelMinGain.getWrapped().addChangeListener(ev -> {
                logger.trace("modelMinGain: value={}", modelMinGain.getFormatedText());
                double valMinGain = modelMinGain.getValue();
                if (valMinGain > modelMaxGain.getValue())
                    modelMaxGain.setValue(valMinGain);
                debounceResetImage();
            });
            modelMaxGain.getWrapped().addChangeListener(ev -> {
                logger.trace("modelMaxGain: value={}", modelMaxGain.getFormatedText());
                double valMaxGain = modelMaxGain.getValue();
                if (valMaxGain < modelMinGain.getValue())
                    modelMinGain.setValue(valMaxGain);
                debounceResetImage();
            });
        }
    }

    private void debounceResetImage() {
        UiHelper.debounceExecutor(() -> timer, t -> timer = t, 300, this::resetImage, logger);
    }

}
