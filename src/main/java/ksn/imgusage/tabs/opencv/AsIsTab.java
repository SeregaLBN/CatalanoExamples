package ksn.imgusage.tabs.opencv;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.opencv.core.Mat;

import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.OpenCvHelper;

/** for testing internal classes {@link ImgHelper} {@link OpenCvHelper} */
public class AsIsTab extends OpencvFilterTab {

    private boolean isGray;

    public AsIsTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, null, false);
    }

    public AsIsTab(ITabHandler tabHandler, ITab source, Boolean boosting, boolean isGray) {
        super(tabHandler, source, boosting);
        this.isGray = isGray;

        makeTab();
    }

    @Override
    public String getTabName() { return "As is"; }

    @Override
    protected void applyFilter() {
        Mat mat = ImgHelper.toMat(getSourceImage());
        if ((boosting != null) && boosting.booleanValue())
            mat = boostImage(mat, logger);
        if (isGray)
            mat = OpenCvHelper.toGray(mat);

        image = ImgHelper.toBufferedImage(mat);
    }

    @Override
    protected void makeOptions(JPanel imagePanel, Box boxCenterLeft) {
        Box box = Box.createHorizontalBox();
        JCheckBox btnAsGray = new JCheckBox("Gray", isGray);
        btnAsGray.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAsGray.setToolTipText("Speed up by reducing the image");
        btnAsGray.addActionListener(ev -> {
            isGray = btnAsGray.isSelected();
            resetImage();
        });
        box.add(btnAsGray);
        boxCenterLeft.add(box);
    }

    @Override
    public void printParams() {
        logger.info("isGray={}", isGray);
    }

}
