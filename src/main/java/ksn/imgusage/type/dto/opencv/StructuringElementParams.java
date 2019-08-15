package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.opencv.type.CvMorphShapes;
import ksn.imgusage.type.Size;

/** for {@link EMatSource#STRUCTURING_ELEMENT} */
public class StructuringElementParams {

    public CvMorphShapes shape;
    public Size          kernelSize;
    public int           anchorX;
    public int           anchorY;

    public StructuringElementParams() {}

    public StructuringElementParams(CvMorphShapes shape, Size kernelSize, int anchorX, int anchorY) {
        this.shape      = shape;
        this.kernelSize = kernelSize;
        this.anchorX    = anchorX;
        this.anchorY    = anchorY;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ shape=%s, kernelSize=%s, anchorX=%d, anchorY=%d }",
            shape.name(),
            kernelSize.toString(),
            anchorX, anchorY);
    }

}
