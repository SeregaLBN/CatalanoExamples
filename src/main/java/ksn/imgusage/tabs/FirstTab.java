package ksn.imgusage.tabs;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.*;

import Catalano.Imaging.FastBitmap;
import ksn.imgusage.filtersdemo.MainApp;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.Padding;
import ksn.imgusage.type.Size;
import ksn.imgusage.type.dto.FirstTabParams;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.UiHelper;

/** The first tab to select an image to work with. */
public class FirstTab extends BaseTab<FirstTabParams> {

    public static final File DEFAULT_IMAGE = Paths.get("exampleImages", "VolodHill.jpg").toAbsolutePath().toFile();

    public static final String TAB_TITLE = "Original";
    public static final String TAB_NAME  = "FirstTab";
    public static final String TAB_DESCRIPTION = "The first tab to select an image to work with";

    private static final int MIN_IMAGE_WIDTH  = 10;
    private static final int MIN_IMAGE_HEIGHT = 10;
    private static final int MAX_IMAGE_WIDTH  = 10000;
    private static final int MAX_IMAGE_HEIGHT = 10000;
    private static final Color COLOR_LEFT   = Color.RED;
    private static final Color COLOR_RIGHT  = Color.GREEN;
    private static final Color COLOR_TOP    = Color.BLUE;
    private static final Color COLOR_BOTTOM = Color.ORANGE;
    private static final int MAX_ZOOM_KOEF = 2;

    private BufferedImage sourceImage;
    private BufferedImage previewImage;
    private File latestImageDir = DEFAULT_IMAGE.getParentFile();
    private FirstTabParams params;
    private boolean lockCheckKeepAspectRation;
    private Runnable  onCheckKeepAspectRationByWidth;
    private Runnable  onCheckKeepAspectRationByHeight;
    private Runnable applyMaxSizeLimits;

    @Override
    public Component makeTab(FirstTabParams params) {
        if (params == null)
            params = new FirstTabParams(DEFAULT_IMAGE, false, true, new Size(-1, -1), true, new Padding(0,0,0,0));

        if (params.keepToSize.width < MIN_IMAGE_WIDTH)
            params.keepToSize.width = MAX_IMAGE_WIDTH;
        if (params.keepToSize.height < MIN_IMAGE_HEIGHT)
            params.keepToSize.height = MAX_IMAGE_HEIGHT;
        this.params = params;

        readImageFile(params.imageFile);
        return makeTab();
    }

    @Override
    public String getTitle() { return TAB_TITLE; }
    @Override
    public String getName() { return TAB_NAME; }
    @Override
    public String getDescription() { return TAB_DESCRIPTION; }

    @Override
    protected BufferedImage getSourceImage() {
        return sourceImage;
    }

    public File getLatestImageDir() {
        return latestImageDir;
    }

    public boolean isScale() {
        return params.useScale;
    }

    @Override
    public void resetImage() {
        previewImage = null;
        super.resetImage();
    }

    @Override
    protected void applyFilter() {
        image = ImgHelper.resize(sourceImage, params.keepToSize.width, params.keepToSize.height);
        FastBitmap bmp = new FastBitmap(image);
        if (params.useGray && !bmp.isGrayscale())
            bmp.toGrayscale();
        image = bmp.toBufferedImage();
        cutIndent();
    }

    private void cutIndent() {
        int left   = params.boundOfRoi.left;
        int right  = params.boundOfRoi.right;
        int top    = params.boundOfRoi.top;
        int bottom = params.boundOfRoi.bottom;
        if ((left   <= 0) &&
            (right  <= 0) &&
            (top    <= 0) &&
            (bottom <= 0))
        {
            return;
        }

        int wSrc = sourceImage.getWidth();
        int hSrc = sourceImage.getHeight();
        int wDst = params.keepToSize.width;
        int hDst = params.keepToSize.height;
        double koefX = wDst / (double)wSrc;
        double koefY = hDst / (double)hSrc;

        BufferedImage tmp = new BufferedImage(
            Math.max(1, (int)((wSrc - left - right) * koefX)),
            Math.max(1, (int)((hSrc - top - bottom) * koefY)),
            image.getType());
        Graphics2D g = tmp.createGraphics();
        try {
            g.drawImage(
                image,
                0,0, tmp.getWidth(), tmp.getHeight(),
                (int)(left * koefX),
                (int)(top  * koefY),
                (int)((wSrc - right ) * koefX),
                (int)((hSrc - bottom) * koefX),
                null);
        } finally {
            g.dispose();
        }
        image = tmp;
    }

    public BufferedImage getPreviewImage() {
        if (previewImage != null)
            return previewImage;

        int left   = params.boundOfRoi.left;
        int right  = params.boundOfRoi.right;
        int top    = params.boundOfRoi.top;
        int bottom = params.boundOfRoi.bottom;
        if ((left   <= 0) &&
            (right  <= 0) &&
            (top    <= 0) &&
            (bottom <= 0))
        {
            previewImage = getImage();
            return previewImage;
        }

        int wSrc = sourceImage.getWidth();
        int hSrc = sourceImage.getHeight();
        int wDst = params.keepToSize.width;
        int hDst = params.keepToSize.height;
        double koefX = wDst / (double)wSrc;
        double koefY = hDst / (double)hSrc;

        previewImage = ImgHelper.resize(sourceImage, wDst, hDst);
        FastBitmap bmp = new FastBitmap(previewImage);
        if (params.useGray && !bmp.isGrayscale()) {
            bmp.toGrayscale();
            bmp.toRGB(); // ! restore colors for preview !
        }
        previewImage = bmp.toBufferedImage();

        Graphics2D g = previewImage.createGraphics();
        try {
            BasicStroke penLine1 = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
            BasicStroke penLine2 = new BasicStroke(2.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
            if (left > 0) {
                int leftDst = (int)(left * koefX);
                g.setStroke(penLine2);
                g.setColor(Color.WHITE);
                g.drawLine(leftDst, 0, leftDst, hDst);
                g.setStroke(penLine1);
                g.setColor(COLOR_LEFT);
                g.drawLine(leftDst, 0, leftDst, hDst);
            }
            if (right > 0) {
                int rightDst = (int)(right * koefX);
                g.setStroke(penLine2);
                g.setColor(Color.WHITE);
                g.drawLine(wDst - rightDst, 0, wDst - rightDst, hDst);
                g.setStroke(penLine1);
                g.setColor(COLOR_RIGHT);
                g.drawLine(wDst - rightDst, 0, wDst - rightDst, hDst);
            }
            if (top > 0) {
                int topDst = (int)(top * koefY);
                g.setStroke(penLine2);
                g.setColor(Color.WHITE);
                g.drawLine(0, topDst, wDst, topDst);
                g.setStroke(penLine1);
                g.setColor(COLOR_TOP);
                g.drawLine(0, topDst, wDst, topDst);
            }
            if (bottom > 0) {
                int bottomDst = (int)(bottom * koefY);
                g.setStroke(penLine2);
                g.setColor(Color.WHITE);
                g.drawLine(0, hDst - bottomDst, wDst, hDst - bottomDst);
                g.setStroke(penLine1);
                g.setColor(COLOR_BOTTOM);
                g.drawLine(0, hDst - bottomDst, wDst, hDst - bottomDst);
            }
        } finally {
            g.dispose();
        }

        return previewImage;
    }

    public void onSelectImage() {
        logger.trace("onSelectImage");

        File file = UiHelper.chooseFileToLoadImage(JOptionPane.getRootFrame(), latestImageDir);
        readImageFile(file);
    }

    private void readImageFile(File imageFile) {
        if (imageFile == null)
            return;

        try {
            if (!imageFile.exists()) {
                logger.warn("File not found: {}", imageFile);
                tabHandler.onError("File not found: " + imageFile, this, null);
                return;
            }
            sourceImage = ImageIO.read(imageFile);

            params.imageFile = imageFile;
            tabHandler.getFrame().setTitle(MainApp.DEFAULT_TITLE + ": " + imageFile.getName());
            latestImageDir = imageFile.getParentFile();

            if (applyMaxSizeLimits != null)
                applyMaxSizeLimits.run();
            if (onCheckKeepAspectRationByWidth != null)
                onCheckKeepAspectRationByWidth.run();

            resetImage();
        } catch (IOException ex) {
            logger.error("Can`t read image", ex);
        }
    }

    private final JButton makeButtonLoadPipeline() {
        JButton btnLoad = new JButton("Load pipeline...");
        btnLoad.setToolTipText("Load image pipeline tabs");
        btnLoad.addActionListener(ev -> tabHandler.onLoadPipeline());
        return btnLoad;
    }

    private final JButton makeButtonSavePipeline() {
        JButton btnSave = new JButton("Save pipeline...");
        btnSave.setToolTipText("Save current pipeline tabs for selected image");
        btnSave.addActionListener(ev -> tabHandler.onSavePipeline());
        return btnSave;
    }

    private final JButton makeButtonCancel() {
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(ev -> tabHandler.onCancel());
        return btnCancel;
    }

    private final JButton makeButtonLoadImage() {
        JButton btnLoadImage = new JButton("Load image...");
        btnLoadImage.addActionListener(ev -> onSelectImage());
        if (sourceImage == null)
            SwingUtilities.invokeLater(btnLoadImage::doClick);

        return btnLoadImage;
    }

    private final JCheckBox makeButtonUseGray() {
        JCheckBox btnUseGray = new JCheckBox("Gray", params.useGray);
        btnUseGray.addActionListener(ev -> {
            params.useGray  = btnUseGray.isSelected();
            resetImage();
        });

        return btnUseGray;
    }

    private final JCheckBox makeButtonUseScale() {
        JCheckBox btnScale = new JCheckBox("Scale", params.useScale);
        btnScale.addActionListener(ev -> {
            params.useScale = btnScale.isSelected();
            resetImage();
        });

        return btnScale;
    }

    @Override
    protected Component makeUpButtons() {
        Box box4Buttons = Box.createVerticalBox();
        box4Buttons.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        box4Buttons.add(makeButtonLoadImage());
        box4Buttons.add(Box.createVerticalStrut(2));
        box4Buttons.add(makeButtonUseGray());
        box4Buttons.add(Box.createVerticalStrut(2));
        box4Buttons.add(makeButtonUseScale());

        return box4Buttons;
    }

    @Override
    protected Component makeDownButtons() {
        Box boxUpButtons = Box.createHorizontalBox();
        boxUpButtons.add(makeButtonLoadPipeline());
        boxUpButtons.add(Box.createHorizontalStrut(6));
        boxUpButtons.add(makeButtonSavePipeline());

        Box boxDownButtons = Box.createHorizontalBox();
        boxDownButtons.add(makeButtonAddFilter());
        boxDownButtons.add(Box.createHorizontalStrut(6));
        boxDownButtons.add(makeButtonCancel());

        Box box4Buttons = Box.createVerticalBox();
        box4Buttons.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

        box4Buttons.add(boxUpButtons);
        box4Buttons.add(Box.createVerticalStrut(2));
        box4Buttons.add(boxDownButtons);

        return box4Buttons;
    }

    @Override
    protected Component makeOptions() {
        SliderIntModel modelSizeW = new SliderIntModel(params.keepToSize.width, 0, MIN_IMAGE_WIDTH , MAX_IMAGE_WIDTH);
        SliderIntModel modelSizeH = new SliderIntModel(params.keepToSize.height, 0, MIN_IMAGE_HEIGHT, MAX_IMAGE_HEIGHT);
        SliderIntModel modelPadLeft   = new SliderIntModel(params.boundOfRoi.left  , 0, 0, MAX_IMAGE_WIDTH);
        SliderIntModel modelPadRight  = new SliderIntModel(params.boundOfRoi.right , 0, 0, MAX_IMAGE_WIDTH);
        SliderIntModel modelPadTop    = new SliderIntModel(params.boundOfRoi.top   , 0, 0, MAX_IMAGE_HEIGHT);
        SliderIntModel modelPadBottom = new SliderIntModel(params.boundOfRoi.bottom, 0, 0, MAX_IMAGE_HEIGHT);

        onCheckKeepAspectRationByWidth = () -> {
            if (sourceImage == null)
                return;

            if (lockCheckKeepAspectRation)
                return;
            lockCheckKeepAspectRation = true;
            try {
                if (params.useKeepAspectRatio) {
                    double koef = params.keepToSize.width / (double)sourceImage.getWidth();
                    double newHeight = sourceImage.getHeight() * koef;
                    int currentHeight = params.keepToSize.height;
                    if (Math.abs(newHeight - currentHeight) > 1) {
                        logger.trace("onCheckKeepAspectRationByWidth: diff={}; old={}; newDouble={}; new={}", (newHeight - currentHeight), currentHeight, newHeight, (int)newHeight);
                        modelSizeH.setValue((int)newHeight);
                    }
                }
            } finally {
                lockCheckKeepAspectRation = false;
            }
        };

        onCheckKeepAspectRationByHeight = () -> {
            if (sourceImage == null)
                return;

            if (lockCheckKeepAspectRation)
                return;
            lockCheckKeepAspectRation = true;
            try {
                if (params.useKeepAspectRatio) {
                    double koef = params.keepToSize.height / (double)sourceImage.getHeight();
                    double newWidth = sourceImage.getWidth() * koef;
                    int currentWidth = params.keepToSize.width;
                    if (Math.abs(newWidth - currentWidth) > 1) {
                        logger.trace("onCheckKeepAspectRationByHeight: diff={}; old={}; newDouble={}; new={}", (newWidth - currentWidth), currentWidth, newWidth, (int)newWidth);
                        modelSizeW.setValue((int)newWidth);
                    }
                }
            } finally {
                lockCheckKeepAspectRation = false;
            }
        };

        applyMaxSizeLimits = () -> {
            if (sourceImage == null)
                return;
            modelSizeW.setMaximum(sourceImage.getWidth()  * MAX_ZOOM_KOEF);
            modelSizeH.setMaximum(sourceImage.getHeight() * MAX_ZOOM_KOEF);
            modelPadLeft  .setMaximum(sourceImage.getWidth()  - 1);
            modelPadRight .setMaximum(sourceImage.getWidth()  - 1);
            modelPadTop   .setMaximum(sourceImage.getHeight() - 1);
            modelPadBottom.setMaximum(sourceImage.getHeight() - 1);

            params.keepToSize.width  = Math.min(params.keepToSize.width , modelSizeW    .getMaximum());
            params.keepToSize.height = Math.min(params.keepToSize.height, modelSizeH    .getMaximum());
            params.boundOfRoi.left   = Math.min(params.boundOfRoi.left  , modelPadLeft  .getMaximum());
            params.boundOfRoi.right  = Math.min(params.boundOfRoi.right , modelPadRight .getMaximum());
            params.boundOfRoi.top    = Math.min(params.boundOfRoi.top   , modelPadTop   .getMaximum());
            params.boundOfRoi.bottom = Math.min(params.boundOfRoi.bottom, modelPadBottom.getMaximum());
        };


        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

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

            Box box4ImageSize2 = Box.createHorizontalBox();
            JCheckBox btnKeepAspectRatio = new JCheckBox("Keep aspect ratio", params.useKeepAspectRatio);
            btnKeepAspectRatio.addActionListener(ev -> {
                params.useKeepAspectRatio = btnKeepAspectRatio.isSelected();
                onCheckKeepAspectRationByWidth.run();
                resetImage();
            });
            JButton btnOrigSize = new JButton(" • ");
            btnOrigSize.setToolTipText("Reset to original size");
            btnOrigSize.addActionListener(ev -> {
                if (sourceImage == null)
                    return;
                modelSizeW.setValue(sourceImage.getWidth());
                modelSizeH.setValue(sourceImage.getHeight());
                resetImage();
            });
            box4ImageSize2.add(Box.createHorizontalStrut(2));
            box4ImageSize2.add(btnKeepAspectRatio);
            box4ImageSize2.add(Box.createHorizontalGlue());
            box4ImageSize2.add(btnOrigSize);
            box4ImageSize2.add(Box.createHorizontalStrut(2));

            panelImageSize.add(box4ImageSize , BorderLayout.CENTER);
            panelImageSize.add(box4ImageSize2, BorderLayout.SOUTH);
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

        box4Options.add(panelImageSize);
        box4Options.add(Box.createVerticalStrut(2));
        box4Options.add(boxOfRoi);

        applyMaxSizeLimits.run();
        onCheckKeepAspectRationByWidth.run();

        modelSizeW.getWrapped().addChangeListener(ev -> {
            logger.trace("modelSizeW: value={}", modelSizeW.getFormatedText());
            params.keepToSize.width = modelSizeW.getValue();
            onCheckKeepAspectRationByWidth.run();
            resetImage();
        });
        modelSizeH.getWrapped().addChangeListener(ev -> {
            logger.trace("modelSizeH: value={}", modelSizeH.getFormatedText());
            params.keepToSize.height = modelSizeH.getValue();
            onCheckKeepAspectRationByHeight.run();
            resetImage();
        });

        modelPadLeft.getWrapped().addChangeListener(ev -> {
            logger.trace("modelPadLeft: value={}", modelPadLeft.getFormatedText());
            params.boundOfRoi.left = modelPadLeft.getValue();
            if ((modelPadLeft.getValue() + modelPadRight.getValue()) >= sourceImage.getWidth())
                SwingUtilities.invokeLater(() -> modelPadRight.setValue(sourceImage.getWidth() - 1 - modelPadLeft.getValue()) );
            resetImage();
        });
        modelPadRight.getWrapped().addChangeListener(ev -> {
            logger.trace("modelPadRight: value={}", modelPadRight.getFormatedText());
            params.boundOfRoi.right = modelPadRight.getValue();
            if ((modelPadLeft.getValue() + modelPadRight.getValue()) >= sourceImage.getWidth())
                SwingUtilities.invokeLater(() -> modelPadLeft.setValue(sourceImage.getWidth() - 1 - modelPadRight.getValue()) );
            resetImage();
        });
        modelPadTop.getWrapped().addChangeListener(ev -> {
            logger.trace("modelPadTop: value={}", modelPadTop.getFormatedText());
            params.boundOfRoi.top = modelPadTop.getValue();
            if ((modelPadTop.getValue() + modelPadBottom.getValue()) >= sourceImage.getHeight())
                SwingUtilities.invokeLater(() -> modelPadBottom.setValue(sourceImage.getHeight() - 1 - modelPadTop.getValue()) );
            resetImage();
        });
        modelPadBottom.getWrapped().addChangeListener(ev -> {
            logger.trace("modelPadBottom: value={}", modelPadBottom.getFormatedText());
            params.boundOfRoi.bottom = modelPadBottom.getValue();
            if ((modelPadTop.getValue() + modelPadBottom.getValue()) >= sourceImage.getHeight())
                SwingUtilities.invokeLater(() -> modelPadTop.setValue(sourceImage.getHeight() - 1 - modelPadBottom.getValue()) );
            resetImage();
        });

        return box4Options;
    }

    @Override
    public FirstTabParams getParams() {
        return params;
    }

}
