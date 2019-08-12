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

    public static class SizeImage {
        public int width, height;
        public SizeImage(int width, int height) { this.width = width; this.height = height; }
    }
    /** paddion of Region Of Interest */
    public static class BoundOfRoi {
        public int left, top, right, bottom;
        public BoundOfRoi(int left, int top, int right, int bottom) { this.left = left; this.top = top; this.right = right; this.bottom = bottom; }
    }

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
    private final SliderIntModel modelPadLeft;
    private final SliderIntModel modelPadRight;
    private final SliderIntModel modelPadTop;
    private final SliderIntModel modelPadBottom;

    public FirstTab(ITabHandler tabHandler) {
        this(tabHandler, DEFAULT_IMAGE, false, true, new SizeImage(-1, -1), true, new BoundOfRoi(0,0,0,0));
    }
    public FirstTab(
        ITabHandler tabHandler,
        File imageFile,
        boolean isGray,
        boolean isScale,
        SizeImage imageResize,
        boolean isKeepAspectRatio,
        BoundOfRoi roi
    ) {
        super(tabHandler, null);
        this.isGray  = isGray;
        this.isScale = isScale;
        if (imageResize.width < MIN_IMAGE_WIDTH)
            imageResize.width = MAX_IMAGE_WIDTH;
        if (imageResize.height < MIN_IMAGE_HEIGHT)
            imageResize.height = MAX_IMAGE_HEIGHT;
        this.modelSizeW = new SliderIntModel(Math.max(imageResize.width , MIN_IMAGE_WIDTH ), 0, MIN_IMAGE_WIDTH , MAX_IMAGE_WIDTH);
        this.modelSizeH = new SliderIntModel(Math.max(imageResize.height, MIN_IMAGE_HEIGHT), 0, MIN_IMAGE_HEIGHT, MAX_IMAGE_HEIGHT);
        this.isKeepAspectRatio = isKeepAspectRatio;
        this.addRemoveFilterButton = false;
        this.modelPadLeft   = new SliderIntModel(roi.left  , 0, 0, MAX_IMAGE_WIDTH);
        this.modelPadRight  = new SliderIntModel(roi.right , 0, 0, MAX_IMAGE_WIDTH);
        this.modelPadTop    = new SliderIntModel(roi.top   , 0, 0, MAX_IMAGE_HEIGHT);
        this.modelPadBottom = new SliderIntModel(roi.bottom, 0, 0, MAX_IMAGE_HEIGHT);

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
            modelPadLeft  .setMaximum(sourceImage.getWidth()  - 1);
            modelPadRight .setMaximum(sourceImage.getWidth()  - 1);
            modelPadTop   .setMaximum(sourceImage.getHeight() - 1);
            modelPadBottom.setMaximum(sourceImage.getHeight() - 1);

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
        btnLoadImage.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (sourceImage == null)
            SwingUtilities.invokeLater(btnLoadImage::doClick);

        JCheckBox btnAsGray = new JCheckBox("Gray", isGray);
        btnAsGray.addActionListener(ev -> {
            isGray  = btnAsGray.isSelected();
            resetImage();
        });
        btnAsGray.setAlignmentX(Component.LEFT_ALIGNMENT);

        JCheckBox btnScale = new JCheckBox("Scale", isScale);
        btnScale.addActionListener(ev -> {
            isScale = btnScale.isSelected();
            resetImage();
        });
        btnScale.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panelImageSize = new JPanel();
        {
            panelImageSize.setLayout(new BorderLayout());
            panelImageSize.setBorder(BorderFactory.createTitledBorder("Size"));

            Box box4ImageSize = Box.createHorizontalBox();
            box4ImageSize.add(Box.createHorizontalGlue());
            box4ImageSize.add(makeSliderVert(modelSizeW, "Width", "Image size"));
            box4ImageSize.add(Box.createHorizontalStrut(2));
            box4ImageSize.add(makeSliderVert(modelSizeH, "Height", "Image width"));
            box4ImageSize.add(Box.createHorizontalGlue());

            JCheckBox btnKeepAspectRatio = new JCheckBox("Keep aspect ratio", isKeepAspectRatio);
            btnKeepAspectRatio.addActionListener(ev -> {
                isKeepAspectRatio = btnKeepAspectRatio.isSelected();
                onCheckKeepAspectRationByWidth();
                resetImage();
            });

            panelImageSize.add(box4ImageSize     , BorderLayout.CENTER);
            panelImageSize.add(btnKeepAspectRatio, BorderLayout.SOUTH);
        }

        Box boxOfRoi = Box.createHorizontalBox();
        {
            boxOfRoi.setBorder(BorderFactory.createTitledBorder("ROI"));
            boxOfRoi.setToolTipText("Region Of Interest");

            boxOfRoi.add(Box.createHorizontalStrut(8));
            boxOfRoi.add(makeSliderVert(modelPadLeft  , "Left"  , "Padding left"));
            boxOfRoi.add(Box.createHorizontalStrut(2));
            boxOfRoi.add(makeSliderVert(modelPadRight , "Right" , "Padding right"));
            boxOfRoi.add(Box.createHorizontalStrut(2));
            boxOfRoi.add(makeSliderVert(modelPadTop   , "Top"   , "Padding top"));
            boxOfRoi.add(Box.createHorizontalStrut(2));
            boxOfRoi.add(makeSliderVert(modelPadBottom, "Bottom", "Padding bottom"));
            boxOfRoi.add(Box.createHorizontalStrut(8));
        }

        box4Options.add(btnLoadImage);
        box4Options.add(Box.createVerticalStrut(6));
        box4Options.add(btnAsGray);
        box4Options.add(Box.createVerticalStrut(2));
        box4Options.add(btnScale);
        box4Options.add(Box.createVerticalStrut(2));
        box4Options.add(panelImageSize);
        box4Options.add(Box.createVerticalStrut(2));
        box4Options.add(boxOfRoi);

        UiHelper.makeSameWidth(new Component[] { btnLoadImage, btnAsGray, btnScale });

        onCheckKeepAspectRationByWidth();

        modelSizeW.getWrapped().addChangeListener(ev -> {
            logger.trace("modelSizeW: value={}", modelSizeW.getFormatedText());
            onCheckKeepAspectRationByWidth();
            resetImage();
        });
        modelSizeH.getWrapped().addChangeListener(ev -> {
            logger.trace("modelSizeH: value={}", modelSizeH.getFormatedText());
            onCheckKeepAspectRationByHeight();
            resetImage();
        });

        modelPadLeft.getWrapped().addChangeListener(ev -> {
            logger.trace("modelPadLeft: value={}", modelPadLeft.getFormatedText());
            if ((modelPadLeft.getValue() + modelPadRight.getValue()) >= sourceImage.getWidth())
                SwingUtilities.invokeLater(() -> modelPadRight.setValue(sourceImage.getWidth() - 1 - modelPadLeft.getValue()) );
            resetImage();
        });
        modelPadRight.getWrapped().addChangeListener(ev -> {
            logger.trace("modelPadRight: value={}", modelPadRight.getFormatedText());
            if ((modelPadLeft.getValue() + modelPadRight.getValue()) >= sourceImage.getWidth())
                SwingUtilities.invokeLater(() -> modelPadLeft.setValue(sourceImage.getWidth() - 1 - modelPadRight.getValue()) );
            resetImage();
        });
        modelPadTop.getWrapped().addChangeListener(ev -> {
            logger.trace("modelPadTop: value={}", modelPadTop.getFormatedText());
            if ((modelPadTop.getValue() + modelPadBottom.getValue()) >= sourceImage.getHeight())
                SwingUtilities.invokeLater(() -> modelPadBottom.setValue(sourceImage.getHeight() - 1 - modelPadTop.getValue()) );
            resetImage();
        });
        modelPadBottom.getWrapped().addChangeListener(ev -> {
            logger.trace("modelPadBottom: value={}", modelPadBottom.getFormatedText());
            if ((modelPadTop.getValue() + modelPadBottom.getValue()) >= sourceImage.getHeight())
                SwingUtilities.invokeLater(() -> modelPadTop.setValue(sourceImage.getHeight() - 1 - modelPadBottom.getValue()) );
            resetImage();
        });
    }

    private boolean lockCheckKeepAspectRation;
    private void onCheckKeepAspectRationByWidth() {
        if (lockCheckKeepAspectRation)
            return;
        lockCheckKeepAspectRation = true;
        try {
            if (isKeepAspectRatio) {
                double koef = modelSizeW.getValue() / (double)sourceImage.getWidth();
                double newHeight = sourceImage.getHeight() * koef;
                int currentHeight = modelSizeH.getValue();
                if (Math.abs(newHeight - currentHeight) > 1) {
                    logger.trace("onCheckKeepAspectRationByWidth: diff={}; old={}; newDouble={}; new={}", (newHeight - currentHeight), currentHeight, newHeight, (int)newHeight);
                    modelSizeH.setValue((int)newHeight);
                }
            }
        } finally {
            lockCheckKeepAspectRation = false;
        }
    }

    private void onCheckKeepAspectRationByHeight() {
        if (lockCheckKeepAspectRation)
            return;
        lockCheckKeepAspectRation = true;
        try {
            if (isKeepAspectRatio) {
                double koef = modelSizeH.getValue() / (double)sourceImage.getHeight();
                double newWidth = sourceImage.getWidth() * koef;
                int currentWidth = modelSizeW.getValue();
                if (Math.abs(newWidth - currentWidth) > 1) {
                    logger.trace("onCheckKeepAspectRationByHeight: diff={}; old={}; newDouble={}; new={}", (newWidth - currentWidth), currentWidth, newWidth, (int)newWidth);
                    modelSizeW.setValue((int)newWidth);
                }
            }
        } finally {
            lockCheckKeepAspectRation = false;
        }
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
