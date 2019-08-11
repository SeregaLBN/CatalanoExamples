package ksn.imgusage.tabs.opencv.type;

import org.opencv.imgproc.Imgproc;

/** @see <a href="https://docs.opencv.org/3.4.2/d7/d1b/group__imgproc__misc.html#gaa9e58d2860d4afa658ef70a9b1115576">type of the threshold operation</a> */
public enum CvThresholdTypes {

    THRESH_BINARY    (Imgproc.THRESH_BINARY    ),
    THRESH_BINARY_INV(Imgproc.THRESH_BINARY_INV),
    THRESH_TRUNC     (Imgproc.THRESH_TRUNC     ),
    THRESH_TOZERO    (Imgproc.THRESH_TOZERO    ),
    THRESH_TOZERO_INV(Imgproc.THRESH_TOZERO_INV),

    THRESH_MASK      (Imgproc.THRESH_MASK      ),

    /** flag, use Otsu algorithm to choose the optimal threshold value */
    THRESH_OTSU      (Imgproc.THRESH_OTSU      ),

    /** flag, use Triangle algorithm to choose the optimal threshold value */
    THRESH_TRIANGLE  (Imgproc.THRESH_TRIANGLE  );


    private final int val;
    private CvThresholdTypes(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public int getVal(boolean useOtsuAlg, boolean useTriangleAlg) {
        if (useOtsuAlg)
            return val | THRESH_OTSU.val;
        if (useTriangleAlg)
            return val | THRESH_TRIANGLE.val;
        return val;
    }

}
