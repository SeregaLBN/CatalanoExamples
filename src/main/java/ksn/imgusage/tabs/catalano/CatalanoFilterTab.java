package ksn.imgusage.tabs.catalano;

import org.slf4j.Logger;

import Catalano.Imaging.FastBitmap;
import ksn.imgusage.tabs.BaseTab;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.opencv.OpencvFilterTab;
import ksn.imgusage.utils.ImgHelper;

public abstract class CatalanoFilterTab extends BaseTab {

    protected CatalanoFilterTab(ITabHandler tabHandler, ITab source, boolean boosting) {
        super(tabHandler, source, boosting);
    }

    public static FastBitmap boostImage(FastBitmap image, Logger logger) {
        double zoomX = OpencvFilterTab.BOOST_SIZE_MAX_X / (double)image.getWidth();
        double zoomY = OpencvFilterTab.BOOST_SIZE_MAX_Y / (double)image.getHeight();
        double zoom = Math.min(zoomX, zoomY);
        logger.trace("zoom={}", zoom);
        if (zoom < 1) {
            int newWidth  = (int)(zoom * image.getWidth());
            int newHeight = (int)(zoom * image.getHeight());
            return ImgHelper.resize(image, newWidth, newHeight);
        }
        return image;
    }

}
