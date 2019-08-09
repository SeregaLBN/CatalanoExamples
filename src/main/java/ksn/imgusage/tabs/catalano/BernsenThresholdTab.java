package ksn.imgusage.tabs.catalano;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

import Catalano.Imaging.Filters.BernsenThreshold;
import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/BernsenThreshold.java'>Bernsen Threshold</a> */
public class BernsenThresholdTab extends CatalanoFilterTab {

    private static final int    MIN_RADIUS = 0;
    private static final int    MAX_RADIUS = 100;
    private static final double MIN_CONTRAST_THRESHOLD = 0;
    private static final double MAX_CONTRAST_THRESHOLD = 300;

    private final SliderIntModel    modelRadius;
    private final SliderDoubleModel modelContrastThreshold;

    public BernsenThresholdTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, true, 15, 15);
    }

    public BernsenThresholdTab(ITabHandler tabHandler, ITab source, boolean boosting, int radius, double contrastThreshold) {
        super(tabHandler, source, boosting, true);
        this.modelRadius            = new SliderIntModel   (radius           , 0, MIN_RADIUS            , MAX_RADIUS);
        this.modelContrastThreshold = new SliderDoubleModel(contrastThreshold, 0, MIN_CONTRAST_THRESHOLD, MAX_CONTRAST_THRESHOLD);

        makeTab();
    }

    @Override
    public String getTabName() { return BernsenThreshold.class.getSimpleName(); }

    @Override
    protected void applyCatalanoFilter() {
        new BernsenThreshold(modelRadius.getValue(), modelContrastThreshold.getValue())
            .applyInPlace(imageFBmp);
    }

    @Override
    protected void makeOptions(JPanel imagePanel, Box boxCenterLeft) {
        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder("Adaptive contrast"));

        boxOptions.add(Box.createHorizontalGlue());
        boxOptions.add(makeSliderVert(modelRadius, "Radius", "Radius"));
        boxOptions.add(Box.createHorizontalStrut(8));
        boxOptions.add(makeSliderVert(modelContrastThreshold, "Contrast Threshold", "Contrast Threshold"));
        boxOptions.add(Box.createHorizontalGlue());

        boxCenterLeft.add(boxOptions);

        modelRadius.getWrapped().addChangeListener(ev -> {
            logger.trace("modelRadius: value={}", modelRadius.getFormatedText());
            resetImage();
        });
        modelContrastThreshold.getWrapped().addChangeListener(ev -> {
            logger.trace("modelContrastThreshold: value={}", modelContrastThreshold.getFormatedText());
            resetImage();
        });
    }

    @Override
    public void printParams() {
        logger.info("radius={}, contrastThreshold={}",
            modelRadius           .getFormatedText(),
            modelContrastThreshold.getFormatedText());
    }

}
