package ksn.imgusage.type.dto.opencv;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/** Describe how to created {@link Mat}
 * @see <a href='https://docs.opencv.org/3.4.2/d3/d63/classcv_1_1Mat.html'>n-dimensional dense array class </a> */
public enum EMatSource {

    /** The {@link Mat} created directly through the constructor {@link Mat#Mat(int, int, int, org.opencv.core.Scalar)}
     * @see <a href='https://docs.opencv.org/3.4.2/d3/d63/classcv_1_1Mat.html#a3620c370690b5ca4d40c767be6fb4ceb'>cv::Mat(int rows, int cols, int type, const Scalar &s)</a> */
    CTOR,

    /** The {@link Mat} object created by calling {@link Imgproc#getStructuringElement(int, org.opencv.core.Size, org.opencv.core.Point)}
     * @see <a href='https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#gac342a1bb6eabf6f55c803b09268e36dc'>Mat cv::getStructuringElement(int shape, Size ksize, Point anchor = Point(-1,-1) )</a> */
    STRUCTURING_ELEMENT

}
