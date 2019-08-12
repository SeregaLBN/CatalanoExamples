package ksn.imgusage.tabs.catalano;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.AdaptiveContrastEnhancement;
import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;

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

    private final SliderIntModel    modelWinSize;
    private final SliderDoubleModel modelK1;
    private final SliderDoubleModel modelK2;
    private final SliderDoubleModel modelMinGain;
    private final SliderDoubleModel modelMaxGain;

    public AdaptiveContrastTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, true, 20, 0.3, 0.6, 0.1, 1);
    }

    public AdaptiveContrastTab(ITabHandler tabHandler, ITab source, boolean boosting, int windowSize, double k1, double k2, double minGain, double maxGain) {
        super(tabHandler, source, boosting, true);
        this.modelWinSize = new SliderIntModel   (windowSize, 0, MIN_WINDOW_SIZE, MAX_WINDOW_SIZE);
        this.modelK1      = new SliderDoubleModel(k1        , 0, MIN_K1         , MAX_K1);
        this.modelK2      = new SliderDoubleModel(k2        , 0, MIN_K2         , MAX_K2);
        this.modelMinGain = new SliderDoubleModel(minGain   , 0, MIN_GAIN       , MAX_GAIN);
        this.modelMaxGain = new SliderDoubleModel(maxGain   , 0, MIN_GAIN       , MAX_GAIN);

        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyCatalanoFilter() {
        new AdaptiveContrastEnhancement(
            modelWinSize.getValue(),
            modelK1     .getValue(),
            modelK2     .getValue(),
            modelMinGain.getValue(),
            modelMaxGain.getValue()
        ).applyInPlace(imageFBmp);
    }

    @Override
    protected void makeOptions(Box box4Options) {
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
            resetImage();
        });
        modelK1.getWrapped().addChangeListener(ev -> {
            logger.trace("modelK1: value={}", modelK1.getFormatedText());
            resetImage();
        });
        modelK2.getWrapped().addChangeListener(ev -> {
            logger.trace("modelK2: value={}", modelK2.getFormatedText());
            resetImage();
        });
        modelMinGain.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMinGain: value={}", modelMinGain.getFormatedText());
            double valMinGain = modelMinGain.getValue();
            if (valMinGain > modelMaxGain.getValue())
                modelMaxGain.setValue(valMinGain);
            resetImage();
        });
        modelMaxGain.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMaxGain: value={}", modelMaxGain.getFormatedText());
            double valMaxGain = modelMaxGain.getValue();
            if (valMaxGain < modelMinGain.getValue())
                modelMinGain.setValue(valMaxGain);
            resetImage();
        });
    }

    @Override
    public void printParams() {
        logger.info("windowSize={}, k1={}, k2={}, minGain={}, maxGain={}",
            modelWinSize.getFormatedText(),
            modelK1     .getFormatedText(),
            modelK2     .getFormatedText(),
            modelMinGain.getFormatedText(),
            modelMaxGain.getFormatedText());
    }

}
