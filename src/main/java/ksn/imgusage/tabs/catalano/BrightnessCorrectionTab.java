package ksn.imgusage.tabs.catalano;

import java.awt.Cursor;
import java.awt.image.BufferedImage;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BrightnessCorrection;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.utils.UiHelper;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/BrightnessCorrection.java'>Brightness adjusting in RGB color space</a> */
public class BrightnessCorrectionTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(BrightnessCorrectionTab.class);
    private static final int MIN = -255;
    private static final int MAX =  255;

    private final ITabHandler tabHandler;
    private ITab source;
    private BufferedImage image;
    private boolean boosting = true;
    private Runnable imagePanelInvalidate;
    private SliderIntModel modelAdjust = new SliderIntModel(100, 0, MIN, MAX);
    private Timer timer;

    public BrightnessCorrectionTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;

        makeTab();
    }

    public BrightnessCorrectionTab(ITabHandler tabHandler, ITab source, boolean boosting, int adjustValue) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.boosting = boosting;
        this.modelAdjust.setValue(adjustValue);

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

            BrightnessCorrection brightnessCorrection = new BrightnessCorrection(modelAdjust.getValue());
            brightnessCorrection.applyInPlace(bmp);

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
             BrightnessCorrection.class.getSimpleName(),
             true,
             this::makeFilterOptions
         );
    }

    public void makeFilterOptions(JPanel imagePanel, Box boxCenterLeft) {
        imagePanelInvalidate = imagePanel::repaint;

        boxCenterLeft.add(UiHelper.makeAsBoostCheckBox(() -> boosting, b -> boosting = b, this::resetImage));

        {
            Box boxOptions = Box.createHorizontalBox();
            boxOptions.setBorder(BorderFactory.createTitledBorder("Brightness correction"));

            boxOptions.add(Box.createHorizontalGlue());
            UiHelper.makeSliderVert(boxOptions, modelAdjust, "Adjust", "Brightness adjust value");
            boxOptions.add(Box.createHorizontalGlue());

            boxCenterLeft.add(boxOptions);

            modelAdjust.getWrapped().addChangeListener(ev -> {
                logger.trace("modelAdjust: value={}", modelAdjust.getFormatedText());
                debounceResetImage();
            });
        }
    }

    private void debounceResetImage() {
        UiHelper.debounceExecutor(() -> timer, t -> timer = t, 300, this::resetImage, logger);
    }

    @Override
    public void printParams() {
        logger.info("adjustValue={}", modelAdjust.getFormatedText());
    }

}
