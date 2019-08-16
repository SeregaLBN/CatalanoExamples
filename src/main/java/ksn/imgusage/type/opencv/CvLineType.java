package ksn.imgusage.tabs.opencv.type;

import org.opencv.imgproc.Imgproc;

/** <a href='https://docs.opencv.org/3.4.2/d0/de1/group__core.html#ggaf076ef45de481ac96e0ab3dc2c29a777a89c5f6beef080e6df347167f85e07b9e'>Type of line</a> */
public enum CvLineType {

    FILLED ( -1 ), // Imgproc.FILLED
    LINE_4 (Imgproc.LINE_4),
    LINE_8 (Imgproc.LINE_8),
    LINE_AA(Imgproc.LINE_AA);


    private final int val;
    private CvLineType(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

}
