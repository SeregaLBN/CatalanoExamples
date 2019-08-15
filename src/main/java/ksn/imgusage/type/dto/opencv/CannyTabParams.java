package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.CannyTab;

/** Init parameters for {@link CannyTab} */
public class CannyTabParams implements ITabParams {

    public double  threshold1;
    public double  threshold2;
    public int     apertureSize;
    public boolean l2gradient;

    public CannyTabParams() {}

    public CannyTabParams(double threshold1, double threshold2, int apertureSize, boolean l2gradient) {
        this.threshold1 = threshold1;
        this.threshold2 = threshold2;
        this.apertureSize = onlyOdd(apertureSize, CannyTab.MIN_APERTURE_SIZE);
        this.l2gradient = l2gradient;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ threshold1=%.2f, threshold2=%.2f, apertureSize=%d, l2gradient=%b }",
            threshold1,
            threshold2,
            apertureSize,
            l2gradient);
    }

    public static int onlyOdd(int value, int prevValue) {
        if ((value & 1) == 0)
            return ((value - 1) == prevValue)
                ? value + 1
                : value - 1;
        return value;
    }

}
