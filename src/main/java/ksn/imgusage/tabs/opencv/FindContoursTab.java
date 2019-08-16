package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.opencv.type.CvArrayType;
import ksn.imgusage.tabs.opencv.type.CvContourApproximationModes;
import ksn.imgusage.tabs.opencv.type.CvRetrievalModes;
import ksn.imgusage.type.Size;
import ksn.imgusage.type.dto.opencv.EFindContoursDrawMethod;
import ksn.imgusage.type.dto.opencv.FindContoursTabParams;
import ksn.imgusage.utils.OpenCvHelper;

/** <a href='https://docs.opencv.org/3.4.2/d3/dc0/group__imgproc__shape.html#ga17ed9f5d79ae97bd4c7cf18403e1689a'>Finds contours in a binary image</a> */
public class FindContoursTab extends OpencvFilterTab<FindContoursTabParams> {

    public static final String TAB_TITLE = "FindContours";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Finds contours in a binary image";

    private static final int MIN_MIN_LIMIT_CONTOUR_SIZE =    0;
    private static final int MAX_MIN_LIMIT_CONTOUR_SIZE = 1000;
    private static final int MIN_MAX_CONTOUR_AREA =    0;
    private static final int MAX_MAX_CONTOUR_AREA = 10000;

    private FindContoursTabParams params;

    @Override
    public Component makeTab(FindContoursTabParams params) {
        if (params == null)
            params = new FindContoursTabParams(CvRetrievalModes.RETR_EXTERNAL, CvContourApproximationModes.CHAIN_APPROX_SIMPLE,
                                               EFindContoursDrawMethod.EXTERNAL_RECT, new Size(10, 10),
                                               100);
        this.params = params;

        return makeTab();
    }

    @Override
    public String getTitle() { return TAB_TITLE; }
    @Override
    public String getName() { return TAB_NAME; }

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
            params.mode.getVal(),
            params.method.getVal());

        { // cast to color image
            Mat mat = new Mat();
            Imgproc.cvtColor(imageMat, mat, Imgproc.COLOR_GRAY2RGB);
            imageMat = mat;
        }

        Scalar green = new Scalar(0, 255, 0);
        final Point offset = new Point();
        switch (params.drawMethod) {
        case DRAW_CONTOURS:
            if (params.maxContourArea > 0) {
                for (int i = 0; i < contours.size(); i++) {
                    MatOfPoint contour = contours.get(i);
                    double area = Math.abs(Imgproc.contourArea(contour));
                    if (area > params.maxContourArea) {
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
                //if (true) {
                    contours2 = contours;
                /** /
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
                /**/

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
                .filter(rc -> (rc.width >= params.minLimitContours.width) && (rc.height >= params.minLimitContours.height))
                .forEach(rc -> Imgproc.rectangle(imageMat,
                                                 new Point(rc.x, rc.y),
                                                 new Point(rc.x + rc.width, rc.y + rc.height),
                                                 green, 1));
            break;
        default:
            logger.error("Unknown this.drawMethod={}! Support him!", params.drawMethod);
        }
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderIntModel modelMinLimitContoursW = new SliderIntModel(params.minLimitContours.width , 0, MIN_MIN_LIMIT_CONTOUR_SIZE, MAX_MIN_LIMIT_CONTOUR_SIZE);
        SliderIntModel modelMinLimitContoursH = new SliderIntModel(params.minLimitContours.height, 0, MIN_MIN_LIMIT_CONTOUR_SIZE, MAX_MIN_LIMIT_CONTOUR_SIZE);
        SliderIntModel modelMaxContourArea    = new SliderIntModel(params.maxContourArea, 0, MIN_MAX_CONTOUR_AREA, MAX_MAX_CONTOUR_AREA);

        Box boxFindContoursOptions = Box.createVerticalBox();
        {
            Box boxMode = Box.createHorizontalBox();
            {
                boxMode.setBorder(BorderFactory.createTitledBorder("Contour retrieval mode"));
                JComboBox<CvRetrievalModes> comboBoxMode = new JComboBox<>(CvRetrievalModes.values());
                comboBoxMode.setSelectedItem(params.mode);
                comboBoxMode.setToolTipText("Contour retrieval mode");
                comboBoxMode.addActionListener(ev -> {
                    params.mode = (CvRetrievalModes)comboBoxMode.getSelectedItem();
                    logger.trace("Mode changed to {}", params.mode);
                    resetImage();
                });
                boxMode.add(comboBoxMode);
            }
            Box boxMethod = Box.createHorizontalBox();
            {
                boxMethod.setBorder(BorderFactory.createTitledBorder("Contour approximation method"));
                JComboBox<CvContourApproximationModes> comboBoxMeethod = new JComboBox<>(CvContourApproximationModes.values());
                comboBoxMeethod.setSelectedItem(params.method);
                comboBoxMeethod.setToolTipText("Contour approximation method");
                comboBoxMeethod.addActionListener(ev -> {
                    params.method = (CvContourApproximationModes)comboBoxMeethod.getSelectedItem();
                    logger.trace("Method changed to {}", params.method);
                    resetImage();
                });
                boxMethod.add(comboBoxMeethod);
            }

            boxFindContoursOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
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

                JRadioButton radioBtn1 = new JRadioButton(EFindContoursDrawMethod.DRAW_CONTOURS.name(), params.drawMethod == EFindContoursDrawMethod.DRAW_CONTOURS);
                radioBtn1.setToolTipText("to display the contours using <p>drawContours()</p> method");
                radioBtn1.addItemListener(ev -> {
                    if (ev.getStateChange() == ItemEvent.SELECTED) {
                        params.drawMethod = EFindContoursDrawMethod.DRAW_CONTOURS;
                        logger.trace("params.drawMethod changed to {}", params.drawMethod);
                        makeDrawContoursParams(panelCustomParams, modelMaxContourArea);
                        panelCustomParams.revalidate();
                        resetImage();
                    }
                });
                boxSelectDrawMethod.add(radioBtn1);
                radioGroup.add(radioBtn1);

                JRadioButton radioBtn2 = new JRadioButton(EFindContoursDrawMethod.EXTERNAL_RECT.name(), params.drawMethod == EFindContoursDrawMethod.EXTERNAL_RECT);
                radioBtn2.setToolTipText("draw external rectangle of contours region");
                radioBtn2.addItemListener(ev -> {
                    if (ev.getStateChange() == ItemEvent.SELECTED) {
                        params.drawMethod = EFindContoursDrawMethod.EXTERNAL_RECT;
                        logger.trace("params.drawMethod changed to {}", params.drawMethod);
                        makeExteranlRectParams(panelCustomParams, modelMinLimitContoursW, modelMinLimitContoursH);
                        panelCustomParams.revalidate();
                        resetImage();
                    }
                });
                boxSelectDrawMethod.add(radioBtn2);
                radioGroup.add(radioBtn2);

                switch (params.drawMethod) {
                case DRAW_CONTOURS: makeDrawContoursParams(panelCustomParams, modelMaxContourArea); break;
                case EXTERNAL_RECT: makeExteranlRectParams(panelCustomParams, modelMinLimitContoursW, modelMinLimitContoursH); break;
                default:
                    logger.error("Unknown params.drawMethod={}! Support him!", params.drawMethod);
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
            params.minLimitContours.width = modelMinLimitContoursW.getValue();
            resetImage();
        });
        modelMinLimitContoursH.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMinLimitContoursH: value={}", modelMinLimitContoursH.getFormatedText());
            params.minLimitContours.height = modelMinLimitContoursH.getValue();
            resetImage();
        });
        modelMaxContourArea.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMaxContourArea: value={}", modelMaxContourArea.getFormatedText());
            params.maxContourArea = modelMaxContourArea.getValue();
            resetImage();
        });

        return box4Options;
    }

    private Box boxDrawContoursParams;
    private void makeDrawContoursParams(JPanel panelCustomParams, SliderIntModel modelMaxContourArea) {
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
    private void makeExteranlRectParams(JPanel panelCustomParams, SliderIntModel modelMinLimitContoursW, SliderIntModel modelMinLimitContoursH) {
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
    public FindContoursTabParams getParams() {
        return params;
    }

}
