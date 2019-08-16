package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.type.opencv.CvBorderTypes;

/** Init parameters for {@link BirateralTab} */
public class BilateralTabParams implements ITabParams {

    public int           diameter;
    public double        sigmaColor;
    public double        sigmaSpace;
    public CvBorderTypes borderType;

    public BilateralTabParams() {}

    public BilateralTabParams(int diameter, double sigmaColor, double sigmaSpace, CvBorderTypes borderType) {
        this.diameter   = diameter;
        this.sigmaColor = sigmaColor;
        this.sigmaSpace = sigmaSpace;
        this.borderType = borderType;
    }

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
