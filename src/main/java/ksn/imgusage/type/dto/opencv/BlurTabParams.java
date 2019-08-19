package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.BlurTab;
import ksn.imgusage.type.Point;
import ksn.imgusage.type.Size;
import ksn.imgusage.type.opencv.CvBorderTypes;

/** Init parameters for {@link BlurTab} */
public class BlurTabParams implements ITabParams {

    public Size          kernelSize;
    public Point         anchor;
    public CvBorderTypes borderType;

    public BlurTabParams() {}

    public BlurTabParams(Size kernelSize, Point anchor, CvBorderTypes borderType) {
        this.kernelSize = kernelSize;
        this.anchor     = anchor;
        this.borderType = borderType;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ kernelSize=%s, anchor=%s, borderType=%s }",
            kernelSize.toString(),
            anchor.toString(),
            borderType.name());
    }

}
