package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.*;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.EFindContoursDrawMethod;
import ksn.imgusage.type.dto.opencv.FindContoursTabParams;
import ksn.imgusage.type.opencv.CvContourApproximationModes;
import ksn.imgusage.type.opencv.CvLineType;
import ksn.imgusage.type.opencv.CvRetrievalModes;
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
    private Box boxDrawContoursParams;
    private Box boxExteranlRectParams;
    private IntConsumer setMaxContourIdx;

    @Override
    public Component makeTab(FindContoursTabParams params) {
        if (params == null)
            params = new FindContoursTabParams();
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
        // cast to gray image
        imageMat = OpenCvHelper.toGray(imageMat);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(
            imageMat,  // src
            contours,  // out1
            hierarchy, // out2
            params.mode.getVal(),
            params.method.getVal());

        { // restore color image
            Mat mat = new Mat();
            Imgproc.cvtColor(imageMat, mat, Imgproc.COLOR_GRAY2RGB);
            imageMat = mat;
        }

        if (contours.isEmpty()) {
            logger.warn("No any contours found!");
            return;
        }


        { // !!! recheck params !!!
            if (params.contourIdx >= contours.size())
                params.contourIdx = contours.size() - 1;
            setMaxContourIdx.accept(contours.size() - 1);
        }


        Scalar green = new Scalar(0, 255, 0);
        Random rnd = ThreadLocalRandom.current();
        Supplier<Scalar> getColor = () -> params.randomColors
                ? new Scalar(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
                : green;

        switch (params.drawMethod) {
        case DRAW_CONTOURS:
            final Point offset = new Point();

            List<Integer> ids;
            if (params.maxContourArea > 0) {
                ids = new ArrayList<>();
                List<Integer> tmpList = ids;
                IntConsumer check = idx -> {
                    MatOfPoint contour = contours.get(idx);
                    double area = Math.abs(Imgproc.contourArea(contour));
                    if (area > params.maxContourArea)
                        tmpList.add(idx);
                };
                if (params.contourIdx < 0) {
                    for (int i = 0; i < contours.size(); i++)
                        check.accept(i);
                } else {
                    check.accept(params.contourIdx);
                }
            } else {
                if (params.contourIdx < 0) {
                    if (params.randomColors)
                        ids = IntStream.range(0, contours.size()).boxed().collect(Collectors.toList());
                    else
                        ids = Arrays.asList(params.contourIdx);
                } else {
                    ids = Arrays.asList(params.contourIdx);
                }
            }

            if (ids.isEmpty()) {
                logger.warn("All contours are filtered!");
                return;
            }

            for (int idx : ids)
                Imgproc.drawContours(
                        imageMat,                        // Mat image
                        contours,                        // List<MatOfPoint> contours
                        idx,                             // int contourIdx
                        getColor.get(),                  // Scalar color
                        params.fillContour               // int thickness
                            ? CvLineType.FILLED.getVal()
                            : 1,
                        CvLineType.LINE_AA.getVal(),     // int lineType
                        hierarchy,                       // Mat hierarchy
                        params.maxLevel,                 // int maxLevel
                        offset);                         // Point offset

            /** /
            if (false) {
                // example of loop by hierarchy

                // iterate through all the top-level contours,
                // draw each connected component with its own random color
                int maxLevel = +2147483647; // C lang INT_MAX
                int[] tmp = new int[ hierarchy.channels() ];
                boolean isTraceEnabeld = logger.isTraceEnabled();
                for (int idx = 0; idx >= 0;) {
                    Scalar color = new Scalar(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    Imgproc.drawContours(
                            imageMat,                   // Mat image
                            contours,                   // List<MatOfPoint> contours
                            idx,                        // int contourIdx
                            color,                      // Scalar color
                            params.fillContour          // int thickness
                                ? CvLineType.FILLED.getVal()
                                : 1,
                            CvLineType.LINE_AA.getVal(),// int lineType
                            hierarchy,                  // Mat hierarchy
                            maxLevel,                   // int maxLevel
                            offset);                    // Point offset

                    // idx = hierarchy[idx][0]; // where     vector<Vec4i> hierarchy; // C++
                    int readed = hierarchy.get(0, idx, tmp);
                    if (readed < 1)
                        break;
                    if (isTraceEnabeld)
                        logger.trace("hierarchy.get(0, {}) = {}", idx, tmp);
                    idx = tmp[0];
                }
            }
            /**/
            break;
        case EXTERNAL_RECT:
            contours.stream()
                .map(Imgproc::boundingRect)
                .filter(rc -> (rc.width >= params.minLimitContours.width) && (rc.height >= params.minLimitContours.height))
                .forEach(rc -> Imgproc.rectangle(imageMat,
                                                 new Point(rc.x, rc.y),
                                                 new Point(rc.x + rc.width, rc.y + rc.height),
                                                 getColor.get(), 1));
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
        SliderIntModel modelContourIdx        = new SliderIntModel(params.contourIdx    , 0, -1, Math.max(0, params.contourIdx));
        SliderIntModel modelMaxLevel          = new SliderIntModel(params.maxLevel      , 0, 0, 100);

        setMaxContourIdx = maxVal -> {
            if (modelContourIdx.getMaximum() != maxVal)
                SwingUtilities.invokeLater(() -> modelContourIdx.setMaximum(maxVal));
        };

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

            Component boxSelectDrawMethod = makeBoxedRadioButtons(
                Stream.of(EFindContoursDrawMethod.values()),
                () -> params.drawMethod,
                v  -> params.drawMethod = v,
                "Draw method",
                "params.drawMethod",
                "How to draw contours?",
                null,
                v -> v == EFindContoursDrawMethod.DRAW_CONTOURS
                    ? "to display the contours using <p>drawContours()</p> method"
                    : "draw external rectangle of contours region",
                v -> {
                    if (v == EFindContoursDrawMethod.DRAW_CONTOURS)
                        makeDrawContoursParams(panelCustomParams, modelMaxContourArea, modelContourIdx, modelMaxLevel);
                    else
                        makeExteranlRectParams(panelCustomParams, modelMinLimitContoursW, modelMinLimitContoursH);
                    panelCustomParams.revalidate();
                });

            switch (params.drawMethod) {
            case DRAW_CONTOURS: makeDrawContoursParams(panelCustomParams, modelMaxContourArea, modelContourIdx, modelMaxLevel); break;
            case EXTERNAL_RECT: makeExteranlRectParams(panelCustomParams, modelMinLimitContoursW, modelMinLimitContoursH); break;
            default:
                logger.error("Unknown params.drawMethod={}! Support him!", params.drawMethod);
            }

            Component cntrl4RandomColor = makeBoxedCheckBox(
                    () -> params.randomColors,
                    v  -> params.randomColors = v,
                    "",
                    "Random color",
                    "params.randomColors",
                    null, null);
            panelDrawContoursOptions.add(boxSelectDrawMethod, BorderLayout.NORTH);
            panelDrawContoursOptions.add(panelCustomParams  , BorderLayout.CENTER);
            panelDrawContoursOptions.add(cntrl4RandomColor  , BorderLayout.SOUTH);
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
        modelContourIdx.getWrapped().addChangeListener(ev -> {
            logger.trace("modelContourId: value={}", modelContourIdx.getFormatedText());
            params.contourIdx = modelContourIdx.getValue();
            resetImage();
        });
        modelMaxLevel.getWrapped().addChangeListener(ev -> {
            logger.trace("modelMaxLevel: value={}", modelMaxLevel.getFormatedText());
            params.maxLevel = modelMaxLevel.getValue();
            resetImage();
        });

        return box4Options;
    }

    private void makeDrawContoursParams(JPanel panelCustomParams, SliderIntModel modelMaxContourArea, SliderIntModel modelContourIdx, SliderIntModel modelMaxLevel) {
        if (boxExteranlRectParams != null)
            boxExteranlRectParams.setVisible(false);

        if (boxDrawContoursParams == null) {
            boxDrawContoursParams = Box.createHorizontalBox();
            boxDrawContoursParams.setBorder(BorderFactory.createTitledBorder("Outlines or filled contours"));

            Component boxFilled = makeCheckBox(
                () -> params.fillContour,
                v  -> params.fillContour = v,
                "Fill",
                "params.fillContour",
                null, null);

            boxDrawContoursParams.add(Box.createHorizontalGlue());
            boxDrawContoursParams.add(makeSliderVert(modelMaxContourArea, "Area", "Max show contour area"));
            boxDrawContoursParams.add(Box.createHorizontalStrut(2));
            boxDrawContoursParams.add(boxFilled);
            boxDrawContoursParams.add(Box.createHorizontalStrut(2));
            boxDrawContoursParams.add(makeSliderVert(modelContourIdx, "contourIdx", "Parameter indicating a contour to draw. If it is negative, all the contours are drawn"));
            boxDrawContoursParams.add(Box.createHorizontalStrut(2));
            boxDrawContoursParams.add(makeSliderVert(modelMaxLevel, "maxLevel", "Maximal level for drawn contours. If it is 0, only the specified contour is drawn. If it is 1, the function draws the contour(s) and all the nested contours. If it is 2, the function draws the contours, all the nested contours, all the nested-to-nested contours, and so on. (Theoretical max value: +2147483647 - C lang INT_MAX)"));
            boxDrawContoursParams.add(Box.createHorizontalGlue());
        }
        boxDrawContoursParams.setVisible(true);

        panelCustomParams.setBorder(BorderFactory.createTitledBorder("Method drawContours() options"));
        panelCustomParams.add(boxDrawContoursParams, BorderLayout.CENTER);
    }


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
