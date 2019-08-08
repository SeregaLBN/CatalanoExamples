package ksn.imgusage.tabs.catalano;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.FourierTransform;
import Catalano.Imaging.Filters.FrequencyFilter;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/FrequencyFilter.java'>Filtering of frequencies outside of specified range in complex Fourier transformed image</a> */
public class FrequencyFilterTab extends CatalanoFilterTab {

    private static final int MIN = 0;
    private static final int MAX = 1024;

    private final SliderIntModel modelMin;
    private final SliderIntModel modelMax;

    public FrequencyFilterTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, true, 0, 100);
    }

    public FrequencyFilterTab(ITabHandler tabHandler, ITab source, boolean boosting, int min, int max) {
        super(tabHandler, source, boosting);
        this.modelMin = new SliderIntModel(min, 0, MIN, MAX);
        this.modelMax = new SliderIntModel(max, 0, MIN, MAX);

        makeTab();
    }

    @Override
    public String getTabName() { return FrequencyFilter.class.getSimpleName(); }

    @Override
    protected void applyFilter() {
        FastBitmap bmp = new FastBitmap(source.getImage());
        if (boosting)
            bmp = boostImage(bmp, logger);
        if (!bmp.isGrayscale())
            bmp.toGrayscale();

        FourierTransform fourierTransform = new FourierTransform(bmp);
        fourierTransform.Forward();

        FrequencyFilter frequencyFilter = new FrequencyFilter(modelMin.getValue(), modelMax.getValue());
        frequencyFilter.ApplyInPlace(fourierTransform);

        fourierTransform.Backward();
        bmp = fourierTransform.toFastBitmap();

        image = bmp.toBufferedImage();
    }

    @Override
    protected void makeOptions(JPanel imagePanel, Box boxCenterLeft) {
        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder("Frequency filter"));

        boxOptions.add(Box.createHorizontalGlue());
        boxOptions.add(makeSliderVert(modelMin, "Min", "Minimum value for to keep"));
        boxOptions.add(Box.createHorizontalStrut(8));
        boxOptions.add(makeSliderVert(modelMax, "Max", "Maximum value for to keep"));
        boxOptions.add(Box.createHorizontalGlue());

        boxCenterLeft.add(boxOptions);

        modelMin.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMin: value={}", modelMin.getFormatedText());
            int valMin = modelMin.getValue();
            if (valMin > modelMax.getValue())
                modelMax.setValue(valMin);
            debounceResetImage();
        });
        modelMax.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMax: value={}", modelMax.getFormatedText());
            int valMax = modelMax.getValue();
            if (valMax < modelMin.getValue())
                modelMin.setValue(valMax);
            debounceResetImage();
        });
    }

    @Override
    public void printParams() {
        logger.info("min={}, max={}",
            modelMin.getFormatedText(),
            modelMax.getFormatedText());
    }

}
