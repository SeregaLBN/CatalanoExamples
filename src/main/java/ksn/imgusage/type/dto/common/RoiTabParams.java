package ksn.imgusage.type.dto.common;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.commons.RoiTab;
import ksn.imgusage.type.Padding;

/** Init parameters for {@link RoiTab} */
public class RoiTabParams implements ITabParams {

    /** padding of Region Of Interest */
    public Padding boundOfRoi = new Padding();

    public RoiTabParams() {}

    public RoiTabParams(
        Padding boundOfRoi)
    {
        this.boundOfRoi = boundOfRoi;
    }

    @Override
    public String toString() {
        return "{boundOfRoi=" + boundOfRoi + "}";
    }

}
