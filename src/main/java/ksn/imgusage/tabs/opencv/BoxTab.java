package ksn.imgusage.tabs.opencv;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.ISliderModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.BoxTabParams;
import ksn.imgusage.type.opencv.CvBorderTypes;

/** <a href='https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#gad533230ebf2d42509547d514f7d3fbc3'>Blurs an image using the box filter</a> */
public class BoxTab extends OpencvFilterTab<BoxTabParams> {

    public static final String TAB_TITLE = "Box";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Blurs an image using the box filter";

    public  static final int MIN_DDEPTH =  -1;
    private static final int MAX_DDEPTH = 999;
    public  static final int MIN_KSIZE  =   1;
    private static final int MAX_KSIZE  = 999;
    private static final int MIN_ANCHOR =  -1;
    private static final int MAX_ANCHOR = MAX_KSIZE;

    private BoxTabParams params;

    @Override
    public Component makeTab(BoxTabParams params) {
        if (params == null)
            params = new BoxTabParams();
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
        Mat dst = new Mat();
        Imgproc.boxFilter(
            imageMat, // src
            dst,
            params.ddepth,
            new org.opencv.core.Size(
                    params.kernelSize.width,
                    params.kernelSize.height),
            new org.opencv.core.Point(
                params.anchor.x,
                params.anchor.y),
            params.normalize,
            params.borderType.getVal());
        imageMat = dst;
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderIntModel modelDdepth      = new SliderIntModel(params.ddepth           , 0, MIN_DDEPTH, MAX_DDEPTH);
        SliderIntModel modelKernelSizeW = new SliderIntModel(params.kernelSize.width , 0, MIN_KSIZE , MAX_KSIZE);
        SliderIntModel modelKernelSizeH = new SliderIntModel(params.kernelSize.height, 0, MIN_KSIZE , MAX_KSIZE);
        SliderIntModel modelAnchorX     = new SliderIntModel(params.anchor.x         , 0, MIN_ANCHOR, MAX_ANCHOR);
        SliderIntModel modelAnchorY     = new SliderIntModel(params.anchor.y         , 0, MIN_ANCHOR, MAX_ANCHOR);

        Component cntrlKernelSize = makeSize(modelKernelSizeW, modelKernelSizeH, "Kernel size", "Blurring kernel size", "Blurring kernel width", "Blurring kernel height");
        Component cntrlAnchor = makePoint(modelAnchorX, modelAnchorY, "Anchor", "Anchor point. Default value Point(-1,-1) means that the anchor is at the kernel center", "Anchor X", "Anchor Y");

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(makeSliderVert(modelDdepth, "dDepth", "The output image depth (-1 to use src.depth())."));
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(cntrlKernelSize);
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(cntrlAnchor);
        box4Sliders.add(Box.createHorizontalGlue());

        Box boxOptions = Box.createVerticalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(box4Sliders);
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(makeCheckBox(
            () -> params.normalize,
            v  -> params.normalize = v,
            "Normalize",
            "params.normalize",
            "Flag, specifying whether the kernel is normalized by its area or not",
            null));
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(makeBox4Border(
            b -> (b != CvBorderTypes.BORDER_WRAP)        // CvException [org.opencv.core.CvException: cv::Exception: OpenCV(3.4.2) /home/osboxes/opencv/opencv/opencv-3.4.2/modules/imgproc/src/filter.cpp:127: error: (-215:Assertion failed) columnBorderType != BORDER_WRAP in function 'init']
              && (b != CvBorderTypes.BORDER_TRANSPARENT),// CvException [org.opencv.core.CvException: cv::Exception: OpenCV(3.4.2) /home/osboxes/opencv/opencv/opencv-3.4.2/modules/core/src/copy.cpp:940: error: (-5:Bad argument) Unknown/unsupported border type in function 'borderInterpolate']
            () -> params.borderType,
            bt -> params.borderType = bt,
                "Border mode used to extrapolate pixels outside of the image"));
        boxOptions.add(Box.createVerticalStrut(2));
        box4Options.add(boxOptions);

        addChangeListener("ddepth", modelDdepth, v -> params.ddepth = v);
        addModelK2ChangeListener("modelKernelSizeW", modelKernelSizeW, false, modelAnchorX    , () -> params.kernelSize.width  = modelKernelSizeW.getValue());
        addModelK2ChangeListener("modelKernelSizeH", modelKernelSizeH, false, modelAnchorY    , () -> params.kernelSize.height = modelKernelSizeH.getValue());
        addModelK2ChangeListener("modelAnchorX"    , modelAnchorX    ,  true, modelKernelSizeW, () -> params.anchor.x          = modelAnchorX    .getValue());
        addModelK2ChangeListener("modelAnchorY"    , modelAnchorY    ,  true, modelKernelSizeH, () -> params.anchor.y          = modelAnchorY    .getValue());

        return box4Options;
    }

    private void addModelK2ChangeListener(String name, ISliderModel<Integer> model, boolean checkMax, ISliderModel<Integer> modelToCheck, Runnable applyValueParams) {
        addChangeListenerDiff1WithModels(name, model, checkMax, modelToCheck, applyValueParams);
    }

    @Override
    public BoxTabParams getParams() {
        return params;
    }

}
