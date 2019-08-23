package ksn.imgusage.tabs.opencv;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

import javax.swing.BorderFactory;
import javax.swing.Box;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.ISliderModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.PerspectiveTransformTabParams;
import ksn.imgusage.type.opencv.CvInterpolationFlags;
import ksn.imgusage.utils.OpenCvHelper;
import ksn.imgusage.utils.UiHelper;

/** <a href='https://docs.opencv.org/3.4.2/da/d54/group__imgproc__transform.html#ga8c1ae0e3589a9d77fffc962c49b22043'>Perspective transformation for the corresponding 4 point pairs</a>
 * <br>
 *  <a href='https://docs.opencv.org/3.4.2/da/d54/group__imgproc__transform.html#gaf73673a7e8e18ec6963e3774e6a94b87'>Applies a perspective transformation to an image</a>
 **/
public class PerspectiveTransformTab extends OpencvFilterTab<PerspectiveTransformTabParams> {

    public static final String TAB_TITLE = "PerspectiveTransform";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Applies a perspective transformation to an image";

    private PerspectiveTransformTabParams params;
    private Runnable checkModelsDiapason;

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
        checkModelsDiapason.run();

        imageMat = params.applyToFirstTab
                ? getSourceMat(tabHandler.getFirstTab())
                : imageMat;
        if (params.showRegion) {

            imageMat = OpenCvHelper.to3Channel(imageMat);

            Imgproc.line(imageMat, new Point(params.pointLeftTop    .x, params.pointLeftTop    .y),
                                   new Point(params.pointRightTop   .x, params.pointRightTop   .y), new Scalar(255,   0,   0), 3);
            Imgproc.line(imageMat, new Point(params.pointRightTop   .x, params.pointRightTop   .y),
                                   new Point(params.pointRightBottom.x, params.pointRightBottom.y), new Scalar(  0, 255,   0), 3);
            Imgproc.line(imageMat, new Point(params.pointRightBottom.x, params.pointRightBottom.y),
                                   new Point(params.pointLeftBottom .x, params.pointLeftBottom .y), new Scalar(  0,   0, 255), 3);
            Imgproc.line(imageMat, new Point(params.pointLeftBottom .x, params.pointLeftBottom .y),
                                   new Point(params.pointLeftTop    .x, params.pointLeftTop    .y), new Scalar(255, 200,   0), 3);

            return;
        }

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

        SliderIntModel modelLTX;
        SliderIntModel modelLTY;
        SliderIntModel modelRTX;
        SliderIntModel modelRTY;
        SliderIntModel modelLBX;
        SliderIntModel modelLBY;
        SliderIntModel modelRBX;
        SliderIntModel modelRBY;
        SliderIntModel modelSizeW;
        SliderIntModel modelSizeH;

        {
            BufferedImage img = source.getImage();
            int w = (img==null) ? DEFAULT_WIDTH  : img.getWidth();
            int h = (img==null) ? DEFAULT_HEIGHT : img.getHeight();

            params.pointLeftTop    .x = Math.max(0, Math.min(w, params.pointLeftTop    .x));
            params.pointLeftTop    .y = Math.max(0, Math.min(h, params.pointLeftTop    .y));
            params.pointRightTop   .x = Math.max(0, Math.min(w, params.pointRightTop   .x));
            params.pointRightTop   .y = Math.max(0, Math.min(h, params.pointRightTop   .y));
            params.pointLeftBottom .x = Math.max(0, Math.min(w, params.pointLeftBottom .x));
            params.pointLeftBottom .y = Math.max(0, Math.min(h, params.pointLeftBottom .y));
            params.pointRightBottom.x = Math.max(0, Math.min(w, params.pointRightBottom.x));
            params.pointRightBottom.y = Math.max(0, Math.min(h, params.pointRightBottom.y));

            modelLTX   = new SliderIntModel(params.pointLeftTop    .x, 0, 0, w);
            modelLTY   = new SliderIntModel(params.pointLeftTop    .y, 0, 0, h);
            modelRTX   = new SliderIntModel(params.pointRightTop   .x, 0, 0, w);
            modelRTY   = new SliderIntModel(params.pointRightTop   .y, 0, 0, h);
            modelLBX   = new SliderIntModel(params.pointLeftBottom .x, 0, 0, w);
            modelLBY   = new SliderIntModel(params.pointLeftBottom .y, 0, 0, h);
            modelRBX   = new SliderIntModel(params.pointRightBottom.x, 0, 0, w);
            modelRBY   = new SliderIntModel(params.pointRightBottom.y, 0, 0, h);
            modelSizeW = new SliderIntModel(params.dsize.width       , 0, 0, w);
            modelSizeH = new SliderIntModel(params.dsize.height      , 0, 0, h);
        }

        checkModelsDiapason = () -> {
            BufferedImage img = source.getImage();
            int w = (img==null) ? DEFAULT_WIDTH  : img.getWidth();
            int h = (img==null) ? DEFAULT_HEIGHT : img.getHeight();
            int dx = w / 10;
            int dy = h / 10;
            int maxX = w + dx;
            int minX = 0 - dx;
            int maxY = h + dy;
            int minY = 0 - dy;

            BiConsumer<SliderIntModel, SliderIntModel> applyMinMax = (modelX, modelY) -> {
                if (modelX.getMinimum() != minX) modelX.setMinimum(minX);
                if (modelX.getMaximum() != maxX) modelX.setMaximum(maxX);
                if (modelY.getMinimum() != minY) modelY.setMinimum(minY);
                if (modelY.getMaximum() != maxY) modelY.setMaximum(maxY);
            };
            applyMinMax.accept(modelLTX, modelLTY);
            applyMinMax.accept(modelRTX, modelRTY);
            applyMinMax.accept(modelLBX, modelLBY);
            applyMinMax.accept(modelRBX, modelRBY);

            if (modelSizeW.getMaximum() != (w * 2)) modelSizeW.setMaximum(w * 2);
            if (modelSizeH.getMaximum() != (h * 2)) modelSizeH.setMaximum(h * 2);
        };
        checkModelsDiapason.run();


        Box boxPointsTop = Box.createHorizontalBox();
        boxPointsTop.add(Box.createHorizontalGlue());
        boxPointsTop.add(makePoint(modelLTX , modelLTY, "Left-Top", "Left top point for perspective transformation"  , null, null));
        boxPointsTop.add(Box.createHorizontalStrut(2));
        boxPointsTop.add(makePoint(modelRTX , modelRTY, "Right-Top", "Right top point for perspective transformation", null, null));
        boxPointsTop.add(Box.createHorizontalGlue());

        Box boxPointsBottom = Box.createHorizontalBox();
        boxPointsBottom.add(Box.createHorizontalGlue());
        boxPointsBottom.add(makePoint(modelLBX , modelLBY, "Left-Bottom", "Left bottom point for perspective transformation"  , null, null));
        boxPointsBottom.add(Box.createHorizontalStrut(2));
        boxPointsBottom.add(makePoint(modelRBX , modelRBY, "Right-Bottom", "Right bottom point for perspective transformation", null, null));
        boxPointsBottom.add(Box.createHorizontalGlue());

        Box boxPoints = Box.createVerticalBox();
        boxPoints.setBorder(BorderFactory.createTitledBorder("Transformation region"));
        boxPoints.setToolTipText("Points for perspective transformation");
      //boxPoints.add(Box.createVerticalGlue());
        boxPoints.add(boxPointsTop);
        boxPoints.add(Box.createVerticalStrut(2));
        boxPoints.add(boxPointsBottom);
      //boxPoints.add(Box.createVerticalGlue());

        Box boxSizeInterpol = Box.createHorizontalBox();
        boxSizeInterpol.add(Box.createHorizontalGlue());
        boxSizeInterpol.add(makeSize(modelSizeW, modelSizeH, "dsize", "Size of the output image", "Size Width", "Size Height"));
        boxSizeInterpol.add(Box.createHorizontalStrut(2));
        boxSizeInterpol.add(makeInterpolations(
            params::getInterpolation,
            params::setInterpolation,
            e -> e != CvInterpolationFlags.INTER_LINEAR_EXACT,
            params.useFlagInverseMap,
            v -> params.useFlagInverseMap = v));
        boxSizeInterpol.add(Box.createHorizontalGlue());

        Component cntrlToFirstTab  = makeCheckBox(
                () -> params.applyToFirstTab,       // getter
                v  -> params.applyToFirstTab = v,   // setter
                "Apply to first tab",               // title
                "params.applyToFirstTab",           // paramName
                null,                               // tip
                null);                              // customListener
        Runnable showRegionCustomListener = () -> {
          //UiHelper.enableAllChilds(cntrlToFirstTab, !params.showRegion);
            UiHelper.enableAllChilds(boxSizeInterpol, !params.showRegion);
        };
        Component cntrlShowRegion = makeCheckBox(
                () -> params.showRegion,        // getter
                v  -> params.showRegion = v,    // setter
                "Show transformation region",   // title
                "params.showRegion",            // paramName
                null,                           // tip
                showRegionCustomListener);      // customListener
        Box boxCustomsV = Box.createVerticalBox();
        boxCustomsV.setBorder(BorderFactory.createTitledBorder(""));
        boxCustomsV.add(cntrlShowRegion);
        boxCustomsV.add(cntrlToFirstTab);
        Box boxCustomsH = Box.createHorizontalBox();
        boxCustomsH.add(boxCustomsV);

        Box boxOptions = Box.createVerticalBox();
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(boxCustomsH);
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(boxPoints);
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(boxSizeInterpol);
        boxOptions.add(Box.createVerticalStrut(2));

        box4Options.add(boxOptions);

        showRegionCustomListener.run();

        addPointsChangeListener("modelLTX", modelLTX,  true, modelRTX, () -> params.pointLeftTop    .x = modelLTX.getValue());
        addPointsChangeListener("modelLTY", modelLTY,  true, modelLBY, () -> params.pointLeftTop    .y = modelLTY.getValue());

        addPointsChangeListener("modelRTX", modelRTX, false, modelLTX, () -> params.pointRightTop   .x = modelRTX.getValue());
        addPointsChangeListener("modelRTY", modelRTY,  true, modelRBY, () -> params.pointRightTop   .y = modelRTY.getValue());

        addPointsChangeListener("modelLBX", modelLBX,  true, modelRBX, () -> params.pointLeftBottom .x = modelLBX.getValue());
        addPointsChangeListener("modelLBY", modelLBY, false, modelLTY, () -> params.pointLeftBottom .y = modelLBY.getValue());

        addPointsChangeListener("modelRBX", modelRBX, false, modelLBX, () -> params.pointRightBottom.x = modelRBX.getValue());
        addPointsChangeListener("modelRBY", modelRBY, false, modelRTY, () -> params.pointRightBottom.y = modelRBY.getValue());

        addChangeListener("modelSizeW", modelSizeW, v -> params.dsize.width  = v);
        addChangeListener("modelSizeH", modelSizeH, v -> params.dsize.height = v);

        return box4Options;
    }

    private void addPointsChangeListener(String name, ISliderModel<Integer> model, boolean checkMax, ISliderModel<Integer> modelToCheck, Runnable applyValueParams) {
        addChangeListenerDiff1WithModels(name, model, checkMax, modelToCheck, applyValueParams);
    }

    @Override
    public PerspectiveTransformTabParams getParams() {
        return params;
    }

}
