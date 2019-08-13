package ksn.imgusage.tabs.catalano;

import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.BradleyLocalThreshold;
import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.ITabParams;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/BradleyLocalThreshold.java'>Adaptive thresholding using the integral image</a> */
public class BradleyLocalThresholdTab extends CatalanoFilterTab<BradleyLocalThresholdTab.Params> {

    public static final String TAB_NAME = BradleyLocalThreshold.class.getSimpleName();
    public static final String TAB_DESCRIPTION = "Adaptive thresholding using the integral image";

    private static final int    MIN_WINDOW_SIZE           =    2;
    private static final int    MAX_WINDOW_SIZE           = 1000;
    private static final double MIN_PIXEL_BRIGHTNESS_DIFF =    0;
    private static final double MAX_PIXEL_BRIGHTNESS_DIFF =  300;

    public static class Params implements ITabParams {
        public int    windowSize;
        public double pixelBrightnessDiff;

        public Params(
            int    windowSize,
            double pixelBrightnessDiff)
        {
            this.windowSize          = windowSize;
            this.pixelBrightnessDiff = pixelBrightnessDiff;
        }

        @Override
        public String toString() {
            return String.format(Locale.US,
                    "{ windowSize=%d, pixelBrightnessDiff=%.2f }",
                    windowSize,
                    pixelBrightnessDiff);
        }
    }
    private final Params params;


    public BradleyLocalThresholdTab(ITabHandler tabHandler, ITab<?> source) {
        this(tabHandler, source, new Params(41, 0.15));
    }

    public BradleyLocalThresholdTab(ITabHandler tabHandler, ITab<?> source, Params params) {
        super(tabHandler, source, true);
        this.params = params;

        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyCatalanoFilter() {
        new BradleyLocalThreshold(params.windowSize, (float)params.pixelBrightnessDiff)
            .applyInPlace(imageFBmp);
    }

    @Override
    protected void makeOptions(Box box4Options) {
        SliderIntModel    modelWindowSize          = new SliderIntModel   (params.windowSize         , 0, MIN_WINDOW_SIZE          , MAX_WINDOW_SIZE);
        SliderDoubleModel modelPixelBrightnessDiff = new SliderDoubleModel(params.pixelBrightnessDiff, 0, MIN_PIXEL_BRIGHTNESS_DIFF, MAX_PIXEL_BRIGHTNESS_DIFF);

        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTabName() + " options"));

        boxOptions.add(Box.createHorizontalGlue());
        boxOptions.add(makeSliderVert(modelWindowSize, "Window size", "Window size to calculate average value of pixels for"));
        boxOptions.add(Box.createHorizontalStrut(8));
        boxOptions.add(makeSliderVert(modelPixelBrightnessDiff, "Brightness difference", "Brightness difference limit between processing pixel and average value across neighbors"));
        boxOptions.add(Box.createHorizontalGlue());

        box4Options.add(boxOptions);

        modelWindowSize.getWrapped().addChangeListener(ev -> {
            logger.trace("modelRadius: value={}", modelWindowSize.getFormatedText());
            params.windowSize = modelWindowSize.getValue();
            resetImage();
        });
        modelPixelBrightnessDiff.getWrapped().addChangeListener(ev -> {
            logger.trace("modelPixelBrightnessDiff: value={}", modelPixelBrightnessDiff.getFormatedText());
            params.pixelBrightnessDiff = modelPixelBrightnessDiff.getValue();
            resetImage();
        });
    }

    @Override
    public Params getParams() {
        return params;
    }

}
