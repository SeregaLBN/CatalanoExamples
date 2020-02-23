package ksn.imgusage.tabs.commons;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.Size;
import ksn.imgusage.type.dto.common.ResizeTabParams;
import ksn.imgusage.utils.ImgHelper;

/** Resize image */
public class ResizeTab extends CommonTab<ResizeTabParams> {

    public static final String TAB_TITLE = "Resize";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Resize image";

    private static final int MIN_IMAGE_WIDTH  = 10;
    private static final int MIN_IMAGE_HEIGHT = 10;
    private static final int MAX_IMAGE_WIDTH  = 7000;
    private static final int MAX_IMAGE_HEIGHT = 7000;

    private ResizeTabParams params;
    private Runnable onCheckKeepAspectRationByWidth;
    private Runnable applyMaxSizeLimits;

    @Override
    public Component makeTab(ResizeTabParams params) {
        if (params == null) {
            int w = -1;
            int h = -1;
            BufferedImage sourceImage = getSourceImage();
            if (sourceImage != null) {
                w = sourceImage.getWidth();
                h = sourceImage.getHeight();
            }
            params = new ResizeTabParams(new Size(w, h), true);
        }

        if (params.keepToSize.width < MIN_IMAGE_WIDTH)
            params.keepToSize.width = MIN_IMAGE_WIDTH;
        if (params.keepToSize.height < MIN_IMAGE_HEIGHT)
            params.keepToSize.height = MIN_IMAGE_HEIGHT;
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
    protected void applyFilter() {
        if (image == null) {
            if (applyMaxSizeLimits != null)
                applyMaxSizeLimits.run();
            if (onCheckKeepAspectRationByWidth != null)
                onCheckKeepAspectRationByWidth.run();
        }

        image = ImgHelper.resize(getSourceImage(), params.keepToSize.width, params.keepToSize.height);
    }

    @Override
    protected Component makeOptions() {
        SliderIntModel modelSizeW = new SliderIntModel(params.keepToSize.width , 0, MIN_IMAGE_WIDTH , MAX_IMAGE_WIDTH);
        SliderIntModel modelSizeH = new SliderIntModel(params.keepToSize.height, 0, MIN_IMAGE_HEIGHT, MAX_IMAGE_HEIGHT);

        onCheckKeepAspectRationByWidth = () -> {
            BufferedImage sourceImage = getSourceImage();
            if (sourceImage == null)
                return;
            if (!params.useKeepAspectRatio)
                return;

            double koef = params.keepToSize.width / (double)sourceImage.getWidth();
            double newHeight = sourceImage.getHeight() * koef;
            int currentHeight = params.keepToSize.height;
            if (Math.abs(newHeight - currentHeight) > 1) {
                logger.trace("onCheckKeepAspectRationByWidth: diff={}; old={}; newDouble={}; new={}", (newHeight - currentHeight), currentHeight, newHeight, (int)newHeight);
                modelSizeH.setValue((int)newHeight);
            }
        };

        Runnable onCheckKeepAspectRationByHeight = () -> {
            BufferedImage sourceImage = getSourceImage();
            if (sourceImage == null)
                return;
            if (!params.useKeepAspectRatio)
                return;

            double koef = params.keepToSize.height / (double)sourceImage.getHeight();
            double newWidth = sourceImage.getWidth() * koef;
            int currentWidth = params.keepToSize.width;
            if (Math.abs(newWidth - currentWidth) > 1) {
                logger.trace("onCheckKeepAspectRationByHeight: diff={}; old={}; newDouble={}; new={}", (newWidth - currentWidth), currentWidth, newWidth, (int)newWidth);
                modelSizeW.setValue((int)newWidth);
            }
        };

        applyMaxSizeLimits = () -> {
            BufferedImage sourceImage = getSourceImage();
            if (sourceImage == null)
                return;

            params.keepToSize.width  = Math.min(params.keepToSize.width , MAX_IMAGE_WIDTH);
            params.keepToSize.height = Math.min(params.keepToSize.height, MAX_IMAGE_HEIGHT);
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
            Component btnKeepAspectRatio = makeCheckBox(
                () -> params.useKeepAspectRatio,
                v  -> params.useKeepAspectRatio = v,
                "Keep aspect ratio",
                "params.useKeepAspectRatio",
                null,
                onCheckKeepAspectRationByWidth);
            JButton btnOrigSize = new JButton(" • ");
            btnOrigSize.setToolTipText("Reset to original size");
            btnOrigSize.addActionListener(ev -> {
                if (getSourceImage() == null)
                    return;
                modelSizeW.setValue(getSourceImage().getWidth());
                modelSizeH.setValue(getSourceImage().getHeight());
//                invalidateAsync();
            });
            box4ImageSize2.add(Box.createHorizontalStrut(2));
            box4ImageSize2.add(btnKeepAspectRatio);
            box4ImageSize2.add(Box.createHorizontalGlue());
            box4ImageSize2.add(btnOrigSize);
            box4ImageSize2.add(Box.createHorizontalStrut(2));

            panelImageSize.add(box4ImageSize , BorderLayout.CENTER);
            panelImageSize.add(box4ImageSize2, BorderLayout.SOUTH);
        }

        box4Options.add(panelImageSize);

        addChangeListener("modelSizeW", modelSizeW, v -> params.keepToSize.width  = v, onCheckKeepAspectRationByWidth);
        addChangeListener("modelSizeH", modelSizeH, v -> params.keepToSize.height = v, onCheckKeepAspectRationByHeight);

        applyMaxSizeLimits.run();
        onCheckKeepAspectRationByWidth.run();

        return box4Options;
    }

    @Override
    public ResizeTabParams getParams() {
        return params;
    }

}
