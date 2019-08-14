package ksn.imgusage.tabs.catalano;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.FourierTransform;
import Catalano.Imaging.Filters.FrequencyFilter;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.ITabParams;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/FrequencyFilter.java'>Filtering of frequencies outside of specified range in complex Fourier transformed image</a> */
public class FrequencyFilterTab extends CatalanoFilterTab<FrequencyFilterTab.Params> {

    public static final String TAB_NAME = FrequencyFilter.class.getSimpleName();
    public static final String TAB_DESCRIPTION = "Filtering of frequencies outside of specified range in complex Fourier transformed image";

    private static final int MIN = 0;
    private static final int MAX = 1024;

    public static class Params implements ITabParams {
        public int min, max;
        public Params(int min, int max) { this.min = min; this.max = max; }
        @Override
        public String toString() { return "{ min=" + min + ", max=" + max + " }"; }
    }

    private final Params params;

    public FrequencyFilterTab(ITabHandler tabHandler, ITab<?> source) {
        this(tabHandler, source, new Params(0, 100));
    }

    public FrequencyFilterTab(ITabHandler tabHandler, ITab<?> source, Params params) {
        super(tabHandler, source, true);
        this.params = params;

        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyCatalanoFilter() {
        FourierTransform fourierTransform = new FourierTransform(imageFBmp);
        fourierTransform.Forward();

        FrequencyFilter frequencyFilter = new FrequencyFilter(params.min, params.max);
        frequencyFilter.ApplyInPlace(fourierTransform);

        fourierTransform.Backward();
        imageFBmp = fourierTransform.toFastBitmap();
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderIntModel modelMin = new SliderIntModel(params.min, 0, MIN, MAX);
        SliderIntModel modelMax = new SliderIntModel(params.max, 0, MIN, MAX);

        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTabName() + " options"));

        boxOptions.add(Box.createHorizontalGlue());
        boxOptions.add(makeSliderVert(modelMin, "Min", "Minimum value for to keep"));
        boxOptions.add(Box.createHorizontalStrut(8));
        boxOptions.add(makeSliderVert(modelMax, "Max", "Maximum value for to keep"));
        boxOptions.add(Box.createHorizontalGlue());

        box4Options.add(boxOptions);

        modelMin.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMin: value={}", modelMin.getFormatedText());
            params.min = modelMin.getValue();
            if (params.min > modelMax.getValue())
                modelMax.setValue(params.min);
            resetImage();
        });
        modelMax.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMax: value={}", modelMax.getFormatedText());
            params.max = modelMax.getValue();
            if (params.max < modelMin.getValue())
                modelMin.setValue(params.max);
            resetImage();
        });

        return box4Options;
    }

    @Override
    public Params getParams() {
        return params;
    }

}
