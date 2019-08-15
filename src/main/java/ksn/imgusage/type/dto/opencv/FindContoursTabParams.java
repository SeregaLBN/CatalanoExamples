package ksn.imgusage.type.dto.opencv;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.FindContoursTab;
import ksn.imgusage.tabs.opencv.type.CvContourApproximationModes;
import ksn.imgusage.tabs.opencv.type.CvRetrievalModes;
import ksn.imgusage.type.Size;

/** Init parameters for {@link FindContoursTab} */
public class FindContoursTabParams implements ITabParams {

    public CvRetrievalModes            mode;
    public CvContourApproximationModes method;
    public EFindContoursDrawMethod     drawMethod;
    public Size                        minLimitContours;
    public int                         maxContourArea;

    public FindContoursTabParams() {}

    public FindContoursTabParams(
        CvRetrievalModes mode, CvContourApproximationModes method,
        EFindContoursDrawMethod drawMethod, Size minLimitContours,
        int maxContourArea)
    {
        this.mode   = mode;
        this.method = method;
        this.drawMethod = drawMethod;
        this.minLimitContours = minLimitContours;
        this.maxContourArea   = maxContourArea;
    }

    @Override
    public String toString() {
        return "{ mode=" + mode
            + ", method=" + method
            + ", drawMethod=" + drawMethod
            + ", minLimitContours=" + minLimitContours
            + ", maxContourArea=" + maxContourArea
            + " }";
    }

}
