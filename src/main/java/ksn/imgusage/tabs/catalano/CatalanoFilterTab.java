package ksn.imgusage.tabs.catalano;

import java.awt.image.BufferedImage;

import Catalano.Imaging.FastBitmap;
import ksn.imgusage.tabs.BaseTab;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;

public abstract class CatalanoFilterTab extends BaseTab {

    public static final String TAB_PREFIX = "Catalano:";

    /** filter only works with gray image */
    protected final boolean onlyGray;
    /** filtered image of the current tab */
    protected FastBitmap imageFBmp;

    protected CatalanoFilterTab(ITabHandler tabHandler, ITab source, boolean onlyGray) {
        super(tabHandler, source);
        this.onlyGray = onlyGray;
    }

    protected FastBitmap getSourceFastBitmap() {
        if (source instanceof CatalanoFilterTab) {
            return ((CatalanoFilterTab)source).imageFBmp;
        } else {
            BufferedImage src = source.getImage();
            if (src == null)
                return null;
            return new FastBitmap(src);
        }
    }

    protected abstract void applyCatalanoFilter();

    @Override
    protected final void applyFilter() {
        imageFBmp = new FastBitmap(getSourceFastBitmap()); // clone

        // predefined filters
        if (onlyGray && !imageFBmp.isGrayscale())
            imageFBmp.toGrayscale();

        // specific filter
        applyCatalanoFilter();

        image = imageFBmp.toBufferedImage();
    }

    @Override
    public void resetImage() {
        imageFBmp = null;
        super.resetImage();
    }

}
