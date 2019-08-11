package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.opencv.type.CvArrayType;
import ksn.imgusage.tabs.opencv.type.CvContourApproximationModes;
import ksn.imgusage.tabs.opencv.type.CvRetrievalModes;
import ksn.imgusage.utils.OpenCvHelper;

/** <a href='https://docs.opencv.org/3.4.2/d3/dc0/group__imgproc__shape.html#ga17ed9f5d79ae97bd4c7cf18403e1689a'>Finds contours in a binary image</a> */
public class FindContoursTab extends OpencvFilterTab {

    public static final String TAB_NAME = "FindContours";
    public static final String TAB_DESCRIPTION = "Finds contours in a binary image";

    private static final int MIN_OFFSET =    0;
    private static final int MAX_OFFSET = 1000;
    private static final int MIN_MIN_LIMIT_COUNTOUR_SIZE =    0;
    private static final int MAX_MIN_LIMIT_COUNTOUR_SIZE = 1000;

    private CvRetrievalModes            mode;
    private CvContourApproximationModes method;
    private final SliderIntModel        modelOffsetX;
    private final SliderIntModel        modelOffsetY;
    private boolean showHierarhy;
    private final SliderIntModel        modelMinLimitContoursW;
    private final SliderIntModel        modelMinLimitContoursH;

    public FindContoursTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, null,
             CvRetrievalModes.RETR_EXTERNAL, CvContourApproximationModes.CHAIN_APPROX_SIMPLE, new Point(0, 0),
             false, new Size(10, 10));
    }

    public FindContoursTab(ITabHandler tabHandler, ITab source, Boolean boosting,
            CvRetrievalModes mode, CvContourApproximationModes method, Point offset,
            boolean showHierarhy, Size minLimitContours
    ) {
        super(tabHandler, source, boosting);
        this.mode   = mode;
        this.method = method;
        this.modelOffsetX = new SliderIntModel((int)offset.x, 0, MIN_OFFSET, MAX_OFFSET);
        this.modelOffsetY = new SliderIntModel((int)offset.y, 0, MIN_OFFSET, MAX_OFFSET);
        this.showHierarhy = showHierarhy;
        this.modelMinLimitContoursW = new SliderIntModel((int)minLimitContours.width , 0, MIN_MIN_LIMIT_COUNTOUR_SIZE, MAX_MIN_LIMIT_COUNTOUR_SIZE);
        this.modelMinLimitContoursH = new SliderIntModel((int)minLimitContours.height, 0, MIN_MIN_LIMIT_COUNTOUR_SIZE, MAX_MIN_LIMIT_COUNTOUR_SIZE);

        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyOpencvFilter() {
//        this.modelMinLimitContoursW.setMaximum(imageMat.width());
//        this.modelMinLimitContoursH.setMaximum(imageMat.height());

        // only 8UC1
        if (imageMat.type() != CvArrayType.CV_8UC1.getVal())
            imageMat = OpenCvHelper.toGray(imageMat);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(
            imageMat, // src
            contours, // out
            hierarchy, // dst
            mode.getVal(),
            method.getVal(),
            new Point(modelOffsetX.getValue(),
                      modelOffsetY.getValue()));
        if (showHierarhy)
            imageMat = hierarchy;

        { // to color image
            Mat mat = new Mat();
            Imgproc.cvtColor(imageMat, mat, Imgproc.COLOR_GRAY2RGB);
            imageMat = mat;
        }

        contours.stream()
            .map(Imgproc::boundingRect)
            .filter(rc -> (rc.width > modelMinLimitContoursW.getValue()) && (rc.height > modelMinLimitContoursH.getValue()))
            .forEach(rc -> Imgproc.rectangle(imageMat,
                                             new Point(rc.x, rc.y),
                                             new Point(rc.x + rc.width, rc.y + rc.height),
                                             new Scalar(0, 0, 255), 2));
    }

    @Override
    protected void makeOptions(JPanel imagePanel, Box boxCenterLeft) {
        JPanel panelOptions;
        {
            Box boxMode = Box.createHorizontalBox();
            {
                boxMode.setBorder(BorderFactory.createTitledBorder("Contour retrieval mode"));
                JComboBox<CvRetrievalModes> comboBoxMode = new JComboBox<>(CvRetrievalModes.values());
                comboBoxMode.setSelectedItem(this.mode);
              //comboBoxMode.setAlignmentX(Component.LEFT_ALIGNMENT);
                comboBoxMode.setToolTipText("Contour retrieval mode");
                comboBoxMode.addActionListener(ev -> {
                    this.mode = (CvRetrievalModes)comboBoxMode.getSelectedItem();
                    logger.trace("Mode changed to {}", this.mode);
                    resetImage();
                });
                boxMode.add(comboBoxMode);
            }
            Box boxMethod = Box.createHorizontalBox();
            {
                boxMethod.setBorder(BorderFactory.createTitledBorder("Contour approximation method"));
                JComboBox<CvContourApproximationModes> comboBoxMeethod = new JComboBox<>(CvContourApproximationModes.values());
                comboBoxMeethod.setSelectedItem(this.method);
              //comboBoxMode.setAlignmentX(Component.LEFT_ALIGNMENT);
                comboBoxMeethod.setToolTipText("Contour approximation method");
                comboBoxMeethod.addActionListener(ev -> {
                    this.method = (CvContourApproximationModes)comboBoxMeethod.getSelectedItem();
                    logger.trace("Method changed to {}", this.method);
                    resetImage();
                });
                boxMethod.add(comboBoxMeethod);
            }
            Box boxTop = Box.createVerticalBox();
            boxTop.add(boxMode);
            boxTop.add(Box.createVerticalStrut(2));
            boxTop.add(boxMethod);

            Box boxOffsetSliders = Box.createHorizontalBox();
            boxOffsetSliders.setBorder(BorderFactory.createTitledBorder("Offset"));
            boxOffsetSliders.setToolTipText("Optional offset by which every contour point is shifted. This is useful if the contours are extracted from the image ROI and then they should be analyzed in the whole image context");
            boxOffsetSliders.add(Box.createHorizontalGlue());
            boxOffsetSliders.add(makeSliderVert(modelOffsetX, "X", "offset.X"));
            boxOffsetSliders.add(Box.createHorizontalStrut(2));
            boxOffsetSliders.add(makeSliderVert(modelOffsetY, "Y", "offset.Y"));
            boxOffsetSliders.add(Box.createHorizontalGlue());

            panelOptions = new JPanel();
            panelOptions.setLayout(new BorderLayout());
            panelOptions.setBorder(BorderFactory.createTitledBorder(getTabName() + " options"));
            panelOptions.add(boxTop     , BorderLayout.NORTH);
            panelOptions.add(boxOffsetSliders, BorderLayout.CENTER);
        }

        JPanel panelCustom;
        {
            JCheckBox checkBoxShowHierarhy = new JCheckBox("showHierarhy", this.showHierarhy);
            checkBoxShowHierarhy.addItemListener(ev -> {
                this.showHierarhy = (ev.getStateChange() == ItemEvent.SELECTED);
                logger.trace("showHierarhy changet to {}", (showHierarhy ? "checked" : "unchecked"));
                resetImage();
            });

            Box boxMinLimitContourSliders = Box.createHorizontalBox();
            boxMinLimitContourSliders.setBorder(BorderFactory.createTitledBorder("MinLimitContour"));
          //boxMinLimitContourSliders.setToolTipText("...");
            boxMinLimitContourSliders.add(Box.createHorizontalGlue());
            boxMinLimitContourSliders.add(makeSliderVert(modelMinLimitContoursW, "Width", "MinLimitContour.Width"));
            boxMinLimitContourSliders.add(Box.createHorizontalStrut(2));
            boxMinLimitContourSliders.add(makeSliderVert(modelMinLimitContoursH, "Height", "MinLimitContour.Height"));
            boxMinLimitContourSliders.add(Box.createHorizontalGlue());

            panelCustom = new JPanel();
            panelCustom.setLayout(new BorderLayout());
            panelCustom.setBorder(BorderFactory.createTitledBorder("Custom"));
            panelCustom.add(checkBoxShowHierarhy     , BorderLayout.NORTH);
            panelCustom.add(boxMinLimitContourSliders, BorderLayout.CENTER);
        }

        Box boxAll = Box.createVerticalBox();
        boxAll.add(panelOptions);
        boxAll.add(Box.createVerticalStrut(2));
        boxAll.add(panelCustom);

        boxCenterLeft.add(boxAll);

        modelOffsetX.getWrapped().addChangeListener(ev -> {
            logger.trace("modelOffsetX: value={}", modelOffsetX.getFormatedText());
            resetImage();
        });
        modelOffsetY.getWrapped().addChangeListener(ev -> {
            logger.trace("modelOffsetY: value={}", modelOffsetY.getFormatedText());
            resetImage();
        });
        modelMinLimitContoursW.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMinLimitContoursW: value={}", modelMinLimitContoursW.getFormatedText());
            resetImage();
        });
        modelMinLimitContoursH.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMinLimitContoursH: value={}", modelMinLimitContoursH.getFormatedText());
            resetImage();
        });
    }

    @Override
    public void printParams() {
        logger.info("showHierarhy={}, mode={}, method={}, offset={{}, {}}, minLimitContours={{}, {}}",
                showHierarhy,
                mode, method,
                modelOffsetX.getFormatedText(),
                modelOffsetY.getFormatedText(),
                modelMinLimitContoursW.getFormatedText(),
                modelMinLimitContoursH.getFormatedText());
    }

}
