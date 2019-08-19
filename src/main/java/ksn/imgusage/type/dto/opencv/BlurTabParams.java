package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.BlurTab;
import ksn.imgusage.type.Point;
import ksn.imgusage.type.Size;
import ksn.imgusage.type.opencv.CvBorderTypes;

/** Init parameters for {@link BlurTab} */
public class BlurTabParams implements ITabParams {

    public Size          kernelSize = new Size(5, 5);
    public Point         anchor     = new Point(-1, -1);
    public CvBorderTypes borderType = CvBorderTypes.BORDER_DEFAULT;

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ kernelSize=%s, anchor=%s, borderType=%s }",
            kernelSize.toString(),
            anchor.toString(),
            borderType.name());
    }

}
