package ksn.imgusage.tabs.opencv;

import java.awt.Component;
import java.awt.Container;
import java.util.function.Consumer;

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
    private Consumer<String> showImageSize;

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
        showImageSize.accept(imageMat.width() + "x" + imageMat.height());
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

        Container cntrlEditBoxSize = makeEditBox(x -> showImageSize = x, null, "Image size", null, null);
        showImageSize.accept("X*Y");
        box4Options.add(cntrlEditBoxSize);

        return box4Options;
    }

    @Override
    public AsIsTabParams getParams() {
        return params;
    }

}
