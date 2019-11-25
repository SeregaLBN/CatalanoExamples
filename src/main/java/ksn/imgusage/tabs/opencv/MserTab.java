package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.opencv.core.*;
import org.opencv.features2d.MSER;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.MserTabParams;

/** <a href='https://docs.opencv.org/3.4.2/d3/d28/classcv_1_1MSER.html'>Maximally stable extremal region extractor</a> */
public class MserTab extends OpencvFilterTab<MserTabParams> {

    public static final String TAB_TITLE = "MSER";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Maximally stable extremal region extractor";

    public  static final int    MIN_DELTA = 1;
    private static final int    MAX_DELTA = 150;
    private static final int    MIN_MIN_AREA =   1;
    private static final int    MAX_MIN_AREA = 120000;
    private static final int    MIN_MAX_AREA =   2;
    private static final int    MAX_MAX_AREA = 120000;
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

    private static final Scalar CONTOUR_COLOR = new Scalar(255);

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

        List<MatOfPoint> msers = new ArrayList<>(); // resulting list of point sets
        MatOfRect bboxes = new MatOfRect(); // resulting bounding boxes
        mser.detectRegions(imageMat, msers, bboxes);

        List<Rect> rcBoxes = bboxes.toList();

        Mat mask = Mat.zeros(imageMat.size(), CvType.CV_8UC1);
        for (Rect rect : rcBoxes) {
            Mat roi = new Mat(mask, rect);
            roi.setTo(CONTOUR_COLOR);
        }

        imageMat = mask;
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
//        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderIntModel    modelDelta         = new    SliderIntModel(params.delta        , 0, MIN_DELTA         , MAX_DELTA);
        SliderIntModel    modelMinArea       = new    SliderIntModel(params.minArea      , 0, MIN_MIN_AREA      , MAX_MIN_AREA);
        SliderIntModel    modelMaxArea       = new    SliderIntModel(params.maxArea      , 0, MIN_MAX_AREA      , MAX_MAX_AREA);
        SliderDoubleModel modelMaxVariation  = new SliderDoubleModel(params.maxVariation , 0, MIN_MAX_VARIATION , MAX_MAX_VARIATION);
        SliderDoubleModel modelMinDiversity  = new SliderDoubleModel(params.minDiversity , 0, MIN_MIN_DIVERSITY , MAX_MIN_DIVERSITY);
        SliderIntModel    modelMaxEvolution  = new    SliderIntModel(params.maxEvolution , 0, MIN_MAX_EVOLUTION , MAX_MAX_EVOLUTION);
        SliderDoubleModel modelAreaThreshold = new SliderDoubleModel(params.areaThreshold, 0, MIN_AREA_THRESHOLD, MAX_AREA_THRESHOLD);
        SliderDoubleModel modelMinMargin     = new SliderDoubleModel(params.minMargin    , 0, MIN_MIN_MARGIN    , MAX_MIN_MARGIN, 3);
        SliderIntModel    modelEdgeBlurSize  = new    SliderIntModel(params.edgeBlurSize , 0, MIN_EDGE_BLUR_SIZE, MAX_EDGE_BLUR_SIZE);


        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(makeSliderVert(modelDelta, "Delta", "it compares (size<i>−size<i−delta>)/size<i−delta>"));
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(makeMinMax(modelMinArea, modelMaxArea, "Area", "Prune the area", "Prune the area which smaller than minArea", "Prune the area which bigger than maxArea"));
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
        Box box4colorImage = Box.createVerticalBox();
        box4colorImage.setBorder(BorderFactory.createTitledBorder("For color image"));
        box4colorImage.add(box4Sliders2);


        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new BorderLayout());
        panelOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
        panelOptions.add(box4Sliders   , BorderLayout.CENTER);
        panelOptions.add(box4colorImage, BorderLayout.SOUTH);

        box4Options.add(panelOptions);

        addChangeListener("modelDelta"        , modelDelta        , v -> params.delta         = v);
        addChangeListener("modelMinArea"      , modelMinArea      , v -> params.minArea       = v);
        addChangeListener("modelMaxArea"      , modelMaxArea      , v -> params.maxArea       = v);
        addChangeListener("modelMaxVariation" , modelMaxVariation , v -> params.maxVariation  = v);
        addChangeListener("modelMinDiversity" , modelMinDiversity , v -> params.minDiversity  = v);
        addChangeListener("modelMaxEvolution" , modelMaxEvolution , v -> params.maxEvolution  = v);
        addChangeListener("modelAreaThreshold", modelAreaThreshold, v -> params.areaThreshold = v);
        addChangeListener("modelMinMargin"    , modelMinMargin    , v -> params.minMargin     = v);
      //addChangeListener("modelEdgeBlurSize" , modelEdgeBlurSize , v -> params.edgeBlurSize  = v);
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
