package ksn.imgusage.tabs.opencv;

import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.WarpAffineTabParams;
import ksn.imgusage.type.opencv.CvInterpolationFlags;

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
    private Runnable apllyParamsSettings;
    private Runnable apllyRotateSettings;

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
        Mat mt = new Mat(2, 3, CvType.CV_64FC1);
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
        Imgproc.warpAffine(
            imageMat,
            dst,
            mt,
            new Size(
                params.dsize.width,
                params.dsize.height),
            params.getInterpolation().getVal(
                false,
                params.useFlagInverseMap)
        );
        imageMat = dst;
    }

    @Override
    protected Component makeOptions() {
        JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPane.setBorder(BorderFactory.createEmptyBorder(8,8,2,8));
        tabPane.addChangeListener(this::onTabChanged);

        tabPane.addTab(getTitle() + " options", null, makeWarpAffineOptions(), "The function warpAffine transforms the source image using the specified matrix: dst(x,y)=src(M11x+M12y+M13,M21x+M22y+M23)");
        tabPane.addTab("Rotate", null, makeRotateOptions(), "The function warpAffine transforms the source image using the specified matrix: dst(x,y)=src(M11x+M12y+M13,M21x+M22y+M23)");

        return tabPane;
    }

    private void onTabChanged(ChangeEvent ev) {
        logger.trace("onTabChanged");
        final int i = ((JTabbedPane)ev.getSource()).getSelectedIndex();
        if (i == 1)
            // its Rotate tab
            apllyRotateSettings.run();
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

        Box boxSizeInterpol = Box.createHorizontalBox();
        boxSizeInterpol.add(Box.createHorizontalGlue());
        boxSizeInterpol.add(makeSize(modelSizeW, modelSizeH, "dsize", "size of the output image"));
        boxSizeInterpol.add(Box.createHorizontalStrut(2));
        boxSizeInterpol.add(makeInterpolations(
            params::getInterpolation,
            params::setInterpolation,
            e -> e != CvInterpolationFlags.INTER_LINEAR_EXACT, // CvException [org.opencv.core.CvException:
                                                               //  cv::Exception: OpenCV(3.4.2) /home/osboxes/opencv/opencv/opencv-3.4.2/modules/core/src/parallel.cpp:240:
                                                               //  error: (-2:Unspecified error) in function 'finalize'
                                                               //  Exception in parallel_for() body:
                                                               //  OpenCV(3.4.2) /home/osboxes/opencv/opencv/opencv-3.4.2/modules/imgproc/src/imgwarp.cpp:1803:
                                                               //  error: (-5:Bad argument) Unknown interpolation method in function 'remap' ]
            params.useFlagInverseMap,
            v -> params.useFlagInverseMap = v));
        boxSizeInterpol.add(Box.createHorizontalGlue());

        Box boxOptions = Box.createVerticalBox();
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(box4MSliders);
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(boxSizeInterpol);
        boxOptions.add(Box.createVerticalStrut(2));
        box4Options.add(boxOptions);

        apllyParamsSettings = () -> {
            modelM11.setValue(params.transfMatrix.m11);
            modelM12.setValue(params.transfMatrix.m12);
            modelM13.setValue(params.transfMatrix.m13);
            modelM21.setValue(params.transfMatrix.m21);
            modelM22.setValue(params.transfMatrix.m22);
            modelM23.setValue(params.transfMatrix.m23);

            modelSizeW.setValue(params.dsize.width);
            modelSizeH.setValue(params.dsize.height);
        };

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

        SliderIntModel modelCenterX  = new    SliderIntModel(0, 0, -1000, +1000);
        SliderIntModel modelCenterY  = new    SliderIntModel(0, 0, -1000, +1000);
        SliderDoubleModel modelAngle = new SliderDoubleModel(0, 0, -360, +360);
        SliderDoubleModel modelScale = new SliderDoubleModel(1, 0, 0.1, 100);

        Box boxCenter = Box.createHorizontalBox();
        boxCenter.setBorder(BorderFactory.createTitledBorder("Center"));
        boxCenter.setToolTipText("Center of the rotation in the source image");
        boxCenter.add(Box.createHorizontalGlue());
        boxCenter.add(makeSliderVert(modelCenterX, "X", "center X"));
        boxCenter.add(Box.createHorizontalStrut(2));
        boxCenter.add(makeSliderVert(modelCenterY, "Y", "center Y"));
        boxCenter.add(Box.createHorizontalGlue());

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(boxCenter);
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(makeSliderVert(modelAngle, "Angle", "Rotation angle in degrees. Positive values mean counter-clockwise rotation (the coordinate origin is assumed to be the top-left corner)"));
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(makeSliderVert(modelScale, "Scale", "Isotropic scale factor"));
        box4Sliders.add(Box.createHorizontalGlue());

        Runnable reset =() -> {
            BufferedImage img = source.getImage();
            modelCenterX.setValue((img==null) ? DEFAULT_WIDTH  : img.getWidth()  / 2);
            modelCenterY.setValue((img==null) ? DEFAULT_HEIGHT : img.getHeight() / 2);
            modelAngle.setValue(0.0);
            modelScale.setValue(1.0);
        };
        reset.run();

        JButton btnReset = new JButton(" reset ");
        btnReset.addActionListener(ev -> reset.run());

        box4Options.add(box4Sliders);
        box4Options.add(Box.createVerticalStrut(4));
        box4Options.add(btnReset);

        apllyRotateSettings = () -> {
            Mat rotateMatrix = Imgproc.getRotationMatrix2D(
                    new Point(
                        modelCenterX.getValue(),
                        modelCenterY.getValue()),
                    modelAngle.getValue(),
                    modelScale.getValue());

            assert rotateMatrix.rows() == 2;
            assert rotateMatrix.cols() == 3;

            double[] mtx = new double[6];
            int readed = rotateMatrix.get(0, 0, mtx);
            long fullSize = 6 * rotateMatrix.elemSize();
            if (readed != fullSize) {
                logger.error("hmm... fix me");
                return;
            }

            params.transfMatrix.m11 = mtx[0];
            params.transfMatrix.m12 = mtx[1];
            params.transfMatrix.m13 = mtx[2];
            params.transfMatrix.m21 = mtx[3];
            params.transfMatrix.m22 = mtx[4];
            params.transfMatrix.m23 = mtx[5];

            params.dsize.width  = 0;
            params.dsize.height = 0;

            apllyParamsSettings.run();
        };

        modelAngle.getWrapped().addChangeListener(ev -> {
            logger.trace("modelAngle: value={}", modelAngle.getFormatedText());
            apllyRotateSettings.run();
        });
        modelScale.getWrapped().addChangeListener(ev -> {
            logger.trace("modelScale: value={}", modelScale.getFormatedText());
            apllyRotateSettings.run();
        });
        modelCenterX.getWrapped().addChangeListener(ev -> {
            logger.trace("modelCenterX: value={}", modelCenterX.getFormatedText());
            apllyRotateSettings.run();
        });
        modelCenterY.getWrapped().addChangeListener(ev -> {
            logger.trace("modelCenterY: value={}", modelCenterY.getFormatedText());
            apllyRotateSettings.run();
        });


        return box4Options;
    }

    @Override
    public WarpAffineTabParams getParams() {
        return params;
    }

}
