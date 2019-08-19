package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.GaussianBlurTab;
import ksn.imgusage.type.Size;
import ksn.imgusage.type.opencv.CvBorderTypes;

/** Init parameters for {@link GaussianBlurTab} */
public class GaussianBlurTabParams implements ITabParams {

    public Size          kernelSize = new Size(7, 0);
    public double        sigmaX     = 25;
    public double        sigmaY     = 25;
    public CvBorderTypes borderType = CvBorderTypes.BORDER_DEFAULT;

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
