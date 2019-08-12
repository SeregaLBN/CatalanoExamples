package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.util.stream.Stream;

import javax.swing.*;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.opencv.type.CvThresholdTypes;

/** <a href='https://docs.opencv.org/3.4.2/d7/d1b/group__imgproc__misc.html#gae8a4a146d1ca78c626a53577199e9c57'>Applies a fixed-level threshold to each array element</a> */
public class ThresholdTab extends OpencvFilterTab {

    public static final String TAB_NAME = "Threshold";
    public static final String TAB_DESCRIPTION = "Applies a fixed-level threshold to each array element";

    private static final double MIN_THRESH =   0;
    private static final double MAX_THRESH = 999;
    private static final double MIN_MAXVAL =   0;
    private static final double MAX_MAXVAL = 500;

    private final SliderDoubleModel modelThresh;
    private final SliderDoubleModel modelMaxVal;
    private       CvThresholdTypes  threshType;
    private       boolean           useOtsuMask;
    private       boolean           useTriangleMask;

    public ThresholdTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, null, 100, 250, CvThresholdTypes.THRESH_BINARY, false, false);
    }

    public ThresholdTab(ITabHandler tabHandler, ITab source, Boolean boosting, double thresh, double maxval, CvThresholdTypes threshType, boolean useOtsuMask, boolean useTriangleMask) {
        super(tabHandler, source, boosting);
        this.modelThresh = new SliderDoubleModel(thresh, 0, MIN_THRESH, MAX_THRESH);
        this.modelMaxVal = new SliderDoubleModel(maxval, 0, MIN_MAXVAL, MAX_MAXVAL);
        switch (threshType) {
            case THRESH_BINARY    :
            case THRESH_BINARY_INV:
            case THRESH_TRUNC     :
            case THRESH_TOZERO    :
            case THRESH_TOZERO_INV:
                this.threshType = threshType;
                break;

            case THRESH_OTSU:
            case THRESH_TRIANGLE:
            case THRESH_MASK:
            default:
                throw new IllegalArgumentException("Unsupported threshType=" + threshType);
        }
        this.useOtsuMask     = useOtsuMask;
        this.useTriangleMask = useTriangleMask;

        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyOpencvFilter() {
        Mat dst = new Mat();
        Imgproc.threshold(
            imageMat, // src
            dst,
            modelThresh.getValue(),
            modelMaxVal.getValue(),
            threshType.getVal(useOtsuMask, useTriangleMask));
        imageMat = dst;
    }

    @Override
    protected void makeOptions(Box box4Options) {
        Container cntrlThreshSlider = makeSliderVert(modelThresh, "Thresh", "Threshold value");
        Container cntrlMaxvalSlider = makeSliderVert(modelMaxVal, "MaxVal", "Maximum value to use with the THRESH_BINARY and THRESH_BINARY_INV thresholding types");

        Runnable applyThresholdingType = () -> {
            // maximum value to use with the THRESH_BINARY and THRESH_BINARY_INV thresholding types.
            boolean enabled1 = (this.threshType == CvThresholdTypes.THRESH_BINARY) ||
                               (this.threshType == CvThresholdTypes.THRESH_BINARY_INV);

            // Also, the special values THRESH_OTSU or THRESH_TRIANGLE may be combined with one of the above values.
            // In these cases, the function determines the optimal threshold value using the Otsu's or Triangle algorithm and uses it instead of the specified thresh.
            boolean enabled2 = !this.useOtsuMask && !this.useTriangleMask;

            for (Component c : cntrlMaxvalSlider.getComponents())
                c.setEnabled(enabled1 && enabled2);
            for (Component c : cntrlThreshSlider.getComponents())
                c.setEnabled(enabled2);
        };


        Box box4Types = Box.createVerticalBox();
        box4Types.setBorder(BorderFactory.createTitledBorder("Thresholding type"));
        Box box4TypesRadioBttns = Box.createVerticalBox();
        Box box4TypesCheckBoxes = Box.createVerticalBox();
        {
            box4TypesRadioBttns.setToolTipText("Thresholding types");
            ButtonGroup radioGroup1 = new ButtonGroup();
            Stream.of(CvThresholdTypes.values())
                .filter(b -> b.getVal() < CvThresholdTypes.THRESH_MASK.getVal())
                .forEach(thresholdingType ->
            {
                JRadioButton radioBtnThresh = new JRadioButton(thresholdingType.name(), thresholdingType == this.threshType);
                radioBtnThresh.setToolTipText("Type of the threshold operation");
                radioBtnThresh.addItemListener(ev -> {
                    if (ev.getStateChange() == ItemEvent.SELECTED) {
                        this.threshType = thresholdingType;
                        logger.trace("Thresholding type changed to {}", thresholdingType);
                        applyThresholdingType.run();
                        resetImage();
                    }
                });
                box4TypesRadioBttns.add(radioBtnThresh);
                radioGroup1.add(radioBtnThresh);
            });
        }
        {
            box4TypesCheckBoxes.setBorder(BorderFactory.createTitledBorder("Special values"));

            JCheckBox checkBoxOtsuMask = new JCheckBox(CvThresholdTypes.THRESH_OTSU.name(), this.useOtsuMask);
            checkBoxOtsuMask.setToolTipText("the function determines the optimal threshold value using the Otsu's algorithm and uses it instead of the specified thresh");

            JCheckBox checkBoxTriangleMask = new JCheckBox(CvThresholdTypes.THRESH_TRIANGLE.name(), this.useTriangleMask);
            checkBoxTriangleMask.setToolTipText("the function determines the optimal threshold value using the Triangle algorithm and uses it instead of the specified thresh");

            checkBoxOtsuMask.addItemListener(ev -> {
                useOtsuMask = (ev.getStateChange() == ItemEvent.SELECTED);
                logger.trace("Thresholding type THRESH_OTSU is {}", (useOtsuMask ? "checked" : "unchecked"));
                if (useOtsuMask)
                    checkBoxTriangleMask.setSelected(false);
                applyThresholdingType.run();
                resetImage();
            });
            checkBoxTriangleMask.addItemListener(ev -> {
                useTriangleMask = (ev.getStateChange() == ItemEvent.SELECTED);
                logger.trace("Thresholding type THRESH_TRIANGLE is {}", (useTriangleMask ? "checked" : "unchecked"));
                if (useTriangleMask)
                    checkBoxOtsuMask.setSelected(false);
                applyThresholdingType.run();
                resetImage();
            });

            box4TypesCheckBoxes.add(checkBoxOtsuMask);
            box4TypesCheckBoxes.add(checkBoxTriangleMask);
        }

        box4Types.add(box4TypesRadioBttns);
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
        panelOptions.setBorder(BorderFactory.createTitledBorder(getTabName() + " options"));
        panelOptions.add(box4Sliders, BorderLayout.CENTER);
        panelOptions.add(box4Types  , BorderLayout.SOUTH);

        box4Options.add(panelOptions);

        applyThresholdingType.run();

        // Note
        // Currently, the Otsu's and Triangle methods are implemented only for 8-bit single-channel images.

        modelThresh.getWrapped().addChangeListener(ev -> {
            logger.trace("modelThresh: value={}", modelThresh.getFormatedText());
            resetImage();
        });
        modelMaxVal.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMaxVal: value={}", modelMaxVal.getFormatedText());
            resetImage();
        });
    }

    @Override
    public void printParams() {
        logger.info("thresh={}, maxval={}, type={}{}{}",
                modelThresh.getFormatedText(),
                modelMaxVal.getFormatedText(),
                threshType,
                useOtsuMask     ? " | THRESH_OTSU"     : "",
                useTriangleMask ? " | THRESH_TRIANGLE" : "");
    }

}
