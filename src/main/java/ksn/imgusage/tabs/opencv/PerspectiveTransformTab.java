package ksn.imgusage.tabs.opencv;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.ISliderModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.PerspectiveTransformTabParams;
import ksn.imgusage.type.opencv.CvBorderTypes;

/** <a href='https://docs.opencv.org/3.4.2/da/d54/group__imgproc__transform.html#ga8c1ae0e3589a9d77fffc962c49b22043'>Perspective transformation for the corresponding 4 point pairs</a>
 * <br>
 *  <a href='https://docs.opencv.org/3.4.2/da/d54/group__imgproc__transform.html#gaf73673a7e8e18ec6963e3774e6a94b87'>Applies a perspective transformation to an image</a>
 **/
public class PerspectiveTransformTab extends OpencvFilterTab<PerspectiveTransformTabParams> {

    public static final String TAB_TITLE = "PerspectiveTransform";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Applies a perspective transformation to an image";

    public  static final int MIN_KSIZE  =   1;
    private static final int MAX_KSIZE  = 999;
    private static final int MIN_ANCHOR =  -1;
    private static final int MAX_ANCHOR = MAX_KSIZE;

    private PerspectiveTransformTabParams params;

    @Override
    public Component makeTab(PerspectiveTransformTabParams params) {
        if (params == null)
            params = new PerspectiveTransformTabParams();
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
        Mat src = new MatOfPoint2f(
            new Point(params.pointLeftTop    .x, params.pointLeftTop    .y),
            new Point(params.pointRightTop   .x, params.pointRightTop   .y),
            new Point(params.pointLeftBottom .x, params.pointLeftBottom .y),
            new Point(params.pointRightBottom.x, params.pointRightBottom.y));
        int w = (params.dsize.width  == 0) ? imageMat.width () : params.dsize.width;
        int h = (params.dsize.height == 0) ? imageMat.height() : params.dsize.height;
        Mat dst = new MatOfPoint2f(
                new Point(0, 0),
                new Point(w, 0),
                new Point(0, h),
                new Point(w, h));
        Mat transformMatrix = Imgproc.getPerspectiveTransform(src, dst);

        Mat dst2 = new Mat();
        Imgproc.warpPerspective(
            imageMat, // src
            dst2,
            transformMatrix,
            new Size(
                    params.dsize.width,
                    params.dsize.height),
                params.getInterpolation().getVal(
                    false,
                    params.useFlagInverseMap));
        imageMat = dst2;
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderIntModel modelKernelSizeW = new SliderIntModel(params.kernelSize.width , 0, MIN_KSIZE, MAX_KSIZE);
        SliderIntModel modelKernelSizeH = new SliderIntModel(params.kernelSize.height, 0, MIN_KSIZE, MAX_KSIZE);
        SliderIntModel modelAnchorX     = new SliderIntModel(params.anchor.x, 0, MIN_ANCHOR, MAX_ANCHOR);
        SliderIntModel modelAnchorY     = new SliderIntModel(params.anchor.y, 0, MIN_ANCHOR, MAX_ANCHOR);

        Box boxKernelSize = Box.createHorizontalBox();
        boxKernelSize.setBorder(BorderFactory.createTitledBorder("Kernel size"));
        boxKernelSize.setToolTipText("Blurring kernel size");
        boxKernelSize.add(Box.createHorizontalGlue());
        boxKernelSize.add(makeSliderVert(modelKernelSizeW, "Width", "Blurring kernel width"));
        boxKernelSize.add(Box.createHorizontalStrut(2));
        boxKernelSize.add(makeSliderVert(modelKernelSizeH, "Height", "Blurring kernel height"));
        boxKernelSize.add(Box.createHorizontalGlue());

        Box boxAnchor = Box.createHorizontalBox();
        boxAnchor.setBorder(BorderFactory.createTitledBorder("Anchor"));
        boxAnchor.setToolTipText("Anchor point. Default value Point(-1,-1) means that the anchor is at the kernel center.");
        boxAnchor.add(Box.createHorizontalGlue());
        boxAnchor.add(makeSliderVert(modelAnchorX, "X", "Anchor X"));
        boxAnchor.add(Box.createHorizontalStrut(2));
        boxAnchor.add(makeSliderVert(modelAnchorY, "Y", "Anchor Y"));
        boxAnchor.add(Box.createHorizontalGlue());

        Box box4Borders = GaussianBlurTab.makeBox4Border(
                b -> (b != CvBorderTypes.BORDER_WRAP)        // CvException [org.opencv.core.CvException: cv::Exception: OpenCV(3.4.2) /home/osboxes/opencv/opencv/opencv-3.4.2/modules/imgproc/src/filter.cpp:127: error: (-215:Assertion failed) columnBorderType != BORDER_WRAP in function 'init']
                  && (b != CvBorderTypes.BORDER_TRANSPARENT) // CvException [org.opencv.core.CvException: cv::Exception: OpenCV(3.4.2) /home/osboxes/opencv/opencv/opencv-3.4.2/modules/core/src/copy.cpp:940: error: (-5:Bad argument) Unknown/unsupported border type in function 'borderInterpolate']
                  && (b != CvBorderTypes.BORDER_DEFAULT),    // dublicate
                () -> params.borderType,
                bt -> params.borderType = bt,
                this::resetImage,
                "Border mode used to extrapolate pixels outside of the image",
                logger);

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(boxKernelSize);
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(boxAnchor);
        box4Sliders.add(Box.createHorizontalGlue());

        Box boxOptions = Box.createVerticalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(box4Sliders);
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(box4Borders);
        boxOptions.add(Box.createVerticalStrut(2));
        box4Options.add(boxOptions);

        addModelK2ChangeListener("modelKernelSizeW", modelKernelSizeW,  true, modelAnchorX    , () -> params.kernelSize.width  = modelKernelSizeW.getValue());
        addModelK2ChangeListener("modelKernelSizeH", modelKernelSizeH,  true, modelAnchorY    , () -> params.kernelSize.height = modelKernelSizeH.getValue());
        addModelK2ChangeListener("modelAnchorX"    , modelAnchorX    , false, modelKernelSizeW, () -> params.anchor.x          = modelAnchorX    .getValue());
        addModelK2ChangeListener("modelAnchorY"    , modelAnchorY    , false, modelKernelSizeH, () -> params.anchor.y          = modelAnchorY    .getValue());

        return box4Options;
    }

    private void addModelK2ChangeListener(String name, ISliderModel<Integer> model, boolean checkMax, ISliderModel<Integer> modelToCheck, Runnable applyValueParams) {
        MorphologyExTab.addModelK2ChangeListener(name, model, checkMax, modelToCheck, applyValueParams, logger, this::resetImage);
    }

    @Override
    public PerspectiveTransformTabParams getParams() {
        return params;
    }

}
