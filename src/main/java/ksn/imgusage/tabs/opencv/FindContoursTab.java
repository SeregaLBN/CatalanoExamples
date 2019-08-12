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

    private static final int MIN_MIN_LIMIT_CONTOUR_SIZE =    0;
    private static final int MAX_MIN_LIMIT_CONTOUR_SIZE = 1000;
    private static final int MIN_MAX_CONTOUR_AREA =    0;
    private static final int MAX_MAX_CONTOUR_AREA = 10000;

    public enum EDrawMethod {
        /** to display the contours using <a href='https://docs.opencv.org/3.4.2/d6/d6e/group__imgproc__draw.html#ga746c0625f1781f1ffc9056259103edbc'>drawContours()</a> method. */
        DRAW_CONTOURS,
        /** draw external rectangle of contours region */
        EXTERNAL_RECT
    }

    private CvRetrievalModes            mode;
    private CvContourApproximationModes method;
    private EDrawMethod drawMethod;
    private final SliderIntModel        modelMinLimitContoursW;
    private final SliderIntModel        modelMinLimitContoursH;
    private final SliderIntModel        modelMaxContourArea;

    public FindContoursTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, null,
             CvRetrievalModes.RETR_EXTERNAL, CvContourApproximationModes.CHAIN_APPROX_SIMPLE,
             EDrawMethod.EXTERNAL_RECT, new Size(10, 10),
             100);
    }

    public FindContoursTab(ITabHandler tabHandler, ITab source, Boolean boosting,
        CvRetrievalModes mode, CvContourApproximationModes method,
        EDrawMethod drawMethod, Size minLimitContours,
        int maxContourArea
    ) {
        super(tabHandler, source, boosting);
        this.mode   = mode;
        this.method = method;
        this.drawMethod = drawMethod;
        this.modelMinLimitContoursW = new SliderIntModel((int)minLimitContours.width , 0, MIN_MIN_LIMIT_CONTOUR_SIZE, MAX_MIN_LIMIT_CONTOUR_SIZE);
        this.modelMinLimitContoursH = new SliderIntModel((int)minLimitContours.height, 0, MIN_MIN_LIMIT_CONTOUR_SIZE, MAX_MIN_LIMIT_CONTOUR_SIZE);
        this.modelMaxContourArea    = new SliderIntModel(maxContourArea, 0, MIN_MAX_CONTOUR_AREA, MAX_MAX_CONTOUR_AREA);

        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyOpencvFilter() {
//        if (this.modelMinLimitContoursW.getMaximum() != imageMat.width())
//            SwingUtilities.invokeLater(() -> this.modelMinLimitContoursW.setMaximum(imageMat.width()) );
//        if (this.modelMinLimitContoursH.getMaximum() != imageMat.height())
//            SwingUtilities.invokeLater(() -> this.modelMinLimitContoursH.setMaximum(imageMat.height()) );

        // cast to gray image
        if (imageMat.type() != CvArrayType.CV_8UC1.getVal())
            imageMat = OpenCvHelper.toGray(imageMat);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(
            imageMat, // src
            contours, // out
            hierarchy, // dst
            mode.getVal(),
            method.getVal());

        { // cast to color image
            Mat mat = new Mat();
            Imgproc.cvtColor(imageMat, mat, Imgproc.COLOR_GRAY2RGB);
            imageMat = mat;
        }

        Scalar green = new Scalar(0, 255, 0);
        final Point offset = new Point();
        switch (drawMethod) {
        case DRAW_CONTOURS:
            final int maxArea = modelMaxContourArea.getValue();
            if (maxArea > 0) {
                for (int i = 0; i < contours.size(); i++) {
                    MatOfPoint contour = contours.get(i);
                    double area = Math.abs(Imgproc.contourArea(contour));
                    if ( area > maxArea) {
                        Imgproc.drawContours(//contours, contourList, i, new Scalar(r.nextInt(255), r.nextInt(255), r.nextInt(255)), -1);
                                imageMat, // Mat image
                                contours, // List<MatOfPoint> contours
                                i,        // int contourIdx
                                green,    // Scalar color
                                1         // int thickness
                            );
                    }
                }

            } else {

                List<MatOfPoint> contours2;
                if (true) {
                    contours2 = contours;
                } else {
                    contours2 = new ArrayList<>(contours.size());
                    for (int k = 0; k < contours.size(); k++ ) {
                        MatOfPoint2f in = new MatOfPoint2f(contours.get(k).toArray());
                        MatOfPoint2f out = new MatOfPoint2f();
                        Imgproc.approxPolyDP(in, out, 3, true);
                        MatOfPoint out2 = new MatOfPoint(out.toArray());
                        contours2.add(out2);
                    }
                }

                Imgproc.drawContours(
                    imageMat,        // Mat image
                    contours2,       // List<MatOfPoint> contours
                    -1,              // int contourIdx
                    green,           // Scalar color
                    1,               // int thickness
                    Imgproc.LINE_AA, // int lineType
                    hierarchy,       // Mat hierarchy
                    3,               // int maxLevel
                    offset           // Point offset
                );
            }
            break;
        case EXTERNAL_RECT:
            contours.stream()
                .map(Imgproc::boundingRect)
                .filter(rc -> (rc.width > modelMinLimitContoursW.getValue()) && (rc.height > modelMinLimitContoursH.getValue()))
                .forEach(rc -> Imgproc.rectangle(imageMat,
                                                 new Point(rc.x, rc.y),
                                                 new Point(rc.x + rc.width, rc.y + rc.height),
                                                 green, 1));
            break;
        default:
            logger.error("Unknown this.drawMethod={}! Support him!", this.drawMethod);
        }
    }

    @Override
    protected void makeOptions(Box box4Options) {
        Box boxFindContoursOptions = Box.createVerticalBox();
        {
            Box boxMode = Box.createHorizontalBox();
            {
                boxMode.setBorder(BorderFactory.createTitledBorder("Contour retrieval mode"));
                JComboBox<CvRetrievalModes> comboBoxMode = new JComboBox<>(CvRetrievalModes.values());
                comboBoxMode.setSelectedItem(this.mode);
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
                comboBoxMeethod.setToolTipText("Contour approximation method");
                comboBoxMeethod.addActionListener(ev -> {
                    this.method = (CvContourApproximationModes)comboBoxMeethod.getSelectedItem();
                    logger.trace("Method changed to {}", this.method);
                    resetImage();
                });
                boxMethod.add(comboBoxMeethod);
            }

            boxFindContoursOptions.setBorder(BorderFactory.createTitledBorder(getTabName() + " options"));
            boxFindContoursOptions.add(boxMode);
            boxFindContoursOptions.add(Box.createVerticalStrut(2));
            boxFindContoursOptions.add(boxMethod);
        }

        JPanel panelDrawContoursOptions = new JPanel();
        {
            panelDrawContoursOptions.setLayout(new BorderLayout());
            panelDrawContoursOptions.setBorder(BorderFactory.createTitledBorder("Draw contours"));

            JPanel panelCustomParams = new JPanel();
            panelCustomParams.setLayout(new BorderLayout());

            Box boxSelectDrawMethod = Box.createVerticalBox();
            {
                boxSelectDrawMethod.setBorder(BorderFactory.createTitledBorder("Draw method"));
                boxSelectDrawMethod.setToolTipText("How to draw contours?");
                ButtonGroup radioGroup = new ButtonGroup();

                JRadioButton radioBtn1 = new JRadioButton(EDrawMethod.DRAW_CONTOURS.name(), this.drawMethod == EDrawMethod.DRAW_CONTOURS);
                radioBtn1.setToolTipText("to display the contours using <p>drawContours()</p> method");
                radioBtn1.addItemListener(ev -> {
                    if (ev.getStateChange() == ItemEvent.SELECTED) {
                        this.drawMethod = EDrawMethod.DRAW_CONTOURS;
                        logger.trace("this.drawMethod changed to {}", this.drawMethod);
                        makeDrawContoursParams(panelCustomParams);
                        panelCustomParams.revalidate();
                        resetImage();
                    }
                });
                boxSelectDrawMethod.add(radioBtn1);
                radioGroup.add(radioBtn1);

                JRadioButton radioBtn2 = new JRadioButton(EDrawMethod.EXTERNAL_RECT.name(), this.drawMethod == EDrawMethod.EXTERNAL_RECT);
                radioBtn2.setToolTipText("draw external rectangle of contours region");
                radioBtn2.addItemListener(ev -> {
                    if (ev.getStateChange() == ItemEvent.SELECTED) {
                        this.drawMethod = EDrawMethod.EXTERNAL_RECT;
                        logger.trace("this.drawMethod changed to {}", this.drawMethod);
                        makeExteranlRectParams(panelCustomParams);
                        panelCustomParams.revalidate();
                        resetImage();
                    }
                });
                boxSelectDrawMethod.add(radioBtn2);
                radioGroup.add(radioBtn2);

                switch (this.drawMethod) {
                case DRAW_CONTOURS: makeDrawContoursParams(panelCustomParams); break;
                case EXTERNAL_RECT: makeExteranlRectParams(panelCustomParams); break;
                default:
                    logger.error("Unknown this.drawMethod={}! Support him!", this.drawMethod);
                }
            }

            panelDrawContoursOptions.add(boxSelectDrawMethod, BorderLayout.NORTH);
            panelDrawContoursOptions.add(panelCustomParams  , BorderLayout.CENTER);
        }

        JPanel panelAll = new JPanel();
        panelAll.setLayout(new BorderLayout());
        panelAll.add(boxFindContoursOptions  , BorderLayout.NORTH);
        panelAll.add(panelDrawContoursOptions, BorderLayout.CENTER);

        box4Options.add(panelAll);

        modelMinLimitContoursW.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMinLimitContoursW: value={}", modelMinLimitContoursW.getFormatedText());
            resetImage();
        });
        modelMinLimitContoursH.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMinLimitContoursH: value={}", modelMinLimitContoursH.getFormatedText());
            resetImage();
        });
        modelMaxContourArea.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMaxContourArea: value={}", modelMaxContourArea.getFormatedText());
            resetImage();
        });
    }

    private Box boxDrawContoursParams;
    private void makeDrawContoursParams(JPanel panelCustomParams) {
        if (boxExteranlRectParams != null)
            boxExteranlRectParams.setVisible(false);

        if (boxDrawContoursParams == null) {
            boxDrawContoursParams = Box.createHorizontalBox();
            boxDrawContoursParams.setBorder(BorderFactory.createTitledBorder("..."));

            boxDrawContoursParams.add(Box.createHorizontalGlue());
            boxDrawContoursParams.add(makeSliderVert(modelMaxContourArea, "Area", "Max show contour area"));
            boxDrawContoursParams.add(Box.createHorizontalGlue());
}
        boxDrawContoursParams.setVisible(true);

        panelCustomParams.setBorder(BorderFactory.createTitledBorder("Method drawContours() options"));
        panelCustomParams.add(boxDrawContoursParams, BorderLayout.CENTER);
    }


    private Box boxExteranlRectParams;
    private void makeExteranlRectParams(JPanel panelCustomParams) {
        if (boxDrawContoursParams != null)
            boxDrawContoursParams.setVisible(false);

        if (boxExteranlRectParams == null) {
            boxExteranlRectParams = Box.createHorizontalBox();
            boxExteranlRectParams.setBorder(BorderFactory.createTitledBorder("MinLimitContour"));
          //boxMinLimitContourSliders.setToolTipText("...");
            boxExteranlRectParams.add(Box.createHorizontalGlue());
            boxExteranlRectParams.add(makeSliderVert(modelMinLimitContoursW, "Width", "MinLimitContour.Width"));
            boxExteranlRectParams.add(Box.createHorizontalStrut(2));
            boxExteranlRectParams.add(makeSliderVert(modelMinLimitContoursH, "Height", "MinLimitContour.Height"));
            boxExteranlRectParams.add(Box.createHorizontalGlue());
        }
        boxExteranlRectParams.setVisible(true);

        panelCustomParams.setBorder(BorderFactory.createTitledBorder("Raw contours options"));
        panelCustomParams.add(boxExteranlRectParams, BorderLayout.CENTER);
    }

    @Override
    public void printParams() {
        logger.info("mode={}, method={}, drawMethod={}, minLimitContours={{}, {}}, maxContourArea={}",
                mode, method,
                drawMethod,
                modelMinLimitContoursW.getFormatedText(),
                modelMinLimitContoursH.getFormatedText(),
                modelMaxContourArea.getFormatedText());
    }

}
