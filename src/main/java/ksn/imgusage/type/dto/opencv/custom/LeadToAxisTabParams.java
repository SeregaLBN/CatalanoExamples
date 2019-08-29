package ksn.imgusage.type.dto.another;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.another.LeadToAxisTab;

/** Init parameters for {@link LeadToAxisTab} */
public class LeadToAxisTabParams implements ITabParams {

    public boolean leadToHorizontal = true;

    /** leave not exceeding the median by X percent (101 - no limits) */
    public int limitAreaDiffInPercent = 101; // 0% .. 101%   101% - no limits

    @Override
    public String toString() { return "{ leadToHorizontal=" + leadToHorizontal + ", limitAreaDiffInPercent=" + limitAreaDiffInPercent + " }"; }

}
