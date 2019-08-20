package ksn.imgusage.tabs.opencv;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JTabbedPane;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.WarpAffineTabParams;

/** <a href='https://docs.opencv.org/3.4.2/da/d54/group__imgproc__transform.html#ga0203d9ee5fcd28d40dbc4a1ea4451983'>Applies an affine transformation to an image </a> */
public class WarpAffineTab extends OpencvFilterTab<WarpAffineTabParams> {

    public static final String TAB_TITLE = "WarpAffine";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Applies an affine transformation to an image";

    public  static final int    MIN_DSIZE      =   0;
    private static final int    MAX_DSIZE      = 10000;
    private static final double MIN_MATRIX_VAL = -3000;
    private static final double MAX_MATRIX_VAL =  3000;

    private WarpAffineTabParams params;

    @Override
    public Component makeTab(WarpAffineTabParams params) {
        if (params == null)
            params = new WarpAffineTabParams();
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
        Mat dst = new Mat(imageMat.rows(), imageMat.cols(), imageMat.type());
        Mat mt = new Mat(2, 3, CvType.CV_32FC1);
        double[] mtx = new double[] {
            params.transfMatrix.m11,
            params.transfMatrix.m12,
            params.transfMatrix.m13,
            params.transfMatrix.m21,
            params.transfMatrix.m22,
            params.transfMatrix.m23
        };
        int checkVal = mt.put(0, 0, mtx);
        assert checkVal == 6;
        Imgproc.warpAffine(imageMat, dst, mt, new Size(params.dsize.width, params.dsize.height));
        imageMat = dst;
    }

    @Override
    protected Component makeOptions() {
        JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPane.setBorder(BorderFactory.createEmptyBorder(8,8,2,8));

        tabPane.addTab(getTitle() + " options", null, makeWarpAffineOptions(), "The function warpAffine transforms the source image using the specified matrix: dst(x,y)=src(M11x+M12y+M13,M21x+M22y+M23)");
        tabPane.addTab("Rotate", null, makeRotateOptions(), "The function warpAffine transforms the source image using the specified matrix: dst(x,y)=src(M11x+M12y+M13,M21x+M22y+M23)");

        return tabPane;
    }

    private Component makeWarpAffineOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderDoubleModel modelM11 = new SliderDoubleModel(params.transfMatrix.m11, 0, MIN_MATRIX_VAL, MAX_MATRIX_VAL);
        SliderDoubleModel modelM12 = new SliderDoubleModel(params.transfMatrix.m12, 0, MIN_MATRIX_VAL, MAX_MATRIX_VAL);
        SliderDoubleModel modelM13 = new SliderDoubleModel(params.transfMatrix.m13, 0, MIN_MATRIX_VAL, MAX_MATRIX_VAL);
        SliderDoubleModel modelM21 = new SliderDoubleModel(params.transfMatrix.m21, 0, MIN_MATRIX_VAL, MAX_MATRIX_VAL);
        SliderDoubleModel modelM22 = new SliderDoubleModel(params.transfMatrix.m22, 0, MIN_MATRIX_VAL, MAX_MATRIX_VAL);
        SliderDoubleModel modelM23 = new SliderDoubleModel(params.transfMatrix.m23, 0, MIN_MATRIX_VAL, MAX_MATRIX_VAL);
        SliderIntModel modelSizeW  = new    SliderIntModel(params.dsize.width , 0, MIN_DSIZE, MAX_DSIZE);
        SliderIntModel modelSizeH  = new    SliderIntModel(params.dsize.height, 0, MIN_DSIZE, MAX_DSIZE);

        Box boxM1 = Box.createHorizontalBox();
        boxM1.add(Box.createHorizontalGlue());
        boxM1.add(makeSliderVert(modelM11, "m11", null));
        boxM1.add(Box.createHorizontalStrut(2));
        boxM1.add(makeSliderVert(modelM12, "m12", null));
        boxM1.add(Box.createHorizontalStrut(2));
        boxM1.add(makeSliderVert(modelM13, "m13", null));
        boxM1.add(Box.createHorizontalGlue());

        Box boxM2 = Box.createHorizontalBox();
        boxM2.add(Box.createHorizontalGlue());
        boxM2.add(makeSliderVert(modelM21, "m21", null));
        boxM2.add(Box.createHorizontalStrut(2));
        boxM2.add(makeSliderVert(modelM22, "m22", null));
        boxM2.add(Box.createHorizontalStrut(2));
        boxM2.add(makeSliderVert(modelM23, "m23", null));
        boxM2.add(Box.createHorizontalGlue());

        Box box4MSliders = Box.createVerticalBox();
        box4MSliders.setBorder(BorderFactory.createTitledBorder("2×3 transformation matrix"));
      //box4MSliders.add(Box.createVerticalGlue());
        box4MSliders.add(boxM1);
      //box4MSliders.add(Box.createVerticalStrut(2));
        box4MSliders.add(boxM2);
      //box4MSliders.add(Box.createVerticalGlue());

        Box boxSize = Box.createHorizontalBox();
        boxSize.setBorder(BorderFactory.createTitledBorder("dsize"));
        boxSize.setToolTipText("size of the output image");
        boxSize.add(Box.createHorizontalGlue());
        boxSize.add(makeSliderVert(modelSizeW, "Width", "Size Width"));
        boxSize.add(Box.createHorizontalStrut(2));
        boxSize.add(makeSliderVert(modelSizeH, "Height", "Size Height"));
        boxSize.add(Box.createHorizontalGlue());

        Box boxOptions = Box.createVerticalBox();
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(box4MSliders);
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(boxSize);
        boxOptions.add(Box.createVerticalStrut(2));
        box4Options.add(boxOptions);

        modelM11.getWrapped().addChangeListener(ev -> {
            logger.trace("modelM11: value={}", modelM11.getFormatedText());
            params.transfMatrix.m11 = modelM11.getValue();
            resetImage();
        });
        modelM12.getWrapped().addChangeListener(ev -> {
            logger.trace("modelM12: value={}", modelM12.getFormatedText());
            params.transfMatrix.m12 = modelM12.getValue();
            resetImage();
        });
        modelM13.getWrapped().addChangeListener(ev -> {
            logger.trace("modelM13: value={}", modelM13.getFormatedText());
            params.transfMatrix.m13 = modelM13.getValue();
            resetImage();
        });
        modelM21.getWrapped().addChangeListener(ev -> {
            logger.trace("modelM21: value={}", modelM21.getFormatedText());
            params.transfMatrix.m21 = modelM21.getValue();
            resetImage();
        });
        modelM22.getWrapped().addChangeListener(ev -> {
            logger.trace("modelM22: value={}", modelM22.getFormatedText());
            params.transfMatrix.m22 = modelM22.getValue();
            resetImage();
        });
        modelM23.getWrapped().addChangeListener(ev -> {
            logger.trace("modelM23: value={}", modelM23.getFormatedText());
            params.transfMatrix.m23 = modelM23.getValue();
            resetImage();
        });
        modelSizeW.getWrapped().addChangeListener(ev -> {
            logger.trace("modelSizeW: value={}", modelSizeW.getFormatedText());
            params.dsize.width = modelSizeW.getValue();
            resetImage();
        });
        modelSizeH.getWrapped().addChangeListener(ev -> {
            logger.trace("modelSizeH: value={}", modelSizeH.getFormatedText());
            params.dsize.height = modelSizeH.getValue();
            resetImage();
        });

        return box4Options;
    }

    /** <a href='https://docs.opencv.org/3.4.2/da/d54/group__imgproc__transform.html#gafbbc470ce83812914a70abfb604f4326'>Calculates an affine matrix of 2D rotation</a> */
    private Component makeRotateOptions() {
        Box box4Options = Box.createVerticalBox();

        return box4Options;
    }

    @Override
    public WarpAffineTabParams getParams() {
        return params;
    }

}
