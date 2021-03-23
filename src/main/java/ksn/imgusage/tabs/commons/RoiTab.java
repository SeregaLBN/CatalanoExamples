package ksn.imgusage.tabs.commons;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.Rect;
import ksn.imgusage.type.Size;
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

    @Override
    public Component makeTab(RoiTabParams params) {
        if (params == null)
            params = new RoiTabParams(new Size(500, 300), new Rect(0, 0, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT));

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

        int x = fromRatioX(params.roi.x);
        int y = fromRatioY(params.roi.y);
        int w = fromRatioX(params.roi.width);
        int h = fromRatioY(params.roi.height);

        if ((x <= 0) &&
            (y <= 0) &&
            (w >= srcW) &&
            (h >= srcH))
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

        int x = fromRatioX(params.roi.x);
        int y = fromRatioY(params.roi.y);
        int w = fromRatioX(params.roi.width);
        int h = fromRatioY(params.roi.height);

        if ((x <= 0) &&
            (y <= 0) &&
            (w >= srcW) &&
            (h >= srcH))
        {
            drawImage = getImage();
            return drawImage;
        }

        drawImage = ImgHelper.copy(sourceImage);

        if (sourceImage.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            // ! restore colors for preview !
            BufferedImage image = new BufferedImage(srcW, srcH, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics g = image.getGraphics();
            g.drawImage(drawImage, 0, 0, null);
            g.dispose();
            drawImage = image;
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
        SliderIntModel modelRatioW = new SliderIntModel(params.ratio.width , 0, 15, MAX_IMAGE_WIDTH);
        SliderIntModel modelRatioH = new SliderIntModel(params.ratio.height, 0, 15, MAX_IMAGE_HEIGHT);
        SliderIntModel modelRoiX = new SliderIntModel(params.roi.x     , 0, 0, MAX_IMAGE_WIDTH  - MIN_SIZE_ROI);
        SliderIntModel modelRoiY = new SliderIntModel(params.roi.y     , 0, 0, MAX_IMAGE_HEIGHT - MIN_SIZE_ROI);
        SliderIntModel modelRoiW = new SliderIntModel(params.roi.width , 0, MIN_SIZE_ROI, MAX_IMAGE_WIDTH);
        SliderIntModel modelRoiH = new SliderIntModel(params.roi.height, 0, MIN_SIZE_ROI, MAX_IMAGE_HEIGHT);

        applyLimits = () -> {
            BufferedImage sourceImage = getSourceImage();
            if (sourceImage == null)
                return;

            int srcW = toRatioX(sourceImage.getWidth());
            int srcH = toRatioY(sourceImage.getHeight());

            modelRoiX.setMaximum(srcW - MIN_SIZE_ROI);
            modelRoiY.setMaximum(srcH - MIN_SIZE_ROI);
            modelRoiW.setMaximum(srcW - modelRoiX.getValue());
            modelRoiH.setMaximum(srcH - modelRoiY.getValue());
        };

        JPanel panel4Options = new JPanel();
        panel4Options.setLayout(new BorderLayout());

        Box boxOfRoi = Box.createHorizontalBox();
        {
            boxOfRoi.setBorder(BorderFactory.createTitledBorder("ROI"));
            boxOfRoi.setToolTipText("Region Of Interest");

            boxOfRoi.add(Box.createHorizontalStrut(8));
            boxOfRoi.add(makeSliderVert(modelRoiX, "X", "Padding left - offset of ROI"));
            boxOfRoi.add(Box.createHorizontalStrut(2));
            boxOfRoi.add(makeSliderVert(modelRoiY, "Y", "Padding right - offset of ROI"));
            boxOfRoi.add(Box.createHorizontalStrut(2));
            boxOfRoi.add(makeSliderVert(modelRoiW, "Width" , "Size of ROI"));
            boxOfRoi.add(Box.createHorizontalStrut(2));
            boxOfRoi.add(makeSliderVert(modelRoiH, "Height", "Size of ROI"));
            boxOfRoi.add(Box.createHorizontalStrut(8));
        }

        Box boxRatio = Box.createHorizontalBox();
        boxRatio.setBorder(BorderFactory.createTitledBorder("Ratio"));
        boxRatio.setToolTipText("Ratio applies only to ROI");

        Container cntrlRatioW = makeEditBox("params.ratio.width" , modelRatioW, "Width" , null, null);
        Container cntrlRatioH = makeEditBox("params.ratio.height", modelRatioH, "Height", null, null);

        JButton btnOrigSize = new JButton(" • ");
        btnOrigSize.setToolTipText("Ratio as source size");
        btnOrigSize.addActionListener(ev -> {
            BufferedImage sourceImage = getSourceImage();
            if (sourceImage == null)
                return;

            modelRatioW.setValue(sourceImage.getWidth());
            modelRatioH.setValue(sourceImage.getHeight());
        });

        boxRatio.add(Box.createHorizontalGlue());
        boxRatio.add(cntrlRatioW);
        boxRatio.add(Box.createHorizontalGlue());
        boxRatio.add(cntrlRatioH);
        boxRatio.add(Box.createHorizontalGlue());
        boxRatio.add(btnOrigSize);
        boxRatio.add(Box.createHorizontalGlue());

        panel4Options.add(boxRatio, BorderLayout.NORTH);
        panel4Options.add(boxOfRoi, BorderLayout.CENTER);

        addChangeListener("params.ratio.width", modelRatioW, v -> params.ratio.width = v, () -> {
            applyLimits.run();
        });
        addChangeListener("params.ratio.height", modelRatioH, v -> params.ratio.height = v, () -> {
            applyLimits.run();
        });

        addChangeListener("params.roi.x", modelRoiX, v -> params.roi.x = v, () -> {
            BufferedImage sourceImage = getSourceImage();
            if (sourceImage == null)
                return;
            if ((params.roi.x + params.roi.width) > sourceImage.getWidth())
                SwingUtilities.invokeLater(() -> modelRoiW.setValue(sourceImage.getWidth() - modelRoiX.getValue()));
        });
        addChangeListener("params.roi.y", modelRoiY, v -> params.roi.y = v, () -> {
            BufferedImage sourceImage = getSourceImage();
            if (sourceImage == null)
                return;
            if ((params.roi.y + params.roi.height) > sourceImage.getHeight())
                SwingUtilities.invokeLater(() -> modelRoiH.setValue(sourceImage.getHeight() - modelRoiY.getValue()));
        });

        addChangeListener("params.roi.width",
                          modelRoiW,
                          v -> params.roi.width = v,
                          null);
        addChangeListener("params.roi.height",
                          modelRoiH,
                          v -> params.roi.height = v,
                          null);

        applyLimits.run();

        return panel4Options;
    }

    @Override
    public RoiTabParams getParams() {
        return params;
    }

    private int toRatioX(int val) {
        BufferedImage sourceImage = getSourceImage();
        if (sourceImage == null)
            return val;

        int srcW = sourceImage.getWidth();
        if (srcW == params.ratio.width)
            return val;

        return (int)(val * (double)params.ratio.width / srcW);
    }

    private int toRatioY(int val) {
        BufferedImage sourceImage = getSourceImage();
        if (sourceImage == null)
            return val;

        int srcH = sourceImage.getHeight();
        if (srcH == params.ratio.height)
            return val;

        return (int)(val * (double)params.ratio.height / srcH);
    }


    private int fromRatioX(int val) {
        BufferedImage sourceImage = getSourceImage();
        if (sourceImage == null)
            return val;

        int srcW = sourceImage.getWidth();
        if (srcW == params.ratio.width)
            return val;

        return (int)(val * (double)srcW / params.ratio.width);
    }

    private int fromRatioY(int val) {
        BufferedImage sourceImage = getSourceImage();
        if (sourceImage == null)
            return val;

        int srcH = sourceImage.getHeight();
        if (srcH == params.ratio.height)
            return val;

        return (int)(val * (double)srcH / params.ratio.height);
    }

}
