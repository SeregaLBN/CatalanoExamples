package ksn.imgusage.tabs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.*;

import Catalano.Imaging.FastBitmap;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.UiHelper;

public class FirstTab extends BaseTab {

    public static final File DEFAULT_IMAGE = Paths.get("./exampleImages", "1024px-VolodimirHillAndDnieper.jpg").toFile();

    private static final int MIN_IMAGE_WIDTH  = 10;
    private static final int MIN_IMAGE_HEIGHT = 10;
    private static final int MAX_IMAGE_WIDTH  = 10000;
    private static final int MAX_IMAGE_HEIGHT = 10000;

    private BufferedImage sourceImage;
    private File sourceImageFile;
    private File latestImageDir;
    private boolean isGray;
    private boolean isScale;
    private final SliderIntModel modelSizeW;
    private final SliderIntModel modelSizeH;
    private boolean isKeepAspectRatio;

    public FirstTab(ITabHandler tabHandler) {
        this(tabHandler, DEFAULT_IMAGE, false, true, 0, 0, true);
    }
    public FirstTab(
        ITabHandler tabHandler,
        File imageFile,
        boolean isGray,
        boolean isScale,
        int imageSizeWidth,
        int imageSizeHeight,
        boolean isKeepAspectRatio
    ) {
        super(tabHandler, null);
        this.isGray  = isGray;
        this.isScale = isScale;
        if (imageSizeWidth < 1)
            imageSizeWidth = MAX_IMAGE_WIDTH;
        if (imageSizeHeight < 1)
            imageSizeHeight = MAX_IMAGE_HEIGHT;
        this.modelSizeW = new SliderIntModel(Math.max(imageSizeWidth , MIN_IMAGE_WIDTH ), 0, MIN_IMAGE_WIDTH , MAX_IMAGE_WIDTH);
        this.modelSizeH = new SliderIntModel(Math.max(imageSizeHeight, MIN_IMAGE_HEIGHT), 0, MIN_IMAGE_HEIGHT, MAX_IMAGE_HEIGHT);
        this.isKeepAspectRatio = isKeepAspectRatio;
        this.addRemoveFilterButton = false;

        readImageFile(imageFile);
        makeTab();
    }

    @Override
    public String getTabName() { return "Original"; }

    @Override
    protected BufferedImage getSourceImage() {
        return sourceImage;
    }

    public boolean isScale() {
        return isScale;
    }

    @Override
    protected void applyFilter() {
        image = ImgHelper.resize(sourceImage, modelSizeW.getValue(), modelSizeH.getValue());
        FastBitmap bmp = new FastBitmap(image);
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
            if (!imageFile.exists()) {
                logger.warn("File not found: {}", imageFile);
                return false;
            }
            sourceImage = ImageIO.read(imageFile);
            modelSizeW.setMaximum(sourceImage.getWidth());
            modelSizeH.setMaximum(sourceImage.getHeight());

            sourceImageFile = imageFile;
            latestImageDir = imageFile.getParentFile();
            resetImage();
            return true;
        } catch (IOException ex) {
            logger.error("Can`t read image", ex);
            return false;
        }
    }

    @Override
    protected void makeOptions(Box box4Options) {
        JButton btnLoadImage = new JButton("Load image...");
        btnLoadImage.addActionListener(ev -> {
            logger.trace("onSelectImage");

            File file = UiHelper.selectImageFile(latestImageDir);
            if (!readImageFile(file))
                return;

            resetImage();
        });
        //btnLoadImage.setBorder(BorderFactory.createTitledBorder("btnLoadImage"));
        btnLoadImage.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (sourceImage == null)
            SwingUtilities.invokeLater(btnLoadImage::doClick);

        JCheckBox btnAsGray = new JCheckBox("Gray", isGray);
        btnAsGray.addActionListener(ev -> {
            isGray  = btnAsGray.isSelected();
            resetImage();
        });
        //btnAsGray.setBorder(BorderFactory.createTitledBorder("btnAsGray"));
        btnAsGray.setAlignmentX(Component.LEFT_ALIGNMENT);

        JCheckBox btnScale = new JCheckBox("Scale", isScale);
        btnScale.addActionListener(ev -> {
            isScale = btnScale.isSelected();
            resetImage();
        });
        //btnScale.setBorder(BorderFactory.createTitledBorder("btnScale"));
        btnScale.setAlignmentX(Component.LEFT_ALIGNMENT);

        Runnable onCheckKeepAspectRationByWidth = () -> {
            if (isKeepAspectRatio) {
                double koef = modelSizeW.getValue() / (double)sourceImage.getWidth();
                double newHeight = sourceImage.getHeight() * koef;
                int currentHeight = modelSizeH.getValue();
                if (Math.abs(newHeight - currentHeight) > 1)
                    SwingUtilities.invokeLater(() -> modelSizeH.setValue((int)newHeight));
            }
        };
        Runnable onCheckKeepAspectRationByHeight = () -> {
            if (isKeepAspectRatio) {
                double koef = modelSizeH.getValue() / (double)sourceImage.getHeight();
                double newWidth = sourceImage.getWidth() * koef;
                int currentWidth = modelSizeW.getValue();
                if (Math.abs(newWidth - currentWidth) > 1)
                    SwingUtilities.invokeLater(() -> modelSizeW.setValue((int)newWidth));
            }
        };

        JPanel panelImageSize = new JPanel();
        panelImageSize.setLayout(new BorderLayout());
        panelImageSize.setBorder(BorderFactory.createTitledBorder("Size"));
        {
            Box box4ImageSize = Box.createHorizontalBox();
            box4ImageSize.add(Box.createHorizontalGlue());
            box4ImageSize.add(makeSliderVert(modelSizeW, "Width", "Image size"));
            box4ImageSize.add(Box.createHorizontalStrut(2));
            box4ImageSize.add(makeSliderVert(modelSizeH, "Height", "Image width"));
            box4ImageSize.add(Box.createHorizontalGlue());

            JCheckBox btnKeepAspectRatio = new JCheckBox("Keep aspect ratio", isKeepAspectRatio);
            btnKeepAspectRatio.addActionListener(ev -> {
                isKeepAspectRatio = btnKeepAspectRatio.isSelected();
                onCheckKeepAspectRationByWidth.run();
                resetImage();
            });

            panelImageSize.add(box4ImageSize     , BorderLayout.CENTER);
            panelImageSize.add(btnKeepAspectRatio, BorderLayout.SOUTH);
        }


        box4Options.add(btnLoadImage);
        box4Options.add(Box.createVerticalStrut(6));
        box4Options.add(btnAsGray);
        box4Options.add(Box.createVerticalStrut(2));
        box4Options.add(btnScale);
        box4Options.add(Box.createVerticalStrut(2));
        box4Options.add(panelImageSize);

        UiHelper.makeSameWidth(new Component[] { btnLoadImage, btnAsGray, btnScale });

        onCheckKeepAspectRationByWidth.run();

        modelSizeW.getWrapped().addChangeListener(ev -> {
            logger.trace("modelSizeW: value={}", modelSizeW.getFormatedText());
            onCheckKeepAspectRationByWidth.run();
            resetImage();
        });
        modelSizeH.getWrapped().addChangeListener(ev -> {
            logger.trace("modelSizeH: value={}", modelSizeH.getFormatedText());
            onCheckKeepAspectRationByHeight.run();
            resetImage();
        });
    }

    @Override
    public void printParams() {
        logger.info("file={}, isGray={}, size={{}x{}}, isKeeapAspectRatio={}",
            sourceImageFile,
            isGray,
            modelSizeW.getFormatedText(),
            modelSizeH.getFormatedText(),
            isKeepAspectRatio);
    }

}
