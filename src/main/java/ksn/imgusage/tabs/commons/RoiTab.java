package ksn.imgusage.tabs.commons;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.SwingUtilities;

import Catalano.Imaging.FastBitmap;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.Rect;
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
    private Runnable applyLimits;
    private Consumer<String> showRatioX;
    private Consumer<String> showRatioY;

    @Override
    public Component makeTab(RoiTabParams params) {
        if (params == null)
            params = new RoiTabParams(new Rect(0, 0, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT));

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
        if (image == null)
            applyLimits.run();

        BufferedImage sourceImage = getSourceImage();
        int srcW = sourceImage.getWidth();
        int srcH = sourceImage.getHeight();

        int x = params.rc.x;
        int y = params.rc.y;
        int w = params.rc.width;
        int h = params.rc.height;

        if ((x == 0) &&
            (y == 0) &&
            (w == srcW) &&
            (w == srcH))
        {
            image = sourceImage;
            return;
        }

        BufferedImage tmp = new BufferedImage(w, h, sourceImage.getType());
        Graphics2D g = tmp.createGraphics();
        try {
            g.drawImage(
                sourceImage,
                0, 0, w, h,
                x, y, x + w, y + h,
                null);
        } finally {
            g.dispose();
        }
        image = tmp;
    }

    @Override
    public BufferedImage getDrawImage() {
        if (drawImage == null)
            applyLimits.run();
        else
            return drawImage;

        BufferedImage sourceImage = getSourceImage();
        if (sourceImage == null)
            return null;

        int srcW = sourceImage.getWidth();
        int srcH = sourceImage.getHeight();

        int x = params.rc.x;
        int y = params.rc.y;
        int w = params.rc.width;
        int h = params.rc.height;

        if ((x == 0) &&
            (y == 0) &&
            (w == srcW) &&
            (w == srcH))
        {
            drawImage = getImage();
            return drawImage;
        }

        drawImage = ImgHelper.copy(sourceImage);

        if (sourceImage.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            FastBitmap bmp = new FastBitmap(drawImage);
            assert bmp.isGrayscale();
            bmp.toRGB(); // ! restore colors for preview !
            drawImage = bmp.toBufferedImage();
        }

        Graphics2D g = drawImage.createGraphics();
        try {
            BasicStroke penLine1 = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
            BasicStroke penLine2 = new BasicStroke(2.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
            if (x > 0) {
                g.setStroke(penLine2);
                g.setColor(Color.WHITE);
                g.drawLine(x, 0, x, srcH);
                g.setStroke(penLine1);
                g.setColor(COLOR_LEFT);
                g.drawLine(x, 0, x, srcH);
            }
            if (y > 0) {
                g.setStroke(penLine2);
                g.setColor(Color.WHITE);
                g.drawLine(0, y, srcW, y);
                g.setStroke(penLine1);
                g.setColor(COLOR_RIGHT);
                g.drawLine(0, y, srcW, y);
            }
            if ((x + w) < srcW) {
                g.setStroke(penLine2);
                g.setColor(Color.WHITE);
                g.drawLine(x + w, 0, x + w, srcH);
                g.setStroke(penLine1);
                g.setColor(COLOR_TOP);
                g.drawLine(x + w, 0, x + w, srcH);
            }
            if ((y + h) < srcH) {
                g.setStroke(penLine2);
                g.setColor(Color.WHITE);
                g.drawLine(0, y + h, srcW, y + h);
                g.setStroke(penLine1);
                g.setColor(COLOR_BOTTOM);
                g.drawLine(0, y + h, srcW, y + h);
            }
        } finally {
            g.dispose();
        }

        return drawImage;
    }

    @Override
    protected Component makeOptions() {
        SliderIntModel modelRcX = new SliderIntModel(params.rc.x     , 0, 0, MAX_IMAGE_WIDTH - MIN_SIZE_ROI);
        SliderIntModel modelRcY = new SliderIntModel(params.rc.y     , 0, 0, MAX_IMAGE_WIDTH - MIN_SIZE_ROI);
        SliderIntModel modelRcW = new SliderIntModel(params.rc.width , 0, MIN_SIZE_ROI, MAX_IMAGE_HEIGHT);
        SliderIntModel modelRcH = new SliderIntModel(params.rc.height, 0, MIN_SIZE_ROI, MAX_IMAGE_HEIGHT);

        applyLimits = () -> {
            BufferedImage sourceImage = getSourceImage();
            if (sourceImage == null)
                return;

            int srcW = sourceImage.getWidth();
            int srcH = sourceImage.getHeight();

            modelRcX.setMaximum(srcW - MIN_SIZE_ROI);
            modelRcY.setMaximum(srcH - MIN_SIZE_ROI);
            modelRcW.setMaximum(srcW - modelRcX.getValue());
            modelRcH.setMaximum(srcH - modelRcY.getValue());
        };


        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        Box boxOfRoi = Box.createHorizontalBox();
        {
            boxOfRoi.setBorder(BorderFactory.createTitledBorder("ROI"));
            boxOfRoi.setToolTipText("Region Of Interest");

            boxOfRoi.add(Box.createHorizontalStrut(8));
            boxOfRoi.add(makeSliderVert(modelRcX, "X", "Padding left - offset of ROI"));
            boxOfRoi.add(Box.createHorizontalStrut(2));
            boxOfRoi.add(makeSliderVert(modelRcY, "Y", "Padding right - offset of ROI"));
            boxOfRoi.add(Box.createHorizontalStrut(2));
            boxOfRoi.add(makeSliderVert(modelRcW, "Width" , "Size of ROI"));
            boxOfRoi.add(Box.createHorizontalStrut(2));
            boxOfRoi.add(makeSliderVert(modelRcH, "Height", "Size of ROI"));
            boxOfRoi.add(Box.createHorizontalStrut(8));
        }

        Box boxRatio = Box.createHorizontalBox();
        boxRatio.setBorder(BorderFactory.createTitledBorder("Ratio"));

        Container cntrlRatioX = makeEditBox(s -> showRatioX = s, v -> {}, "Ratio X", null, null);
        Container cntrlRatioY = makeEditBox(s -> showRatioY = s, v -> {}, "Ratio Y", null, null);

        boxRatio.add(cntrlRatioX);
        boxRatio.add(cntrlRatioY);

        box4Options.add(boxRatio);
        box4Options.add(boxOfRoi);


        box4Options.add(boxOfRoi);

        addChangeListener("params.rc.x", modelRcX, v -> params.rc.x = v, () -> {
            BufferedImage sourceImage = getSourceImage();
            if (sourceImage == null)
                return;
            if ((params.rc.x + params.rc.width) > sourceImage.getWidth())
                SwingUtilities.invokeLater(() -> {
                    modelRcW.setValue(sourceImage.getWidth() - modelRcX.getValue());
                });
        });
        addChangeListener("params.rc.y", modelRcY, v -> params.rc.y = v, () -> {
            BufferedImage sourceImage = getSourceImage();
            if (sourceImage == null)
                return;
            if ((params.rc.y + params.rc.height) > sourceImage.getHeight())
                SwingUtilities.invokeLater(() -> {
                    modelRcH.setValue(sourceImage.getHeight() - modelRcY.getValue());
                });
        });

        addChangeListener("params.rc.width",
                          modelRcW,
                          v -> {
                              params.rc.width = v;
                          },
                          null);
        addChangeListener("params.rc.height",
                          modelRcH,
                          v -> {
                              params.rc.height = v;
                          },
                          null);

        applyLimits.run();

        BufferedImage sourceImage = getSourceImage();
        if (sourceImage == null) {
            showRatioX.accept("100");
            showRatioY.accept("100");
        } else {
            showRatioX.accept(sourceImage.getWidth() + "");
            showRatioY.accept(sourceImage.getHeight() + "");
        }

        return box4Options;
    }

    @Override
    public RoiTabParams getParams() {
        return params;
    }

}
