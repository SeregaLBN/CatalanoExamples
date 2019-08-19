package ksn.imgusage.type.dto.catalano;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.catalano.FrequencyFilterTab;

/** Init parameters for {@link FrequencyFilterTab} */
public class FrequencyFilterTabParams implements ITabParams {

    public int min =   0;
    public int max = 100;

    @Override
    public String toString() { return "{ min=" + min + ", max=" + max + " }"; }

}
