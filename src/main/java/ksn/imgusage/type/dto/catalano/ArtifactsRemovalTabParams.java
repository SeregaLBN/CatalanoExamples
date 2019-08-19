package ksn.imgusage.type.dto.catalano;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.catalano.ArtifactsRemovalTab;

/** Init parameters for {@link ArtifactsRemovalTab} */
public class ArtifactsRemovalTabParams implements ITabParams {

    public int windowSize = 15;

    @Override
    public String toString() {
        return "{ windowSize=" + windowSize + " }";
    }

}
