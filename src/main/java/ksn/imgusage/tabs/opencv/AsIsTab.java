package ksn.imgusage.tabs.opencv;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.OpenCvHelper;
import ksn.imgusage.utils.UiHelper;

public class AsIsTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(AsIsTab.class);

    private final ITabHandler tabHandler;
    private ITab source;
    private BufferedImage image;
    private boolean boosting = true;
    private boolean isGray = false;
    private Runnable imagePanelInvalidate;

    public AsIsTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;

        makeTab();
    }

    public AsIsTab(ITabHandler tabHandler, ITab source, boolean boosting, boolean isGray) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.boosting = boosting;
        this.isGray = isGray;

        makeTab();
    }

    @Override
    public BufferedImage getImage() {
        if (image != null)
            return image;

        BufferedImage src = source.getImage();
        if (src == null)
            return null;

        JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(tabHandler.getTabPanel());
        try {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Mat mat = ImgHelper.toMat(src);
            if (boosting)
                mat = UiHelper.boostImage(mat, logger);
            if (isGray)
                mat = OpenCvHelper.toGray(mat);

            image = ImgHelper.toBufferedImage(mat);
        } finally {
            frame.setCursor(Cursor.getDefaultCursor());
        }
        return image;
    }


    @Override
    public void resetImage() {
        if (image == null)
            return;

        image = null;
        imagePanelInvalidate.run();
        SwingUtilities.invokeLater(() -> tabHandler.onImageChanged(this));
    }

    @Override
    public void updateSource(ITab newSource) {
        this.source = newSource;
        resetImage();
    }

    private void makeTab() {
        UiHelper.makeTab(
             tabHandler,
             this,
             "As is",
             true,
             this::makeFilterOptions
         );
    }

    public void makeFilterOptions(JPanel imagePanel, Box boxCenterLeft) {
        imagePanelInvalidate = imagePanel::repaint;

        boxCenterLeft.add(UiHelper.makeAsBoostCheckBox(() -> boosting, b -> boosting = b, this::resetImage));

        {
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
    }

    @Override
    public void printParams() {
        logger.info("isGray={}", isGray);
    }

}
