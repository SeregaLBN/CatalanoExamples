package ksn.imgusage.type.opencv;

import org.opencv.imgproc.Imgproc;

/** <a href='https://docs.opencv.org/3.4.2/d7/d1b/group__imgproc__misc.html#gaa42a3e6ef26247da787bf34030ed772c'>Adaptive threshold algorithm<a> */
public enum CvAdaptiveThresholdTypes {

    /** the threshold value T(x,y) is a mean of the blockSize×blockSize neighborhood of (x,y) minus C */
    ADAPTIVE_THRESH_MEAN_C   (Imgproc.ADAPTIVE_THRESH_MEAN_C),

    /** the threshold value T(x,y) is a weighted sum (cross-correlation with a Gaussian window) of the blockSize×blockSize neighborhood of (x,y) minus C .
     * The default sigma (standard deviation) is used for the specified blockSize . See <a href='https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#gac05a120c1ae92a6060dd0db190a61afa'>getGaussianKernel<a>
     */
    ADAPTIVE_THRESH_GAUSSIAN_C(Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);


    private final int val;
    private CvAdaptiveThresholdTypes(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

}
