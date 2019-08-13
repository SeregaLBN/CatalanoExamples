package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.Locale;
import java.util.function.BiConsumer;

import javax.swing.*;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.ISliderModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.type.CvArrayType;
import ksn.imgusage.tabs.opencv.type.CvMorphShapes;
import ksn.imgusage.tabs.opencv.type.CvMorphTypes;
import ksn.imgusage.tabs.opencv.type.IMatter;
import ksn.imgusage.utils.Size;

/** <a href='https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#ga67493776e3ad1a3df63883829375201f'>Performs advanced morphological transformations</a> */
public class MorphologyExTab extends OpencvFilterTab<MorphologyExTab.Params> {

    public static final String TAB_NAME = "MorphologyEx";
    public static final String TAB_DESCRIPTION = "Performs advanced morphological transformations";

    private static final int MIN_ROWS = 1;
    private static final int MAX_ROWS = 1000;
    private static final int MIN_COLS = 1;
    private static final int MAX_COLS = 1000;
    private static final double MIN_SCALAR_VECTOR = -999;
    private static final double MAX_SCALAR_VECTOR =  999;

    private static final int MIN_KERNEL_SIZE =   1;
    private static final int MAX_KERNEL_SIZE = 999;
    private static final int MIN_ANCHOR      =  -1;
    private static final int MAX_ANCHOR      = MAX_KERNEL_SIZE;


    /** Describe how to created {@link Mat}
     * @see <a href='https://docs.opencv.org/3.4.2/d3/d63/classcv_1_1Mat.html'>n-dimensional dense array class </a> */
    public enum EMatSource {
        /** The {@link Mat} created directly through the constructor {@link Mat#Mat(int, int, int, org.opencv.core.Scalar)}
         * @see <a href='https://docs.opencv.org/3.4.2/d3/d63/classcv_1_1Mat.html#a3620c370690b5ca4d40c767be6fb4ceb'>cv::Mat(int rows, int cols, int type, const Scalar &s)</a> */
        CTOR,

        /** The {@link Mat} object created by calling {@link Imgproc#getStructuringElement(int, org.opencv.core.Size, org.opencv.core.Point)}
         * @see <a href='https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#gac342a1bb6eabf6f55c803b09268e36dc'>Mat cv::getStructuringElement(int shape, Size ksize, Point anchor = Point(-1,-1) )</a> */
        STRUCTURING_ELEMENT
    }

    /** for {@link EMatSource#CTOR} */
    public static class CtorParams {

        public int         rows;
        public int         cols;
        public CvArrayType type;
        public double scalarVal0;
        public double scalarVal1;
        public double scalarVal2;
        public double scalarVal3;

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
            return String.format(Locale.US, "{ rows=%d, cols=%d, type=%s, scalar={%.2f, %.2f, %.2f, %.2f} }",
                                 rows, cols,
                                 type.name(),
                                 scalarVal0, scalarVal1, scalarVal2, scalarVal3);
        }

    }

    /** for {@link EMatSource#STRUCTURING_ELEMENT} */
    public static class StructuringElementParams {

        private CvMorphShapes shape;
        private Size kernelSize;
        private int anchorX;
        private int anchorY;

        public StructuringElementParams(CvMorphShapes shape, Size kernelSize, int anchorX, int anchorY) {
            this.shape      = shape;
            this.kernelSize = kernelSize;
            this.anchorX    = anchorX;
            this.anchorY    = anchorY;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "{ shape=%s, kernelSize=%s, anchorX=%d, anchorY=%d }",
                                 shape.name(),
                                 kernelSize.toString(),
                                 anchorX, anchorY);
        }

    }

    public static class Params implements ITabParams {
        public CvMorphTypes morphologicalOperation;
        public EMatSource               kernelSource;
        public CtorParams               kernel1;
        public StructuringElementParams kernel2;

        public Params(CvMorphTypes morphologicalOperation,
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
            return String.format(Locale.US, "{ morphologicalOperation=%s, kernelSource=%s, kernel1=%s, kernel2=%s }",
                    morphologicalOperation.name(),
                    kernelSource,
                    kernel1.toString(),
                    kernel2.toString());
        }
    }

    private JPanel panelKernel1; // for this.kernel1
    private JPanel panelKernel2; // for this.kernel2
    private final Params params;

    public MorphologyExTab(ITabHandler tabHandler, ITab<?> source) {
        this(tabHandler, source, new Params(CvMorphTypes.MORPH_GRADIENT,
                                            EMatSource.STRUCTURING_ELEMENT,
                                            new CtorParams(1,1, CvArrayType.CV_8UC1, 1,0,0,0),
                                            new StructuringElementParams(CvMorphShapes.MORPH_RECT, new Size(10, 10), -1,-1)));
    }

    public MorphologyExTab(ITabHandler tabHandler, ITab<?> source, Params params) {
        super(tabHandler, source);
        this.params = params;

        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyOpencvFilter() {
        // TODO
        // Source image. The number of channels can be arbitrary. The depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F.

        Mat kernel;
        switch (params.kernelSource) {
        case CTOR:
            kernel = new Mat(
                params.kernel1.rows,
                params.kernel1.cols,
                params.kernel1.type.getVal(),
                new Scalar(
                    params.kernel1.scalarVal0,
                    params.kernel1.scalarVal1,
                    params.kernel1.scalarVal2,
                    params.kernel1.scalarVal3
                )
            );
            break;
        case STRUCTURING_ELEMENT:
            kernel = Imgproc.getStructuringElement(
                params.kernel2.shape.getVal(),
                new org.opencv.core.Size(
                    params.kernel2.kernelSize.width,
                    params.kernel2.kernelSize.height),
                new Point(
                    params.kernel2.anchorX,
                    params.kernel2.anchorY)
            );
            break;
        default:
            logger.error("Unknown kernel source! Support him!");
            throw new IllegalArgumentException("Unknown kernel source " + params.kernelSource);
        }

        Mat dst = new Mat();
        Imgproc.morphologyEx(
            imageMat, // src
            dst,
            params.morphologicalOperation.getVal(),
            kernel);
        imageMat = dst;
    }

    @Override
    protected void makeOptions(Box box4Options) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(getTabName() + " options"));

        {
            JComboBox<CvMorphTypes> comboBoxMorphOper = new JComboBox<>(CvMorphTypes.values());
            comboBoxMorphOper.setBorder(BorderFactory.createTitledBorder("Morphological operation"));
            comboBoxMorphOper.setSelectedItem(morphologicalOperation);
            comboBoxMorphOper.setAlignmentX(Component.LEFT_ALIGNMENT);
            comboBoxMorphOper.setToolTipText("Type of a morphological operation");
            comboBoxMorphOper.addActionListener(ev -> {
                morphologicalOperation = (CvMorphTypes)comboBoxMorphOper.getSelectedItem();
                logger.trace("Morphological operation changed to {}", this.morphologicalOperation);
                resetImage();
            });
            panel.add(comboBoxMorphOper, BorderLayout.NORTH);
        }

        {
            JPanel panelKernel = new JPanel();
            panelKernel.setLayout(new BorderLayout());
            panelKernel.setBorder(BorderFactory.createTitledBorder("Kernel"));

            JPanel panelCreateParams = new JPanel();
            panelCreateParams.setLayout(new BorderLayout());

            {
                JPanel panelCreateMethod = new JPanel();
                panelCreateMethod.setLayout(new BorderLayout());
                panelCreateMethod.setBorder(BorderFactory.createTitledBorder("Create method"));
                panelCreateMethod.setToolTipText("How to create kernel?");
                Box boxMatter = Box.createVerticalBox();
                ButtonGroup radioGroup = new ButtonGroup();

                JRadioButton radioBtn1 = new JRadioButton(IMatter.CtorParams.NAME, kernel instanceof IMatter.CtorParams);
                radioBtn1.setToolTipText("The Kernel created directly through the constructor");
                radioBtn1.addItemListener(ev -> {
                    if (ev.getStateChange() == ItemEvent.SELECTED) {
                        this.kernel = kernel1;
                        logger.trace("Kernel type changed to {}", this.kernel.getClass().getSimpleName());
                        makeKernel1(panelCreateParams);
                        panelCreateParams.revalidate();
                        resetImage();
                    }
                });
                boxMatter.add(radioBtn1);
                radioGroup.add(radioBtn1);

                JRadioButton radioBtn2 = new JRadioButton(IMatter.StructuringElementParams.NAME, kernel instanceof IMatter.StructuringElementParams);
                radioBtn2.setToolTipText("The Kernel created using getStructuringElement.");
                radioBtn2.addItemListener(ev -> {
                    if (ev.getStateChange() == ItemEvent.SELECTED) {
                        this.kernel = kernel2;
                        logger.trace("Kernel type changed to {}", this.kernel.getClass().getSimpleName());
                        makeKernel2(panelCreateParams);
                        panelCreateParams.revalidate();
                        resetImage();
                    }
                });
                boxMatter.add(radioBtn2);
                radioGroup.add(radioBtn2);

                if (kernel instanceof IMatter.CtorParams)
                    makeKernel1(panelCreateParams);
                else
                if (kernel instanceof IMatter.StructuringElementParams)
                    makeKernel2(panelCreateParams);
                else
                    logger.error("Unknown kernel type! Support him!");

                panelCreateMethod.add(boxMatter  , BorderLayout.CENTER);

                panelKernel.add(panelCreateMethod, BorderLayout.NORTH);
                panelKernel.add(panelCreateParams, BorderLayout.CENTER);
            }

            panel.add(panelKernel, BorderLayout.CENTER);
        }

        box4Options.add(panel);
    }

    private void makeKernel1(JPanel panelCreateParams) {
        if (panelKernel2 != null)
            panelKernel2.setVisible(false);

        if (panelKernel1 != null) {
            panelKernel1.setVisible(true);
            return;
        }

        Box boxKernelSize = Box.createHorizontalBox();
        boxKernelSize.setBorder(BorderFactory.createTitledBorder("Size"));
        boxKernelSize.setToolTipText("2D array size");
        boxKernelSize.add(Box.createHorizontalGlue());
        boxKernelSize.add(makeSliderVert(kernel1.getModelRows(), "Rows", "Kernel size Width / number of rows"));
        boxKernelSize.add(Box.createHorizontalStrut(2));
        boxKernelSize.add(makeSliderVert(kernel1.getModelCols(), "Cols", "Kernel size Height / number of columns"));
        boxKernelSize.add(Box.createHorizontalGlue());

        Box boxScalar = Box.createHorizontalBox();
        boxScalar.setBorder(BorderFactory.createTitledBorder("Scalar"));
        boxScalar.setToolTipText("An optional value to initialize each matrix element with");

        boxScalar.add(Box.createHorizontalGlue());
        boxScalar.add(makeSliderVert(kernel1.getModelScalarVal0(), "v0", null));
        boxScalar.add(Box.createHorizontalStrut(0));
        boxScalar.add(makeSliderVert(kernel1.getModelScalarVal1(), "v1", null));
        boxScalar.add(Box.createHorizontalStrut(0));
        boxScalar.add(makeSliderVert(kernel1.getModelScalarVal2(), "v2", null));
        boxScalar.add(Box.createHorizontalStrut(0));
        boxScalar.add(makeSliderVert(kernel1.getModelScalarVal3(), "v3", null));
        boxScalar.add(Box.createHorizontalGlue());

        Box box4ArrayType = Box.createHorizontalBox();
        box4ArrayType.setBorder(BorderFactory.createTitledBorder("Array type"));
        JComboBox<CvArrayType> comboArrayType = new JComboBox<>(CvArrayType.values());
        comboArrayType.setSelectedItem(kernel1.getType());
        comboArrayType.addActionListener(ev -> {
            kernel1.setType((CvArrayType)comboArrayType.getSelectedItem());
            resetImage();
        });
        box4ArrayType.add(comboArrayType);

        Box box4Sliders = Box.createVerticalBox();
        box4Sliders.add(Box.createVerticalStrut(2));
        box4Sliders.add(boxKernelSize);
        box4Sliders.add(Box.createVerticalStrut(2));
        box4Sliders.add(boxScalar);
        box4Sliders.add(Box.createVerticalStrut(2));

        panelKernel1 = new JPanel();
        panelKernel1.setLayout(new BorderLayout());
        panelKernel1.setBorder(BorderFactory.createTitledBorder(IMatter.CtorParams.NAME));
        panelKernel1.add(box4Sliders, BorderLayout.CENTER);
        panelKernel1.add(box4ArrayType, BorderLayout.SOUTH);
        panelCreateParams.add(panelKernel1);


        BiConsumer<String, ISliderModel<?>> modelListener = (name, model) ->
            model.getWrapped().addChangeListener(ev -> {
                logger.trace("{}: value={}", name, model.getFormatedText());
                resetImage();
            });
        modelListener.accept("kernel1ModelRows"      , kernel1.getModelRows());
        modelListener.accept("kernel1ModelCols"      , kernel1.getModelCols());
        modelListener.accept("kernel1ModelScalarVal0", kernel1.getModelScalarVal0());
        modelListener.accept("kernel1ModelScalarVal1", kernel1.getModelScalarVal1());
        modelListener.accept("kernel1ModelScalarVal2", kernel1.getModelScalarVal2());
        modelListener.accept("kernel1ModelScalarVal3", kernel1.getModelScalarVal3());
    }

    private void makeKernel2(JPanel panelCreateParams) {
        if (panelKernel1 != null)
            panelKernel1.setVisible(false);

        if (panelKernel2 != null) {
            panelKernel2.setVisible(true);
            return;
        }

        Box box4Shapes = Box.createHorizontalBox();
        box4Shapes.setBorder(BorderFactory.createTitledBorder("Shape"));
        Box box4Borders1 = Box.createVerticalBox();
        box4Borders1.setToolTipText("Shape of the structuring element");
        ButtonGroup radioGroup = new ButtonGroup();
        for (CvMorphShapes shape : CvMorphShapes.values()) {
            JRadioButton radioBtnAlg = new JRadioButton(shape.name(), shape == this.kernel2.getShape());
            radioBtnAlg.setToolTipText("Shape of the structuring element");
            radioBtnAlg.addItemListener(ev -> {
                if (ev.getStateChange() == ItemEvent.SELECTED) {
                    this.kernel2.setShape(shape);
                    logger.trace("Shape param changed to {}", shape);
                    resetImage();
                }
            });
            box4Borders1.add(radioBtnAlg);
            radioGroup.add(radioBtnAlg);
        }
        box4Shapes.add(Box.createHorizontalGlue());
        box4Shapes.add(box4Borders1);
        box4Shapes.add(Box.createHorizontalGlue());

        Box boxKernelSize = Box.createHorizontalBox();
        boxKernelSize.setBorder(BorderFactory.createTitledBorder("Kernel size"));
        boxKernelSize.setToolTipText("Size of the structuring element");
        boxKernelSize.add(Box.createHorizontalGlue());
        boxKernelSize.add(makeSliderVert(kernel2.getModelKernelSizeW(), "Width", "Kernel size Width"));
        boxKernelSize.add(Box.createHorizontalStrut(2));
        boxKernelSize.add(makeSliderVert(kernel2.getModelKernelSizeH(), "Height", "Kernel size Height"));
        boxKernelSize.add(Box.createHorizontalGlue());

        Box boxAnchor = Box.createHorizontalBox();
        boxAnchor.setBorder(BorderFactory.createTitledBorder("Anchor"));
        boxAnchor.setToolTipText("Anchor position within the element. The default value (−1,−1) means that the anchor is at the center. Note that only the shape of a cross-shaped element depends on the anchor position. In other cases the anchor just regulates how much the result of the morphological operation is shifted.");
        boxAnchor.add(Box.createHorizontalGlue());
        boxAnchor.add(makeSliderVert(kernel2.getModelAnchorX(), "X", "X direction"));
        boxAnchor.add(Box.createHorizontalStrut(2));
        boxAnchor.add(makeSliderVert(kernel2.getModelAnchorY(), "Y", "Y direction"));
        boxAnchor.add(Box.createHorizontalGlue());

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(boxKernelSize);
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(boxAnchor);
        box4Sliders.add(Box.createHorizontalGlue());

        panelKernel2 = new JPanel();
        panelKernel2.setLayout(new BorderLayout());
        panelKernel2.setBorder(BorderFactory.createTitledBorder(IMatter.StructuringElementParams.NAME));
        panelKernel2.add(box4Shapes , BorderLayout.NORTH);
        panelKernel2.add(box4Sliders, BorderLayout.CENTER);
        panelCreateParams.add(panelKernel2);

        addModelChangeListener("kernel2ModelKernelSizeW", kernel2.getModelKernelSizeW(),  true, kernel2.getModelAnchorX());
        addModelChangeListener("kernel2ModelKernelSizeH", kernel2.getModelKernelSizeH(),  true, kernel2.getModelAnchorY());
        addModelChangeListener("kernel2ModelAnchorX"    , kernel2.getModelAnchorX()    , false, kernel2.getModelKernelSizeW());
        addModelChangeListener("kernel2ModelAnchorY"    , kernel2.getModelAnchorY()    , false, kernel2.getModelKernelSizeH());
    }

    private void addModelChangeListener(String name, ISliderModel<Integer> model, boolean checkMax, ISliderModel<Integer> modelToCheck) {
        model.getWrapped().addChangeListener(ev -> {
            logger.trace("{}: value={}", name, model.getFormatedText());
            Integer val = model.getValue();
            if (checkMax) {
                if (val <= modelToCheck.getValue())
                    modelToCheck.setValue(val - 1);
            } else {
                if (val >= modelToCheck.getValue())
                    modelToCheck.setValue(val + 1);
            }
            resetImage();
        });
    }

    @Override
    public void printParams() {
        logger.info("morphologicalOperation={}, kernel={}", morphologicalOperation, kernel);
    }

}
