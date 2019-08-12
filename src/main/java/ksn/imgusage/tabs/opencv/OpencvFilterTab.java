package ksn.imgusage.tabs.opencv;

import java.awt.image.BufferedImage;

import org.opencv.core.Mat;

import ksn.imgusage.tabs.BaseTab;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.utils.ImgHelper;

public abstract class OpencvFilterTab extends BaseTab {

    public static final String TAB_PREFIX = "OpenCV:";

    public static final int BOOST_SIZE_MAX_X = 400;
    public static final int BOOST_SIZE_MAX_Y = 250;

    /** filtered image of the current tab */
    protected Mat imageMat;

    protected OpencvFilterTab(ITabHandler tabHandler, ITab source) {
        super(tabHandler, source);
    }

    protected Mat getSourceMat() {
        if (source instanceof OpencvFilterTab) {
            return ((OpencvFilterTab)source).imageMat;
        } else {
            BufferedImage src = source.getImage();
            if (src == null)
                return null;
            return ImgHelper.toMat(src);
        }
    }

    protected abstract void applyOpencvFilter();

    @Override
    protected final void applyFilter() {
        Mat srcMat = getSourceMat();
        imageMat = srcMat.clone();

        // specific filter
        applyOpencvFilter();

        image = ImgHelper.toBufferedImage(imageMat);
    }

    @Override
    public void resetImage() {
        imageMat = null;
        super.resetImage();
    }

}
