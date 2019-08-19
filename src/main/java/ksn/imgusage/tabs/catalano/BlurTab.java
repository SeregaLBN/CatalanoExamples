package ksn.imgusage.tabs.catalano;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.Blur;
import ksn.imgusage.type.dto.catalano.BlurTabParams;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/Blur.java'>Blur filter</a> */
public class BlurTab extends CatalanoFilterTab<BlurTabParams> {

    public static final String TAB_TITLE = Blur.class.getSimpleName();
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Blur filter";

    private BlurTabParams params;

    public BlurTab() {
        super(false);
    }

    @Override
    public Component makeTab(BlurTabParams params) {
        this.params = params;
        return makeTab();
    }

    @Override
    public String getTitle() { return TAB_TITLE; }
    @Override
    public String getName() { return TAB_NAME; }
    @Override
    public String getDescription() { return TAB_DESCRIPTION; }

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
        return params;
    }

}
