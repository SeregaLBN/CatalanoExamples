package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private static final int    MIN_MIN_LINE_HEIGHT  = MIN_MIN_SYMBOL_H;
    private static final int    MAX_MIN_LINE_HEIGHT  = MAX_MAX_SYMBOL_H;
    private static final int    MAX_SYMBOLS_STUCK = 10;

    private static final int NUMBER_OF_BROKEN_VERTICAL_PARTS = 4;

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
        final int minLineHeight = params.minLineHeight;
        final int maxLineHeight = params.maxSymbol.height;//(int)(params.maxSymbol.height * 1.5);

        MSER mser = MSER.create(
            params.delta,

            // filter #1 by area
            params.minSymbol.width * params.minSymbol.height / (params.mergeSymboVertical ? NUMBER_OF_BROKEN_VERTICAL_PARTS : 1),
            params.maxSymbol.width * params.maxSymbol.height * params.stuckSymbols,

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

        { // filter #2 by size (width and height)
            List<Integer> ignored = new ArrayList<>();
            for (int i=0; i < regions.size(); ++i) {
                MatOfPoint contour = regions.get(i);
                Rect rc = Imgproc.boundingRect(contour);
                if ((rc.width  < params.minSymbol.width ) ||
                    (rc.height <(params.minSymbol.height / (params.mergeSymboVertical ? NUMBER_OF_BROKEN_VERTICAL_PARTS : 1))) ||
                    (rc.width  >(params.maxSymbol.width  *  params.stuckSymbols)) ||
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

        class InnerTmp {
            final Rect position;
            final MatOfPoint contour;
            InnerTmp(MatOfPoint contour, Rect pos) { this.contour = contour; this.position = pos; }
        }
        class SymbolTmp {
            final Rect position;
            final List<MatOfPoint> contours;
            List<InnerTmp> inners = new ArrayList<>();
            boolean handled = false;
            SymbolTmp(MatOfPoint contour) { this.contours = new ArrayList<>();
                                            this.contours.add(contour);
                                            this.position = Imgproc.boundingRect(contour); }
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

        logger.trace("Collect symbols");
        List<SymbolTmp> allSymbols = regions
                    .stream()
                    .map(SymbolTmp::new)
                    .collect(Collectors.toList());
        regions = null; // do not use again

        logger.trace("Find inner regions");
        for (SymbolTmp symbol : allSymbols) {
            if (symbol.handled)
                continue;
            for (SymbolTmp symbol2 : allSymbols) {
                if (symbol == symbol2) // by ref
                    continue;
                if (symbol2.handled)
                    continue;
                Rect rc1 = symbol.position;
                Rect rc2 = symbol2.position;
                if ((rc1.x < rc2.x) &&
                    (rc1.y < rc2.y) &&
                    (rc1.x+rc1.width  > rc2.x+rc2.width) &&
                    (rc1.y+rc1.height > rc2.y+rc2.height))
                {
                    symbol2.handled = true; // mark is inner
                    symbol.inners.add(new InnerTmp(symbol2.contours.get(0), symbol2.position));
                    symbol.inners.addAll(symbol2.inners);
                }
            }
        }
        allSymbols = allSymbols.stream()
                .filter(s -> !s.handled) // only own regions
                .collect(Collectors.toList());
        allSymbols.forEach(s -> s.handled = false);


        logger.trace("Draw symbols masks");
        Mat maskChars = Mat.zeros(imageMat.size(), CvType.CV_8UC1);
        for (SymbolTmp symbol : allSymbols) {
            Mat roi = new Mat(maskChars, symbol.position);
            roi.setTo(WHITE);
        }

        logger.trace("Collect word regions");
        List<WordTmp> allWords = mark(maskChars, (int)(params.maxSymbol.width * 0.35), 1)
                .stream()
                .sorted((rc1, rc2) -> Integer.compare(rc1.width * rc1.height,
                                                      rc2.width * rc2.height))
                .map(WordTmp::new)
                .collect(Collectors.toList());

        logger.trace("Draw words masks");
        Mat maskWords = Mat.zeros(imageMat.size(), CvType.CV_8UC1);
        allWords.forEach(w -> {
            Mat roi = new Mat(maskWords, w.position);
            roi.setTo(WHITE);
        });

        logger.trace("Collect line regions");
        List<LineTmp> allLines = mark(maskWords, (int)(params.maxSymbol.width * 0.9), 2)
                .stream()
                .sorted((rc1, rc2) -> Integer.compare(rc1.width * rc1.height,
                                                      rc2.width * rc2.height))
                .map(LineTmp::new)
                .collect(Collectors.toList());

        logger.trace("Build words");
        for (WordTmp wordItem : allWords) {
            Rect rcWord = wordItem.position;
            for (SymbolTmp symbolItem : allSymbols) {
                if (symbolItem.handled)
                    continue;

                Rect rcSymbol = symbolItem.position;
                if (GeomHelper.isIntersected(rcWord, rcSymbol)) {
                    wordItem.symbols.add(symbolItem);
                    symbolItem.handled = true;
                }
            }
        }

        logger.trace("Build lines");
        for (LineTmp lineItem : allLines) {
            boolean any = false;
            Rect rcLine = lineItem.position;
            for (WordTmp wordItem : allWords) {
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
        for (WordTmp wordTmp : allWords)
            if (!wordTmp.handled)
                logger.error("Bad algorithm - word is failed");
        for (SymbolTmp symbolTmp : allSymbols)
            if (!symbolTmp.handled)
                logger.error("Bad algorithm - symbol is failed");

        allSymbols = null; // do not use again
        allWords = null; // do not use again
        allLines = allLines
                .stream()

                // filter #3 by height line
                .filter(l -> l.position.height >= minLineHeight)
                .filter(l -> l.position.height <= maxLineHeight)

                .collect(Collectors.toList());


        if (params.mergeSymboVertical) {
            logger.trace("Merge symbol area vertically");
            for (LineTmp lineItem : allLines) {
                for (WordTmp wordItem : lineItem.words) {
                    if (wordItem.symbols.size() == 1)
                        continue;

                    class HorizTmp {
                        int x;
                        int width;
                        HorizTmp(SymbolTmp s) { this.x = s.position.x; this.width = s.position.width; }
                        @Override
                        public boolean equals(Object obj) {
                            // magic
                            HorizTmp tmp = (HorizTmp)obj;
                            int left = Math.max(this.x, tmp.x);
                            int right = Math.min(x + width, tmp.x + tmp.width);
                            boolean res = (left < right); // if the line segments intersect
                            if (res) {
                                // yes, equals method update itself
                                left  = Math.min(this.x, tmp.x);
                                right = Math.max(x + width, tmp.x + tmp.width);
                                this.x = tmp.x = left;
                                this.width = tmp.width = right - left;
                            }
                            return res;
                        }
                    }
                    wordItem.symbols = wordItem.symbols.stream()
                        .collect(Collectors.groupingBy(HorizTmp::new))
                        .values()
                        .stream()
                        .map(sl -> sl.stream()
                                     .reduce((s1, s2) -> {
                                         s1.contours.addAll(s2.contours);
                                         s1.inners.addAll(s2.inners);
                                         Rect rc = GeomHelper.intersectInclude(s1.position, s2.position);
                                         s1.position.x      = rc.x;
                                         s1.position.y      = rc.y;
                                         s1.position.width  = rc.width;
                                         s1.position.height = rc.height;
                                         return s1;
                                     })
                                    .get())
                        .collect(Collectors.toList());
                }
            }
        }

        if (params.fitSymbolHeight) {
            logger.trace("Fit symbol height to word height");
            for (LineTmp lineItem : allLines)
                for (WordTmp wordItem : lineItem.words)
                    for (SymbolTmp symbolItem : wordItem.symbols) {
                        Rect rc = symbolItem.position;
                        rc.y = wordItem.position.y;
                        rc.height = wordItem.position.height;
                    }
        }


        // show results
        if (params.showRegions) {
            List<MatOfPoint> all = allLines.stream()
                    .flatMap(l -> l.words
                                   .stream()
                                   .flatMap(w -> w.symbols
                                                  .stream()
                                                  .flatMap(s -> s.contours.stream())))
                    .collect(Collectors.toList());

            logger.trace("Show mask: contours.size={}", all.size());
            imageMat = params.invert
                    ? new Mat  (imageMat.size(), CvType.CV_8UC1, WHITE)
                    : Mat.zeros(imageMat.size(), CvType.CV_8UC1);
            Imgproc.drawContours(
                    imageMat,        // Mat image
                    all,             // List<MatOfPoint> contours
                    -1,              // int contourIdx
                    params.invert    // Scalar color
                        ? BLACK : WHITE,
                    Imgproc.FILLED); // int thickness

            if (params.showInner) {
                List<MatOfPoint> inner = allLines.stream()
                        .flatMap(l -> l.words
                                       .stream()
                                       .flatMap(w -> w.symbols
                                                      .stream()
                                                      .flatMap(s -> s.inners
                                                                     .stream()
                                                                     .flatMap(i -> Stream.of(i.contour)))))
                        .collect(Collectors.toList());
                if (!inner.isEmpty())
                    Imgproc.drawContours(
                            imageMat,        // Mat image
                            inner,           // List<MatOfPoint> contours
                            -1,              // int contourIdx
                            params.invert    // Scalar color
                                ? BLACK : WHITE,
                            Imgproc.FILLED); // int thickness
            }
        }

        if (params.markChars || params.markWords || params.markLines)
            imageMat = OpenCvHelper.to3Channel(imageMat);

        if (params.markLines) {
            logger.trace("Mark lines");
            for (LineTmp lineItem : allLines) {
                Rect rc = lineItem.position;
                Imgproc.rectangle(imageMat, rc.br(), rc.tl(), LINE_COLOR, 3);
            }
        }
        if (params.markWords) {
            logger.trace("Mark words");
            for (LineTmp lineItem : allLines) {
                for (WordTmp wordItem : lineItem.words) {
                    Rect rc = wordItem.position;
                    Imgproc.rectangle(imageMat, rc.br(), rc.tl(), WORD_COLOR, 2);
                }
            }
        }
        if (params.markChars) {
            logger.trace("Mark chars");
            for (LineTmp lineItem : allLines) {
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
                        for (InnerTmp inner : symbolItem.inners)
                            Imgproc.rectangle(imageMat, inner.position.br(), inner.position.tl(), INNER_COLOR, 1);
                    }
                }
            }
        }
    }

    private List<Rect> mark(Mat mask, int dilateX, int dilateY) {
        Mat morphology = new Mat();
        Mat kernel = new Mat(dilateY, dilateX, CvType.CV_8UC1, WHITE);
        Imgproc.morphologyEx(mask, morphology, Imgproc.MORPH_DILATE, kernel);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(
                morphology,
                contours,
                new Mat(), // hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_NONE);

        List<Rect> res = new ArrayList<>(contours.size());
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
            res.add(rc);
        }

        return res;
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
//        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderIntModel    modelDelta         = new    SliderIntModel(params.delta           , 0, MIN_DELTA          , MAX_DELTA);
        SliderDoubleModel modelMaxVariation  = new SliderDoubleModel(params.maxVariation    , 0, MIN_MAX_VARIATION  , MAX_MAX_VARIATION);
        SliderDoubleModel modelMinDiversity  = new SliderDoubleModel(params.minDiversity    , 0, MIN_MIN_DIVERSITY  , MAX_MIN_DIVERSITY);
        SliderIntModel    modelMaxEvolution  = new    SliderIntModel(params.maxEvolution    , 0, MIN_MAX_EVOLUTION  , MAX_MAX_EVOLUTION);
        SliderDoubleModel modelAreaThreshold = new SliderDoubleModel(params.areaThreshold   , 0, MIN_AREA_THRESHOLD , MAX_AREA_THRESHOLD);
        SliderDoubleModel modelMinMargin     = new SliderDoubleModel(params.minMargin       , 0, MIN_MIN_MARGIN     , MAX_MIN_MARGIN, 3);
        SliderIntModel    modelEdgeBlurSize  = new    SliderIntModel(params.edgeBlurSize    , 0, MIN_EDGE_BLUR_SIZE , MAX_EDGE_BLUR_SIZE);
        SliderIntModel    modelMinSymbolW    = new    SliderIntModel(params.minSymbol.width , 0, MIN_MIN_SYMBOL_W   , MAX_MIN_SYMBOL_W);
        SliderIntModel    modelMinSymbolH    = new    SliderIntModel(params.minSymbol.height, 0, MIN_MIN_SYMBOL_H   , MAX_MIN_SYMBOL_H);
        SliderIntModel    modelMaxSymbolW    = new    SliderIntModel(params.maxSymbol.width , 0, MIN_MAX_SYMBOL_W   , MAX_MAX_SYMBOL_W);
        SliderIntModel    modelMaxSymbolH    = new    SliderIntModel(params.maxSymbol.height, 0, MIN_MAX_SYMBOL_H   , MAX_MAX_SYMBOL_H);
        SliderIntModel    modelMinLineHeight = new    SliderIntModel(params.minLineHeight   , 0, MIN_MIN_LINE_HEIGHT, MAX_MIN_LINE_HEIGHT);
        SliderIntModel    modelSymbolsStuck  = new    SliderIntModel(params.stuckSymbols    , 0, 1                  , MAX_SYMBOLS_STUCK);


        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(Box.createHorizontalGlue());
        Box box4Area = makeContourLimits(
                          modelMinSymbolW, modelMinSymbolH,
                          modelMaxSymbolW, modelMaxSymbolH,
                          "Symbol", "Symbol limits",
                          "MinSymbol", "MaxSymbol",
                          "Additional restrictions on the minimum symbol size",
                          "Additional restrictions on the maximum symbol size");
        box4Sliders.add(box4Area);
        box4Sliders.add(Box.createHorizontalStrut(2));

        Box boxLineH = Box.createHorizontalBox();
        boxLineH.setBorder(BorderFactory.createTitledBorder("Line"));
        boxLineH.setToolTipText("Line restrictions");
        boxLineH.add(Box.createHorizontalGlue());
        boxLineH.add(makeSliderVert(modelMinLineHeight, "MinHeight", "Minimum line height\n Maximum defined as MaxSymbol.Height"));
        boxLineH.add(Box.createHorizontalGlue());

        modelMinLineHeight.getWrapped().addChangeListener(ev -> {
            if (modelMinLineHeight.getValue() < modelMinSymbolH.getValue())
                modelMinSymbolH.setValue(modelMinLineHeight.getValue());
            if (modelMinLineHeight.getValue() > modelMaxSymbolH.getValue())
                modelMaxSymbolH.setValue(modelMinLineHeight.getValue());
        });
        modelMinSymbolH.getWrapped().addChangeListener(ev -> {
            if (modelMinSymbolH.getValue() > modelMinLineHeight.getValue())
                modelMinLineHeight.setValue(modelMinSymbolH.getValue());
        });
        modelMaxSymbolH.getWrapped().addChangeListener(ev -> {
            if (modelMaxSymbolH.getValue() < modelMinLineHeight.getValue())
                modelMinLineHeight.setValue(modelMaxSymbolH.getValue());
        });

        box4Sliders.add(boxLineH);
        box4Sliders.add(Box.createHorizontalGlue());
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout());
        Container cntrlStuckW = makeEditBox("params.stuckSymbols", modelSymbolsStuck, "Stuck symbols", null, "the number of characters stuck together");
        panel3.add(box4Sliders, BorderLayout.CENTER);
        panel3.add(cntrlStuckW, BorderLayout.SOUTH);

        Box box4Sliders2 = Box.createHorizontalBox();
        box4Sliders2.add(Box.createHorizontalGlue());
        box4Sliders2.add(makeSliderVert(modelDelta, "Delta", "it compares (size<i>−size<i−delta>)/size<i−delta>"));
        box4Sliders2.add(Box.createHorizontalStrut(2));
        box4Sliders2.add(makeSliderVert(modelMaxVariation, "Variation", "MAX Variation: Prune the area have similar size to its children"));
        box4Sliders2.add(Box.createHorizontalGlue());

        Box box4Sliders3 = Box.createHorizontalBox();
        box4Sliders3.add(Box.createHorizontalGlue());
        box4Sliders3.add(makeSliderVert(modelMinDiversity, "Diver", "minDiversity: trace back to cut off mser with diversity less than min_diversity"));
        box4Sliders3.add(Box.createHorizontalStrut(2));
        box4Sliders3.add(makeSliderVert(modelMaxEvolution, "Evol", "maxEvolution: the evolution steps"));
        box4Sliders3.add(Box.createHorizontalStrut(2));
        box4Sliders3.add(makeSliderVert(modelAreaThreshold, "Thresh", "areaThreshold: the area threshold to cause re-initialize"));
        box4Sliders3.add(Box.createHorizontalStrut(2));
        box4Sliders3.add(makeSliderVert(modelMinMargin, "Margin", "minMargin: ignore too small margin"));
        box4Sliders3.add(Box.createHorizontalStrut(2));
        box4Sliders3.add(makeSliderVert(modelEdgeBlurSize, "Blur", "edgeBlurSize: the aperture size for edge blur"));
        box4Sliders3.add(Box.createHorizontalGlue());

        JTabbedPane tabPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPane.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        tabPane.addTab("Area limits", null, panel3, "Additional restrictions on area size");
        tabPane.addTab("Other", null, box4Sliders2, null);
        tabPane.addTab("For color image", null, box4Sliders3, null);

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
        addChangeListener("params.minLineHeight"   , modelMinLineHeight, v -> params.minLineHeight    = v);
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
