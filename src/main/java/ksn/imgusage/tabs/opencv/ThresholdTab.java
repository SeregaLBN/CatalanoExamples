package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.type.dto.opencv.ThresholdTabParams;
import ksn.imgusage.type.opencv.CvThresholdTypes;
import ksn.imgusage.utils.OpenCvHelper;
import ksn.imgusage.utils.UiHelper;

/** <a href='https://docs.opencv.org/3.4.2/d7/d1b/group__imgproc__misc.html#gae8a4a146d1ca78c626a53577199e9c57'>Applies a fixed-level threshold to each array element</a> */
public class ThresholdTab extends OpencvFilterTab<ThresholdTabParams> {

    public static final String TAB_TITLE = "Threshold";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Applies a fixed-level threshold to each array element";

    private static final double MIN_THRESH =   0;
    private static final double MAX_THRESH = 999;
    private static final double MIN_MAXVAL =   0;
    private static final double MAX_MAXVAL = 500;

    private ThresholdTabParams params;

    @Override
    public Component makeTab(ThresholdTabParams params) {
        if (params == null)
            params = new ThresholdTabParams();
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
        if (params.useOtsuMask || params.useTriangleMask) {
            // Note
            // Currently, the Otsu's and Triangle methods are implemented only for 8-bit single-channel images.
            imageMat = OpenCvHelper.toGray(imageMat);
        }
        Mat dst = new Mat();
        Imgproc.threshold(
            imageMat, // src
            dst,
            params.thresh,
            params.maxVal,
            params.getThreshType().getVal(params.useOtsuMask, params.useTriangleMask));
        imageMat = dst;
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderDoubleModel modelThresh = new SliderDoubleModel(params.thresh, 0, MIN_THRESH, MAX_THRESH);
        SliderDoubleModel modelMaxVal = new SliderDoubleModel(params.maxVal, 0, MIN_MAXVAL, MAX_MAXVAL);

        Container cntrlThreshSlider = makeSliderVert(modelThresh, "Thresh", "Threshold value");
        Container cntrlMaxvalSlider = makeSliderVert(modelMaxVal, "MaxVal", "Maximum value to use with the THRESH_BINARY and THRESH_BINARY_INV thresholding types");

        Runnable applyThresholdingType = () -> {
            // maximum value to use with the THRESH_BINARY and THRESH_BINARY_INV thresholding types.
            boolean enabled1 = (params.getThreshType() == CvThresholdTypes.THRESH_BINARY) ||
                               (params.getThreshType() == CvThresholdTypes.THRESH_BINARY_INV);

            // Also, the special values THRESH_OTSU or THRESH_TRIANGLE may be combined with one of the above values.
            // In these cases, the function determines the optimal threshold value using the Otsu's or Triangle algorithm and uses it instead of the specified thresh.
            boolean enabled2 = !params.useOtsuMask && !params.useTriangleMask;

            UiHelper.enableAllChilds(cntrlMaxvalSlider, enabled1 && enabled2);
            UiHelper.enableAllChilds(cntrlThreshSlider, enabled2);
        };


        Box box4Types = makeBoxedRadioButtons(
            CvThresholdTypes.getThresholds(),
            params::getThreshType,
            params::setThreshType,
            "Thresholding type",
            "params.threshType",
            "Thresholding types",
            null,
            v -> "Type of the threshold operation",
            v -> applyThresholdingType.run());
        Box box4TypesCheckBoxes = Box.createVerticalBox();
        {
            box4TypesCheckBoxes.setBorder(BorderFactory.createTitledBorder("Special values"));

            JCheckBox[] checkBoxTriangleMask = { null };
            JCheckBox checkBoxOtsuMask = makeCheckBox(
                () -> params.useOtsuMask,
                v  -> params.useOtsuMask = v,
                CvThresholdTypes.THRESH_OTSU.name(),
                "params.useOtsuMask",
                "The function determines the optimal threshold value using the Otsu's algorithm and uses it instead of the specified thresh",
                () -> {
                    if (params.useOtsuMask)
                        checkBoxTriangleMask[0].setSelected(false);
                    applyThresholdingType.run();
                });
            checkBoxTriangleMask[0] = makeCheckBox(
                () -> params.useTriangleMask,
                v  -> params.useTriangleMask = v,
                CvThresholdTypes.THRESH_TRIANGLE.name(),
                "params.useTriangleMask",
                "The function determines the optimal threshold value using the Triangle algorithm and uses it instead of the specified thresh",
                () -> {
                    if (params.useTriangleMask)
                        checkBoxOtsuMask.setSelected(false);
                    applyThresholdingType.run();
                });

            box4TypesCheckBoxes.add(checkBoxOtsuMask);
            box4TypesCheckBoxes.add(checkBoxTriangleMask[0]);
        }

        box4Types.add(Box.createVerticalStrut(2));
        box4Types.add(box4TypesCheckBoxes);

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(cntrlThreshSlider);
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(cntrlMaxvalSlider);
        box4Sliders.add(Box.createHorizontalGlue());

        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new BorderLayout());
        panelOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
        panelOptions.add(box4Sliders, BorderLayout.CENTER);
        panelOptions.add(box4Types  , BorderLayout.SOUTH);

        box4Options.add(panelOptions);

        applyThresholdingType.run();

        // Note
        // Currently, the Otsu's and Triangle methods are implemented only for 8-bit single-channel images.

        addChangeListener("modelThresh", modelThresh, v -> params.thresh = v);
        addChangeListener("modelMaxVal", modelMaxVal, v -> params.maxVal = v);

        return box4Options;
    }

    @Override
    public ThresholdTabParams getParams() {
        return params;
    }

}
