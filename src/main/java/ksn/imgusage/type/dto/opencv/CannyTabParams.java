package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.CannyTab;

/** Init parameters for {@link CannyTab} */
public class CannyTabParams implements ITabParams {

    public double  threshold1   = 3;
    public double  threshold2   = 3;
    public int     apertureSize = 5;
    public boolean l2gradient   = true;

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
