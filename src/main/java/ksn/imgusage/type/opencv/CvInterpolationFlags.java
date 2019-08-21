package ksn.imgusage.type.opencv;

import java.util.stream.Stream;

import org.opencv.imgproc.Imgproc;

/** @see <a href='https://docs.opencv.org/3.4.2/da/d54/group__imgproc__transform.html#ga5bb5a1fea74ea38e1a5445ca803ff121'>Interpolation algorithm</a> */
public enum CvInterpolationFlags {

    /** nearest neighbor interpolation */
    INTER_NEAREST       (Imgproc.INTER_NEAREST),

    /** bilinear interpolation */
    INTER_LINEAR        (Imgproc.INTER_LINEAR),

    /** bicubic interpolation */
    INTER_CUBIC         (Imgproc.INTER_CUBIC),

    /** resampling using pixel area relation. It may be a preferred method for image decimation, as it gives moire'-free results.
     *  But when the image is zoomed, it is similar to the INTER_NEAREST method */
    INTER_AREA          (Imgproc.INTER_AREA),

    /** Lanczos interpolation over 8x8 neighborhood */
    INTER_LANCZOS4      (Imgproc.INTER_LANCZOS4),

    /** Bit exact bilinear interpolation */
    INTER_LINEAR_EXACT  (Imgproc.INTER_LINEAR_EXACT),

    /** mask for interpolation codes */
    INTER_MAX           (Imgproc.INTER_MAX),

    /** flag, fills all of the destination image pixels. If some of them correspond to outliers in the source image, they are set to zero */
    WARP_FILL_OUTLIERS  (Imgproc.WARP_FILL_OUTLIERS),

    /** flag, inverse transformation */
    WARP_INVERSE_MAP    (Imgproc.WARP_INVERSE_MAP);


    private final int val;
    private CvInterpolationFlags(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public int getVal(boolean useFlagFillOutliers, boolean useFlagInverseMap) {
        int res = this.getVal();
        if (useFlagFillOutliers)
            res |= WARP_FILL_OUTLIERS.val;
        if (useFlagInverseMap)
            res |= WARP_INVERSE_MAP.val;
        return res;
    }

    public static Stream<CvInterpolationFlags> getInterpolations() {
        return Stream.of(CvInterpolationFlags.values())
            .filter(e -> e != INTER_MAX)
            .filter(e -> (e.getVal() & INTER_MAX.val) != 0);
    }

}
