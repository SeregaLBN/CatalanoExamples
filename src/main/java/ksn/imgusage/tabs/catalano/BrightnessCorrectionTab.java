package ksn.imgusage.tabs.catalano;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.BrightnessCorrection;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/BrightnessCorrection.java'>Brightness adjusting in RGB color space</a> */
public class BrightnessCorrectionTab extends CatalanoFilterTab {

    public static final String TAB_NAME = BrightnessCorrection.class.getSimpleName();
    public static final String TAB_DESCRIPTION = "Brightness adjusting in RGB color space";

    private static final int MIN = -255;
    private static final int MAX =  255;

    private final SliderIntModel modelAdjust;

    public BrightnessCorrectionTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, 100);
    }

    public BrightnessCorrectionTab(ITabHandler tabHandler, ITab source, int adjustValue) {
        super(tabHandler, source, false);
        this.modelAdjust = new SliderIntModel(adjustValue, 0, MIN, MAX);

        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyCatalanoFilter() {
        new BrightnessCorrection(modelAdjust.getValue())
            .applyInPlace(imageFBmp);
    }

    @Override
    protected void makeOptions(Box box4Options) {
        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTabName() + " options"));

        boxOptions.add(Box.createHorizontalGlue());
        boxOptions.add(makeSliderVert(modelAdjust, "Adjust", "Brightness adjust value"));
        boxOptions.add(Box.createHorizontalGlue());

        box4Options.add(boxOptions);

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
