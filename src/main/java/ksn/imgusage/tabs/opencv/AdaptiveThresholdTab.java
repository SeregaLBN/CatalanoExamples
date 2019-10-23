package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.AdaptiveThresholdTabParams;
import ksn.imgusage.type.dto.opencv.CannyTabParams;
import ksn.imgusage.type.opencv.CvAdaptiveThresholdTypes;
import ksn.imgusage.type.opencv.CvThresholdTypes;
import ksn.imgusage.utils.OpenCvHelper;

/** <a href='https://docs.opencv.org/3.4.2/d7/d1b/group__imgproc__misc.html#ga72b913f352e4a1b1b397736707afcde3'>Applies an adaptive threshold to an array</a> */
public class AdaptiveThresholdTab extends OpencvFilterTab<AdaptiveThresholdTabParams> {

    public static final String TAB_TITLE = "AdaptiveThreshold";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Applies an adaptive threshold to an array";

    private static final int    MIN_BLOCK_SIZE =  3;
    private static final int    MAX_BLOCK_SIZE = 99;
    private static final double MIN_MAXVAL =   0;
    private static final double MAX_MAXVAL = 500;
    private static final double MIN_C = -999;
    private static final double MAX_C = +999;

    private AdaptiveThresholdTabParams params;

    @Override
    public Component makeTab(AdaptiveThresholdTabParams params) {
        if (params == null)
            params = new AdaptiveThresholdTabParams();
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
    protected void applyOpencvFilter() {
        imageMat = OpenCvHelper.toGray(imageMat);

        Mat dst = new Mat();
        Imgproc.adaptiveThreshold(
            imageMat, //src
            dst,
            params.maxVal,
            params.adaptiveMethod.getVal(),
            params.getThreshType().getVal(),
            params.blockSize,
            params.c
        );
        imageMat = dst;
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderDoubleModel modelMaxVal    = new SliderDoubleModel(params.maxVal   , 0, MIN_MAXVAL    , MAX_MAXVAL);
        SliderIntModel    modelBlockSize = new SliderIntModel(   params.blockSize, 0, MIN_BLOCK_SIZE, MAX_BLOCK_SIZE);
        SliderDoubleModel modelC         = new SliderDoubleModel(params.c        , 0, MIN_C         , MAX_C);

        Container cntrlMaxvalSlider    = makeSliderVert(modelMaxVal   , "MaxVal"   , "Non-zero value assigned to the pixels for which the condition is satisfied");
        Container cntrlBlockSizeSlider = makeSliderVert(modelBlockSize, "BlockSize", "Size of a pixel neighborhood that is used to calculate a threshold value for the pixel: 3, 5, 7, and so on");
        Container cntrlCSlider         = makeSliderVert(modelC        , "C"        , "Constant subtracted from the mean or weighted mean (see the details below). Normally, it is positive but may be zero or negative as well");

        Box box4Adaptive = makeBoxedRadioButtons(
                Stream.of(CvAdaptiveThresholdTypes.values()),
                ()  -> params.adaptiveMethod,
                val -> params.adaptiveMethod = val,
                "Adaptive thresholding algorithm",
                "params.adaptiveMethod",
                "Adaptive thresholding algorithm",
                null,
                v -> (v == CvAdaptiveThresholdTypes.ADAPTIVE_THRESH_MEAN_C)
                    ? "the threshold value T(x,y) is a mean of the blockSize×blockSize neighborhood of (x,y) minus C"
                    : "the threshold value T(x,y) is a weighted sum (cross-correlation with a Gaussian window) of the blockSize×blockSize neighborhood of (x,y) minus C . The default sigma (standard deviation) is used for the specified blockSize",
                null);
        Box box4ThreshTypes = makeBoxedRadioButtons(
            Stream.of(CvThresholdTypes.THRESH_BINARY, CvThresholdTypes.THRESH_BINARY_INV),
            params::getThreshType,
            params::setThreshType,
            "Thresholding type",
            "params.threshType",
            "Thresholding types",
            null,
            v -> "Type of the threshold operation",
            null);

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(cntrlMaxvalSlider);
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(cntrlBlockSizeSlider);
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(cntrlCSlider);
        box4Sliders.add(Box.createHorizontalGlue());

        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new BorderLayout());
        panelOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
        panelOptions.add(box4Adaptive   , BorderLayout.NORTH);
        panelOptions.add(box4Sliders    , BorderLayout.CENTER);
        panelOptions.add(box4ThreshTypes, BorderLayout.SOUTH);

        box4Options.add(panelOptions);


        addChangeListener("modelMaxVal"   , modelMaxVal   , v -> params.maxVal    = v);
        addChangeListener("modelBlockSize", modelBlockSize, v -> params.blockSize = v);
        addChangeListener("modelC"        , modelC        , v -> params.c         = v);

        modelBlockSize.getWrapped().addChangeListener(ev -> {
            logger.trace("modelBlockSize: value={}", modelBlockSize.getFormatedText());
            int val = modelBlockSize.getValue();
            int valValid = CannyTabParams.onlyOdd(val, params.blockSize);
            if (val == valValid) {
                params.blockSize = valValid;
                resetImage();
            } else {
                SwingUtilities.invokeLater(() -> modelBlockSize.setValue(valValid));
            }
        });

        return box4Options;
    }

    @Override
    public AdaptiveThresholdTabParams getParams() {
        return params;
    }

}
