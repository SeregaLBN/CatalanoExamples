package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.BilateralTabParams;
import ksn.imgusage.type.opencv.CvBorderTypes;

/** <a href='https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#ga9d7064d478c95d60003cf839430737ed'>Applies the bilateral filter to an image</a> */
public class BilateralTab extends OpencvFilterTab<BilateralTabParams> {

    public static final String TAB_TITLE = "Bilateral";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Applies the bilateral filter to an image";

    public  static final int MIN_DIAMETER =  -1;
    private static final int MAX_DIAMETER = 200;
    private static final double MIN_SIGMA_COLOR = 0;
    private static final double MAX_SIGMA_COLOR = 700;
    private static final double MIN_SIGMA_SPACE = 0;
    private static final double MAX_SIGMA_SPACE = 500;

    private BilateralTabParams params;

    @Override
    public Component makeTab(BilateralTabParams params) {
        if (params == null)
            params = new BilateralTabParams();
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
        // TODO
        // Source 8-bit or floating-point, 1-channel or 3-channel image.

        Mat dst = new Mat();
        Imgproc.bilateralFilter(
            imageMat, // src
            dst,
            params.diameter,
            params.sigmaColor,
            params.sigmaSpace,
            params.borderType.getVal());
        imageMat = dst;
    }

    @Override
    protected Component makeOptions() {
        SliderIntModel    modelDiameter   = new SliderIntModel   (params.diameter  , 0, MIN_DIAMETER, MAX_DIAMETER);
        SliderDoubleModel modelSigmaColor = new SliderDoubleModel(params.sigmaColor, 0, MIN_SIGMA_COLOR, MAX_SIGMA_COLOR);
        SliderDoubleModel modelSigmaSpace = new SliderDoubleModel(params.sigmaSpace, 0, MIN_SIGMA_SPACE, MAX_SIGMA_SPACE);

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(makeSliderVert(modelDiameter, "Diameter", "Diameter of each pixel neighborhood that is used during filtering. If it is non-positive, it is computed from sigmaSpace"));
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(makeSliderVert(modelSigmaColor, "SigmaColor", "Filter sigma in the color space. A larger value of the parameter means that farther colors within the pixel neighborhood (see sigmaSpace) will be mixed together, resulting in larger areas of semi-equal color. "));
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(makeSliderVert(modelSigmaSpace, "SigmaSpace", "Filter sigma in the coordinate space. A larger value of the parameter means that farther pixels will influence each other as long as their colors are close enough (see sigmaColor ). When d>0, it specifies the neighborhood size regardless of sigmaSpace. Otherwise, d is proportional to sigmaSpace."));
        box4Sliders.add(Box.createHorizontalGlue());

        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new BorderLayout());
        panelOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
        panelOptions.add(box4Sliders, BorderLayout.CENTER);
        panelOptions.add(makeBox4Border(b -> (b != CvBorderTypes.BORDER_TRANSPARENT), // CvException [org.opencv.core.CvException: cv::Exception: OpenCV(3.4.2) C:\build\3_4_winpack-bindings-win64-vc14-static\opencv\modules\core\src\copy.cpp:940: error: (-5:Bad argument) Unknown/unsupported border type in function 'cv::borderInterpolate'
                                        () -> params.borderType,
                                        bt -> params.borderType = bt,
                                        "Border mode used to extrapolate pixels outside of the image"),
                         BorderLayout.SOUTH);

        modelDiameter.getWrapped().addChangeListener(ev -> {
            params.diameter = modelDiameter.getValue();
            logger.trace("modelDiameter: value={}", modelDiameter.getFormatedText());
            resetImage();
        });
        modelSigmaColor.getWrapped().addChangeListener(ev -> {
            logger.trace("modelSigmaColor: value={}", modelSigmaColor.getFormatedText());
            params.sigmaColor = modelSigmaColor.getValue();
            resetImage();
        });
        modelSigmaSpace.getWrapped().addChangeListener(ev -> {
            logger.trace("modelSigmaSpace: value={}", modelSigmaSpace.getFormatedText());
            params.sigmaSpace = modelSigmaSpace.getValue();
            resetImage();
        });

        return panelOptions;
    }

    @Override
    public BilateralTabParams getParams() {
        return params;
    }

}
