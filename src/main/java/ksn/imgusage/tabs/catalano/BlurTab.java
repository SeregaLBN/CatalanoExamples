package ksn.imgusage.tabs.catalano;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.Blur;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.type.dto.catalano.BlurTabParams;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/Blur.java'>Blur filter</a> */
public class BlurTab extends CatalanoFilterTab<BlurTabParams> {

    public static final String TAB_NAME = Blur.class.getSimpleName();
    public static final String TAB_FULL_NAME = TAB_PREFIX + TAB_NAME;
    public static final String TAB_DESCRIPTION = "Blur filter";

    public BlurTab(ITabHandler tabHandler, ITab<?> source) {
        this(tabHandler, source, null);
    }

    public BlurTab(ITabHandler tabHandler, ITab<?> source, BlurTabParams params) {
        super(tabHandler, source, false);
        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }
    @Override
    public String getTabFullName() { return TAB_FULL_NAME; }

    @Override
    protected void applyCatalanoFilter() {
        new Blur().applyInPlace(imageFBmp);
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        // none..

        return box4Options;
    }

    @Override
    public BlurTabParams getParams() {
        return null;
    }

}
