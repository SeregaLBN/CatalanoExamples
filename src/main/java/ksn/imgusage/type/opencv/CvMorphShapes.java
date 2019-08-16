package ksn.imgusage.type.opencv;

import org.opencv.imgproc.Imgproc;

/** @see <a href='https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#gac2db39b56866583a95a5680313c314ad'>shape of the structuring element</a> */
public enum CvMorphShapes {

    /** a rectangular structuring element */
    MORPH_RECT   (Imgproc.MORPH_RECT),

    /** a cross-shaped structuring element */
    MORPH_CROSS  (Imgproc.MORPH_CROSS),

    /** an elliptic structuring element, that is, a filled ellipse inscribed into the rectangle Rect(0, 0, esize.width, esize.height) */
    MORPH_ELLIPSE(Imgproc.MORPH_ELLIPSE);


    private final int val;
    private CvMorphShapes(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

}
