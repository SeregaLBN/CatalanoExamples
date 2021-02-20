package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.*;

import org.opencv.core.*;
import org.opencv.features2d.MSER;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.MserTabParams;
import ksn.imgusage.utils.GeomHelper;
import ksn.imgusage.utils.OpenCvHelper;

/** <a href='https://docs.opencv.org/3.4.2/d3/d28/classcv_1_1MSER.html'>Maximally stable extremal region extractor</a> */
public class MserTab extends OpencvFilterTab<MserTabParams> {

    public static final String TAB_TITLE = "MSER";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Maximally stable extremal region extractor";

    public  static final int    MIN_DELTA = 1;
    private static final int    MAX_DELTA = 150;
    private static final int    MIN_MIN_AREA =   1;
    private static final int    MAX_MIN_AREA = 4200;
    private static final int    MIN_MAX_AREA =   2;
    private static final int    MAX_MAX_AREA = 4201;
    private static final double MIN_MAX_VARIATION = 0.01;
    private static final double MAX_MAX_VARIATION = 100;
    private static final double MIN_MIN_DIVERSITY = 0.01;
    private static final double MAX_MIN_DIVERSITY = 50;
    public  static final int    MIN_MAX_EVOLUTION = 1;
    private static final int    MAX_MAX_EVOLUTION = 2000;
    private static final double MIN_AREA_THRESHOLD =   0;
    private static final double MAX_AREA_THRESHOLD = 9999;
    private static final double MIN_MIN_MARGIN = 0.001;
    private static final double MAX_MIN_MARGIN = 0.999;
    public  static final int    MIN_EDGE_BLUR_SIZE = 1;
    private static final int    MAX_EDGE_BLUR_SIZE = 300;
    private static final int    MIN_MIN_SYMBOL_W =   1;
    private static final int    MIN_MIN_SYMBOL_H =   1;
    private static final int    MAX_MIN_SYMBOL_W = 499;
    private static final int    MAX_MIN_SYMBOL_H = 699;
    private static final int    MIN_MAX_SYMBOL_W = MIN_MIN_SYMBOL_W + 1;
    private static final int    MIN_MAX_SYMBOL_H = MIN_MIN_SYMBOL_H + 1;
    private static final int    MAX_MAX_SYMBOL_W = MAX_MIN_SYMBOL_W + 1;
    private static final int    MAX_MAX_SYMBOL_H = MAX_MIN_SYMBOL_H + 1;
    private static final int    MAX_SYMBOLS_STUCK = 10;

    private static final Scalar BLACK   = new Scalar(0);
    private static final Scalar WHITE   = new Scalar(0xFF);
    private static final Scalar GREEN   = new Scalar(0x00, 0xFF, 0x00);
    private static final Scalar YELLOW  = new Scalar(0x00, 0xFF, 0xFF);
    private static final Scalar MAGENTA = new Scalar(0xFF, 0x00, 0xFF);
    private static final Scalar AMBER   = new Scalar(0x00, 0xBF, 0xFF);

    private static final Scalar  INNER_COLOR = YELLOW;
    private static final Scalar SYMBOL_COLOR = GREEN;
    private static final Scalar   WORD_COLOR = MAGENTA;
    private static final Scalar   LINE_COLOR = AMBER;

    private MserTabParams params;

    @Override
    public Component makeTab(MserTabParams params) {
        if (params == null)
            params = new MserTabParams();
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
        MSER mser = MSER.create(
            params.delta,
            params.minArea,
            params.maxArea,
            params.maxVariation,
            params.minDiversity,
            params.maxEvolution,
            params.areaThreshold,
            params.minMargin,
            params.edgeBlurSize);

        List<MatOfPoint> regions = new ArrayList<>(); // resulting list of point sets
        mser.detectRegions(imageMat,
                           regions,
                           new MatOfRect()); // resulting bounding boxes

        { // filter
            List<Integer> ignored = new ArrayList<>();
            for (int i=0; i < regions.size(); ++i) {
                MatOfPoint contour = regions.get(i);
                Rect rc = Imgproc.boundingRect(contour);
                if ((rc.width  < params.minSymbol.width ) ||
                    (rc.height < params.minSymbol.height) ||
                    (rc.width  >(params.maxSymbol.width * params.stuckSymbols)) ||
                    (rc.height > params.maxSymbol.height))
                {
                    ignored.add(i);
                }
            }
            logger.trace("Ignored contours indexes size: {}", ignored.size());
            for (int i = ignored.size() - 1; i >= 0; --i) {
                int delIndex = ignored.get(i);
                regions.remove(delIndex);
            }
        }

        List<MatOfPoint> inner = Collections.emptyList();
        if (!params.showInner) {
            Set<MatOfPoint> inner2 = new HashSet<>();
            for (MatOfPoint region1 : regions) {
                if (inner2.contains(region1))
                    continue;
                Rect rc1 = Imgproc.boundingRect(region1);
                for (MatOfPoint region2 : regions) {
                    if (region1 == region2) // by ref
                        continue;
                    if (inner2.contains(region2))
                        continue;
                    Rect rc2 = Imgproc.boundingRect(region2);
                    if ((rc1.x > rc2.x) &&
                        (rc1.y > rc2.y) &&
                        (rc1.x+rc1.width  < rc2.x+rc2.width) &&
                        (rc1.y+rc1.height < rc2.y+rc2.height))
                    {
                        inner2.add(region1);
                        break;
                    }
                }
            }
            logger.trace("Inner regions count is {}", inner2.size());
            if (!inner2.isEmpty()) {
                inner = new ArrayList<>(inner2);
                regions.removeAll(inner2);
            }
        }

        if (params.showRegions) {
            logger.trace("Show mask: contours.size={}", regions.size());
            imageMat = params.invert
                    ? new Mat  (imageMat.size(), CvType.CV_8UC1, WHITE)
                    : Mat.zeros(imageMat.size(), CvType.CV_8UC1);
            Imgproc.drawContours(
                    imageMat,        // Mat image
                    regions,         // List<MatOfPoint> contours
                    -1,              // int contourIdx
                    params.invert    // Scalar color
                        ? BLACK : WHITE,
                    Imgproc.FILLED); // int thickness
            if (!inner.isEmpty())
                Imgproc.drawContours(
                        imageMat,        // Mat image
                        inner,           // List<MatOfPoint> contours
                        -1,              // int contourIdx
                        params.invert    // Scalar color
                            ? WHITE : BLACK,
                        Imgproc.FILLED); // int thickness
        }

        if (params.markChars || params.markWords || params.markLines)
            imageMat = OpenCvHelper.to3Channel(imageMat);


        logger.trace("Draw mask");
        Mat maskChars = Mat.zeros(imageMat.size(), CvType.CV_8UC1);
        Mat maskWords = Mat.zeros(imageMat.size(), CvType.CV_8UC1);

        logger.trace("Collect symbols");
        List<Rect> allSymbols = new ArrayList<>(regions.size());
        List<Rect> innerSymbols = new ArrayList<>(inner.size());
        for (MatOfPoint contour : regions) {
            Rect rc = Imgproc.boundingRect(contour);
            allSymbols.add(rc);

            Mat roi = new Mat(maskChars, rc);
            roi.setTo(WHITE);
        }
        for (MatOfPoint contour : inner) {
            Rect rc = Imgproc.boundingRect(contour);
            innerSymbols.add(rc);
        }

        logger.trace("Collect words");
        List<Rect> allWords = new ArrayList<>();
        mark(maskChars, (int)(params.maxSymbol.width * 0.35), 1, rc -> {
            allWords.add(rc);

            Mat roi = new Mat(maskWords, rc);
            roi.setTo(WHITE);
        });


        logger.trace("Collect lines");
        List<Rect> allLines = new ArrayList<>();
        mark(maskWords, (int)(params.maxSymbol.width * 0.9), 2, allLines::add);


        class SymbolTmp {
            final Rect position;
            boolean handled = false;
            SymbolTmp(Rect position) { this.position = position; }
        }
        class WordTmp {
            final Rect position;
            List<SymbolTmp> symbols = new ArrayList<>();
            boolean handled = false;
            WordTmp(Rect position) { this.position = position; }
        }
        class LineTmp {
            final Rect position;
            List<WordTmp> words = new ArrayList<>();
            LineTmp(Rect position) { this.position = position; }
        }

        List<SymbolTmp> allTmpSymbols = allSymbols.stream()
                .map(SymbolTmp::new)
                .collect(Collectors.toList());
        List<WordTmp> allTmpWords = allWords.stream()
                .sorted((a, b) -> Integer.compare(a.width * a.height,
                                                  b.width * b.height))
                .map(WordTmp::new)
                .collect(Collectors.toList());
        List<LineTmp> allTmpLines = allLines.stream()
                .sorted((a, b) -> Integer.compare(a.width * a.height,
                                                  b.width * b.height))
                .map(LineTmp::new)
                .collect(Collectors.toList());

        for (WordTmp wordItem : allTmpWords) {
            Rect rcWord = wordItem.position;
            for (SymbolTmp symbolItem : allTmpSymbols) {
                if (symbolItem.handled)
                    continue;

                Rect rcSymbol = symbolItem.position;
                if (GeomHelper.isIntersected(rcWord, rcSymbol)) {
                    wordItem.symbols.add(symbolItem);
                    symbolItem.handled = true;
                }
            }
        }
        for (LineTmp lineItem : allTmpLines) {
            boolean any = false;
            Rect rcLine = lineItem.position;
            for (WordTmp wordItem : allTmpWords) {
                if (wordItem.handled)
                    continue;

                Rect rcWord = wordItem.position;
                if (GeomHelper.isIntersected(rcLine, rcWord)) {
                    lineItem.words.add(wordItem);
                    wordItem.handled = true;
                    any = true;
                }
            }
            if (!any)
                logger.error("Bad algorithm - line is failed");
        }
        for (WordTmp wordTmp : allTmpWords)
            if (!wordTmp.handled)
                logger.error("Bad algorithm - word is failed");
        for (SymbolTmp symbolTmp : allTmpSymbols)
            if (!symbolTmp.handled)
                logger.error("Bad algorithm - symbol is failed");


        if (params.mergeSymboVertical) {
            logger.trace("Merge symbol area vertically");
            for (LineTmp lineItem : allTmpLines) {
                for (WordTmp wordItem : lineItem.words) {
                    maskChars.setTo(BLACK);
                    for (SymbolTmp symbolItem : wordItem.symbols) {
                        Mat roi = new Mat(maskChars, symbolItem.position);
                        roi.setTo(WHITE);
                    }
                    List<Rect> rebuildSymbols = new ArrayList<>();
                    mark(maskChars, 1, params.minSymbol.height, rebuildSymbols::add);
                    wordItem.symbols = rebuildSymbols.stream()
                            .map(SymbolTmp::new)
                            .collect(Collectors.toList());
                }
            }
        }

        if (params.fitSymbolHeight) {
            logger.trace("Fit symbol height to word height");
            for (LineTmp lineItem : allTmpLines) {
                for (WordTmp wordItem : lineItem.words) {
                    wordItem.symbols = wordItem.symbols
                            .stream()
                            .map(symbol ->
                                new Rect(symbol.position.x,
                                         wordItem.position.y,
                                         symbol.position.width,
                                         wordItem.position.height))
                            .map(SymbolTmp::new)
                            .collect(Collectors.toList());
                }
            }
        }


        if (params.markLines) {
            logger.trace("Mark lines");
            for (LineTmp lineItem : allTmpLines) {
                Rect rc = lineItem.position;
                Imgproc.rectangle(imageMat, rc.br(), rc.tl(), LINE_COLOR, 3);
            }
        }
        if (params.markWords) {
            logger.trace("Mark words");
            for (LineTmp lineItem : allTmpLines) {
                for (WordTmp wordItem : lineItem.words) {
                    Rect rc = wordItem.position;
                    Imgproc.rectangle(imageMat, rc.br(), rc.tl(), WORD_COLOR, 2);
                }
            }
        }
        if (params.markChars) {
            logger.trace("Mark chars");
            for (LineTmp lineItem : allTmpLines) {
                for (WordTmp wordItem : lineItem.words) {
                    for (SymbolTmp symbolItem : wordItem.symbols) {
                        Rect rc = symbolItem.position;
                        //Imgproc.rectangle(imageMat, rc.br(), rc.tl(), SYMBOL_COLOR, 1);
                        if ((params.stuckSymbols == 1) || (rc.width < params.maxSymbol.width)) {
                            Imgproc.rectangle(imageMat, rc.br(), rc.tl(), SYMBOL_COLOR);
                        } else {
                            int cnt = (int)Math.round((rc.width / (params.maxSymbol.width * 0.7)) + 0.5);
                            int w = rc.width / cnt;
                            for (int i = 0; i < cnt; ++i) {
                                Rect rc2 = new Rect(rc.x + i * w, rc.y, w, rc.height);
                                Imgproc.rectangle(imageMat, rc2.br(), rc2.tl(), SYMBOL_COLOR);
                            }
                        }
                    }
                }
            }
            for (MatOfPoint contour : inner) {
                Rect rc = Imgproc.boundingRect(contour);
                Imgproc.rectangle(imageMat, rc.br(), rc.tl(), INNER_COLOR, 1);
            }
        }
    }

    private Mat mark(Mat mask, int dilateX, int dilateY, Consumer<Rect> marker) {
        Mat morphology = new Mat();
        Mat kernel = new Mat(dilateY, dilateX, CvType.CV_8UC1, WHITE);
        Imgproc.morphologyEx(mask, morphology, Imgproc.MORPH_DILATE, kernel);

        List<MatOfPoint> contours = new ArrayList<>();
//        int imgsize = imageMat.height() * imageMat.width();
        Imgproc.findContours(
                morphology,
                contours,
                new Mat(), // hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_NONE);
//        Scalar zeos = new Scalar(0, 0, 0);
        for (MatOfPoint element : contours) {
            Rect rc = Imgproc.boundingRect(element);
            if (rc.width > dilateX) {
                rc.x += (dilateX / 2.0) - 0.5;
                rc.width -= dilateX - 1;
            }
            if (rc.height > dilateY) {
                rc.y += (dilateY / 2.0) - 0.5;
                rc.height -= dilateY - 1;
            }
//            if ((rc.area() > 0.5 * imgsize) || (rc.area() < 100) || (rc.width / rc.height < 2)) {
////                Mat roi = new Mat(morphology, rectan3);
////                roi.setTo(zeos);
//            } else {
                marker.accept(rc);
//            }
        }

        return morphology;
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
//        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderIntModel    modelDelta         = new    SliderIntModel(params.delta           , 0, MIN_DELTA         , MAX_DELTA);
        SliderIntModel    modelMinArea       = new    SliderIntModel(params.minArea         , 0, MIN_MIN_AREA      , MAX_MIN_AREA);
        SliderIntModel    modelMaxArea       = new    SliderIntModel(params.maxArea         , 0, MIN_MAX_AREA      , MAX_MAX_AREA);
        SliderDoubleModel modelMaxVariation  = new SliderDoubleModel(params.maxVariation    , 0, MIN_MAX_VARIATION , MAX_MAX_VARIATION);
        SliderDoubleModel modelMinDiversity  = new SliderDoubleModel(params.minDiversity    , 0, MIN_MIN_DIVERSITY , MAX_MIN_DIVERSITY);
        SliderIntModel    modelMaxEvolution  = new    SliderIntModel(params.maxEvolution    , 0, MIN_MAX_EVOLUTION , MAX_MAX_EVOLUTION);
        SliderDoubleModel modelAreaThreshold = new SliderDoubleModel(params.areaThreshold   , 0, MIN_AREA_THRESHOLD, MAX_AREA_THRESHOLD);
        SliderDoubleModel modelMinMargin     = new SliderDoubleModel(params.minMargin       , 0, MIN_MIN_MARGIN    , MAX_MIN_MARGIN, 3);
        SliderIntModel    modelEdgeBlurSize  = new    SliderIntModel(params.edgeBlurSize    , 0, MIN_EDGE_BLUR_SIZE, MAX_EDGE_BLUR_SIZE);
        SliderIntModel    modelMinSymbolW    = new    SliderIntModel(params.minSymbol.width , 0, MIN_MIN_SYMBOL_W  , MAX_MIN_SYMBOL_W);
        SliderIntModel    modelMinSymbolH    = new    SliderIntModel(params.minSymbol.height, 0, MIN_MIN_SYMBOL_H  , MAX_MIN_SYMBOL_H);
        SliderIntModel    modelMaxSymbolW    = new    SliderIntModel(params.maxSymbol.width , 0, MIN_MAX_SYMBOL_W  , MAX_MAX_SYMBOL_W);
        SliderIntModel    modelMaxSymbolH    = new    SliderIntModel(params.maxSymbol.height, 0, MIN_MAX_SYMBOL_H  , MAX_MAX_SYMBOL_H);
        SliderIntModel    modelSymbolsStuck  = new    SliderIntModel(params.stuckSymbols    , 0, 1                 , MAX_SYMBOLS_STUCK);


        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(makeSliderVert(modelDelta, "Delta", "it compares (size<i>−size<i−delta>)/size<i−delta>"));
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(makeMinMax(modelMinArea,                                 // modelMin
                                   modelMaxArea,                                 // modelMax
                                   "Area",                                       // borderTitle
                                   "Prune the area",                             // tip
                                   "Prune the area which smaller than minArea",  // tipMin
                                   "Prune the area which bigger than maxArea")); // tipMax
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(makeSliderVert(modelMaxVariation, "Variation", "MAX Variation: Prune the area have similar size to its children"));
        box4Sliders.add(Box.createHorizontalGlue());

        Box box4Sliders2 = Box.createHorizontalBox();
        box4Sliders2.add(Box.createHorizontalGlue());
        box4Sliders2.add(makeSliderVert(modelMinDiversity, "Diver", "minDiversity: trace back to cut off mser with diversity less than min_diversity"));
        box4Sliders2.add(Box.createHorizontalStrut(2));
        box4Sliders2.add(makeSliderVert(modelMaxEvolution, "Evol", "maxEvolution: the evolution steps"));
        box4Sliders2.add(Box.createHorizontalStrut(2));
        box4Sliders2.add(makeSliderVert(modelAreaThreshold, "Thresh", "areaThreshold: the area threshold to cause re-initialize"));
        box4Sliders2.add(Box.createHorizontalStrut(2));
        box4Sliders2.add(makeSliderVert(modelMinMargin, "Margin", "minMargin: ignore too small margin"));
        box4Sliders2.add(Box.createHorizontalStrut(2));
        box4Sliders2.add(makeSliderVert(modelEdgeBlurSize, "Blur", "edgeBlurSize: the aperture size for edge blur"));
        box4Sliders2.add(Box.createHorizontalGlue());

        Box box4Sliders3 = Box.createHorizontalBox();
        box4Sliders3.add(Box.createHorizontalGlue());
        box4Sliders3.add(makeContourLimits(
                          modelMinSymbolW, modelMinSymbolH,
                          modelMaxSymbolW, modelMaxSymbolH,
                          "Symbol", "Symbol limits",
                          "MinSymbol", "MaxSymbol",
                          "Additional restrictions on the minimum symbol size",
                          "Additional restrictions on the maximum symbol size"));
        box4Sliders3.add(Box.createHorizontalGlue());
        ////////////
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout());
        Container cntrlStuckW = makeEditBox("params.stuckSymbols", modelSymbolsStuck, "Stuck symbols", null, "the number of characters stuck together");
        panel3.add(box4Sliders3, BorderLayout.CENTER);
        panel3.add(cntrlStuckW, BorderLayout.SOUTH);
        ///////////////

        JTabbedPane tabPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPane.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        tabPane.addTab("Common", null, box4Sliders, null);
        tabPane.addTab("For color image", null, box4Sliders2, null);
        tabPane.addTab("Symbol limits", null, panel3, "Additional restrictions on symbol size");

        Box boxRegions = Box.createHorizontalBox();
        boxRegions.add(Box.createHorizontalStrut(7));
        JCheckBox boxInner = makeCheckBox(
                () -> params.showInner,     // getter
                v  -> params.showInner = v, // setter
                "Inner?",                   // title
                "params.showInner",         // paramName
                "Show inner contours",      // tip
                null);                      // customListener
        JCheckBox boxInvert = makeCheckBox(
                () -> params.invert,        // getter
                v  -> params.invert = v,    // setter
                "Invert",                   // title
                "params.invert",            // paramName
                null,                       // tip
                null);                      // customListener
        boxRegions.add(makeCheckBox(
                () -> params.showRegions,       // getter
                v  -> params.showRegions = v,   // setter
                "Show regions",                 // title
                "params.showRegions",           // paramName
                null,                           // tip
                () -> {                         // customListener
                    boxInner .setEnabled(params.showRegions);
                    boxInvert.setEnabled(params.showRegions);
                }));
        boxRegions.add(Box.createHorizontalStrut(7));
        boxRegions.add(boxInner);
        boxRegions.add(Box.createHorizontalStrut(7));
        boxRegions.add(boxInvert);
        boxRegions.add(Box.createHorizontalGlue());

        Box boxChars = Box.createHorizontalBox();
        boxChars.add(Box.createHorizontalStrut(7));
        JCheckBox boxMergeVertical = makeCheckBox(
                  () -> params.mergeSymboVertical,     // getter
                  v  -> params.mergeSymboVertical = v, // setter
                  "Merge vertical",                    // title
                  "params.mergeSymboVertical",         // paramName
                  "Merge symbol area vertically",      // tip
                  null);                               // customListener
        JCheckBox boxFitHeight = makeCheckBox(
                  () -> params.fitSymbolHeight,        // getter
                  v  -> params.fitSymbolHeight = v,    // setter
                  "Fit height",                        // title
                  "params.fitSymbolHeight",            // paramName
                  "Fit symbol height to word height",  // tip
                  null);                               // customListener
        boxChars.add(makeCheckBox(
                () -> params.markChars,      // getter
                v  -> params.markChars = v,  // setter
                "Mark chars",                // title
                "params.markChars",          // paramName
                null,                        // tip
                () -> {                      // customListener
                    boxMergeVertical.setEnabled(params.markChars);
                    boxFitHeight    .setEnabled(params.markChars);
                }));                      // customListener
        boxChars.add(Box.createHorizontalStrut(7));
        boxChars.add(boxMergeVertical);
        boxChars.add(Box.createHorizontalStrut(7));
        boxChars.add(boxFitHeight);
        boxChars.add(Box.createHorizontalGlue());

        Box boxWords = Box.createHorizontalBox();
        boxWords.add(Box.createHorizontalStrut(7));
        boxWords.add(makeCheckBox(
                () -> params.markWords,      // getter
                v  -> params.markWords = v,  // setter
                "Mark words",                // title
                "params.markWords",          // paramName
                null,                        // tip
                null));                      // customListener
        boxWords.add(Box.createHorizontalGlue());

        Box boxLines = Box.createHorizontalBox();
        boxLines.add(Box.createHorizontalStrut(7));
        boxLines.add(makeCheckBox(
                () -> params.markLines,      // getter
                v  -> params.markLines = v,  // setter
                "Mark lines",                // title
                "params.markLines",          // paramName
                null,                        // tip
                null));                      // customListener
        boxLines.add(Box.createHorizontalGlue());

        Box boxDown = Box.createVerticalBox();
        boxDown.setBorder(BorderFactory.createTitledBorder(""));
        boxDown.add(Box.createVerticalGlue());
        boxDown.add(boxRegions);
        boxDown.add(Box.createVerticalStrut(2));
        boxDown.add(boxChars);
        boxDown.add(Box.createVerticalStrut(2));
        boxDown.add(boxWords);
        boxDown.add(Box.createVerticalStrut(2));
        boxDown.add(boxLines);
        boxDown.add(Box.createVerticalGlue());


        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new BorderLayout());
        panelOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
        panelOptions.add(tabPane, BorderLayout.CENTER);
        panelOptions.add(boxDown, BorderLayout.SOUTH);

        box4Options.add(panelOptions);

        addChangeListener("params.delta"           , modelDelta        , v -> params.delta            = v);
        addChangeListener("params.minArea"         , modelMinArea      , v -> params.minArea          = v);
        addChangeListener("params.maxArea"         , modelMaxArea      , v -> params.maxArea          = v);
        addChangeListener("params.maxVariation"    , modelMaxVariation , v -> params.maxVariation     = v);
        addChangeListener("params.minDiversity"    , modelMinDiversity , v -> params.minDiversity     = v);
        addChangeListener("params.maxEvolution"    , modelMaxEvolution , v -> params.maxEvolution     = v);
        addChangeListener("params.areaThreshold"   , modelAreaThreshold, v -> params.areaThreshold    = v);
        addChangeListener("params.minMargin"       , modelMinMargin    , v -> params.minMargin        = v);
      //addChangeListener("params.edgeBlurSize"    , modelEdgeBlurSize , v -> params.edgeBlurSize     = v);
        addChangeListener("params.minSymbol.width" , modelMinSymbolW   , v -> params.minSymbol.width  = v);
        addChangeListener("params.minSymbol.height", modelMinSymbolH   , v -> params.minSymbol.height = v);
        addChangeListener("params.maxSymbol.width" , modelMaxSymbolW   , v -> params.maxSymbol.width  = v);
        addChangeListener("params.maxSymbol.height", modelMaxSymbolH   , v -> params.maxSymbol.height = v);
        addChangeListener("params.stuckSymbols"    , modelSymbolsStuck , v -> params.stuckSymbols     = v);
        modelEdgeBlurSize.getWrapped().addChangeListener(ev -> {
            logger.trace("modelEdgeBlurSize: value={}", modelEdgeBlurSize.getFormatedText());
            int val = modelEdgeBlurSize.getValue();
            int valValid = MserTabParams.onlyOdd(val, params.edgeBlurSize);
            if (val == valValid) {
                params.edgeBlurSize = valValid;
                invalidateAsync();
            } else {
                SwingUtilities.invokeLater(() -> modelEdgeBlurSize.setValue(valValid));
            }
        });

        return box4Options;
    }

    @Override
    public MserTabParams getParams() {
        return params;
    }

}
