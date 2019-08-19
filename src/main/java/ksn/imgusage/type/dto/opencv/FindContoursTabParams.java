package ksn.imgusage.type.dto.opencv;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.FindContoursTab;
import ksn.imgusage.type.Size;
import ksn.imgusage.type.opencv.CvContourApproximationModes;
import ksn.imgusage.type.opencv.CvRetrievalModes;

/** Init parameters for {@link FindContoursTab} */
public class FindContoursTabParams implements ITabParams {

    public CvRetrievalModes            mode             = CvRetrievalModes.RETR_EXTERNAL;

    public CvContourApproximationModes method           = CvContourApproximationModes.CHAIN_APPROX_SIMPLE;

    public EFindContoursDrawMethod     drawMethod       = EFindContoursDrawMethod.EXTERNAL_RECT;

    /** usage only for {@link EFindContoursDrawMethod#EXTERNAL_RECT} */
    public Size                        minLimitContours = new Size(10, 10);

    /** usage only for {@link EFindContoursDrawMethod#DRAW_CONTOURS} */
    public int                         maxContourArea   = 100;

    /** usage only for {@link EFindContoursDrawMethod#DRAW_CONTOURS} */
    public boolean                     fillContour      = true;

    @Override
    public String toString() {
        return "{ mode=" + mode
            + ", method=" + method
            + ", drawMethod=" + drawMethod
            + ", minLimitContours=" + minLimitContours
            + ", maxContourArea=" + maxContourArea
            + ", fillContour=" + fillContour
            + " }";
    }

}
