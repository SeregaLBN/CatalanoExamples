package ksn.imgusage.type.opencv;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

/** Type depth constants {@link Mat} */
public enum CvDepthType {

    CV_8U      (CvType.CV_8U      ),
    CV_8S      (CvType.CV_8S      ),
    CV_16U     (CvType.CV_16U     ),
    CV_16S     (CvType.CV_16S     ),
    CV_32S     (CvType.CV_32S     ),
    CV_32F     (CvType.CV_32F     ),
    CV_64F     (CvType.CV_64F     ),
    CV_USRTYPE1(CvType.CV_USRTYPE1);


    private final int val;
    private CvDepthType(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public static CvDepthType fromValue(int val) {
        for (CvDepthType depth : CvDepthType.values())
            if (depth.getVal() == val)
                return depth;

        throw new IllegalArgumentException("Illegal argument value=" + val);
    }

}
