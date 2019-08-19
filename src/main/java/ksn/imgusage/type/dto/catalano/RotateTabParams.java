package ksn.imgusage.type.dto.catalano;

import java.util.Locale;

import Catalano.Imaging.Filters.Rotate;
import Catalano.Imaging.Filters.Rotate.Algorithm;
import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.catalano.RotateTab;

/** Init parameters for {@link RotateTab} */
public class RotateTabParams implements ITabParams {

    public double           angle     = 0;
    public boolean          keepSize  = true;
    public Rotate.Algorithm algorithm = Algorithm.BICUBIC;

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ angle=%.2f, keepSize=%b, algorithm=%s }",
            angle,
            keepSize,
            algorithm.name());
    }

}
