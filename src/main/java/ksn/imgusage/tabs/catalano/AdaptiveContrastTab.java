package ksn.imgusage.tabs.catalano;

import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.AdaptiveContrastEnhancement;
import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.ITabParams;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/AdaptiveContrastEnhancement.java'>Adaptive Contrast Enhancement is modification of the gray level values based on some criterion that adjusts its parameters as local image characteristics change</a> */
public class AdaptiveContrastTab extends CatalanoFilterTab {

    public static final String TAB_NAME = AdaptiveContrastEnhancement.class.getSimpleName();
    public static final String TAB_DESCRIPTION = "Adaptive Contrast Enhancement is modification of the gray level values based on some criterion that adjusts its parameters as local image characteristics change";

    private static final int    MIN_WINDOW_SIZE =   1; // Size of window (should be an odd number).
    private static final int    MAX_WINDOW_SIZE = 201;
    private static final double MIN_K1          =   0; // Local gain factor, between 0 and 1.
    private static final double MAX_K1          =   1;
    private static final double MIN_K2          =   0; // Local mean constant, between 0 and 1.
    private static final double MAX_K2          =   1;
    private static final double MIN_GAIN        =   0; // The minimum gain factor
    private static final double MAX_GAIN        =  20; // The maximum gain factor

    public static class Params implements ITabParams {
        public int    winSize;
        public double k1;
        public double k2;
        public double minGain;
        public double maxGain;

        public Params(
            int    winSize,
            double k1,
            double k2,
            double minGain,
            double maxGain)
        {
            this.winSize = winSize;
            this.k1      = k1;
            this.k2      = k2;
            this.minGain = minGain;
            this.maxGain = maxGain;
        }

        @Override
        public String toString() {
            return String.format(Locale.US,
                    "winSize=%d, k1=%.2f, k2=%.2f, minGain=%.2f, maxGain=%.2f",
                    winSize,
                    k1,
                    k1,
                    minGain,
                    maxGain);
        }
    }
    private final Params params;

    public AdaptiveContrastTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, new Params(20, 0.3, 0.6, 0.1, 1));
    }

    public AdaptiveContrastTab(ITabHandler tabHandler, ITab source, Params params) {
        super(tabHandler, source, true);
        this.params = params;

        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyCatalanoFilter() {
        new AdaptiveContrastEnhancement(
            params.winSize,
            params.k1,
            params.k2,
            params.minGain,
            params.maxGain
        ).applyInPlace(imageFBmp);
    }

    @Override
    protected void makeOptions(Box box4Options) {
        SliderIntModel    modelWinSize = new SliderIntModel   (params.winSize, 0, MIN_WINDOW_SIZE, MAX_WINDOW_SIZE);
        SliderDoubleModel modelK1      = new SliderDoubleModel(params.k1     , 0, MIN_K1         , MAX_K1);
        SliderDoubleModel modelK2      = new SliderDoubleModel(params.k2     , 0, MIN_K2         , MAX_K2);
        SliderDoubleModel modelMinGain = new SliderDoubleModel(params.minGain, 0, MIN_GAIN       , MAX_GAIN);
        SliderDoubleModel modelMaxGain = new SliderDoubleModel(params.maxGain, 0, MIN_GAIN       , MAX_GAIN);

        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTabName() + " options"));

        boxOptions.add(Box.createHorizontalGlue());
        boxOptions.add(makeSliderVert(modelWinSize, "WinSize", "Size of window"));
        boxOptions.add(Box.createHorizontalStrut(2));
        boxOptions.add(makeSliderVert(modelK1     , "K1"     , "Local gain factor"));
        boxOptions.add(Box.createHorizontalStrut(2));
        boxOptions.add(makeSliderVert(modelK2     , "K2"     , "Local mean constant"));
        boxOptions.add(Box.createHorizontalStrut(2));
        boxOptions.add(makeSliderVert(modelMinGain, "MinGain", "The minimum gain factor"));
        boxOptions.add(Box.createHorizontalStrut(2));
        boxOptions.add(makeSliderVert(modelMaxGain, "MaxGain", "The maximum gain factor"));
        boxOptions.add(Box.createHorizontalGlue());

        box4Options.add(boxOptions);

        modelWinSize.getWrapped().addChangeListener(ev -> {
            logger.trace("modelWinSize: value={}", modelWinSize.getFormatedText());
            params.winSize = modelWinSize.getValue();
            resetImage();
        });
        modelK1.getWrapped().addChangeListener(ev -> {
            logger.trace("modelK1: value={}", modelK1.getFormatedText());
            params.k1 = modelK1.getValue();
            resetImage();
        });
        modelK2.getWrapped().addChangeListener(ev -> {
            logger.trace("modelK2: value={}", modelK2.getFormatedText());
            params.k2 = modelK2.getValue();
            resetImage();
        });
        modelMinGain.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMinGain: value={}", modelMinGain.getFormatedText());
            params.minGain = modelMinGain.getValue();
            if (params.minGain > modelMaxGain.getValue())
                modelMaxGain.setValue(params.minGain);
            resetImage();
        });
        modelMaxGain.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMaxGain: value={}", modelMaxGain.getFormatedText());
            params.maxGain = modelMaxGain.getValue();
            if (params.maxGain < modelMinGain.getValue())
                modelMinGain.setValue(params.maxGain);
            resetImage();
        });
    }

    @Override
    public void printParams() {
        logger.info("params={{}}", params);
    }

}
