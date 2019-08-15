package ksn.imgusage.tabs.opencv;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;

import ksn.imgusage.type.dto.opencv.AsIsTabParams;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.OpenCvHelper;

/** for testing internal classes {@link ImgHelper} {@link OpenCvHelper} */
public class AsIsTab extends OpencvFilterTab<AsIsTabParams> {

    public static final String TAB_NAME = "AsIs";
    public static final String TAB_FULL_NAME = TAB_PREFIX + TAB_NAME;
    public static final String TAB_DESCRIPTION = "As is";

    private AsIsTabParams params;

    @Override
    public Component makeTab(AsIsTabParams params) {
        if (params == null)
            params = new AsIsTabParams(false);
        this.params = params;

        return makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }
    @Override
    public String getTabFullName() { return TAB_FULL_NAME; }

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
        JCheckBox btnAsGray = new JCheckBox("Gray", params.useGray);
        btnAsGray.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAsGray.setToolTipText("Speed up by reducing the image");
        btnAsGray.addActionListener(ev -> {
            params.useGray = btnAsGray.isSelected();
            resetImage();
        });
        box.add(btnAsGray);
        box4Options.add(box);

        return box4Options;
    }

    @Override
    public AsIsTabParams getParams() {
        return params;
    }

}
