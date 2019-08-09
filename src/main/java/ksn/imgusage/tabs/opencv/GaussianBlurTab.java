package ksn.imgusage.tabs.opencv;

import java.awt.event.ItemEvent;
import java.util.stream.Stream;

import javax.swing.*;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.opencv.type.CvBorderTypes;

/** <a href='https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#gaabe8c836e97159a9193fb0b11ac52cf1'>Blurs an image using a Gaussian filter</a> */
public class GaussianBlurTab extends OpencvFilterTab {

    private static final int MIN_KSIZE    =   0; // Gaussian kernel size. ksize.width and ksize.height can differ but they both must be positive and odd. Or,
    private static final int MAX_KSIZE    = 999; //  they can be zero’s and then they are computed from sigma* .
    private static final double MIN_SIGMA =   0; // Gaussian kernel standard deviation in X direction.
    private static final double MAX_SIGMA = 200; // Gaussian kernel standard deviation in Y direction; if sigmaY is zero,
                                                 //  it is set to be equal to sigmaX, if both sigmas are zeros, they are computed from ksize.width and ksize.height ,
                                                 //  respectively (see getGaussianKernel() for details); to fully control the result regardless of possible future
                                                 //  modifications of all this semantics, it is recommended to specify all of ksize, sigmaX, and sigmaY.

    private final SliderIntModel    modelKernelSizeW;
    private final SliderIntModel    modelKernelSizeH;
    private final SliderDoubleModel modelSigmaX;
    private final SliderDoubleModel modelSigmaY;
    private       CvBorderTypes     borderType;

    public GaussianBlurTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, null, new Size(7, 0), 25, 25, CvBorderTypes.BORDER_DEFAULT);
    }

    public GaussianBlurTab(ITabHandler tabHandler, ITab source, Boolean boosting, Size kernelSize, double sigmaX, double sigmaY, CvBorderTypes borderType) {
        super(tabHandler, source, boosting);
        this.modelKernelSizeW = new    SliderIntModel(onlyZeroOrOdd((int)kernelSize.width ), 0, MIN_KSIZE, MAX_KSIZE);
        this.modelKernelSizeH = new    SliderIntModel(onlyZeroOrOdd((int)kernelSize.height), 0, MIN_KSIZE, MAX_KSIZE);
        this.modelSigmaX      = new SliderDoubleModel(sigmaX, 0, MIN_SIGMA, MAX_SIGMA);
        this.modelSigmaY      = new SliderDoubleModel(sigmaY, 0, MIN_SIGMA, MAX_SIGMA);
        this.borderType = borderType;

        makeTab();
    }

    @Override
    public String getTabName() { return "GaussianBlur"; }

    @Override
    protected void applyOpencvFilter() {
        // TODO
        // input image; the image can have any number of channels, which are processed independently, but the depth should be CV_8U, CV_16U, CV_16S, CV_32F or CV_64F.

        Mat dst = new Mat();
        Imgproc.GaussianBlur(
            imageMat, // src
            dst,
            new Size(modelKernelSizeW.getValue(),
                     modelKernelSizeH.getValue()),
            modelSigmaX.getValue(),
            modelSigmaY.getValue(),
            borderType.getVal());
        imageMat = dst;
    }

    @Override
    protected void makeOptions(JPanel imagePanel, Box boxCenterLeft) {
        Box boxKernelSize = Box.createHorizontalBox();
        boxKernelSize.setBorder(BorderFactory.createTitledBorder("Kernel size"));
        boxKernelSize.setToolTipText("Gaussian kernel size. ksize.width and ksize.height can differ but they both must be positive and odd."
                                   + " Or they can be zero’s and then they are computed from sigma* .");
        boxKernelSize.add(Box.createHorizontalGlue());
        boxKernelSize.add(makeSliderVert(modelKernelSizeW, "Width", "Kernel size Width"));
        boxKernelSize.add(Box.createHorizontalStrut(2));
        boxKernelSize.add(makeSliderVert(modelKernelSizeH, "Height", "Kernel size Height"));
        boxKernelSize.add(Box.createHorizontalGlue());

        Box boxSigma = Box.createHorizontalBox();
        boxSigma.setBorder(BorderFactory.createTitledBorder("Sigma"));
        boxSigma.add(Box.createHorizontalGlue());
        boxSigma.add(makeSliderVert(modelSigmaX, "X", "Gaussian kernel standard deviation in X direction"));
        boxSigma.add(Box.createHorizontalStrut(2));
        boxSigma.add(makeSliderVert(modelSigmaY, "Y", "Gaussian kernel standard deviation in Y direction; if sigmaY is zero, it is set to be equal to sigmaX, if both sigmas are zeros, they are computed from ksize.width and ksize.height , respectively (see getGaussianKernel() for details); to fully control the result regardless of possible future modifications of all this semantics, it is recommended to specify all of ksize, sigmaX, and sigmaY."));
        boxSigma.add(Box.createHorizontalGlue());

        Box box4Borders = Box.createHorizontalBox();
        box4Borders.setBorder(BorderFactory.createTitledBorder("Border type"));
        Box box4Borders1 = Box.createVerticalBox();
        box4Borders1.setToolTipText("Pixel extrapolation method");
        ButtonGroup radioGroup = new ButtonGroup();
        Stream.of(CvBorderTypes.values())
            .filter(b -> b != CvBorderTypes.BORDER_TRANSPARENT) // CvException [org.opencv.core.CvException: cv::Exception: OpenCV(3.4.2) C:\build\3_4_winpack-bindings-win64-vc14-static\opencv\modules\core\src\copy.cpp:940: error: (-5:Bad argument) Unknown/unsupported border type in function 'cv::borderInterpolate'
            .filter(b -> b != CvBorderTypes.BORDER_DEFAULT) // dublicate
            .forEach(border ->
        {
            JRadioButton radioBtnAlg = new JRadioButton(border.name(), (border == this.borderType) || (border.getVal() == this.borderType.getVal()));
            radioBtnAlg.setActionCommand(border.name());
            radioBtnAlg.setToolTipText("Pixel extrapolation method");
            radioBtnAlg.addItemListener(ev -> {
                if (ev.getStateChange() == ItemEvent.SELECTED) {
                    this.borderType = border;
                    logger.trace("Border type changed to {}", border);
                    resetImage();
                }
            });
            box4Borders1.add(radioBtnAlg);
            radioGroup.add(radioBtnAlg);
        });
        box4Borders.add(Box.createHorizontalGlue());
        box4Borders.add(box4Borders1);
        box4Borders.add(Box.createHorizontalGlue());

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(boxKernelSize);
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(boxSigma);
        box4Sliders.add(Box.createHorizontalGlue());

        Box boxOptions = Box.createVerticalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTabName() + " options"));
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(box4Sliders);
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(box4Borders);
        boxOptions.add(Box.createVerticalStrut(2));
        boxCenterLeft.add(boxOptions);

        modelKernelSizeW.getWrapped().addChangeListener(ev -> {
            logger.trace("modelKernelSizeW: value={}", modelKernelSizeW.getFormatedText());
            int val = modelKernelSizeW.getValue();
            int valValid = onlyZeroOrOdd(val);
            if (val == valValid)
                resetImage();
            else
                SwingUtilities.invokeLater(() -> modelKernelSizeW.setValue(valValid));
        });
        modelKernelSizeH.getWrapped().addChangeListener(ev -> {
            logger.trace("modelKernelSizeH: value={}", modelKernelSizeH.getFormatedText());
            int val = modelKernelSizeH.getValue();
            int valValid = onlyZeroOrOdd(val);
            if (val == valValid)
                resetImage();
            else
                SwingUtilities.invokeLater(() -> modelKernelSizeH.setValue(valValid));
        });
        modelSigmaX.getWrapped().addChangeListener(ev -> {
            logger.trace("modelSigmaX: value={}", modelSigmaX.getFormatedText());
            resetImage();
        });
        modelSigmaY.getWrapped().addChangeListener(ev -> {
            logger.trace("modelSigmaY: value={}", modelSigmaY.getFormatedText());
            resetImage();
        });
    }

    static int onlyZeroOrOdd(int value) {
        if (value == 0)
            return value;
        if ((value & 1) == 0)
            return value - 1;
        return value;
    }

    @Override
    public void printParams() {
        logger.info("kernelSize={{}, {}}, sigmaX={}, sigmaY={}, borderType={}",
                modelKernelSizeW.getFormatedText(),
                modelKernelSizeH.getFormatedText(),
                modelSigmaX     .getFormatedText(),
                modelSigmaY     .getFormatedText(),
                borderType);
    }

}
