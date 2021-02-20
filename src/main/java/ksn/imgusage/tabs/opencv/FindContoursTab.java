package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.util.Pair;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.EFindContoursDrawMethod;
import ksn.imgusage.type.dto.opencv.FindContoursTabParams;
import ksn.imgusage.type.opencv.CvContourApproximationModes;
import ksn.imgusage.type.opencv.CvLineType;
import ksn.imgusage.type.opencv.CvRetrievalModes;
import ksn.imgusage.utils.GeomHelper;
import ksn.imgusage.utils.OpenCvHelper;

/** <a href='https://docs.opencv.org/3.4.2/d3/dc0/group__imgproc__shape.html#ga17ed9f5d79ae97bd4c7cf18403e1689a'>Finds contours in a binary image</a> */
public class FindContoursTab extends OpencvFilterTab<FindContoursTabParams> {

    public static final String TAB_TITLE = "FindContours";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Finds contours in a binary image";

    private static final int MIN_MIN_LIMIT_CONTOUR_SIZE =    0;
    private static final int MAX_MIN_LIMIT_CONTOUR_SIZE = 1000;
    private static final int MIN_MAX_LIMIT_CONTOUR_SIZE =    5;
    private static final int MAX_MAX_LIMIT_CONTOUR_SIZE = 1001;
    private static final int MIN_MIN_CONTOUR_AREA =    0;
    private static final int MAX_MIN_CONTOUR_AREA = 10000;
    private static final int MIN_MAX_CONTOUR_AREA =    0;
    private static final int MAX_MAX_CONTOUR_AREA = 10001;

    private static final Scalar GREEN       = new Scalar(0x00, 0xFF, 0x00);
    private static final Scalar MEDIUM_BLUE = new Scalar(0xCD, 0x00, 0x00);
    private static final Scalar GOLD        = new Scalar(0x00, 0xD7, 0xFF);
    private static final Scalar MAGENTA     = new Scalar(0xFF, 0x00, 0xFF);

    private static final Point offset = new Point();

    private FindContoursTabParams params;
    private Box boxDrawContoursParams;
    private Component cntrlExteranlRectParams;
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


        Random rnd = ThreadLocalRandom.current();
        Supplier<Scalar> getColor = () -> params.randomColors
                ? new Scalar(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
                : GREEN;

        switch (params.drawMethod) {
        case DRAW_CONTOURS:
            List<Integer> ids;
            if (params.maxContourArea > 0) {
                ids = new ArrayList<>();
                List<Integer> tmpList = ids;
                IntConsumer check = idx -> {
                    MatOfPoint contour = contours.get(idx);
                    double area = Math.abs(Imgproc.contourArea(contour));
                    if ((area >= params.minContourArea) && (area <= params.maxContourArea))
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

            FindContoursResult finded = new FindContoursResult();
            finded.contours  = contours;
            finded.hierarchy = hierarchy;
            ArrayList<ContourContext> all = new ArrayList<>(finded.contours.size());
            for (int i = 0; i < finded.contours.size(); ++i) {
                MatOfPoint contour = finded.contours.get(i);

                ContourContext cc = new ContourContext();
                cc.index = i;
                cc.rc = Imgproc.boundingRect(contour);
                all.add(cc);
            }
            finded.limited = all.stream()
                    .filter(this::checkBounds)
                    .collect(Collectors.toList());

            // group rects (intersect) and draw pass round
            List<SymbolContours> groups = groupNearbyСontours(finded);

            // colorized borders symbol/regions
            imageMat = OpenCvHelper.to3Channel(imageMat);
            groups.forEach(gc -> {
                Rect rc = gc.getOwnRect();
                Imgproc.rectangle(
                    imageMat,
                    rc.tl(), rc.br(),
                    MAGENTA, 3);
            });
            groups.stream()
                .map(gc -> gc.excluded.stream())
                .flatMap(x -> x)
                .forEach(cc ->
                    Imgproc.rectangle(
                        imageMat,
                        cc.rc.tl(), cc.rc.br(),
                        GOLD, 2)
                );
            groups.stream()
                .map(gc -> gc.included.stream())
                .flatMap(x -> x)
                .forEach(cc ->
                    Imgproc.rectangle(
                        imageMat,
                        cc.rc.tl(), cc.rc.br(),
                        MEDIUM_BLUE, 1)
                );

            break;
        default:
            logger.error("Unknown this.drawMethod={}! Support him!", params.drawMethod);
        }
    }

    static class ContourContext {
        /** the contour index in the original result sequence {@link FindContoursResult#contours} */
        int index;
        /** contour own rectangle */
        Rect rc;
    }

    static class FindContoursResult {
        /** original from @see {link {@link Imgproc#findContours(Mat, List, Mat, int, int)}} */
        List<MatOfPoint> contours;

        /** original from @see {link {@link Imgproc#findContours(Mat, List, Mat, int, int)}} */
        Mat hierarchy;

        /** {@link contours}.stream.map(cast to {@link ContourContext}).filtered(this::{@link #checkBounds}()) */
        List<ContourContext> limited;
    }

    /** a group of contours that form a single character */
    static class SymbolContours {
        /** own contours */
        Set<ContourContext> included;

        /** inner contours */
        Set<ContourContext> excluded;

        /** own rectangle */
        Rect getOwnRect() {
            if (included == null)
                return null;
            if (included.isEmpty())
                return new Rect();

            Optional<Rect> oRc = included.stream()
                .map(cntxt -> cntxt.rc)
                .reduce((rc1, rc2) -> {
                    Point br1 = rc1.br();
                    Point br2 = rc2.br();
                    return new Rect(
                        new Point(Math.min(rc1.x, rc2.x),
                                  Math.min(rc1.y, rc2.y)),
                        new Point(Math.max(br1.x, br2.x),
                                  Math.max(br1.y, br2.y))
                    );
                });
            return oRc.isPresent() ? oRc.get() : null;
        }

    }

    private List<SymbolContours> groupNearbyСontours(FindContoursResult finded) {
        Set<ContourContext> excludedAll = new HashSet<>();
        Set<Pair<ContourContext /* excluded */, ContourContext /* excludedFrom */>> excludedFrom = new HashSet<>();
        // exclude all inner rects
        for (ContourContext cc1 : finded.limited) {
            if (excludedAll.contains(cc1))
                continue;
            Rect rc1 = cc1.rc;
            Point br1 = rc1.br();
            for (ContourContext cc2 : finded.limited) {
                if (cc1.equals(cc2))
                    continue;
                if (excludedAll.contains(cc2))
                    continue;

                Rect rc2 = cc2.rc;
                Point br2 = rc2.br();
                if ((rc2.x >= rc1.x) &&
                    (rc2.y >= rc1.y) &&
                    (br2.x <= br1.x) &&
                    (br2.y <= br1.y))
                {
                    excludedAll.add(cc2);
                    excludedFrom.add(new Pair<>(cc2, cc1));
                }
            }
        }

        List<SymbolContours> res = new ArrayList<>();
        // group by predicate
        Set<ContourContext> includedAll = new HashSet<>();
        for (ContourContext cc1 : finded.limited) {
            if (excludedAll.contains(cc1))
                continue;
            if (includedAll.contains(cc1))
                continue;

            SymbolContours gc = new SymbolContours();
            res.add(gc);
            gc.included = new HashSet<>();
            gc.excluded = new HashSet<>();

            gc.included.add(cc1);
            includedAll.add(cc1);

            Rect rc1 = cc1.rc;
            Rect rcOwn = rc1;
            for (ContourContext cc2 : finded.limited) {
                if (cc2.equals(cc1))
                    continue;
                if (excludedAll.contains(cc2))
                    continue;
                if (includedAll.contains(cc2))
                    continue;

                Rect rc2 = cc2.rc;
                Rect rc = GeomHelper.intersectInclude(rcOwn, rc2);
                if (!checkBounds(rc) ||
                    rc.equals(rcOwn) ||
                    rc.equals(rc2))
                {
                    continue;
                }

                gc.included.add(cc2);
                includedAll.add(cc2);
                rcOwn = rc;
            }
        }

        for (Pair<ContourContext /* excluded */, ContourContext /* excludedFrom */> pair : excludedFrom) {
            ContourContext excluded = pair.getFirst();
            ContourContext included = pair.getSecond();

            Optional<SymbolContours> opGc = res.stream()
                .filter(gc -> gc.included.contains(included))
                .findAny();
            if (!opGc.isPresent()) {
                logger.error("Bad algorithm... ;(");
            } else {
                opGc.get()
                    .excluded
                    .add(excluded);
            }
        }

        return res;
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderIntModel modelMinLimitContoursW = new SliderIntModel(params.minLimitContours.width , 0, MIN_MIN_LIMIT_CONTOUR_SIZE, MAX_MIN_LIMIT_CONTOUR_SIZE);
        SliderIntModel modelMinLimitContoursH = new SliderIntModel(params.minLimitContours.height, 0, MIN_MIN_LIMIT_CONTOUR_SIZE, MAX_MIN_LIMIT_CONTOUR_SIZE);
        SliderIntModel modelMaxLimitContoursW = new SliderIntModel(params.maxLimitContours.width , 0, MIN_MAX_LIMIT_CONTOUR_SIZE, MAX_MAX_LIMIT_CONTOUR_SIZE);
        SliderIntModel modelMaxLimitContoursH = new SliderIntModel(params.maxLimitContours.height, 0, MIN_MAX_LIMIT_CONTOUR_SIZE, MAX_MAX_LIMIT_CONTOUR_SIZE);
        SliderIntModel modelMinContourArea    = new SliderIntModel(params.minContourArea, 0, MIN_MIN_CONTOUR_AREA, MAX_MIN_CONTOUR_AREA);
        SliderIntModel modelMaxContourArea    = new SliderIntModel(params.maxContourArea, 0, MIN_MAX_CONTOUR_AREA, MAX_MAX_CONTOUR_AREA);
        SliderIntModel modelContourIdx        = new SliderIntModel(params.contourIdx    , 0, -1, Math.max(0, params.contourIdx));
        SliderIntModel modelMaxLevel          = new SliderIntModel(params.maxLevel      , 0, 0, 100);

        setMaxContourIdx = maxVal -> {
            if (modelContourIdx.getMaximum() != maxVal)
                SwingUtilities.invokeLater(() -> modelContourIdx.setMaximum(maxVal));
        };

        Box boxFindContoursOptions = Box.createVerticalBox();
        {
            boxFindContoursOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
            boxFindContoursOptions.add(makeComboBox(CvRetrievalModes.values(),
                                                    () -> params.mode,
                                                    v  -> params.mode = v,
                                                    "params.mode",
                                                    "Contour retrieval mode",
                                                    "Contour retrieval mode"));
            boxFindContoursOptions.add(Box.createVerticalStrut(2));
            boxFindContoursOptions.add(makeComboBox(CvContourApproximationModes.values(),
                                                    () -> params.method,
                                                    v  -> params.method = v,
                                                    "params.method",
                                                    "Contour approximation method",
                                                    "Contour approximation method"));
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
                        makeDrawContoursParams(panelCustomParams, modelMinContourArea, modelMaxContourArea, modelContourIdx, modelMaxLevel);
                    else
                        makeExteranlRectParams(panelCustomParams, modelMinLimitContoursW, modelMinLimitContoursH, modelMaxLimitContoursW, modelMaxLimitContoursH);
                    panelCustomParams.revalidate();
                });

            switch (params.drawMethod) {
            case DRAW_CONTOURS: makeDrawContoursParams(panelCustomParams, modelMinContourArea, modelMaxContourArea, modelContourIdx, modelMaxLevel); break;
            case EXTERNAL_RECT: makeExteranlRectParams(panelCustomParams, modelMinLimitContoursW, modelMinLimitContoursH, modelMaxLimitContoursW, modelMaxLimitContoursH); break;
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

        return box4Options;
    }

    private void makeDrawContoursParams(
            JPanel panelCustomParams,
            SliderIntModel modelMinContourArea,
            SliderIntModel modelMaxContourArea,
            SliderIntModel modelContourIdx,
            SliderIntModel modelMaxLevel
    ) {
        if (cntrlExteranlRectParams != null)
            cntrlExteranlRectParams.setVisible(false);

        if (boxDrawContoursParams == null) {
            boxDrawContoursParams = Box.createHorizontalBox();
            boxDrawContoursParams.setBorder(BorderFactory.createTitledBorder("Outlines or filled contours"));

            Component boxFilled = makeCheckBox(
                () -> params.fillContour,
                v  -> params.fillContour = v,
                "Fill",
                "params.fillContour",
                null, null);

            Component cntrlMinMaxContourArea = makeMinMax(
                    modelMinContourArea, // modelMin
                    modelMaxContourArea, // modelMax
                    "Area",              // borderTitle
                    "Limits area",       // tip
                    "Area minimum",      // tipMin
                    "Area maximum");     // tipMax

            boxDrawContoursParams.add(Box.createHorizontalGlue());
            boxDrawContoursParams.add(cntrlMinMaxContourArea);
            boxDrawContoursParams.add(Box.createHorizontalStrut(2));
            boxDrawContoursParams.add(boxFilled);
            boxDrawContoursParams.add(Box.createHorizontalStrut(2));
            boxDrawContoursParams.add(makeSliderVert(modelContourIdx, "contourIdx", "Parameter indicating a contour to draw. If it is negative, all the contours are drawn"));
            boxDrawContoursParams.add(Box.createHorizontalStrut(2));
            boxDrawContoursParams.add(makeSliderVert(modelMaxLevel, "maxLevel", "Maximal level for drawn contours. If it is 0, only the specified contour is drawn. If it is 1, the function draws the contour(s) and all the nested contours. If it is 2, the function draws the contours, all the nested contours, all the nested-to-nested contours, and so on. (Theoretical max value: +2147483647 - C lang INT_MAX)"));
            boxDrawContoursParams.add(Box.createHorizontalGlue());

            addChangeListener("modelMinContourArea"   , modelMinContourArea    , v -> params.minContourArea          = v);
            addChangeListener("modelMaxContourArea"   , modelMaxContourArea    , v -> params.maxContourArea          = v);
            addChangeListener("modelContourId"        , modelContourIdx        , v -> params.contourIdx              = v);
            addChangeListener("modelMaxLevel"         , modelMaxLevel          , v -> params.maxLevel                = v);
        }
        boxDrawContoursParams.setVisible(true);

        panelCustomParams.setBorder(BorderFactory.createTitledBorder("Method drawContours() options"));
        panelCustomParams.add(boxDrawContoursParams, BorderLayout.CENTER);
    }


    private void makeExteranlRectParams(
            JPanel panelCustomParams,
            SliderIntModel modelMinLimitContoursW, SliderIntModel modelMinLimitContoursH,
            SliderIntModel modelMaxLimitContoursW, SliderIntModel modelMaxLimitContoursH
    ) {
        if (boxDrawContoursParams != null)
            boxDrawContoursParams.setVisible(false);

        if (cntrlExteranlRectParams == null) {
            cntrlExteranlRectParams = makeContourLimits(
                modelMinLimitContoursW,
                modelMinLimitContoursH,
                modelMaxLimitContoursW,
                modelMaxLimitContoursH,
                null, null,
                "MinLimitContour", "MaxLimitContour",
                null, null);

            addChangeListener("params.minLimitContours.width" , modelMinLimitContoursW, v -> params.minLimitContours.width = v);
            addChangeListener("params.minLimitContours.height", modelMinLimitContoursH, v -> params.minLimitContours.height = v);
            addChangeListener("params.maxLimitContours.width" , modelMaxLimitContoursW, v -> params.maxLimitContours.width = v);
            addChangeListener("params.maxLimitContours.height", modelMaxLimitContoursH, v -> params.maxLimitContours.height = v);
        }

        cntrlExteranlRectParams.setVisible(true);

        panelCustomParams.setBorder(BorderFactory.createTitledBorder("Raw contours options"));
        panelCustomParams.add(cntrlExteranlRectParams, BorderLayout.CENTER);
    }

    @Override
    public FindContoursTabParams getParams() {
        return params;
    }

    private boolean checkBounds(ContourContext cntxt) {
        return checkBounds(cntxt.rc);
    }
    private boolean checkBounds(Rect rc) {
        return (rc.width  >= params.minLimitContours.width ) &&
               (rc.height >= params.minLimitContours.height) &&
               (rc.width  <= params.maxLimitContours.width ) &&
               (rc.height <= params.maxLimitContours.height);
    }

}
