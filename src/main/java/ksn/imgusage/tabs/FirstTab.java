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
import javax.swing.event.ChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import ksn.imgusage.utils.UiHelper;

public class FirstTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(FirstTab.class);
    public static final File DEFAULT_IMAGE = Paths.get("./exampleImages", "1024px-VolodimirHillAndDnieper.jpg").toFile();

    private final ITabHandler tabHandler;

    private BufferedImage source;
    private BufferedImage image;
    private File latestImageDir;
    private Runnable imagePanelInvalidate;

    private boolean isGray = false;
    private boolean isScale = true;


    public FirstTab(ITabHandler tabHandler) {
        this.tabHandler = tabHandler;

        latestImageDir = DEFAULT_IMAGE.getParentFile();
        if (!latestImageDir.exists())
            latestImageDir = null;

        makeTab();
    }
    public FirstTab(
        ITabHandler tabHandler,
        File imageFile,
        boolean isGray,
        boolean isScale
    ) {
        this.tabHandler = tabHandler;
        this.isGray  = isGray;
        this.isScale = isScale;
        if (readImageFile(imageFile))
            latestImageDir = imageFile.getParentFile();

        makeTab();
    }

    public boolean isScale() {
        return isScale;
    }

    @Override
    public BufferedImage getImage() {
        if (source == null)
            return null;

        if (image == null) {
            FastBitmap bmp = new FastBitmap(source);
            if (isGray && !bmp.isGrayscale())
                bmp.toGrayscale();
            image = bmp.toBufferedImage();
        }
        return image;
    }


    @Override
    public void resetImage() {
        if (image == null)
            return;

        image = null;
        if (imagePanelInvalidate != null)
            imagePanelInvalidate.run();
        SwingUtilities.invokeLater(() -> tabHandler.onImageChanged(this));
    }

    @Override
    public void updateSource(ITab newSource) {
        throw new UnsupportedOperationException("Illegal call");
    }

    private void makeTab() {
        UiHelper.makeTab(
            tabHandler,
            this,
            "Original",
            false,
            this::makeOptions
        );
    }


    private boolean readImageFile(File imageFile) {
        if (imageFile == null)
            return false;

        try {
            source = ImageIO.read(imageFile);
            resetImage();
            return true;
        } catch (IOException ex) {
            logger.error("Can`t read image", ex);
            return false;
        }
    }

    public void makeOptions(JPanel imagePanel, Box boxCenterLeft) {
        imagePanelInvalidate = imagePanel::repaint;

        JButton btnLoadImage = new JButton("Load image...");
        btnLoadImage.addActionListener(ev -> {
            logger.trace("onSelectImage");

            File file = UiHelper.selectImageFile(latestImageDir);
            if (!readImageFile(file))
                return;

            latestImageDir = file.getParentFile();
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


    void onTabChanged(ChangeEvent ev) {
        logger.info("onTabChanged");
    }

}
