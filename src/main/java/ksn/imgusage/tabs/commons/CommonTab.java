package ksn.imgusage.tabs.commons;

import ksn.imgusage.tabs.BaseTab;
import ksn.imgusage.tabs.ITabParams;

public abstract class CommonTab<TTabParams extends ITabParams> extends BaseTab<TTabParams> {

    private static final String GROUP = "Common";
    public static final String TAB_PREFIX = GROUP + ":";

    @Override
    public String getGroup() {
        return GROUP;
    }

}
