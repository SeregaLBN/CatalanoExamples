package ksn.imgusage.tabs.opencv;

import java.awt.Component;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.xphoto.Xphoto;

import ksn.imgusage.type.dto.opencv.ColorizedTabParams;
import ksn.imgusage.type.dto.opencv.ColorizedTabParams.ECastTo;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.OpenCvHelper;
import ksn.imgusage.utils.UiHelper;

/** Transform image colors {@link ImgHelper} {@link OpenCvHelper} */
public class ColorizedTab extends OpencvFilterTab<ColorizedTabParams> {

    public static final String TAB_TITLE = "Colorized";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Cast image to gray or full colors";

    private ColorizedTabParams params;
    private Consumer<Boolean> onEnablerUseWhiteBalancer;

    @Override
    public Component makeTab(ColorizedTabParams params) {
        if (params == null)
            params = new ColorizedTabParams();
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
    protected void applyOpencvFilter() {
        switch (params.colorsTo) {
        case GRAY:
            imageMat = OpenCvHelper.toGray(imageMat);
            break;
        case RGB:
            imageMat = OpenCvHelper.to3Channel(imageMat);
            break;
        default:
            throw new UnsupportedOperationException("Unsupported image colors cast to " + params.colorsTo);
        }

        boolean canUseWhiteBalancer = imageMat.type() == CvType.CV_8UC3 || imageMat.type() == CvType.CV_16UC3;
        if (onEnablerUseWhiteBalancer != null)
            onEnablerUseWhiteBalancer.accept(canUseWhiteBalancer);

        if (params.useWhiteBalancer && canUseWhiteBalancer) {
            Mat dst = new Mat();
            Xphoto.createGrayworldWB().balanceWhite(imageMat, dst);
            imageMat = dst;
        }
    }

    @Override
    protected Component makeOptions() {
        Box boxColorsTo = makeBoxedRadioButtons(
            Stream.of(ColorizedTabParams.ECastTo.values()), // values
            () -> params.colorsTo,                          // getter
            v  -> params.colorsTo = v,                      // setter
            "Transform colors",                             // borderTitle
            "params.useGray",                               // paramName
            TAB_DESCRIPTION,                                // boxTip
            v -> v.userText() + "                    ",     // radioText
            ECastTo::userTip,                               // radioTip
            null                                            // customListener
        );

        JCheckBox cntrlUseWhiteBalancer = makeCheckBox(
            () -> params.useWhiteBalancer,
            v  -> params.useWhiteBalancer = v,
            "WhiteBalancer",
            "params.useWhiteBalancer",
            "Applies white balancing to the input image", null);
        onEnablerUseWhiteBalancer = canUseWhiteBalancer -> UiHelper.enableAllChilds(cntrlUseWhiteBalancer, canUseWhiteBalancer);

        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        box4Options.add(boxColorsTo);
        box4Options.add(cntrlUseWhiteBalancer);

        return box4Options;
    }

    @Override
    public ColorizedTabParams getParams() {
        return params;
    }

}
