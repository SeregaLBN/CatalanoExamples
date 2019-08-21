package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.ThresholdTab;
import ksn.imgusage.type.opencv.CvThresholdTypes;

/** Init parameters for {@link ThresholdTab} */
public class ThresholdTabParams implements ITabParams {

    public  double            thresh          = 100;
    public  double            maxVal          = 250;
    private CvThresholdTypes  threshType      = CvThresholdTypes.THRESH_BINARY;
    public  boolean           useOtsuMask     = false;
    public  boolean           useTriangleMask = false;

    public CvThresholdTypes getThreshType() {
        return threshType;
    }
    public void setThreshType(CvThresholdTypes threshType) {
        if (threshType.getVal() < CvThresholdTypes.THRESH_MASK.getVal())
            this.threshType = threshType;
        else
            throw new IllegalArgumentException("Unsupported threshType=" + threshType);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ threshX=%.2f, maxVal=%.2f, threshType=%s, useOtsuMask=%b, useTriangleMask=%b }",
            thresh,
            maxVal,
            threshType.name(),
            useOtsuMask,
            useTriangleMask);
    }

}
