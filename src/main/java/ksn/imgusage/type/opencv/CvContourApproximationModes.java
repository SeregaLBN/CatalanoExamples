package ksn.imgusage.type.opencv;

import org.opencv.imgproc.Imgproc;

/** @see <a href='https://docs.opencv.org/3.4.2/d3/dc0/group__imgproc__shape.html#ga4303f45752694956374734a03c54d5ff'>The contour approximation algorithm</a> */
public enum CvContourApproximationModes {

    /** stores absolutely all the contour points. That is, any 2 subsequent points (x1,y1) and (x2,y2) of the contour will be either horizontal, vertical or diagonal neighbors, that is, max(abs(x1-x2),abs(y2-y1))==1 */
    CHAIN_APPROX_NONE     (Imgproc.CHAIN_APPROX_NONE),

    /** compresses horizontal, vertical, and diagonal segments and leaves only their end points. For example, an up-right rectangular contour is encoded with 4 points */
    CHAIN_APPROX_SIMPLE   (Imgproc.CHAIN_APPROX_SIMPLE),

    /** applies one of the flavors of the Teh-Chin chain approximation algorithm  */
    CHAIN_APPROX_TC89_L1  (Imgproc.CHAIN_APPROX_TC89_L1),

    /** applies one of the flavors of the Teh-Chin chain approximation algorithm */
    CHAIN_APPROX_TC89_KCOS(Imgproc.CHAIN_APPROX_TC89_KCOS);


    private final int val;
    private CvContourApproximationModes(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

}
