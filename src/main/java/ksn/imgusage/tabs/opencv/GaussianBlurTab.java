package ksn.imgusage.tabs.opencv;

import java.awt.Cursor;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import javax.swing.*;

import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.opencv.type.CvBorderTypes;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.UiHelper;

/** <a href='https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#gaabe8c836e97159a9193fb0b11ac52cf1'>Blurs an image using a Gaussian filter</a> */
public class GaussianBlurTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(GaussianBlurTab.class);

    private static final int MIN_KSIZE    =   0; // Gaussian kernel size. ksize.width and ksize.height can differ but they both must be positive and odd. Or,
    private static final int MAX_KSIZE    = 999; //  they can be zero’s and then they are computed from sigma* .
    private static final double MIN_SIGMA =   0; // Gaussian kernel standard deviation in X direction.
    private static final double MAX_SIGMA = 200; // Gaussian kernel standard deviation in Y direction; if sigmaY is zero,
                                                 //  it is set to be equal to sigmaX, if both sigmas are zeros, they are computed from ksize.width and ksize.height ,
                                                 //  respectively (see getGaussianKernel() for details); to fully control the result regardless of possible future
                                                 //  modifications of all this semantics, it is recommended to specify all of ksize, sigmaX, and sigmaY.

    private final ITabHandler tabHandler;
    private ITab source;
    private BufferedImage image;
    private boolean boosting = true;
    private Runnable imagePanelInvalidate;
    private SliderIntModel modelKernelSizeW = new    SliderIntModel( 7, 0, MIN_KSIZE, MAX_KSIZE);
    private SliderIntModel modelKernelSizeH = new    SliderIntModel( 0, 0, MIN_KSIZE, MAX_KSIZE);
    private SliderDoubleModel modelSigmaX   = new SliderDoubleModel(25, 0, MIN_SIGMA, MAX_SIGMA);
    private SliderDoubleModel modelSigmaY   = new SliderDoubleModel(25, 0, MIN_SIGMA, MAX_SIGMA);
    private CvBorderTypes borderType = CvBorderTypes.BORDER_DEFAULT;
    private Timer timer;

    public GaussianBlurTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;

        makeTab();
    }

    public GaussianBlurTab(ITabHandler tabHandler, ITab source, boolean boosting, Size kernelSize, double sigmaX, double sigmaY, CvBorderTypes borderType) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.boosting = boosting;
        this.modelKernelSizeW.setValue(onlyZeroOrOdd((int)kernelSize.width));
        this.modelKernelSizeH.setValue(onlyZeroOrOdd((int)kernelSize.height));
        this.modelSigmaX     .setValue(sigmaX);
        this.modelSigmaY     .setValue(sigmaY);
        this.borderType = borderType;

        makeTab();
    }

    @Override
    public BufferedImage getImage() {
        if (image != null)
            return image;

        BufferedImage src = source.getImage();
        if (src == null)
            return null;

        JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(tabHandler.getTabPanel());
        try {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Mat matSrc = ImgHelper.toMat(src);
            // TODO
            // input image; the image can have any number of channels, which are processed independently, but the depth should be CV_8U, CV_16U, CV_16S, CV_32F or CV_64F.

            try {
                Mat matDest = new Mat();
                Imgproc.GaussianBlur(
                    matSrc,
                    matDest,
                    new Size(modelKernelSizeW.getValue(),
                             modelKernelSizeH.getValue()),
                    modelSigmaX.getValue(),
                    modelSigmaY.getValue(),
                    borderType.getVal());

                image = ImgHelper.toBufferedImage(matDest);
            } catch (CvException ex) {
                logger.error(ex.toString());
                image = ImgHelper.failedImage();
            }
        } finally {
            frame.setCursor(Cursor.getDefaultCursor());
        }
        return image;
    }


    @Override
    public void resetImage() {
        if (image == null)
            return;

        image = null;
        imagePanelInvalidate.run();
        SwingUtilities.invokeLater(() -> tabHandler.onImageChanged(this));
    }

    @Override
    public void updateSource(ITab newSource) {
        this.source = newSource;
        resetImage();
    }

    private void makeTab() {
        UiHelper.makeTab(
             tabHandler,
             this,
             "GaussianBlur",
             true,
             this::makeFilterOptions
         );
    }

    public void makeFilterOptions(JPanel imagePanel, Box boxCenterLeft) {
        imagePanelInvalidate = imagePanel::repaint;

        boxCenterLeft.add(UiHelper.makeAsBoostCheckBox(() -> boosting, b -> boosting = b, this::resetImage));

        {
            Box boxKernelSize = Box.createHorizontalBox();
            boxKernelSize.setBorder(BorderFactory.createTitledBorder("Kernel size"));
            boxKernelSize.setToolTipText("Gaussian kernel size. ksize.width and ksize.height can differ but they both must be positive and odd."
                                       + " Or they can be zero’s and then they are computed from sigma* .");
            boxKernelSize.add(Box.createHorizontalGlue());
            UiHelper.makeSliderVert(boxKernelSize, modelKernelSizeW, "Width", "Kernel size Width");
            boxKernelSize.add(Box.createHorizontalStrut(2));
            UiHelper.makeSliderVert(boxKernelSize, modelKernelSizeH, "Height", "Kernel size Height");
            boxKernelSize.add(Box.createHorizontalGlue());

            Box boxSigma = Box.createHorizontalBox();
            boxSigma.setBorder(BorderFactory.createTitledBorder("Sigma"));

            boxSigma.add(Box.createHorizontalGlue());
            UiHelper.makeSliderVert(boxSigma, modelSigmaX, "X", "Gaussian kernel standard deviation in X direction");
            boxSigma.add(Box.createHorizontalStrut(2));
            UiHelper.makeSliderVert(boxSigma, modelSigmaY, "Y", "Gaussian kernel standard deviation in Y direction; if sigmaY is zero, it is set to be equal to sigmaX, if both sigmas are zeros, they are computed from ksize.width and ksize.height , respectively (see getGaussianKernel() for details); to fully control the result regardless of possible future modifications of all this semantics, it is recommended to specify all of ksize, sigmaX, and sigmaY.");
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
            boxOptions.setBorder(BorderFactory.createTitledBorder("GaussianBlur"));
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
                    debounceResetImage();
                else
                    SwingUtilities.invokeLater(() -> modelKernelSizeW.setValue(valValid));
            });
            modelKernelSizeH.getWrapped().addChangeListener(ev -> {
                logger.trace("modelKernelSizeH: value={}", modelKernelSizeH.getFormatedText());
                int val = modelKernelSizeH.getValue();
                int valValid = onlyZeroOrOdd(val);
                if (val == valValid)
                    debounceResetImage();
                else
                    SwingUtilities.invokeLater(() -> modelKernelSizeH.setValue(valValid));
            });
            modelSigmaX.getWrapped().addChangeListener(ev -> {
                logger.trace("modelSigmaX: value={}", modelSigmaX.getFormatedText());
                debounceResetImage();
            });
            modelSigmaY.getWrapped().addChangeListener(ev -> {
                logger.trace("modelSigmaY: value={}", modelSigmaY.getFormatedText());
                debounceResetImage();
            });
        }
    }

    private void debounceResetImage() {
        UiHelper.debounceExecutor(() -> timer, t -> timer = t, 300, this::resetImage, logger);
    }

    private static int onlyZeroOrOdd(int value) {
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
