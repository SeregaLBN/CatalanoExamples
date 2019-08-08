package ksn.imgusage.tabs;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import Catalano.Imaging.FastBitmap;
import ksn.imgusage.utils.UiHelper;

public class FirstTab extends BaseTab {

    public static final File DEFAULT_IMAGE = Paths.get("./exampleImages", "1024px-VolodimirHillAndDnieper.jpg").toFile();

    private BufferedImage sourceImage;
    private File latestImageDir;
    private boolean isGray;
    private boolean isScale;

    public FirstTab(ITabHandler tabHandler) {
        this(tabHandler, DEFAULT_IMAGE, false, true);
    }
    public FirstTab(
        ITabHandler tabHandler,
        File imageFile,
        boolean isGray,
        boolean isScale
    ) {
        super(tabHandler, null, false);
        this.isGray  = isGray;
        this.isScale = isScale;
        this.addRemoveFilterButton = false;

        readImageFile(imageFile);
        makeTab();
    }

    @Override
    public String getTabName() { return "Original"; }

    public boolean isScale() {
        return isScale;
    }

    @Override
    protected void applyFilter() {
        FastBitmap bmp = new FastBitmap(sourceImage);
        if (isGray && !bmp.isGrayscale())
            bmp.toGrayscale();
        image = bmp.toBufferedImage();
    }

    @Override
    public void updateSource(ITab newSource) {
        throw new UnsupportedOperationException("Illegal call");
    }

    private boolean readImageFile(File imageFile) {
        if (imageFile == null)
            return false;

        try {
            sourceImage = ImageIO.read(imageFile);
            latestImageDir = imageFile.getParentFile();
            resetImage();
            return true;
        } catch (IOException ex) {
            logger.error("Can`t read image", ex);
            return false;
        }
    }

    @Override
    protected void makeOptions(JPanel imagePanel, Box boxCenterLeft) {
        JButton btnLoadImage = new JButton("Load image...");
        btnLoadImage.addActionListener(ev -> {
            logger.trace("onSelectImage");

            File file = UiHelper.selectImageFile(latestImageDir);
            if (!readImageFile(file))
                return;

            imagePanel.repaint();
        });
        SwingUtilities.invokeLater(btnLoadImage::requestFocus);
        if (source == null)
            SwingUtilities.invokeLater(btnLoadImage::doClick);
        boxCenterLeft.add(btnLoadImage);

        boxCenterLeft.add(Box.createVerticalStrut(6));

        JCheckBox btnAsGray = new JCheckBox("Gray", isGray);
        btnAsGray.addActionListener(ev -> {
            isGray  = btnAsGray.isSelected();
            resetImage();
        });
        boxCenterLeft.add(btnAsGray);

        JCheckBox btnScale = new JCheckBox("Scale", isScale);
        btnScale.addActionListener(ev -> {
            isScale = btnScale.isSelected();
            imagePanel.repaint();
        });
        boxCenterLeft.add(btnScale);

        UiHelper.makeSameWidth(new Component[] { btnLoadImage, btnAsGray, btnScale });
    }

    @Override
    public void printParams() {
        logger.info("isGray={}", isGray);
    }

}
