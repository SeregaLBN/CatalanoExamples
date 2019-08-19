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
        switch (threshType) {
        case THRESH_BINARY    :
        case THRESH_BINARY_INV:
        case THRESH_TRUNC     :
        case THRESH_TOZERO    :
        case THRESH_TOZERO_INV:
            this.threshType = threshType;
            break;

        case THRESH_OTSU:
        case THRESH_TRIANGLE:
        case THRESH_MASK:
        default:
            throw new IllegalArgumentException("Unsupported threshType=" + threshType);
        }
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
