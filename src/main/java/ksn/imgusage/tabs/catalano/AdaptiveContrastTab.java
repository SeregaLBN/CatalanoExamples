package ksn.imgusage.tabs.catalano;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.AdaptiveContrastEnhancement;
import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.catalano.AdaptiveContrastTabParams;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/AdaptiveContrastEnhancement.java'>Adaptive Contrast Enhancement is modification of the gray level values based on some criterion that adjusts its parameters as local image characteristics change</a> */
public class AdaptiveContrastTab extends CatalanoFilterTab<AdaptiveContrastTabParams> {

    public static final String TAB_TITLE = AdaptiveContrastEnhancement.class.getSimpleName();
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Adaptive Contrast Enhancement is modification of the gray level values based on some criterion that adjusts its parameters as local image characteristics change";

    private static final int    MIN_WINDOW_SIZE =   1; // Size of window (should be an odd number).
    private static final int    MAX_WINDOW_SIZE = 201;
    private static final double MIN_K1          =   0; // Local gain factor, between 0 and 1.
    private static final double MAX_K1          =   1;
    private static final double MIN_K2          =   0; // Local mean constant, between 0 and 1.
    private static final double MAX_K2          =   1;
    private static final double MIN_GAIN        =   0; // The minimum gain factor
    private static final double MAX_GAIN        =  20; // The maximum gain factor

    private AdaptiveContrastTabParams params;

    public AdaptiveContrastTab() {
        super(true);
    }

    @Override
    public Component makeTab(AdaptiveContrastTabParams params) {
        if (params == null)
           params = new AdaptiveContrastTabParams();
        this.params = params;

        return makeTab();
    }

    @Override
    public String getTitle() { return TAB_TITLE; }
    @Override
    public String getName() { return TAB_NAME; }
    @Override
    public String getDescription() { return TAB_DESCRIPTION; }

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
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderIntModel    modelWinSize = new SliderIntModel   (params.winSize, 0, MIN_WINDOW_SIZE, MAX_WINDOW_SIZE);
        SliderDoubleModel modelK1      = new SliderDoubleModel(params.k1     , 0, MIN_K1         , MAX_K1);
        SliderDoubleModel modelK2      = new SliderDoubleModel(params.k2     , 0, MIN_K2         , MAX_K2);
        SliderDoubleModel modelMinGain = new SliderDoubleModel(params.minGain, 0, MIN_GAIN       , MAX_GAIN);
        SliderDoubleModel modelMaxGain = new SliderDoubleModel(params.maxGain, 0, MIN_GAIN       , MAX_GAIN);

        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));

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

        addChangeListener("modelWinSize", modelWinSize, v -> params.winSize = v);
        addChangeListener("modelK1"     , modelK1     , v -> params.k1      = v);
        addChangeListener("modelK2"     , modelK2     , v -> params.k2      = v);
        addChangeListener("modelMinGain", modelMinGain, v -> params.minGain = v, () -> {
            if (params.minGain > modelMaxGain.getValue())
                modelMaxGain.setValue(params.minGain);
        });
        addChangeListener("modelMaxGain", modelMaxGain, v -> params.maxGain = v, () -> {
            if (params.maxGain < modelMinGain.getValue())
                modelMinGain.setValue(params.maxGain);
        });

        return box4Options;
    }

    @Override
    public AdaptiveContrastTabParams getParams() {
        return params;
    }

}
