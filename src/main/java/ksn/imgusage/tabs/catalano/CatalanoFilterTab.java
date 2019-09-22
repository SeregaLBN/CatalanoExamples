package ksn.imgusage.tabs.catalano;

import java.awt.image.BufferedImage;

import Catalano.Imaging.FastBitmap;
import ksn.imgusage.tabs.BaseTab;
import ksn.imgusage.tabs.ITabParams;

public abstract class CatalanoFilterTab<TTabParams extends ITabParams> extends BaseTab<TTabParams>  {

    public static final String TAB_PREFIX = "Catalano:";

    /** filter only works with gray image */
    protected final boolean onlyGray;
    /** filtered image of the current tab */
    protected FastBitmap imageFBmp;

    protected CatalanoFilterTab(boolean onlyGray) {
        this.onlyGray = onlyGray;
    }

    protected FastBitmap getSourceFastBitmap() {
        if (source instanceof CatalanoFilterTab)
            return ((CatalanoFilterTab<?>)source).imageFBmp;

        BufferedImage src = source.getImage();
        if (src == null)
            return null;
        return new FastBitmap(src);
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
    public void resetImage(boolean debounce) {
        imageFBmp = null;
        super.resetImage(debounce);
    }

}
