package ksn.imgusage.tabs.catalano;

import javax.swing.Box;
import javax.swing.JPanel;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Blur;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/Blur.java'>Blur filter</a> */
public class BlurTab extends CatalanoFilterTab {

    public BlurTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, true);
    }

    public BlurTab(ITabHandler tabHandler, ITab source, boolean boosting) {
        super(tabHandler, source, boosting);
        makeTab();
    }

    @Override
    public String getTabName() { return Blur.class.getSimpleName(); }

    @Override
    protected void applyFilter() {
        FastBitmap bmp = new FastBitmap(source.getImage());
        if (boosting)
            bmp = boostImage(bmp, logger);

        Blur blur = new Blur();
        blur.applyInPlace(bmp);

        image = bmp.toBufferedImage();
    }

    @Override
    protected void makeOptions(JPanel imagePanel, Box boxCenterLeft) {
        // none
    }

    @Override
    public void printParams() {
        logger.info("I am :)");
    }

}
