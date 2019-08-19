package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.BoxTab;
import ksn.imgusage.type.Point;
import ksn.imgusage.type.Size;
import ksn.imgusage.type.opencv.CvBorderTypes;

/** Init parameters for {@link BoxTab} */
public class BoxTabParams implements ITabParams {

    public int           ddepth     = -1;
    public Size          kernelSize = new Size(5, 5);
    public Point         anchor     = new Point(-1, -1);
    public boolean       normalize  = true;
    public CvBorderTypes borderType = CvBorderTypes.BORDER_DEFAULT;

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
