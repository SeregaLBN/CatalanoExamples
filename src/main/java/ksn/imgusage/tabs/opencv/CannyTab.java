package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;

import javax.swing.*;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.CannyTabParams;

/** <a href='https://docs.opencv.org/3.4.2/dd/d1a/group__imgproc__feature.html#ga04723e007ed888ddf11d9ba04e2232de'>Finds edges in an image using the Canny algorithm</a> */
public class CannyTab extends OpencvFilterTab<CannyTabParams> {

    public static final String TAB_TITLE = "Canny";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Finds edges in an image using the Canny algorithm";

    private static final double MIN_THRESHOLD     =   0;
    private static final double MAX_THRESHOLD     = 999;
    public  static final int    MIN_APERTURE_SIZE =   3;
    private static final int    MAX_APERTURE_SIZE =   7;

    private CannyTabParams params;

    @Override
    public Component makeTab(CannyTabParams params) {
        if (params == null)
            params = new CannyTabParams();
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
        // TODO
        // 8-bit input image

        Mat dst = new Mat();
        Imgproc.Canny(
            imageMat, // src
            dst,
            params.threshold1,
            params.threshold2,
            params.apertureSize,
            params.l2gradient);
        imageMat = dst;
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderDoubleModel modelThreshold1   = new SliderDoubleModel(params.threshold1, 0, MIN_THRESHOLD, MAX_THRESHOLD);
        SliderDoubleModel modelThreshold2   = new SliderDoubleModel(params.threshold2, 0, MIN_THRESHOLD, MAX_THRESHOLD);
        SliderIntModel    modelApertureSize = new    SliderIntModel(params.apertureSize, 0, MIN_APERTURE_SIZE, MAX_APERTURE_SIZE);

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(makeSliderVert(modelThreshold1, "Threshold1", "First threshold for the hysteresis procedure"));
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(makeSliderVert(modelThreshold2, "Threshold2", "Second threshold for the hysteresis procedure"));
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(makeSliderVert(modelApertureSize, "Aperture", "Aperture size for the Sobel operator"));
        box4Sliders.add(Box.createHorizontalGlue());

        Box box4L2gradient = Box.createVerticalBox();
        box4L2gradient.setBorder(BorderFactory.createTitledBorder(""));
        JCheckBox checkBoxL2gradient = new JCheckBox("L2 gradient", params.l2gradient);
        checkBoxL2gradient.setToolTipText("A flag, indicating whether a more accurate L2 norm =√‾((dI/dx)^2+(dI/dy)^2) should be used to calculate the image gradient magnitude ( L2gradient=true ), or whether the default L1 norm =|dI/dx|+|dI/dy| is enough ( L2gradient=false )");
        checkBoxL2gradient.addItemListener(ev -> {
            params.l2gradient = (ev.getStateChange() == ItemEvent.SELECTED);
            logger.trace("L2 gradient is {}", (params.l2gradient ? "checked" : "unchecked"));
            resetImage();
        });
        box4L2gradient.add(checkBoxL2gradient);

        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new BorderLayout());
        panelOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
        panelOptions.add(box4Sliders   , BorderLayout.CENTER);
        panelOptions.add(box4L2gradient, BorderLayout.SOUTH);

        box4Options.add(panelOptions);

        modelThreshold1.getWrapped().addChangeListener(ev -> {
            logger.trace("modelThreshold1: value={}", modelThreshold1.getFormatedText());
            params.threshold1 = modelThreshold1.getValue();
            resetImage();
        });
        modelThreshold2.getWrapped().addChangeListener(ev -> {
            logger.trace("modelThreshold2: value={}", modelThreshold2.getFormatedText());
            params.threshold2 = modelThreshold2.getValue();
            resetImage();
        });

        modelApertureSize.getWrapped().addChangeListener(ev -> {
            logger.trace("modelApertureSize: value={}", modelApertureSize.getFormatedText());
            int val = modelApertureSize.getValue();
            int valValid = CannyTabParams.onlyOdd(val, params.apertureSize);
            if (val == valValid) {
                params.apertureSize = valValid;
                resetImage();
            } else {
                SwingUtilities.invokeLater(() -> modelApertureSize.setValue(valValid));
            }
        });

        return box4Options;
    }

    @Override
    public CannyTabParams getParams() {
        return params;
    }

}
