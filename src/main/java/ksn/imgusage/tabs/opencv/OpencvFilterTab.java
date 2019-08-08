package ksn.imgusage.tabs.opencv;

import org.opencv.core.Mat;
import org.slf4j.Logger;

import ksn.imgusage.tabs.BaseTab;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.utils.ImgHelper;

public abstract class OpencvFilterTab extends BaseTab {
    
    public static final int BOOST_SIZE_MAX_X = 400;
    public static final int BOOST_SIZE_MAX_Y = 250;

    protected OpencvFilterTab(ITabHandler tabHandler, ITab source, Boolean boosting) {
        super(tabHandler, source, boosting);
    }

    public static Mat boostImage(Mat image, Logger logger) {
        double zoomX = BOOST_SIZE_MAX_X / (double)image.width();
        double zoomY = BOOST_SIZE_MAX_Y / (double)image.height();
        double zoom = Math.min(zoomX, zoomY);
        logger.trace("zoom={}", zoom);
        if (zoom < 1) {
            int newWidth  = (int)(zoom * image.width());
            int newHeight = (int)(zoom * image.height());
            return ImgHelper.resize(image, newWidth, newHeight);
        }
        return image;
    }

}
