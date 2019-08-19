package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.BoxTab;
import ksn.imgusage.type.Point;
import ksn.imgusage.type.Size;
import ksn.imgusage.type.opencv.CvBorderTypes;

/** Init parameters for {@link BoxTab} */
public class BoxTabParams implements ITabParams {

    public int           ddepth;
    public Size          kernelSize;
    public Point         anchor;
    public boolean       normalize;
    public CvBorderTypes borderType;

    public BoxTabParams() {}

    public BoxTabParams(int ddepth, Size kernelSize, Point anchor, boolean normalize, CvBorderTypes borderType) {
        this.ddepth     = ddepth;
        this.kernelSize = kernelSize;
        this.anchor     = anchor;
        this.normalize  = normalize;
        this.borderType = borderType;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ ddepth=%d, kernelSize=%s, anchor=%s, normalize=%b, borderType=%s }",
            ddepth,
            kernelSize.toString(),
            anchor.toString(),
            normalize,
            borderType.name());
    }

}
