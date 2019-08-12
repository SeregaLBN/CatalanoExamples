package ksn.imgusage.tabs.catalano;

import javax.swing.Box;

import Catalano.Imaging.Filters.Blur;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/Blur.java'>Blur filter</a> */
public class BlurTab extends CatalanoFilterTab {

    public static final String TAB_NAME = Blur.class.getSimpleName();
    public static final String TAB_DESCRIPTION = "Blur filter";

    public BlurTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, true);
    }

    public BlurTab(ITabHandler tabHandler, ITab source, boolean boosting) {
        super(tabHandler, source, boosting, false);
        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyCatalanoFilter() {
        new Blur().applyInPlace(imageFBmp);
    }

    @Override
    protected void makeOptions(Box box4Options) {
        // none
    }

    @Override
    public void printParams() {
        logger.info("I am :)");
    }

}
