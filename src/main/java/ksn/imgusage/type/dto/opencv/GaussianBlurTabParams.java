package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.GaussianBlurTab;
import ksn.imgusage.tabs.opencv.type.CvBorderTypes;
import ksn.imgusage.type.Size;

/** Init parameters for {@link GaussianBlurTab} */
public class GaussianBlurTabParams implements ITabParams {

    public Size          kernelSize;
    public double        sigmaX;
    public double        sigmaY;
    public CvBorderTypes borderType;

    public GaussianBlurTabParams() {}

    public GaussianBlurTabParams(Size kernelSize, double sigmaX, double sigmaY, CvBorderTypes borderType) {
        kernelSize.width  = onlyZeroOrOdd(kernelSize.width , GaussianBlurTab.MIN_KSIZE);
        kernelSize.height = onlyZeroOrOdd(kernelSize.height, GaussianBlurTab.MIN_KSIZE);
        this.kernelSize = kernelSize;
        this.sigmaX     = sigmaX;
        this.sigmaY     = sigmaY;
        this.borderType = borderType;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ kernelSize=%s, sigmaX=%.2f, sigmaY=%.2f, borderType=%s }",
            kernelSize.toString(),
            sigmaX,
            sigmaY,
            borderType.name());
    }

    public static int onlyZeroOrOdd(int value, int prevValue) {
        if (value == 0)
            return value;
        if ((value & 1) == 0)
            return ((value - 1) == prevValue)
                ? value + 1
                : value - 1;
        return value;
    }

}
