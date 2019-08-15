package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;

import javax.swing.*;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.ISliderModel;
import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.opencv.type.CvArrayType;
import ksn.imgusage.tabs.opencv.type.CvMorphShapes;
import ksn.imgusage.tabs.opencv.type.CvMorphTypes;
import ksn.imgusage.type.Size;
import ksn.imgusage.type.dto.opencv.CtorParams;
import ksn.imgusage.type.dto.opencv.EMatSource;
import ksn.imgusage.type.dto.opencv.MorphologyExTabParams;
import ksn.imgusage.type.dto.opencv.StructuringElementParams;

/** <a href='https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#ga67493776e3ad1a3df63883829375201f'>Performs advanced morphological transformations</a> */
public class MorphologyExTab extends OpencvFilterTab<MorphologyExTabParams> {

    public static final String TAB_NAME = "MorphologyEx";
    public static final String TAB_FULL_NAME = TAB_PREFIX + TAB_NAME;
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

    private JPanel panelKernel1; // for params.kernel1
    private JPanel panelKernel2; // for params.kernel2
    private MorphologyExTabParams params;

    @Override
    public Component makeTab(MorphologyExTabParams params) {
        if (params == null)
            params = new MorphologyExTabParams(CvMorphTypes.MORPH_GRADIENT,
                                               EMatSource.STRUCTURING_ELEMENT,
                                               new CtorParams(1,1, CvArrayType.CV_8UC1, 1,0,0,0),
                                               new StructuringElementParams(CvMorphShapes.MORPH_RECT, new Size(10, 10), -1,-1));
        this.params = params;

        return makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }
    @Override
    public String getTabFullName() { return TAB_FULL_NAME; }

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
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(getTabName() + " options"));

        {
            JComboBox<CvMorphTypes> comboBoxMorphOper = new JComboBox<>(CvMorphTypes.values());
            comboBoxMorphOper.setBorder(BorderFactory.createTitledBorder("Morphological operation"));
            comboBoxMorphOper.setSelectedItem(params.morphologicalOperation);
            comboBoxMorphOper.setAlignmentX(Component.LEFT_ALIGNMENT);
            comboBoxMorphOper.setToolTipText("Type of a morphological operation");
            comboBoxMorphOper.addActionListener(ev -> {
                params.morphologicalOperation = (CvMorphTypes)comboBoxMorphOper.getSelectedItem();
                logger.trace("Morphological operation changed to {}", params.morphologicalOperation);
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

                JRadioButton radioBtn1 = new JRadioButton("Mat::new", params.kernelSource == EMatSource.CTOR);
                radioBtn1.setToolTipText("The Kernel created directly through the constructor");
                radioBtn1.addItemListener(ev -> {
                    if (ev.getStateChange() == ItemEvent.SELECTED) {
                        params.kernelSource = EMatSource.CTOR;
                        logger.trace("Kernel type changed to {}", params.kernelSource);
                        makeKernel1(panelCreateParams);
                        panelCreateParams.revalidate();
                        resetImage();
                    }
                });
                boxMatter.add(radioBtn1);
                radioGroup.add(radioBtn1);

                JRadioButton radioBtn2 = new JRadioButton("Imgproc.getStructuringElement", params.kernelSource == EMatSource.STRUCTURING_ELEMENT);
                radioBtn2.setToolTipText("The Kernel created using getStructuringElement.");
                radioBtn2.addItemListener(ev -> {
                    if (ev.getStateChange() == ItemEvent.SELECTED) {
                        params.kernelSource = EMatSource.STRUCTURING_ELEMENT;
                        logger.trace("Kernel type changed to {}", params.kernelSource);
                        makeKernel2(panelCreateParams);
                        panelCreateParams.revalidate();
                        resetImage();
                    }
                });
                boxMatter.add(radioBtn2);
                radioGroup.add(radioBtn2);

                if (params.kernelSource == EMatSource.CTOR)
                    makeKernel1(panelCreateParams);
                else
                if (params.kernelSource == EMatSource.STRUCTURING_ELEMENT)
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
        return box4Options;
    }

    private void makeKernel1(JPanel panelCreateParams) {
        if (panelKernel2 != null)
            panelKernel2.setVisible(false);

        if (panelKernel1 != null) {
            panelKernel1.setVisible(true);
            return;
        }

        SliderIntModel    modelRows       = new SliderIntModel   (params.kernel1.rows, 0, MIN_ROWS, MAX_ROWS);
        SliderIntModel    modelCols       = new SliderIntModel   (params.kernel1.cols, 0, MIN_COLS, MAX_COLS);
        SliderDoubleModel modelScalarVal0 = new SliderDoubleModel(params.kernel1.scalarVal0, 0, MIN_SCALAR_VECTOR, MAX_SCALAR_VECTOR);
        SliderDoubleModel modelScalarVal1 = new SliderDoubleModel(params.kernel1.scalarVal1, 0, MIN_SCALAR_VECTOR, MAX_SCALAR_VECTOR);
        SliderDoubleModel modelScalarVal2 = new SliderDoubleModel(params.kernel1.scalarVal2, 0, MIN_SCALAR_VECTOR, MAX_SCALAR_VECTOR);
        SliderDoubleModel modelScalarVal3 = new SliderDoubleModel(params.kernel1.scalarVal3, 0, MIN_SCALAR_VECTOR, MAX_SCALAR_VECTOR);

        Box boxKernelSize = Box.createHorizontalBox();
        boxKernelSize.setBorder(BorderFactory.createTitledBorder("Size"));
        boxKernelSize.setToolTipText("2D array size");
        boxKernelSize.add(Box.createHorizontalGlue());
        boxKernelSize.add(makeSliderVert(modelRows, "Rows", "Kernel size Width / number of rows"));
        boxKernelSize.add(Box.createHorizontalStrut(2));
        boxKernelSize.add(makeSliderVert(modelCols, "Cols", "Kernel size Height / number of columns"));
        boxKernelSize.add(Box.createHorizontalGlue());

        Box boxScalar = Box.createHorizontalBox();
        boxScalar.setBorder(BorderFactory.createTitledBorder("Scalar"));
        boxScalar.setToolTipText("An optional value to initialize each matrix element with");

        boxScalar.add(Box.createHorizontalGlue());
        boxScalar.add(makeSliderVert(modelScalarVal0, "v0", null));
        boxScalar.add(Box.createHorizontalStrut(0));
        boxScalar.add(makeSliderVert(modelScalarVal1, "v1", null));
        boxScalar.add(Box.createHorizontalStrut(0));
        boxScalar.add(makeSliderVert(modelScalarVal2, "v2", null));
        boxScalar.add(Box.createHorizontalStrut(0));
        boxScalar.add(makeSliderVert(modelScalarVal3, "v3", null));
        boxScalar.add(Box.createHorizontalGlue());

        Box box4ArrayType = Box.createHorizontalBox();
        box4ArrayType.setBorder(BorderFactory.createTitledBorder("Array type"));
        JComboBox<CvArrayType> comboArrayType = new JComboBox<>(CvArrayType.values());
        comboArrayType.setSelectedItem(params.kernel1.type);
        comboArrayType.addActionListener(ev -> {
            params.kernel1.type = (CvArrayType)comboArrayType.getSelectedItem();
            logger.trace("kernel1 array type changed to {}", params.kernel1.type);
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
        panelKernel1.setBorder(BorderFactory.createTitledBorder("Mat::new"));
        panelKernel1.add(box4Sliders, BorderLayout.CENTER);
        panelKernel1.add(box4ArrayType, BorderLayout.SOUTH);
        panelCreateParams.add(panelKernel1);

        addModelK1ChangeListener("kernel1.modelRows"      , modelRows      , () -> params.kernel1.rows       = modelRows      .getValue());
        addModelK1ChangeListener("kernel1.modelCols"      , modelCols      , () -> params.kernel1.cols       = modelCols      .getValue());
        addModelK1ChangeListener("kernel1.modelScalarVal0", modelScalarVal0, () -> params.kernel1.scalarVal0 = modelScalarVal0.getValue());
        addModelK1ChangeListener("kernel1.modelScalarVal1", modelScalarVal1, () -> params.kernel1.scalarVal1 = modelScalarVal1.getValue());
        addModelK1ChangeListener("kernel1.modelScalarVal2", modelScalarVal2, () -> params.kernel1.scalarVal2 = modelScalarVal2.getValue());
        addModelK1ChangeListener("kernel1.modelScalarVal3", modelScalarVal3, () -> params.kernel1.scalarVal3 = modelScalarVal3.getValue());
    }

    private void addModelK1ChangeListener(String name, ISliderModel<? extends Number> model, Runnable applyValueParams) {
        model.getWrapped().addChangeListener(ev -> {
            logger.trace("{}: value={}", name, model.getFormatedText());
            applyValueParams.run();
            resetImage();
        });
    }

    private void makeKernel2(JPanel panelCreateParams) {
        if (panelKernel1 != null)
            panelKernel1.setVisible(false);

        if (panelKernel2 != null) {
            panelKernel2.setVisible(true);
            return;
        }

        SliderIntModel modelKernelSizeW = new SliderIntModel(params.kernel2.kernelSize.width, 0, MIN_KERNEL_SIZE, MAX_KERNEL_SIZE);
        SliderIntModel modelKernelSizeH = new SliderIntModel(params.kernel2.kernelSize.height, 0, MIN_KERNEL_SIZE, MAX_KERNEL_SIZE);
        SliderIntModel modelAnchorX     = new SliderIntModel(params.kernel2.anchorX, 0, MIN_ANCHOR, MAX_ANCHOR);
        SliderIntModel modelAnchorY     = new SliderIntModel(params.kernel2.anchorY, 0, MIN_ANCHOR, MAX_ANCHOR);

        Box box4Shapes = Box.createHorizontalBox();
        box4Shapes.setBorder(BorderFactory.createTitledBorder("Shape"));
        Box box4Borders1 = Box.createVerticalBox();
        box4Borders1.setToolTipText("Shape of the structuring element");
        ButtonGroup radioGroup = new ButtonGroup();
        for (CvMorphShapes shape : CvMorphShapes.values()) {
            JRadioButton radioBtnAlg = new JRadioButton(shape.name(), shape == params.kernel2.shape);
            radioBtnAlg.setToolTipText("Shape of the structuring element");
            radioBtnAlg.addItemListener(ev -> {
                if (ev.getStateChange() == ItemEvent.SELECTED) {
                    params.kernel2.shape = shape;
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
        boxKernelSize.add(makeSliderVert(modelKernelSizeW, "Width", "Kernel size Width"));
        boxKernelSize.add(Box.createHorizontalStrut(2));
        boxKernelSize.add(makeSliderVert(modelKernelSizeH, "Height", "Kernel size Height"));
        boxKernelSize.add(Box.createHorizontalGlue());

        Box boxAnchor = Box.createHorizontalBox();
        boxAnchor.setBorder(BorderFactory.createTitledBorder("Anchor"));
        boxAnchor.setToolTipText("Anchor position within the element. The default value (−1,−1) means that the anchor is at the center. Note that only the shape of a cross-shaped element depends on the anchor position. In other cases the anchor just regulates how much the result of the morphological operation is shifted.");
        boxAnchor.add(Box.createHorizontalGlue());
        boxAnchor.add(makeSliderVert(modelAnchorX, "X", "X direction"));
        boxAnchor.add(Box.createHorizontalStrut(2));
        boxAnchor.add(makeSliderVert(modelAnchorY, "Y", "Y direction"));
        boxAnchor.add(Box.createHorizontalGlue());

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(boxKernelSize);
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(boxAnchor);
        box4Sliders.add(Box.createHorizontalGlue());

        panelKernel2 = new JPanel();
        panelKernel2.setLayout(new BorderLayout());
        panelKernel2.setBorder(BorderFactory.createTitledBorder("Imgproc.getStructuringElement"));
        panelKernel2.add(box4Shapes , BorderLayout.NORTH);
        panelKernel2.add(box4Sliders, BorderLayout.CENTER);
        panelCreateParams.add(panelKernel2);

        addModelK2ChangeListener("kernel2.modelKernelSizeW", modelKernelSizeW,  true, modelAnchorX    , () -> params.kernel2.kernelSize.width  = modelKernelSizeW.getValue());
        addModelK2ChangeListener("kernel2.modelKernelSizeH", modelKernelSizeH,  true, modelAnchorY    , () -> params.kernel2.kernelSize.height = modelKernelSizeH.getValue());
        addModelK2ChangeListener("kernel2.modelAnchorX"    , modelAnchorX    , false, modelKernelSizeW, () -> params.kernel2.anchorX           = modelAnchorX    .getValue());
        addModelK2ChangeListener("kernel2.modelAnchorY"    , modelAnchorY    , false, modelKernelSizeH, () -> params.kernel2.anchorY           = modelAnchorY    .getValue());
    }

    private void addModelK2ChangeListener(String name, ISliderModel<Integer> model, boolean checkMax, ISliderModel<Integer> modelToCheck, Runnable applyValueParams) {
        model.getWrapped().addChangeListener(ev -> {
            logger.trace("{}: value={}", name, model.getFormatedText());
            Integer val = model.getValue();
            if (checkMax) {
                if (val <= modelToCheck.getValue())
                    modelToCheck.setValue(val - 1);
                else
                    applyValueParams.run();
            } else {
                if (val >= modelToCheck.getValue())
                    modelToCheck.setValue(val + 1);
                else
                    applyValueParams.run();
            }
            resetImage();
        });
    }

    @Override
    public MorphologyExTabParams getParams() {
        return params;
    }

}
