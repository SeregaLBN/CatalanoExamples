package ksn.imgusage.tabs.opencv.type;

import org.opencv.imgproc.Imgproc;

/** @see <a href="https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#ga7be549266bad7b2e6a04db49827f9f32">type of morphological operation</a> */
public enum CvMorphTypes {

    /** Erodes an image by using a specific structuring element. */
    MORPH_ERODE    (Imgproc.MORPH_ERODE),

    /** Dilates an image by using a specific structuring element. */
    MORPH_DILATE   (Imgproc.MORPH_DILATE),

    /** an opening operation
        <p>𝚍𝚜𝚝=open(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)=dilate(erode(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)) */
    MORPH_OPEN     (Imgproc.MORPH_OPEN),

    /** a closing operation
        <p>𝚍𝚜𝚝=close(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)=erode(dilate(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)) */
    MORPH_CLOSE    (Imgproc.MORPH_CLOSE),

    /** a morphological gradient
        <p>𝚍𝚜𝚝=morph_grad(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)=dilate(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)−erode(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝) */
    MORPH_GRADIENT (Imgproc.MORPH_GRADIENT),

    /** "top hat"
        <p>𝚍𝚜𝚝=tophat(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)=𝚜𝚛𝚌−open(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝) */
    MORPH_TOPHAT   (Imgproc.MORPH_TOPHAT),

    /** "black hat"
        <p>𝚍𝚜𝚝=blackhat(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)=close(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)−𝚜𝚛𝚌 */
    MORPH_BLACKHAT (Imgproc.MORPH_BLACKHAT),

    /** "hit or miss" .- Only supported for CV_8UC1 binary images. A tutorial can be found in the documentation */
    MORPH_HITMISS  (Imgproc.MORPH_HITMISS);

    private final int val;
    private CvMorphTypes(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

}
