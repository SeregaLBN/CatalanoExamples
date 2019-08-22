package ksn.imgusage.tabs.opencv;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.*;

import org.opencv.core.Mat;

import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.BaseTab;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.type.opencv.CvInterpolationFlags;
import ksn.imgusage.utils.ImgHelper;

public abstract class OpencvFilterTab<TTabParams extends ITabParams> extends BaseTab<TTabParams>  {

    public static final String TAB_PREFIX = "OpenCV:";

    public static final int BOOST_SIZE_MAX_X = 400;
    public static final int BOOST_SIZE_MAX_Y = 250;

    /** filtered image of the current tab */
    protected Mat imageMat;

    protected Mat getSourceMat() {
        return getSourceMat(source);
    }

    protected static Mat getSourceMat(ITab<?> source) {
        if (source instanceof OpencvFilterTab) {
            return ((OpencvFilterTab<?>)source).imageMat;
        } else {
            BufferedImage src = source.getImage();
            if (src == null)
                return null;
            return ImgHelper.toMat(src);
        }
    }

    protected abstract void applyOpencvFilter();

    @Override
    protected final void applyFilter() {
        Mat srcMat = getSourceMat();
        imageMat = srcMat.clone();

        // specific filter
        applyOpencvFilter();

        image = ImgHelper.toBufferedImage(imageMat);
    }

    @Override
    public void resetImage() {
        imageMat = null;
        super.resetImage();
    }

    public Component makePoint(SliderIntModel modelPointX, SliderIntModel modelPointY, String borderTitle, String tooltip) {
        Box boxSize = Box.createHorizontalBox();
        boxSize.setBorder(BorderFactory.createTitledBorder(borderTitle));
        if (tooltip != null)
            boxSize.setToolTipText(tooltip);
        boxSize.add(Box.createHorizontalGlue());
        boxSize.add(makeSliderVert(modelPointX, "X", null));
        boxSize.add(Box.createHorizontalStrut(2));
        boxSize.add(makeSliderVert(modelPointY, "Y", null));
        boxSize.add(Box.createHorizontalGlue());
        return boxSize;
    }

    public Component makeSize(SliderIntModel modelSizeW, SliderIntModel modelSizeH, String borderTitle, String tooltip) {
        Box boxSize = Box.createHorizontalBox();
        boxSize.setBorder(BorderFactory.createTitledBorder(borderTitle));
        if (tooltip != null)
            boxSize.setToolTipText(tooltip);
        boxSize.add(Box.createHorizontalGlue());
        boxSize.add(makeSliderVert(modelSizeW, "Width", "Size Width"));
        boxSize.add(Box.createHorizontalStrut(2));
        boxSize.add(makeSliderVert(modelSizeH, "Height", "Size Height"));
        boxSize.add(Box.createHorizontalGlue());
        return boxSize;
    }

    public Component makeInterpolations(
            Supplier<CvInterpolationFlags> getter,
            Consumer<CvInterpolationFlags> setter,
            Predicate<CvInterpolationFlags> filter,
            boolean useFlagInverseMap,
            Consumer<Boolean> setterUseFlagInverseMap)
    {
        Box box4Interpolat = Box.createVerticalBox();
        box4Interpolat.setBorder(BorderFactory.createTitledBorder("Interpolations"));
        Box box4TypesRadioBttns = Box.createVerticalBox();
        Box box4TypesCheckBoxes = Box.createVerticalBox();
        {
            box4TypesRadioBttns.setToolTipText("Interpolation methods");
            ButtonGroup radioGroup1 = new ButtonGroup();
            CvInterpolationFlags.getInterpolations()
                .filter(filter)
                .forEach(interpolation ->
            {
                JRadioButton radioBtn = new JRadioButton(interpolation.name(), getter.get() == interpolation);
                radioBtn.setToolTipText("Interpolation method");
                radioBtn.addItemListener(ev -> {
                    if (ev.getStateChange() == ItemEvent.SELECTED) {
                        setter.accept(interpolation);
                        logger.trace("Interpolation method changed to {}", interpolation);
                        resetImage();
                    }
                });
                box4TypesRadioBttns.add(radioBtn);
                radioGroup1.add(radioBtn);
            });
        }
        {
            box4TypesCheckBoxes.setBorder(BorderFactory.createTitledBorder("Optional flag"));

            JCheckBox checkBoxInverseMap = new JCheckBox(CvInterpolationFlags.WARP_INVERSE_MAP.name(), useFlagInverseMap);
            checkBoxInverseMap.setToolTipText("flag, inverse transformation");
            checkBoxInverseMap.addItemListener(ev -> {
                boolean checked = (ev.getStateChange() == ItemEvent.SELECTED);
                setterUseFlagInverseMap.accept(checked);
                logger.trace("useFlagInverseMap is {}", (checked ? "checked" : "unchecked"));
                resetImage();
            });

            box4TypesCheckBoxes.add(checkBoxInverseMap);
        }
        box4Interpolat.add(box4TypesRadioBttns);
        box4Interpolat.add(Box.createVerticalStrut(2));
        box4Interpolat.add(box4TypesCheckBoxes);

        return box4Interpolat;
    }

}
