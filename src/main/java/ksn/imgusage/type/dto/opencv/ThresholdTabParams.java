package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.ThresholdTab;
import ksn.imgusage.tabs.opencv.type.CvThresholdTypes;

/** Init parameters for {@link ThresholdTab} */
public class ThresholdTabParams implements ITabParams {

    public double            thresh;
    public double            maxVal;
    public CvThresholdTypes  threshType;
    public boolean           useOtsuMask;
    public boolean           useTriangleMask;

    public ThresholdTabParams() {}

    public ThresholdTabParams(double thresh, double maxval, CvThresholdTypes threshType, boolean useOtsuMask, boolean useTriangleMask) {
        switch (threshType) {
        case THRESH_BINARY    :
        case THRESH_BINARY_INV:
        case THRESH_TRUNC     :
        case THRESH_TOZERO    :
        case THRESH_TOZERO_INV:
            // Ok
            break;

        case THRESH_OTSU:
        case THRESH_TRIANGLE:
        case THRESH_MASK:
        default:
            throw new IllegalArgumentException("Unsupported threshType=" + threshType);
        }

        this.thresh          = thresh;
        this.maxVal          = maxval;
        this.threshType      = threshType;
        this.useOtsuMask     = useOtsuMask;
        this.useTriangleMask = useTriangleMask;
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
