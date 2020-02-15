package ksn.imgusage.tabs.commons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.SwingUtilities;

import Catalano.Imaging.FastBitmap;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.Padding;
import ksn.imgusage.type.dto.common.RoiTabParams;
import ksn.imgusage.utils.ImgHelper;

/** Region Of Interest */
public class RoiTab extends CommonTab<RoiTabParams> {

    public static final String TAB_TITLE = "ROI";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Region of interest";

    private static final int MAX_IMAGE_WIDTH  = 7000;
    private static final int MAX_IMAGE_HEIGHT = 7000;
    private static final int MIN_SIZE_ROI = 10;
    private static final Color COLOR_LEFT   = Color.RED;
    private static final Color COLOR_RIGHT  = Color.GREEN;
    private static final Color COLOR_TOP    = Color.BLUE;
    private static final Color COLOR_BOTTOM = Color.ORANGE;

    private BufferedImage drawImage;
    private RoiTabParams params;
    Runnable applyMaxSizeLimits;

    @Override
    public Component makeTab(RoiTabParams params) {
        if (params == null)
            params = new RoiTabParams(new Padding(0,0,0,0));

        this.params = params;

        return makeTab();
    }

    @Override
    public String getTitle() { return TAB_TITLE; }
    @Override
    public String getName() { return TAB_NAME; }
    @Override
    public String getDescription() { return TAB_DESCRIPTION; }

    @Override
    protected void resetImage() {
        drawImage = null;
        super.resetImage();
    }

    @Override
    protected void applyFilter() {
        if (image == null) {
            if (applyMaxSizeLimits != null)
                applyMaxSizeLimits.run();
        }

        int left   = params.boundOfRoi.left;
        int right  = params.boundOfRoi.right;
        int top    = params.boundOfRoi.top;
        int bottom = params.boundOfRoi.bottom;

        BufferedImage sourceImage = getSourceImage();
        /**/
        if ((left   <= 0) &&
            (right  <= 0) &&
            (top    <= 0) &&
            (bottom <= 0))
        {
            image = sourceImage;
            return;
        }
        /**/

        int w = sourceImage.getWidth();
        int h = sourceImage.getHeight();

        BufferedImage tmp = new BufferedImage(
            Math.max(1, w - left - right),
            Math.max(1, h - top - bottom),
            sourceImage.getType());
        Graphics2D g = tmp.createGraphics();
        try {
            g.drawImage(
                sourceImage,
                0,0, tmp.getWidth(), tmp.getHeight(),
                left,
                top,
                w - right,
                h - bottom,
                null);
        } finally {
            g.dispose();
        }
        image = tmp;
    }

    @Override
    public BufferedImage getDrawImage() {
        if (drawImage != null)
            return drawImage;

        BufferedImage sourceImage = getSourceImage();
        if (sourceImage == null)
            return null;

        int left   = params.boundOfRoi.left;
        int right  = params.boundOfRoi.right;
        int top    = params.boundOfRoi.top;
        int bottom = params.boundOfRoi.bottom;
        if ((left   <= 0) &&
            (right  <= 0) &&
            (top    <= 0) &&
            (bottom <= 0))
        {
            drawImage = getImage();
            return drawImage;
        }

        int w = sourceImage.getWidth();
        int h = sourceImage.getHeight();

        drawImage = ImgHelper.copy(sourceImage);
        FastBitmap bmp = new FastBitmap(drawImage);
        if (bmp.isGrayscale()) {
            bmp.toRGB(); // ! restore colors for preview !
            drawImage = bmp.toBufferedImage();
        }

        Graphics2D g = drawImage.createGraphics();
        try {
            BasicStroke penLine1 = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
            BasicStroke penLine2 = new BasicStroke(2.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
            if (left > 0) {
                g.setStroke(penLine2);
                g.setColor(Color.WHITE);
                g.drawLine(left, 0, left, h);
                g.setStroke(penLine1);
                g.setColor(COLOR_LEFT);
                g.drawLine(left, 0, left, h);
            }
            if (right > 0) {
                g.setStroke(penLine2);
                g.setColor(Color.WHITE);
                g.drawLine(w - right, 0, w - right, h);
                g.setStroke(penLine1);
                g.setColor(COLOR_RIGHT);
                g.drawLine(w - right, 0, w - right, h);
            }
            if (top > 0) {
                g.setStroke(penLine2);
                g.setColor(Color.WHITE);
                g.drawLine(0, top, w, top);
                g.setStroke(penLine1);
                g.setColor(COLOR_TOP);
                g.drawLine(0, top, w, top);
            }
            if (bottom > 0) {
                g.setStroke(penLine2);
                g.setColor(Color.WHITE);
                g.drawLine(0, h - bottom, w, h - bottom);
                g.setStroke(penLine1);
                g.setColor(COLOR_BOTTOM);
                g.drawLine(0, h - bottom, w, h - bottom);
            }
        } finally {
            g.dispose();
        }

        return drawImage;
    }

    @Override
    protected Component makeOptions() {
        SliderIntModel modelPadLeft   = new SliderIntModel(params.boundOfRoi.left  , 0, 0, MAX_IMAGE_WIDTH);
        SliderIntModel modelPadRight  = new SliderIntModel(params.boundOfRoi.right , 0, 0, MAX_IMAGE_WIDTH);
        SliderIntModel modelPadTop    = new SliderIntModel(params.boundOfRoi.top   , 0, 0, MAX_IMAGE_HEIGHT);
        SliderIntModel modelPadBottom = new SliderIntModel(params.boundOfRoi.bottom, 0, 0, MAX_IMAGE_HEIGHT);

        applyMaxSizeLimits = () -> {
            BufferedImage sourceImage = getSourceImage();
            if (sourceImage == null)
                return;
            modelPadLeft  .setMaximum(sourceImage.getWidth()  - 1);
            modelPadRight .setMaximum(sourceImage.getWidth()  - 1);
            modelPadTop   .setMaximum(sourceImage.getHeight() - 1);
            modelPadBottom.setMaximum(sourceImage.getHeight() - 1);

            params.boundOfRoi.left   = Math.min(params.boundOfRoi.left  , modelPadLeft  .getMaximum());
            params.boundOfRoi.right  = Math.min(params.boundOfRoi.right , modelPadRight .getMaximum());
            params.boundOfRoi.top    = Math.min(params.boundOfRoi.top   , modelPadTop   .getMaximum());
            params.boundOfRoi.bottom = Math.min(params.boundOfRoi.bottom, modelPadBottom.getMaximum());
        };


        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

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

        box4Options.add(boxOfRoi);

        addChangeListener("modelPadLeft", modelPadLeft, v -> params.boundOfRoi.left = v, () -> {
            BufferedImage sourceImage = getSourceImage();
            if ((sourceImage != null) && (modelPadLeft.getValue() + modelPadRight.getValue()) > (sourceImage.getWidth() - MIN_SIZE_ROI))
                SwingUtilities.invokeLater(() -> modelPadRight.setValue(sourceImage.getWidth() - MIN_SIZE_ROI - modelPadLeft.getValue()) );
        });
        addChangeListener("modelPadRight", modelPadRight, v -> params.boundOfRoi.right = v, () -> {
            BufferedImage sourceImage = getSourceImage();
            if ((sourceImage != null) && (modelPadLeft.getValue() + modelPadRight.getValue()) > (sourceImage.getWidth() - MIN_SIZE_ROI))
                SwingUtilities.invokeLater(() -> modelPadLeft.setValue(sourceImage.getWidth() - MIN_SIZE_ROI - modelPadRight.getValue()) );
        });
        addChangeListener("modelPadTop", modelPadTop, v -> params.boundOfRoi.top = v, () -> {
            BufferedImage sourceImage = getSourceImage();
            if ((sourceImage != null) && (modelPadTop.getValue() + modelPadBottom.getValue()) > (sourceImage.getHeight() - MIN_SIZE_ROI))
                SwingUtilities.invokeLater(() -> modelPadBottom.setValue(sourceImage.getHeight() - MIN_SIZE_ROI - modelPadTop.getValue()) );
        });
        addChangeListener("modelPadBottom", modelPadBottom, v -> params.boundOfRoi.bottom = v, () -> {
            BufferedImage sourceImage = getSourceImage();
            if ((sourceImage != null) && (modelPadTop.getValue() + modelPadBottom.getValue()) > (sourceImage.getHeight() - MIN_SIZE_ROI))
                SwingUtilities.invokeLater(() -> modelPadTop.setValue(sourceImage.getHeight() - MIN_SIZE_ROI - modelPadBottom.getValue()) );
        });

        applyMaxSizeLimits.run();

        return box4Options;
    }

    @Override
    public RoiTabParams getParams() {
        return params;
    }

}
