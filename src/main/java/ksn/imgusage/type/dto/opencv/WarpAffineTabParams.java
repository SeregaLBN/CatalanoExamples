package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.GaussianBlurTab;
import ksn.imgusage.type.Size;
import ksn.imgusage.type.opencv.CvInterpolationFlags;

/** Init parameters for {@link GaussianBlurTab} */
public class WarpAffineTabParams implements ITabParams {

    /** 2×3 transformation matrix */
    public static class TransformationMatrix {
        public double m11 = 1;
        public double m12 = 0;
        public double m13 = 0;
        public double m21 = 0;
        public double m22 = 1;
        public double m23 = 0;

        @Override
        public String toString() {
            return String.format(Locale.US,
                    "{ m11=%.3f, m12=%.3f, m13=%.3f, m21=%.3f, m22=%.3f, m23=%.3f }",
                    m11, m12, m13, m21, m22, m23);
        }
    }

    /** 2×3 transformation matrix */
    public TransformationMatrix transfMatrix = new TransformationMatrix();

    /** size of the output image */
    public Size dsize = new Size(0, 0);

    private CvInterpolationFlags interpolation = CvInterpolationFlags.INTER_LINEAR;

    public boolean useFlagInverseMap   = false;


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
            "{ transformMatrix=%s, dsize=%s, interpolation=%s, useFlagInverseMap=%b }",
            transfMatrix.toString(),
            dsize.toString(),
            interpolation,
            useFlagInverseMap);
    }

}
