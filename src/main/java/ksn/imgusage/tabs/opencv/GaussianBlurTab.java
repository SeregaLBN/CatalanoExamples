package ksn.imgusage.tabs.opencv;

import java.awt.Cursor;
import java.awt.image.BufferedImage;

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
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.UiHelper;

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
    private SliderIntModel modelKernelSizeX = new SliderIntModel(1, 0, MIN_KSIZE , MAX_KSIZE);
    private SliderIntModel modelKernelSizeY = new SliderIntModel(0, 0, MIN_KSIZE , MAX_KSIZE);
    private SliderDoubleModel modelSigmaX   = new SliderDoubleModel(0.10, 0, MIN_SIGMA, MAX_SIGMA);
    private SliderDoubleModel modelSigmaY   = new SliderDoubleModel(1.00, 0, MIN_SIGMA, MAX_SIGMA);
    private int borderType = 0; // TODO from c++ code see BORDER_DEFAULT
    private Timer timer;

    public GaussianBlurTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;

        makeTab();
    }

    public GaussianBlurTab(ITabHandler tabHandler, ITab source, boolean boosting, int kernelSizeX, int kernelSizeY, double sigmaX, double sigmaY, int borderType) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.boosting = boosting;
        this.modelKernelSizeX.setValue(kernelSizeX);
        this.modelKernelSizeY.setValue(kernelSizeY);
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
                Imgproc.GaussianBlur(matSrc, matDest, new Size(modelKernelSizeX.getValue(), modelKernelSizeY.getValue()), modelSigmaX.getValue(), modelKernelSizeY.getValue());

                image = ImgHelper.toBufferedImage(matDest);
            } catch (CvException ex) {
                logger.error("", ex);
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
            Box boxOptions = Box.createHorizontalBox();
            boxOptions.setBorder(BorderFactory.createTitledBorder("GaussianBlur"));

            boxOptions.add(Box.createHorizontalGlue());
            UiHelper.makeSliderVert(boxOptions, modelKernelSizeX     , "kSizeX"     , "Kernel size X");
            boxOptions.add(Box.createHorizontalStrut(2));
            UiHelper.makeSliderVert(boxOptions, modelKernelSizeY     , "kSizeY"     , "Kernel size Y");
            boxOptions.add(Box.createHorizontalStrut(2));
            UiHelper.makeSliderVert(boxOptions, modelSigmaX, "SigmaX", "The minimum gain factor");
            boxOptions.add(Box.createHorizontalStrut(2));
            UiHelper.makeSliderVert(boxOptions, modelSigmaY, "SigmaY", "The maximum gain factor");
            boxOptions.add(Box.createHorizontalGlue());

            boxCenterLeft.add(boxOptions);

            modelKernelSizeX.getWrapped().addChangeListener(ev -> {
                logger.trace("modelKernelSizeX: value={}", modelKernelSizeX.getFormatedText());
                debounceResetImage();
            });
            modelKernelSizeY.getWrapped().addChangeListener(ev -> {
                logger.trace("modelKernelSizeY: value={}", modelKernelSizeY.getFormatedText());
                debounceResetImage();
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

}
