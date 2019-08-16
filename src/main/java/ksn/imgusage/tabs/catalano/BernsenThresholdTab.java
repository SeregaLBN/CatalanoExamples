package ksn.imgusage.tabs.catalano;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.BernsenThreshold;
import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.catalano.BernsenThresholdTabParams;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/BernsenThreshold.java'>Bernsen Threshold</a> */
public class BernsenThresholdTab extends CatalanoFilterTab<BernsenThresholdTabParams> {

    public static final String TAB_TITLE = BernsenThreshold.class.getSimpleName();
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "The method uses a user-provided contrast threshold";

    private static final int    MIN_RADIUS = 0;
    private static final int    MAX_RADIUS = 100;
    private static final double MIN_CONTRAST_THRESHOLD = 0;
    private static final double MAX_CONTRAST_THRESHOLD = 300;

    private BernsenThresholdTabParams params;

    public BernsenThresholdTab() {
        super(true);
    }

    @Override
    public Component makeTab(BernsenThresholdTabParams params) {
        if (params == null)
            params = new BernsenThresholdTabParams(15, 15);
        this.params = params;

        return makeTab();
    }

    @Override
    public String getTitle() { return TAB_TITLE; }
    @Override
    public String getName() { return TAB_NAME; }

    @Override
    protected void applyCatalanoFilter() {
        new BernsenThreshold(params.radius, params.contrastThreshold)
            .applyInPlace(imageFBmp);
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderIntModel    modelRadius            = new SliderIntModel   (params.radius           , 0, MIN_RADIUS            , MAX_RADIUS);
        SliderDoubleModel modelContrastThreshold = new SliderDoubleModel(params.contrastThreshold, 0, MIN_CONTRAST_THRESHOLD, MAX_CONTRAST_THRESHOLD);

        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));

        boxOptions.add(Box.createHorizontalGlue());
        boxOptions.add(makeSliderVert(modelRadius, "Radius", "Radius"));
        boxOptions.add(Box.createHorizontalStrut(8));
        boxOptions.add(makeSliderVert(modelContrastThreshold, "Contrast Threshold", "Contrast Threshold"));
        boxOptions.add(Box.createHorizontalGlue());

        box4Options.add(boxOptions);

        modelRadius.getWrapped().addChangeListener(ev -> {
            logger.trace("modelRadius: value={}", modelRadius.getFormatedText());
            params.radius = modelRadius.getValue();
            resetImage();
        });
        modelContrastThreshold.getWrapped().addChangeListener(ev -> {
            logger.trace("modelContrastThreshold: value={}", modelContrastThreshold.getFormatedText());
            params.contrastThreshold = modelContrastThreshold.getValue();
            resetImage();
        });

        return box4Options;
    }

    @Override
    public BernsenThresholdTabParams getParams() {
        return params;
    }

}
