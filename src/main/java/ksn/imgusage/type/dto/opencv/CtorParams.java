package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.type.opencv.CvArrayType;

/** for {@link EMatSource#CTOR} */
public class CtorParams {

    public int         rows  = 1;
    public int         cols  = 1;
    public CvArrayType type  = CvArrayType.CV_8UC1;
    public double scalarVal0 = 1;
    public double scalarVal1 = 0;
    public double scalarVal2 = 0;
    public double scalarVal3 = 0;

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ rows=%d, cols=%d, type=%s, scalar={%.2f, %.2f, %.2f, %.2f} }",
            rows, cols,
            type.name(),
            scalarVal0, scalarVal1, scalarVal2, scalarVal3);
    }

}
