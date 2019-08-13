package ksn.imgusage.tabs.opencv;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JCheckBox;

import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.OpenCvHelper;

/** for testing internal classes {@link ImgHelper} {@link OpenCvHelper} */
public class AsIsTab extends OpencvFilterTab<AsIsTab.Params> {

    public static final String TAB_NAME = "AsIs";
    public static final String TAB_DESCRIPTION = "As is";

    public static class Params implements ITabParams {
        public boolean useGray;
        public Params(boolean useGray) { this.useGray = useGray; }
        @Override
        public String toString() { return "{ useGray=" + useGray + " }"; }
    }

    private Params params;

    public AsIsTab(ITabHandler tabHandler, ITab<?> source) {
        this(tabHandler, source, new Params(false));
    }

    public AsIsTab(ITabHandler tabHandler, ITab<?> source, Params params) {
        super(tabHandler, source);
        this.params = params;

        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyOpencvFilter() {
        if (params.useGray)
            imageMat = OpenCvHelper.toGray(imageMat);
    }

    @Override
    protected void makeOptions(Box box4Options) {
        Box box = Box.createHorizontalBox();
        JCheckBox btnAsGray = new JCheckBox("Gray", params.useGray);
        btnAsGray.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAsGray.setToolTipText("Speed up by reducing the image");
        btnAsGray.addActionListener(ev -> {
            params.useGray = btnAsGray.isSelected();
            resetImage();
        });
        box.add(btnAsGray);
        box4Options.add(box);
    }

    @Override
    public Params getParams() {
        return params;
    }

}
