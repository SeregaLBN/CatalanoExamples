package ksn.imgusage.tabs.opencv.type;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;

/** The {@link Mat} wrapper. Describes how an Mat object is created */
public interface IMatter {

    /** Describe how to created {@link Mat} */
    enum Source {
        /** The {@link Mat} created directly through the constructor {@link Mat#Mat(int, int, int, org.opencv.core.Scalar)} */
        CTOR,

        /** The {@link Mat} object created by calling {@link Imgproc#getStructuringElement(int, org.opencv.core.Size, org.opencv.core.Point)} */
        STRUCTURING_ELEMENT
    }

    Mat createMat();

    String getName();



    /** for {@link Source#CTOR} */
    public static class CtorParams implements IMatter {

        private static final int MIN_ROWS = 1;
        private static final int MAX_ROWS = 99999;
        private static final int MIN_COLS = 1;
        private static final int MAX_COLS = 99999;
        private static final double MIN_SCALAR_VECTOR = -999;
        private static final double MAX_SCALAR_VECTOR =  999;

        public static final String NAME = "Mat::new";

        private SliderIntModel    modelRows       = new SliderIntModel(1, 0, MIN_ROWS, MAX_ROWS);
        private SliderIntModel    modelCols       = new SliderIntModel(1, 0, MIN_COLS, MAX_COLS);
        private CvArrayType       type            = CvArrayType.CV_8UC1;
        private SliderDoubleModel modelScalarVal0 = new SliderDoubleModel(1, 0, MIN_SCALAR_VECTOR, MAX_SCALAR_VECTOR);
        private SliderDoubleModel modelScalarVal1 = new SliderDoubleModel(0, 0, MIN_SCALAR_VECTOR, MAX_SCALAR_VECTOR);
        private SliderDoubleModel modelScalarVal2 = new SliderDoubleModel(0, 0, MIN_SCALAR_VECTOR, MAX_SCALAR_VECTOR);
        private SliderDoubleModel modelScalarVal3 = new SliderDoubleModel(0, 0, MIN_SCALAR_VECTOR, MAX_SCALAR_VECTOR);

        public CtorParams() {}
        public CtorParams(int rows, int cols, CvArrayType type, double scalarVal0, double scalarVal1, double scalarVal2, double scalarVal3) {
            this.modelRows.setValue(rows);
            this.modelCols.setValue(cols);
            this.type = type;
            this.modelScalarVal0.setValue(scalarVal0);
            this.modelScalarVal1.setValue(scalarVal1);
            this.modelScalarVal2.setValue(scalarVal2);
            this.modelScalarVal3.setValue(scalarVal3);
        }

        public SliderIntModel    getModelRows()       { return modelRows; }
        public SliderIntModel    getModelCols()       { return modelCols; }
        public SliderDoubleModel getModelScalarVal0() { return modelScalarVal0; }
        public SliderDoubleModel getModelScalarVal1() { return modelScalarVal1; }
        public SliderDoubleModel getModelScalarVal2() { return modelScalarVal2; }
        public SliderDoubleModel getModelScalarVal3() { return modelScalarVal3; }
        public CvArrayType       getType()                 { return type; }
        public void              setType(CvArrayType type) { this.type = type; }

        @Override
        public Mat createMat() {
            return new Mat(
                modelRows.getValue(),
                modelCols.getValue(),
                type.getVal(),
                new Scalar(
                    modelScalarVal0.getValue(),
                    modelScalarVal1.getValue(),
                    modelScalarVal2.getValue(),
                    modelScalarVal3.getValue()
                )
            );
        }

        @Override
        public String getName() { return NAME; }

        @Override
        public String toString() {
            return String.format(NAME + "(rows={%s}, cols={%s}, type={%s}, new Scalar(v0={%s}, v1={%s}, v2={%s}, v3={%s}))",
                    modelRows.getFormatedText(),
                    modelCols.getFormatedText(),
                    type.name(),
                    modelScalarVal0.getFormatedText(),
                    modelScalarVal1.getFormatedText(),
                    modelScalarVal2.getFormatedText(),
                    modelScalarVal3.getFormatedText());
        }

    }

    /** for {@link Source#STRUCTURING_ELEMENT} */
    public static class StructuringElementParams implements IMatter {

        private static final int MIN_KERNEL_SIZE  =   0;
        private static final int MAX_KERNEL_SIZE  = 999;
        private static final int MIN_ANCHOR  =  -1;
        private static final int MAX_ANCHOR  = 999;

        public static final String NAME = "Imgproc.getStructuringElement";

        private CvMorphShapes  shape            = CvMorphShapes.MORPH_RECT;
        private SliderIntModel modelKernelSizeW = new SliderIntModel(10, 0, MIN_KERNEL_SIZE, MAX_KERNEL_SIZE);
        private SliderIntModel modelKernelSizeH = new SliderIntModel(10, 0, MIN_KERNEL_SIZE, MAX_KERNEL_SIZE);
        private SliderIntModel modelAnchorX     = new SliderIntModel(-1, 0, MIN_ANCHOR, MAX_ANCHOR);
        private SliderIntModel modelAnchorY     = new SliderIntModel(-1, 0, MIN_ANCHOR, MAX_ANCHOR);

        public StructuringElementParams() {}
        public StructuringElementParams(CvMorphShapes shape, int kernelSizeW, int kernelSizeH, int anchorX, int anchorY) {
            this.shape            = shape;
            this.modelKernelSizeW.setValue(kernelSizeW);
            this.modelKernelSizeH.setValue(kernelSizeH);
            this.modelAnchorX    .setValue(anchorX);
            this.modelAnchorY    .setValue(anchorY);
        }

        public CvMorphShapes  getShape()                    { return shape; }
        public void           setShape(CvMorphShapes shape) { this.shape = shape; }
        public SliderIntModel getModelKernelSizeW() { return modelKernelSizeW; }
        public SliderIntModel getModelKernelSizeH() { return modelKernelSizeH; }
        public SliderIntModel getModelAnchorX()     { return modelAnchorX; }
        public SliderIntModel getModelAnchorY()     { return modelAnchorY; }

        @Override
        public Mat createMat() {
            return Imgproc.getStructuringElement(
                shape.getVal(),
                new Size(
                    modelKernelSizeW.getValue(),
                    modelKernelSizeH.getValue()
                ),
                new Point(
                    modelAnchorX.getValue(),
                    modelAnchorY.getValue()
                )
            );
        }

        @Override
        public String getName() { return NAME; }

        @Override
        public String toString() {
            return String.format(NAME + "(shape={%s}, kernel=new Size(width={%s}, height={%s}), anchor=new Point(x={%s}, y={%s}))",
                    shape,
                    modelKernelSizeW.getFormatedText(),
                    modelKernelSizeH.getFormatedText(),
                    modelAnchorX    .getFormatedText(),
                    modelAnchorY    .getFormatedText());
        }

    }

}
