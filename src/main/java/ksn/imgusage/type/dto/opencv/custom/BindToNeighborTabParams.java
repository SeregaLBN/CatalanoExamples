package ksn.imgusage.type.dto.opencv.custom;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.custom.BindToNeighborTab;
import ksn.imgusage.type.Size;

/** Init parameters for {@link BindToNeighborTab} */
public class BindToNeighborTabParams implements ITabParams {

    public Size minLimitContours = new Size(5, 5);
    public Size maxLimitContours = new Size(100, 100);

    public int bindSize = 40;
    public int showBindIndex = -1;

    @Override
    public String toString() {
        return "{ minLimitContours=" + minLimitContours
            + ", maxLimitContours=" + maxLimitContours
            + ", bindSize=" + bindSize
            + ", showBindIndex=" + showBindIndex
            + " }";
    }

}
