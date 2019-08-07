package ksn.imgusage.tabs.opencv;

import java.awt.Cursor;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.opencv.type.CvMorphTypes;
import ksn.imgusage.tabs.opencv.type.IMatter;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.UiHelper;

/** <a href='https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#ga67493776e3ad1a3df63883829375201f'>Performs advanced morphological transformations</a> */
public class MorphologyExTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(MorphologyExTab.class);

    private final ITabHandler tabHandler;
    private ITab source;
    private BufferedImage image;
    private boolean boosting = true;
    private Runnable imagePanelInvalidate;
    private CvMorphTypes morphologicalOperation;
    private IMatter kernel;
    private Timer timer;

    public MorphologyExTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.morphologicalOperation = CvMorphTypes.MORPH_GRADIENT;
        this.kernel = new IMatter.StructuringElementParams();

        makeTab();
    }

    public MorphologyExTab(ITabHandler tabHandler, ITab source, boolean boosting, CvMorphTypes morphologicalOperation, IMatter kernel) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.boosting = boosting;
        this.morphologicalOperation = morphologicalOperation;
        this.kernel = kernel;

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
            // Source image. The number of channels can be arbitrary. The depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F.

            try {
                Mat matDest = new Mat();
                Imgproc.morphologyEx(
                    matSrc,
                    matDest,
                    morphologicalOperation.getVal(),
                    kernel.createMat());

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
             "MorphologyEx",
             true,
             this::makeFilterOptions
         );
    }

    public void makeFilterOptions(JPanel imagePanel, Box boxCenterLeft) {
        imagePanelInvalidate = imagePanel::repaint;

        boxCenterLeft.add(UiHelper.makeAsBoostCheckBox(() -> boosting, b -> boosting = b, this::resetImage));
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
        logger.info("morphologicalOperation={}, kernel={}", morphologicalOperation, kernel);
    }

}
