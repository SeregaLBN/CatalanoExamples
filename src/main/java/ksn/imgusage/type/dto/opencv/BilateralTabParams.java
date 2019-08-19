package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.type.opencv.CvBorderTypes;

/** Init parameters for {@link BirateralTab} */
public class BilateralTabParams implements ITabParams {

    public int           diameter   = 2;
    public double        sigmaColor = 25;
    public double        sigmaSpace = 25;
    public CvBorderTypes borderType = CvBorderTypes.BORDER_DEFAULT;

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ diameter=%d, sigmaColor=%.2f, sigmaSpace=%.2f, borderType=%s }",
            diameter,
            sigmaColor,
            sigmaSpace,
            borderType.name());
    }

}
