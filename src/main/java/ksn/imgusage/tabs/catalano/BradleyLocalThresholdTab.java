package ksn.imgusage.tabs.catalano;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.BradleyLocalThreshold;
import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.catalano.BradleyLocalThresholdTabParams;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/BradleyLocalThreshold.java'>Adaptive thresholding using the integral image</a> */
public class BradleyLocalThresholdTab extends CatalanoFilterTab<BradleyLocalThresholdTabParams> {

    public static final String TAB_TITLE = BradleyLocalThreshold.class.getSimpleName();
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Adaptive thresholding using the integral image";

    private static final int    MIN_WINDOW_SIZE           =    2;
    private static final int    MAX_WINDOW_SIZE           = 1000;
    private static final double MIN_PIXEL_BRIGHTNESS_DIFF =    0;
    private static final double MAX_PIXEL_BRIGHTNESS_DIFF =  300;

    private BradleyLocalThresholdTabParams params;

    public BradleyLocalThresholdTab() {
        super(true);
    }

    @Override
    public Component makeTab(BradleyLocalThresholdTabParams params) {
        if (params == null)
            params = new BradleyLocalThresholdTabParams();
        this.params = params;

        return makeTab();
    }

    @Override
    public String getTitle() { return TAB_TITLE; }
    @Override
    public String getName() { return TAB_NAME; }
    @Override
    public String getDescription() { return TAB_DESCRIPTION; }

    @Override
    protected void applyCatalanoFilter() {
        new BradleyLocalThreshold(params.windowSize, (float)params.pixelBrightnessDiff)
            .applyInPlace(imageFBmp);
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderIntModel    modelWindowSize          = new SliderIntModel   (params.windowSize         , 0, MIN_WINDOW_SIZE          , MAX_WINDOW_SIZE);
        SliderDoubleModel modelPixelBrightnessDiff = new SliderDoubleModel(params.pixelBrightnessDiff, 0, MIN_PIXEL_BRIGHTNESS_DIFF, MAX_PIXEL_BRIGHTNESS_DIFF);

        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));

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

        return box4Options;
    }

    @Override
    public BradleyLocalThresholdTabParams getParams() {
        return params;
    }

}
