package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.type.Point;
import ksn.imgusage.type.Size;
import ksn.imgusage.type.opencv.CvMorphShapes;

/** for {@link EMatSource#STRUCTURING_ELEMENT} */
public class StructuringElementParams {

    public CvMorphShapes shape      = CvMorphShapes.MORPH_RECT;
    public Size          kernelSize = new Size(10, 10);
    public Point         anchor     = new Point(-1,-1);

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ shape=%s, kernelSize=%s, anchor=%s }",
            shape.name(),
            kernelSize.toString(),
            anchor.toString());
    }

}
