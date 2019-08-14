package ksn.imgusage.tabs;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.*;

import Catalano.Imaging.FastBitmap;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.Padding;
import ksn.imgusage.type.Size;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.UiHelper;

public class FirstTab extends BaseTab<FirstTab.Params> {

    public static class Params implements ITabParams {
        /** source image */
        public File    imageFile;
        public boolean useGray;
        public boolean useScale;
        public Size    keepToSize;
        public boolean useKeepAspectRatio;
        /** padding of Region Of Interest */
        public Padding boundOfRoi;

        public Params(
            File    imageFile,
            boolean useGray,
            boolean useScale,
            Size    keepToSize,
            boolean useKeepAspectRatio,
            Padding boundOfRoi)
        {
            this.imageFile          = imageFile;
            this.useGray            = useGray;
            this.useScale           = useScale;
            this.keepToSize         = keepToSize;
            this.useKeepAspectRatio = useKeepAspectRatio;
            this.boundOfRoi         = boundOfRoi;
        }

        @Override
        public String toString() {
            return String.format(
                    "{imageFile='%s', useGray=%b, useScale=%b, keepToSize=%s, useKeepAspectRatio=%b, boundOfRoi=%s}",
                    imageFile,
                    useGray,
                    useScale,
                    keepToSize,
                    useKeepAspectRatio,
                    boundOfRoi);
        }
    }

    public static final File DEFAULT_IMAGE = Paths.get("./exampleImages", "1024px-VolodimirHillAndDnieper.jpg").toFile();

    private static final int MIN_IMAGE_WIDTH  = 10;
    private static final int MIN_IMAGE_HEIGHT = 10;
    private static final int MAX_IMAGE_WIDTH  = 10000;
    private static final int MAX_IMAGE_HEIGHT = 10000;
    private static final Color COLOR_LEFT   = Color.RED;
    private static final Color COLOR_RIGHT  = Color.GREEN;
    private static final Color COLOR_TOP    = Color.BLUE;
    private static final Color COLOR_BOTTOM = Color.ORANGE;

    private BufferedImage sourceImage;
    private BufferedImage previewImage;
    private File latestImageDir;
    private final Params params;
    private final SliderIntModel modelSizeW;
    private final SliderIntModel modelSizeH;
    private final SliderIntModel modelPadLeft;
    private final SliderIntModel modelPadRight;
    private final SliderIntModel modelPadTop;
    private final SliderIntModel modelPadBottom;

    public FirstTab(ITabHandler tabHandler) {
        this(tabHandler, new Params(DEFAULT_IMAGE, false, true, new Size(-1, -1), true, new Padding(0,0,0,0)));
    }
    public FirstTab(ITabHandler tabHandler, Params params) {
        super(tabHandler, null);
        this.addRemoveFilterButton = false;
        if (params.keepToSize.width < MIN_IMAGE_WIDTH)
            params.keepToSize.width = MAX_IMAGE_WIDTH;
        if (params.keepToSize.height < MIN_IMAGE_HEIGHT)
            params.keepToSize.height = MAX_IMAGE_HEIGHT;
        this.params = params;
        this.modelSizeW = new SliderIntModel(params.keepToSize.width, 0, MIN_IMAGE_WIDTH , MAX_IMAGE_WIDTH);
        this.modelSizeH = new SliderIntModel(params.keepToSize.height, 0, MIN_IMAGE_HEIGHT, MAX_IMAGE_HEIGHT);
        this.modelPadLeft   = new SliderIntModel(params.boundOfRoi.left  , 0, 0, MAX_IMAGE_WIDTH);
        this.modelPadRight  = new SliderIntModel(params.boundOfRoi.right , 0, 0, MAX_IMAGE_WIDTH);
        this.modelPadTop    = new SliderIntModel(params.boundOfRoi.top   , 0, 0, MAX_IMAGE_HEIGHT);
        this.modelPadBottom = new SliderIntModel(params.boundOfRoi.bottom, 0, 0, MAX_IMAGE_HEIGHT);

        readImageFile(params.imageFile);
        makeTab();
    }

    @Override
    public String getTabName() { return "Original"; }

    @Override
    protected BufferedImage getSourceImage() {
        return sourceImage;
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
            (int)((wSrc - left - right) * koefX),
            (int)((hSrc - top - bottom) * koefY),
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

    @Override
    public void updateSource(ITab<?> newSource) {
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

            params.imageFile = imageFile;
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

        JCheckBox btnAsGray = new JCheckBox("Gray", params.useGray);
        btnAsGray.addActionListener(ev -> {
            params.useGray  = btnAsGray.isSelected();
            resetImage();
        });
        btnAsGray.setAlignmentX(Component.LEFT_ALIGNMENT);

        JCheckBox btnScale = new JCheckBox("Scale", params.useScale);
        btnScale.addActionListener(ev -> {
            params.useScale = btnScale.isSelected();
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

            JCheckBox btnKeepAspectRatio = new JCheckBox("Keep aspect ratio", params.useKeepAspectRatio);
            btnKeepAspectRatio.addActionListener(ev -> {
                params.useKeepAspectRatio = btnKeepAspectRatio.isSelected();
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
            params.keepToSize.width = modelSizeW.getValue();
            onCheckKeepAspectRationByWidth();
            resetImage();
        });
        modelSizeH.getWrapped().addChangeListener(ev -> {
            logger.trace("modelSizeH: value={}", modelSizeH.getFormatedText());
            params.keepToSize.height = modelSizeH.getValue();
            onCheckKeepAspectRationByHeight();
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
    }

    private boolean lockCheckKeepAspectRation;
    private void onCheckKeepAspectRationByWidth() {
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
    }

    private void onCheckKeepAspectRationByHeight() {
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
    }

    @Override
    public Params getParams() {
        return params;
    }

}
