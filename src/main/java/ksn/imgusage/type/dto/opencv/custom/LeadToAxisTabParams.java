package ksn.imgusage.type.dto.opencv.custom;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.custom.LeadToAxisTab;

/** Init parameters for {@link LeadToAxisTab} */
public class LeadToAxisTabParams implements ITabParams {

    public boolean leadToHorizontal = true;
    public boolean keepSourceSize = true;
    public boolean cutBorders = !keepSourceSize;

    /** leave not exceeding the median by X percent (101 - no limits) */
    public int limitAreaDiffInPercent = 101; // 0% .. 101%   101% - no limits

    public int angleRangeMin = -60;
    public int angleRangeMax = +60;

    @Override
    public String toString() {
        return "{"
            + " leadToHorizontal=" + leadToHorizontal
            + ", keepSourceSize=" + keepSourceSize
            + ", cutBorders=" + cutBorders
            + ", limitAreaDiffInPercent=" + limitAreaDiffInPercent
            + ", angleRangeMin=" + angleRangeMin
            + ", angleRangeMax=" + angleRangeMax
            + " }";
    }

}
