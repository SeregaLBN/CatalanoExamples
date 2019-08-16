package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.MorphologyExTab;
import ksn.imgusage.type.opencv.CvMorphTypes;

/** Init parameters for {@link MorphologyExTab} */
public class MorphologyExTabParams implements ITabParams {

    public CvMorphTypes morphologicalOperation;
    public EMatSource               kernelSource;
    public CtorParams               kernel1;
    public StructuringElementParams kernel2;

    public MorphologyExTabParams() {}

    public MorphologyExTabParams(
        CvMorphTypes morphologicalOperation,
        EMatSource               kernelSource,
        CtorParams               kernel1,
        StructuringElementParams kernel2)
    {
        this.morphologicalOperation = morphologicalOperation;
        this.kernelSource = kernelSource;
        this.kernel1 = kernel1;
        this.kernel2 = kernel2;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ morphologicalOperation=%s, kernelSource=%s, kernel1=%s, kernel2=%s }",
            morphologicalOperation.name(),
            kernelSource,
            kernel1.toString(),
            kernel2.toString());
    }

}
