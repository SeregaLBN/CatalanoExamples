package ksn.imgusage.type.dto.catalano;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.catalano.FrequencyFilterTab;

/** Init parameters for {@link FrequencyFilterTab} */
public class FrequencyFilterTabParams implements ITabParams {

    public int min;
    public int max;

    public FrequencyFilterTabParams() {}

    public FrequencyFilterTabParams(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public String toString() { return "{ min=" + min + ", max=" + max + " }"; }

}
