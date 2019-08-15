package ksn.imgusage.type.dto.catalano;

import java.util.Locale;

import Catalano.Imaging.Filters.Rotate;
import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.catalano.RotateTab;

/** Init parameters for {@link RotateTab} */
public class RotateTabParams implements ITabParams {

    public double angle;
    public boolean keepSize;
    public Rotate.Algorithm algorithm;

    public RotateTabParams() {}

    public RotateTabParams(double angle, boolean keepSize, Rotate.Algorithm algorithm) {
        this.angle = angle;
        this.keepSize = keepSize;
        this.algorithm = algorithm;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ angle=%.2f, keepSize=%b, algorithm=%s }",
            angle,
            keepSize,
            algorithm.name());
    }

}
