package ksn.imgusage.type.opencv;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

/** Array type for {@link Mat} */
public enum CvArrayType {

    CV_8UC1 (CvType.CV_8UC1 ),
    CV_8UC2 (CvType.CV_8UC2 ),
    CV_8UC3 (CvType.CV_8UC3 ),
    CV_8UC4 (CvType.CV_8UC4 ),
    CV_8SC1 (CvType.CV_8SC1 ),
    CV_8SC2 (CvType.CV_8SC2 ),
    CV_8SC3 (CvType.CV_8SC3 ),
    CV_8SC4 (CvType.CV_8SC4 ),
    CV_16UC1(CvType.CV_16UC1),
    CV_16UC2(CvType.CV_16UC2),
    CV_16UC3(CvType.CV_16UC3),
    CV_16UC4(CvType.CV_16UC4),
    CV_16SC1(CvType.CV_16SC1),
    CV_16SC2(CvType.CV_16SC2),
    CV_16SC3(CvType.CV_16SC3),
    CV_16SC4(CvType.CV_16SC4),
    CV_32SC1(CvType.CV_32SC1),
    CV_32SC2(CvType.CV_32SC2),
    CV_32SC3(CvType.CV_32SC3),
    CV_32SC4(CvType.CV_32SC4),
    CV_32FC1(CvType.CV_32FC1),
    CV_32FC2(CvType.CV_32FC2),
    CV_32FC3(CvType.CV_32FC3),
    CV_32FC4(CvType.CV_32FC4),
    CV_64FC1(CvType.CV_64FC1),
    CV_64FC2(CvType.CV_64FC2),
    CV_64FC3(CvType.CV_64FC3),
    CV_64FC4(CvType.CV_64FC4);


    private final int val;
    private CvArrayType(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

}
