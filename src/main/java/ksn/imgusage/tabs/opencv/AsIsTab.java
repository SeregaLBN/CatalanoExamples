package ksn.imgusage.tabs.opencv;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;

import ksn.imgusage.type.dto.opencv.AsIsTabParams;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.OpenCvHelper;

/** for testing internal classes {@link ImgHelper} {@link OpenCvHelper} */
public class AsIsTab extends OpencvFilterTab<AsIsTabParams> {

    public static final String TAB_TITLE = "AsIs";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "As is";

    private AsIsTabParams params;

    @Override
    public Component makeTab(AsIsTabParams params) {
        if (params == null)
            params = new AsIsTabParams();
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
    protected void applyOpencvFilter() {
        if (params.useGray)
            imageMat = OpenCvHelper.toGray(imageMat);
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        Box box = Box.createHorizontalBox();
        box.add(makeCheckBox(
            () -> params.useGray,
            v  -> params.useGray = v,
            "Gray",
            "params.useGray",
            "Speed up by reducing the image", null));
        box4Options.add(box);

        return box4Options;
    }

    @Override
    public AsIsTabParams getParams() {
        return params;
    }

}
