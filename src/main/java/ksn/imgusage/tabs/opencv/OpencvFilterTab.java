package ksn.imgusage.tabs.opencv;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;

import org.opencv.core.Mat;

import ksn.imgusage.tabs.BaseTab;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.type.opencv.CvBorderTypes;
import ksn.imgusage.type.opencv.CvInterpolationFlags;
import ksn.imgusage.utils.ImgHelper;

public abstract class OpencvFilterTab<TTabParams extends ITabParams> extends BaseTab<TTabParams> {

    public static final String TAB_PREFIX = "OpenCV:";

    public static final int BOOST_SIZE_MAX_X = 400;
    public static final int BOOST_SIZE_MAX_Y = 250;

    /** filtered image of the current tab */
    protected Mat imageMat;

    protected Mat getSourceMat() {
        return getSourceMat(source);
    }

    protected static Mat getSourceMat(ITab<?> source) {
        if (source instanceof OpencvFilterTab)
            return ((OpencvFilterTab<?>)source).imageMat;

        BufferedImage src = source.getImage();
        if (src == null)
            return null;
        return ImgHelper.toMat(src);
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
    protected void resetImage() {
        imageMat = null;
        super.resetImage();
    }

    public Component makeInterpolations(
            Supplier<CvInterpolationFlags> getter,
            Consumer<CvInterpolationFlags> setter,
            Predicate<CvInterpolationFlags> filter,
            boolean useFlagInverseMap,
            Consumer<Boolean> setterUseFlagInverseMap)
    {
        Box box4Interpolat = makeBoxedRadioButtons(
            CvInterpolationFlags.getInterpolations()
                .filter(filter),
            getter,
            setter,
            "Interpolations",
            "Interpolation method",
            "Interpolation methods",
            null, // radioText,
            v -> "Interpolation method",
            null // customListener
        );
        box4Interpolat.add(Box.createVerticalStrut(2));
        box4Interpolat.add(makeBoxedCheckBox(
            () -> useFlagInverseMap,
            setterUseFlagInverseMap,
            "Optional flag",
            CvInterpolationFlags.WARP_INVERSE_MAP.name(),
            "useFlagInverseMap",
            "flag, inverse transformation",
            null));
        return box4Interpolat;
    }

    protected Box makeBox4Border(
        Predicate<CvBorderTypes> filterOfBorderTypeValues,
        Supplier<CvBorderTypes> getterBorderType,
        Consumer<CvBorderTypes> setterBorderType,
        String tooltip
    ) {
        Box box4Borders = Box.createHorizontalBox();
        box4Borders.setBorder(BorderFactory.createTitledBorder("Border type"));
        box4Borders.add(Box.createHorizontalGlue());
        box4Borders.add(makeBoxedRadioButtons(
            Stream.of(CvBorderTypes.values())
                .filter(b -> b != CvBorderTypes.BORDER_REFLECT_101) // dublicate of BORDER_DEFAULT
                .filter(filterOfBorderTypeValues),
            getterBorderType,
            setterBorderType,
            null, // borderTitle,
            "Border type",
            tooltip,
            null,
            v -> tooltip,
            null));
        box4Borders.add(Box.createHorizontalGlue());

        return box4Borders;
    }

}
