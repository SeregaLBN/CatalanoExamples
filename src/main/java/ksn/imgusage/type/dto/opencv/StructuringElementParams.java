package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.type.Point;
import ksn.imgusage.type.Size;
import ksn.imgusage.type.opencv.CvMorphShapes;

/** for {@link EMatSource#STRUCTURING_ELEMENT} */
public class StructuringElementParams {

    public CvMorphShapes shape;
    public Size          kernelSize;
    public Point         anchor;

    public StructuringElementParams() {}

    public StructuringElementParams(CvMorphShapes shape, Size kernelSize, Point anchor) {
        this.shape      = shape;
        this.kernelSize = kernelSize;
        this.anchor     = anchor;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ shape=%s, kernelSize=%s, anchor=%s }",
            shape.name(),
            kernelSize.toString(),
            anchor.toString());
    }

}
