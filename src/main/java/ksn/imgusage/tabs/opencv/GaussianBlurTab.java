package ksn.imgusage.tabs.opencv;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.swing.*;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.Size;
import ksn.imgusage.type.dto.opencv.GaussianBlurTabParams;
import ksn.imgusage.type.opencv.CvBorderTypes;

/** <a href='https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#gaabe8c836e97159a9193fb0b11ac52cf1'>Blurs an image using a Gaussian filter</a> */
public class GaussianBlurTab extends OpencvFilterTab<GaussianBlurTabParams> {

    public static final String TAB_TITLE = "GaussianBlur";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Blurs an image using a Gaussian filter";

    public  static final int MIN_KSIZE    =   0; // Gaussian kernel size. ksize.width and ksize.height can differ but they both must be positive and odd. Or,
    private static final int MAX_KSIZE    = 999; //  they can be zero’s and then they are computed from sigma* .
    private static final double MIN_SIGMA =   0; // Gaussian kernel standard deviation in X direction.
    private static final double MAX_SIGMA = 200; // Gaussian kernel standard deviation in Y direction; if sigmaY is zero,
                                                 //  it is set to be equal to sigmaX, if both sigmas are zeros, they are computed from ksize.width and ksize.height ,
                                                 //  respectively (see getGaussianKernel() for details); to fully control the result regardless of possible future
                                                 //  modifications of all this semantics, it is recommended to specify all of ksize, sigmaX, and sigmaY.

    private GaussianBlurTabParams params;

    @Override
    public Component makeTab(GaussianBlurTabParams params) {
        if (params == null)
            params = new GaussianBlurTabParams(new Size(7, 0), 25, 25, CvBorderTypes.BORDER_DEFAULT);
        this.params = params;

        return makeTab();
    }

    @Override
    public String getTitle() { return TAB_TITLE; }
    @Override
    public String getName() { return TAB_NAME; }

    @Override
    protected void applyOpencvFilter() {
        // TODO
        // input image; the image can have any number of channels, which are processed independently, but the depth should be CV_8U, CV_16U, CV_16S, CV_32F or CV_64F.

        Mat dst = new Mat();
        Imgproc.GaussianBlur(
            imageMat, // src
            dst,
            new org.opencv.core.Size(
                    params.kernelSize.width,
                    params.kernelSize.height),
            params.sigmaX,
            params.sigmaY,
            params.borderType.getVal());
        imageMat = dst;
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderIntModel modelKernelSizeW = new    SliderIntModel(params.kernelSize.width , 0, MIN_KSIZE, MAX_KSIZE);
        SliderIntModel modelKernelSizeH = new    SliderIntModel(params.kernelSize.height, 0, MIN_KSIZE, MAX_KSIZE);
        SliderDoubleModel modelSigmaX   = new SliderDoubleModel(params.sigmaX, 0, MIN_SIGMA, MAX_SIGMA);
        SliderDoubleModel modelSigmaY   = new SliderDoubleModel(params.sigmaY, 0, MIN_SIGMA, MAX_SIGMA);

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

        Box box4Borders = makeBox4Border(
                b -> (b != CvBorderTypes.BORDER_TRANSPARENT) // CvException [org.opencv.core.CvException: cv::Exception: OpenCV(3.4.2) C:\build\3_4_winpack-bindings-win64-vc14-static\opencv\modules\core\src\copy.cpp:940: error: (-5:Bad argument) Unknown/unsupported border type in function 'cv::borderInterpolate'
                  && (b != CvBorderTypes.BORDER_DEFAULT),    // dublicate, getterBorderType, setterBorderType, resetImage, tooltip, logger)
                () -> params.borderType,
                bt -> params.borderType = bt,
                this::resetImage,
                "Pixel extrapolation method",
                logger);

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(boxKernelSize);
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(boxSigma);
        box4Sliders.add(Box.createHorizontalGlue());

        Box boxOptions = Box.createVerticalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(box4Sliders);
        boxOptions.add(Box.createVerticalStrut(2));
        boxOptions.add(box4Borders);
        boxOptions.add(Box.createVerticalStrut(2));
        box4Options.add(boxOptions);

        modelKernelSizeW.getWrapped().addChangeListener(ev -> {
            logger.trace("modelKernelSizeW: value={}", modelKernelSizeW.getFormatedText());
            int val = modelKernelSizeW.getValue();
            int valValid = GaussianBlurTabParams.onlyZeroOrOdd(val, params.kernelSize.width);
            if (val == valValid) {
                params.kernelSize.width = valValid;
                resetImage();
            } else {
                SwingUtilities.invokeLater(() -> modelKernelSizeW.setValue(valValid));
            }
        });
        modelKernelSizeH.getWrapped().addChangeListener(ev -> {
            logger.trace("modelKernelSizeH: value={}", modelKernelSizeH.getFormatedText());
            int val = modelKernelSizeH.getValue();
            int valValid = GaussianBlurTabParams.onlyZeroOrOdd(val, params.kernelSize.height);
            if (val == valValid) {
                params.kernelSize.height = valValid;
                resetImage();
            } else {
                SwingUtilities.invokeLater(() -> modelKernelSizeH.setValue(valValid));
            }
        });
        modelSigmaX.getWrapped().addChangeListener(ev -> {
            logger.trace("modelSigmaX: value={}", modelSigmaX.getFormatedText());
            params.sigmaX = modelSigmaX.getValue();
            resetImage();
        });
        modelSigmaY.getWrapped().addChangeListener(ev -> {
            logger.trace("modelSigmaY: value={}", modelSigmaY.getFormatedText());
            params.sigmaY = modelSigmaY.getValue();
            resetImage();
        });

        return box4Options;
    }

    @Override
    public GaussianBlurTabParams getParams() {
        return params;
    }

    static Box makeBox4Border(
            Predicate<CvBorderTypes> filterOfBorderTypeValues,
            Supplier<CvBorderTypes> getterBorderType,
            Consumer<CvBorderTypes> setterBorderType,
            Runnable resetImage,
            String tooltip,
            Logger logger)
    {
        Box box4Borders = Box.createHorizontalBox();
        box4Borders.setBorder(BorderFactory.createTitledBorder("Border type"));
        Box box4Borders1 = Box.createVerticalBox();
        box4Borders1.setToolTipText(tooltip);
        ButtonGroup radioGroup = new ButtonGroup();
        Stream.of(CvBorderTypes.values())
            .filter(filterOfBorderTypeValues)
            .forEach(border ->
        {
            CvBorderTypes bt = getterBorderType.get();
            JRadioButton radioBtnAlg = new JRadioButton(border.name(), (border == bt) || (border.getVal() == bt.getVal()));
            radioBtnAlg.setToolTipText(tooltip);
            radioBtnAlg.addItemListener(ev -> {
                if (ev.getStateChange() == ItemEvent.SELECTED) {
                    setterBorderType.accept(border);
                    logger.trace("Border type changed to {}", border);
                    resetImage.run();
                }
            });
            box4Borders1.add(radioBtnAlg);
            radioGroup.add(radioBtnAlg);
        });
        box4Borders.add(Box.createHorizontalGlue());
        box4Borders.add(box4Borders1);
        box4Borders.add(Box.createHorizontalGlue());

        return box4Borders;
    }

}
