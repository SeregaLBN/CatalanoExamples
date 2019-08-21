package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.PerspectiveTransformTab;
import ksn.imgusage.type.Point;
import ksn.imgusage.type.Size;
import ksn.imgusage.type.opencv.CvInterpolationFlags;

/** Init parameters for {@link PerspectiveTransformTab} */
public class PerspectiveTransformTabParams implements ITabParams {

    // perspective transformation for the corresponding 4 point pairs
    public Point pointLeftTop     = new Point(-1, -1);
    public Point pointRightTop    = new Point(-1, -1);
    public Point pointLeftBottom  = new Point(-1, -1);
    public Point pointRightBottom = new Point(-1, -1);

    /** size of the output image */
    public Size dsize = new Size(0, 0);

    private CvInterpolationFlags interpolation = CvInterpolationFlags.INTER_LINEAR;

    public boolean useFlagInverseMap = false;

    public CvInterpolationFlags getInterpolation() {
        return interpolation;
    }
    public void setInterpolation(CvInterpolationFlags interpolation) {
        if (interpolation.getVal() < CvInterpolationFlags.INTER_MAX.getVal())
            this.interpolation = interpolation;
        else
            throw new IllegalArgumentException("Unsupported interpolation=" + interpolation);
    }


    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ pointLeftTop=%s, pointRightTop=%s, pointLeftBottom=%s, pointRightBottom=%s, dsize=%s, interpolation=%s, useFlagInverseMap=%b }",
            pointLeftTop    .toString(),
            pointRightTop   .toString(),
            pointLeftBottom .toString(),
            pointRightBottom.toString(),
            dsize.toString(),
            interpolation,
            useFlagInverseMap);
    }

}
