package ksn.imgusage.tabs.catalano;

import java.awt.Cursor;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BradleyLocalThreshold;
import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.utils.ImgWrapper;
import ksn.imgusage.utils.UiHelper;

public class BradleyLocalThresholdTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(BradleyLocalThresholdTab.class);

    private static final int    MIN_WINDOW_SIZE           =    2;
    private static final int    MAX_WINDOW_SIZE           = 1000;
    private static final double MIN_PIXEL_BRIGHTNESS_DIFF =    0;
    private static final double MAX_PIXEL_BRIGHTNESS_DIFF =  300;

    private final ITabHandler tabHandler;
    private ITab source;
    private FastBitmap image;
    private boolean boosting = true;
    private Runnable imagePanelInvalidate;
    private SliderIntModel    modelWindowSize          = new SliderIntModel   (  41, 0, MIN_WINDOW_SIZE          , MAX_WINDOW_SIZE);
    private SliderDoubleModel modelPixelBrightnessDiff = new SliderDoubleModel(0.15, 0, MIN_PIXEL_BRIGHTNESS_DIFF, MAX_PIXEL_BRIGHTNESS_DIFF);
    private Timer timer;

    public BradleyLocalThresholdTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;

        makeTab();
    }

    public BradleyLocalThresholdTab(ITabHandler tabHandler, ITab source, boolean boosting, int windowSize, double pixelBrightnessDiff) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.modelWindowSize.setValue(windowSize);
        this.modelPixelBrightnessDiff.setValue(pixelBrightnessDiff);
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

            BradleyLocalThreshold bradleyLocalThreshold = new BradleyLocalThreshold(modelWindowSize.getValue(), (float)(double)modelPixelBrightnessDiff.getValue());
            bradleyLocalThreshold.applyInPlace(image);
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
             BradleyLocalThreshold.class.getSimpleName(),
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
            UiHelper.makeSliderVert(boxOptions, modelWindowSize, "Window size", "Window size to calculate average value of pixels for");
            boxOptions.add(Box.createHorizontalStrut(8));
            UiHelper.makeSliderVert(boxOptions, modelPixelBrightnessDiff, "Brightness difference", "Brightness difference limit between processing pixel and average value across neighbors");
            boxOptions.add(Box.createHorizontalGlue());

            boxCenterLeft.add(boxOptions);

            modelWindowSize.getWrapped().addChangeListener(ev -> {
                logger.trace("modelRadius: value={}", modelWindowSize.getFormatedText());
                debounceResetImage();
            });
            modelPixelBrightnessDiff.getWrapped().addChangeListener(ev -> {
                logger.trace("modelPixelBrightnessDiff: value={}", modelPixelBrightnessDiff.getFormatedText());
                debounceResetImage();
            });
        }
    }

    private void debounceResetImage() {
        UiHelper.debounceExecutor(() -> timer, t -> timer = t, 300, this::resetImage, logger);
    }

}
