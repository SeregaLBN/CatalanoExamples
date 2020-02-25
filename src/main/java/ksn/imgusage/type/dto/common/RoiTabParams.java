package ksn.imgusage.type.dto.common;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.commons.RoiTab;
import ksn.imgusage.type.Rect;
import ksn.imgusage.type.Size;

/** Init parameters for {@link RoiTab} */
public class RoiTabParams implements ITabParams {

    /** Ratio to source image */
    public Size ratio;

    /** rectangle - Region Of Interest */
    public Rect roi;

    public RoiTabParams() {}

    public RoiTabParams(Size ratio, Rect roi) {
        this.ratio = ratio;
        this.roi = roi;
    }

    @Override
    public String toString() {
        return "{ ratio=" + ratio + ", roi=" + roi + " }";
    }

}
