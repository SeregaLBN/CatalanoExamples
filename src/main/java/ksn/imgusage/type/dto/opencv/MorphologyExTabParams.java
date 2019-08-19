package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.MorphologyExTab;
import ksn.imgusage.type.Point;
import ksn.imgusage.type.opencv.CvMorphTypes;

/** Init parameters for {@link MorphologyExTab} */
public class MorphologyExTabParams implements ITabParams {

    public CvMorphTypes morphologicalOperation      = CvMorphTypes.MORPH_GRADIENT;
    public EMatSource               kernelSource    = EMatSource.STRUCTURING_ELEMENT;
    public CtorParams               kernel1         = new CtorParams();
    public StructuringElementParams kernel2         = new StructuringElementParams();
    public Point                    anchor          = new Point(-1,-1);
    public int                      iterations      = 1;

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ morphologicalOperation=%s, kernelSource=%s, kernel1=%s, kernel2=%s, anchor=%s, iterations=%d }",
            morphologicalOperation.name(),
            kernelSource,
            kernel1.toString(),
            kernel2.toString(),
            anchor.toString(),
            iterations);
    }

}
