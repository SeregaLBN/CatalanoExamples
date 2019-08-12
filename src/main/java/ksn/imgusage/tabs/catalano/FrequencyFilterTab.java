package ksn.imgusage.tabs.catalano;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.FourierTransform;
import Catalano.Imaging.Filters.FrequencyFilter;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/FrequencyFilter.java'>Filtering of frequencies outside of specified range in complex Fourier transformed image</a> */
public class FrequencyFilterTab extends CatalanoFilterTab {

    public static final String TAB_NAME = FrequencyFilter.class.getSimpleName();
    public static final String TAB_DESCRIPTION = "Filtering of frequencies outside of specified range in complex Fourier transformed image";

    private static final int MIN = 0;
    private static final int MAX = 1024;

    private final SliderIntModel modelMin;
    private final SliderIntModel modelMax;

    public FrequencyFilterTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, 0, 100);
    }

    public FrequencyFilterTab(ITabHandler tabHandler, ITab source, int min, int max) {
        super(tabHandler, source, true);
        this.modelMin = new SliderIntModel(min, 0, MIN, MAX);
        this.modelMax = new SliderIntModel(max, 0, MIN, MAX);

        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyCatalanoFilter() {
        FourierTransform fourierTransform = new FourierTransform(imageFBmp);
        fourierTransform.Forward();

        FrequencyFilter frequencyFilter = new FrequencyFilter(modelMin.getValue(), modelMax.getValue());
        frequencyFilter.ApplyInPlace(fourierTransform);

        fourierTransform.Backward();
        imageFBmp = fourierTransform.toFastBitmap();
    }

    @Override
    protected void makeOptions(Box box4Options) {
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
            int valMin = modelMin.getValue();
            if (valMin > modelMax.getValue())
                modelMax.setValue(valMin);
            resetImage();
        });
        modelMax.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMax: value={}", modelMax.getFormatedText());
            int valMax = modelMax.getValue();
            if (valMax < modelMin.getValue())
                modelMin.setValue(valMax);
            resetImage();
        });
    }

    @Override
    public void printParams() {
        logger.info("min={}, max={}",
            modelMin.getFormatedText(),
            modelMax.getFormatedText());
    }

}
