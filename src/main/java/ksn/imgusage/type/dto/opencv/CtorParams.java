package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.opencv.type.CvArrayType;

/** for {@link EMatSource#CTOR} */
public class CtorParams {

    public int         rows;
    public int         cols;
    public CvArrayType type;
    public double scalarVal0;
    public double scalarVal1;
    public double scalarVal2;
    public double scalarVal3;

    public CtorParams() {}

    public CtorParams(int rows, int cols, CvArrayType type, double scalarVal0, double scalarVal1, double scalarVal2, double scalarVal3) {
        this.rows = rows;
        this.cols = cols;
        this.type = type;
        this.scalarVal0 = scalarVal0;
        this.scalarVal1 = scalarVal1;
        this.scalarVal2 = scalarVal2;
        this.scalarVal3 = scalarVal3;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ rows=%d, cols=%d, type=%s, scalar={%.2f, %.2f, %.2f, %.2f} }",
            rows, cols,
            type.name(),
            scalarVal0, scalarVal1, scalarVal2, scalarVal3);
    }

}
