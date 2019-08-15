package ksn.imgusage.tabs.catalano;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.BrightnessCorrection;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.catalano.BrightnessCorrectionTabParams;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/BrightnessCorrection.java'>Brightness adjusting in RGB color space</a> */
public class BrightnessCorrectionTab extends CatalanoFilterTab<BrightnessCorrectionTabParams> {

    public static final String TAB_NAME = BrightnessCorrection.class.getSimpleName();
    public static final String TAB_FULL_NAME = TAB_PREFIX + TAB_NAME;
    public static final String TAB_DESCRIPTION = "Brightness adjusting in RGB color space";

    private static final int MIN = -255;
    private static final int MAX =  255;

    private BrightnessCorrectionTabParams params;

    public BrightnessCorrectionTab() {
        super(false);
    }

    @Override
    public Component makeTab(BrightnessCorrectionTabParams params) {
        if (params == null)
            params = new BrightnessCorrectionTabParams(100);
        this.params = params;

        return makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }
    @Override
    public String getTabFullName() { return TAB_FULL_NAME; }

    @Override
    protected void applyCatalanoFilter() {
        new BrightnessCorrection(params.adjust)
            .applyInPlace(imageFBmp);
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

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

        return box4Options;
    }

    @Override
    public BrightnessCorrectionTabParams getParams() {
        return params;
    }

}
