package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.opencv.core.*;
import org.opencv.features2d.MSER;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.MserTabParams;
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

    private static final Scalar WHITE = new Scalar(255);
    private static final Scalar GREEN         = new Scalar(0x00, 0xFF, 0x00);
    private static final Scalar MAGENTA       = new Scalar(0xFF, 0x00, 0xFF);

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
            params.minArea.width * params.minArea.height,
            params.maxArea.width * params.maxArea.height,
            params.maxVariation,
            params.minDiversity,
            params.maxEvolution,
            params.areaThreshold,
            params.minMargin,
            params.edgeBlurSize);

        List<MatOfPoint> msers = new ArrayList<>(); // resulting list of point sets
        mser.detectRegions(imageMat,
                           msers,
                           new MatOfRect()); // resulting bounding boxes

        { // filter
            List<Integer> ignored = new ArrayList<>();
            for (int i=0; i < msers.size(); ++i) {
                MatOfPoint contour = msers.get(i);
                Rect rc = Imgproc.boundingRect(contour);
                if ((rc.width  < params.minArea.width ) ||
                    (rc.height < params.minArea.height) ||
                    (rc.width  > params.maxArea.width) ||
                    (rc.height > params.maxArea.width))
                {
                    ignored.add(i);
                }
            }
            logger.trace("Ignored contours indexes size: {}", ignored.size());
            for (int i = ignored.size() - 1; i >= 0; --i) {
                int delIndex = ignored.get(i);
                msers.remove(delIndex);
            }
        }

        Mat mask = Mat.zeros(imageMat.size(), CvType.CV_8UC1);

        imageMat = OpenCvHelper.to3Channel(imageMat);

        for (MatOfPoint contour : msers) {
            Rect rc = Imgproc.boundingRect(contour);
            Mat roi = new Mat(mask, rc);
            roi.setTo(WHITE);

            // mark single char
            Imgproc.rectangle(imageMat, rc.br(), rc.tl(), GREEN);
        }

        // mark word
        Mat morbyte = new Mat();
        Mat kernel = new Mat(1, 50, CvType.CV_8UC1, Scalar.all(255));
        Imgproc.morphologyEx(mask, morbyte, Imgproc.MORPH_DILATE, kernel);
        List<MatOfPoint> contour2 = new ArrayList<>();
        Mat hierarchy = new Mat();
        int imgsize = imageMat.height() * imageMat.width();
        Imgproc.findContours(morbyte, contour2, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
//        Scalar zeos = new Scalar(0, 0, 0);
        for (MatOfPoint element : contour2) {
            Rect rectan3 = Imgproc.boundingRect(element);
            if ((rectan3.area() > 0.5 * imgsize) || (rectan3.area() < 100) || (rectan3.width / rectan3.height < 2)) {
//                Mat roi = new Mat(morbyte, rectan3);
//                roi.setTo(zeos);
            } else {
                Imgproc.rectangle(imageMat, rectan3.br(), rectan3.tl(), MAGENTA);
            }
        }



        imageMat = mask;
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
//        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderIntModel    modelDelta         = new    SliderIntModel(params.delta         , 0, MIN_DELTA         , MAX_DELTA);
        SliderIntModel    modelMinAreaW      = new    SliderIntModel(params.minArea.width , 0, MIN_MIN_AREA      , MAX_MIN_AREA);
        SliderIntModel    modelMinAreaH      = new    SliderIntModel(params.minArea.height, 0, MIN_MIN_AREA      , MAX_MIN_AREA);
        SliderIntModel    modelMaxAreaW      = new    SliderIntModel(params.maxArea.width , 0, MIN_MAX_AREA      , MAX_MAX_AREA);
        SliderIntModel    modelMaxAreaH      = new    SliderIntModel(params.maxArea.height, 0, MIN_MAX_AREA      , MAX_MAX_AREA);
        SliderDoubleModel modelMaxVariation  = new SliderDoubleModel(params.maxVariation  , 0, MIN_MAX_VARIATION , MAX_MAX_VARIATION);
        SliderDoubleModel modelMinDiversity  = new SliderDoubleModel(params.minDiversity  , 0, MIN_MIN_DIVERSITY , MAX_MIN_DIVERSITY);
        SliderIntModel    modelMaxEvolution  = new    SliderIntModel(params.maxEvolution  , 0, MIN_MAX_EVOLUTION , MAX_MAX_EVOLUTION);
        SliderDoubleModel modelAreaThreshold = new SliderDoubleModel(params.areaThreshold , 0, MIN_AREA_THRESHOLD, MAX_AREA_THRESHOLD);
        SliderDoubleModel modelMinMargin     = new SliderDoubleModel(params.minMargin     , 0, MIN_MIN_MARGIN    , MAX_MIN_MARGIN, 3);
        SliderIntModel    modelEdgeBlurSize  = new    SliderIntModel(params.edgeBlurSize  , 0, MIN_EDGE_BLUR_SIZE, MAX_EDGE_BLUR_SIZE);

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(makeSliderVert(modelDelta, "Delta", "it compares (size<i>−size<i−delta>)/size<i−delta>"));
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(makeContourLimits(
                          modelMinAreaW, modelMinAreaH,
                          modelMaxAreaW, modelMaxAreaH,
                          "Area", "Prune the area",
                          "MinArea", "MaxArea",
                          "Prune the area which smaller than minArea", "Prune the area which bigger than maxArea"));
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

        JTabbedPane tabPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPane.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        tabPane.addTab("Common", null, box4Sliders, null);
        tabPane.addTab("For color image", null, box4Sliders2, null);

        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new BorderLayout());
        panelOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
        panelOptions.add(tabPane, BorderLayout.CENTER);
//        panelOptions.add(box4colorImage, BorderLayout.SOUTH);

        box4Options.add(panelOptions);

        addChangeListener("params.delta"         , modelDelta        , v -> params.delta          = v);
        addChangeListener("params.minArea.width" , modelMinAreaW     , v -> params.minArea.width  = v);
        addChangeListener("params.minArea.height", modelMinAreaH     , v -> params.minArea.height = v);
        addChangeListener("params.maxArea.width" , modelMaxAreaW     , v -> params.maxArea.width  = v);
        addChangeListener("params.maxArea.height", modelMaxAreaH     , v -> params.maxArea.height = v);
        addChangeListener("params.maxVariation"  , modelMaxVariation , v -> params.maxVariation   = v);
        addChangeListener("params.minDiversity"  , modelMinDiversity , v -> params.minDiversity   = v);
        addChangeListener("params.maxEvolution"  , modelMaxEvolution , v -> params.maxEvolution   = v);
        addChangeListener("params.areaThreshold" , modelAreaThreshold, v -> params.areaThreshold  = v);
        addChangeListener("params.minMargin"     , modelMinMargin    , v -> params.minMargin      = v);
      //addChangeListener("params.edgeBlurSize"  , modelEdgeBlurSize , v -> params.edgeBlurSize   = v);
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
