package ksn.imgusage.type.dto.common;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.commons.ResizeTab;
import ksn.imgusage.type.Size;

/** Init parameters for {@link ResizeTab} */
public class ResizeTabParams implements ITabParams {

    public Size    keepToSize;
    public boolean useKeepAspectRatio;

    public ResizeTabParams() {}

    public ResizeTabParams(
        Size    keepToSize,
        boolean useKeepAspectRatio)
    {
        this.keepToSize         = keepToSize;
        this.useKeepAspectRatio = useKeepAspectRatio;
    }

    @Override
    public String toString() {
        return String.format(
                "{keepToSize=%s, useKeepAspectRatio=%b}",
                keepToSize,
                useKeepAspectRatio);
    }

}
