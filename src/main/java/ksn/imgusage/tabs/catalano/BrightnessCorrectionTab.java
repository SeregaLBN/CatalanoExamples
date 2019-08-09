package ksn.imgusage.tabs.catalano;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

import Catalano.Imaging.Filters.BrightnessCorrection;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/BrightnessCorrection.java'>Brightness adjusting in RGB color space</a> */
public class BrightnessCorrectionTab extends CatalanoFilterTab {

    private static final int MIN = -255;
    private static final int MAX =  255;

    private final SliderIntModel modelAdjust;

    public BrightnessCorrectionTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, true, 100);
    }

    public BrightnessCorrectionTab(ITabHandler tabHandler, ITab source, boolean boosting, int adjustValue) {
        super(tabHandler, source, boosting, false);
        this.modelAdjust = new SliderIntModel(adjustValue, 0, MIN, MAX);

        makeTab();
    }

    @Override
    public String getTabName() { return BrightnessCorrection.class.getSimpleName(); }

    @Override
    protected void applyCatalanoFilter() {
        new BrightnessCorrection(modelAdjust.getValue())
            .applyInPlace(imageFBmp);
    }

    @Override
    protected void makeOptions(JPanel imagePanel, Box boxCenterLeft) {
        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder("Brightness correction"));

        boxOptions.add(Box.createHorizontalGlue());
        boxOptions.add(makeSliderVert(modelAdjust, "Adjust", "Brightness adjust value"));
        boxOptions.add(Box.createHorizontalGlue());

        boxCenterLeft.add(boxOptions);

        modelAdjust.getWrapped().addChangeListener(ev -> {
            logger.trace("modelAdjust: value={}", modelAdjust.getFormatedText());
            resetImage();
        });
    }

    @Override
    public void printParams() {
        logger.info("adjustValue={}", modelAdjust.getFormatedText());
    }

}
