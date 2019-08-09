package ksn.imgusage.tabs.catalano;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

import Catalano.Imaging.Filters.BradleyLocalThreshold;
import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/BradleyLocalThreshold.java'>Adaptive thresholding using the integral image</a> */
public class BradleyLocalThresholdTab extends CatalanoFilterTab {

    private static final int    MIN_WINDOW_SIZE           =    2;
    private static final int    MAX_WINDOW_SIZE           = 1000;
    private static final double MIN_PIXEL_BRIGHTNESS_DIFF =    0;
    private static final double MAX_PIXEL_BRIGHTNESS_DIFF =  300;

    private final SliderIntModel    modelWindowSize;
    private final SliderDoubleModel modelPixelBrightnessDiff;

    public BradleyLocalThresholdTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, true, 41, 0.15);
    }

    public BradleyLocalThresholdTab(ITabHandler tabHandler, ITab source, boolean boosting, int windowSize, double pixelBrightnessDiff) {
        super(tabHandler, source, boosting, true);
        this.modelWindowSize          = new SliderIntModel   (windowSize         , 0, MIN_WINDOW_SIZE          , MAX_WINDOW_SIZE);
        this.modelPixelBrightnessDiff = new SliderDoubleModel(pixelBrightnessDiff, 0, MIN_PIXEL_BRIGHTNESS_DIFF, MAX_PIXEL_BRIGHTNESS_DIFF);

        makeTab();
    }

    @Override
    public String getTabName() { return BradleyLocalThreshold.class.getSimpleName(); }

    @Override
    protected void applyCatalanoFilter() {
        new BradleyLocalThreshold(modelWindowSize.getValue(), (float)(double)modelPixelBrightnessDiff.getValue())
            .applyInPlace(imageFBmp);
    }

    @Override
    protected void makeOptions(JPanel imagePanel, Box boxCenterLeft) {
        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTabName() + " options"));

        boxOptions.add(Box.createHorizontalGlue());
        boxOptions.add(makeSliderVert(modelWindowSize, "Window size", "Window size to calculate average value of pixels for"));
        boxOptions.add(Box.createHorizontalStrut(8));
        boxOptions.add(makeSliderVert(modelPixelBrightnessDiff, "Brightness difference", "Brightness difference limit between processing pixel and average value across neighbors"));
        boxOptions.add(Box.createHorizontalGlue());

        boxCenterLeft.add(boxOptions);

        modelWindowSize.getWrapped().addChangeListener(ev -> {
            logger.trace("modelRadius: value={}", modelWindowSize.getFormatedText());
            resetImage();
        });
        modelPixelBrightnessDiff.getWrapped().addChangeListener(ev -> {
            logger.trace("modelPixelBrightnessDiff: value={}", modelPixelBrightnessDiff.getFormatedText());
            resetImage();
        });
    }

    @Override
    public void printParams() {
        logger.info("windowSize={}, pixelBrightnessDiff={}", modelWindowSize.getFormatedText(), modelPixelBrightnessDiff.getFormatedText());
    }

}
