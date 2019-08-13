package ksn.imgusage.tabs.catalano;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.BrightnessCorrection;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.ITabParams;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/BrightnessCorrection.java'>Brightness adjusting in RGB color space</a> */
public class BrightnessCorrectionTab extends CatalanoFilterTab<BrightnessCorrectionTab.Params> {

    public static final String TAB_NAME = BrightnessCorrection.class.getSimpleName();
    public static final String TAB_DESCRIPTION = "Brightness adjusting in RGB color space";

    private static final int MIN = -255;
    private static final int MAX =  255;

    public static class Params implements ITabParams {
        public int adjust;
        public Params(int adjustValue) { this.adjust = adjustValue; }
        @Override
        public String toString() { return "{ adjust=" + adjust + " }"; }
    }

    private final Params params;

    public BrightnessCorrectionTab(ITabHandler tabHandler, ITab<?> source) {
        this(tabHandler, source, new Params(100));
    }

    public BrightnessCorrectionTab(ITabHandler tabHandler, ITab<?> source, Params params) {
        super(tabHandler, source, false);
        this.params = params;

        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyCatalanoFilter() {
        new BrightnessCorrection(params.adjust)
            .applyInPlace(imageFBmp);
    }

    @Override
    protected void makeOptions(Box box4Options) {
        SliderIntModel modelAdjust = new SliderIntModel(params.adjust, 0, MIN, MAX);

        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTabName() + " options"));

        boxOptions.add(Box.createHorizontalGlue());
        boxOptions.add(makeSliderVert(modelAdjust, "Adjust", "Brightness adjust value"));
        boxOptions.add(Box.createHorizontalGlue());

        box4Options.add(boxOptions);

        modelAdjust.getWrapped().addChangeListener(ev -> {
            logger.trace("modelAdjust: value={}", modelAdjust.getFormatedText());
            params.adjust = modelAdjust.getValue();
            resetImage();
        });
    }

    @Override
    public Params getParams() {
        return params;
    }

}
