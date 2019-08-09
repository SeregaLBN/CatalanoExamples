package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.function.BiConsumer;

import javax.swing.*;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.ISliderModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.opencv.type.CvArrayType;
import ksn.imgusage.tabs.opencv.type.CvMorphShapes;
import ksn.imgusage.tabs.opencv.type.CvMorphTypes;
import ksn.imgusage.tabs.opencv.type.IMatter;

/** <a href='https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#ga67493776e3ad1a3df63883829375201f'>Performs advanced morphological transformations</a> */
public class MorphologyExTab extends OpencvFilterTab {

    private CvMorphTypes morphologicalOperation;
    private IMatter kernel;
    private IMatter.CtorParams               kernel1 = new IMatter.CtorParams();
    private IMatter.StructuringElementParams kernel2 = new IMatter.StructuringElementParams();
    private JPanel panelKernel1; // for this.kernel1
    private JPanel panelKernel2; // for this.kernel2

    public MorphologyExTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, null, CvMorphTypes.MORPH_GRADIENT, new IMatter.StructuringElementParams());
    }

    public MorphologyExTab(ITabHandler tabHandler, ITab source, Boolean boosting, CvMorphTypes morphologicalOperation, IMatter kernel) {
        super(tabHandler, source, boosting);
        this.morphologicalOperation = morphologicalOperation;
        this.kernel = kernel;

        if (kernel instanceof IMatter.CtorParams)
            this.kernel1 = (IMatter.CtorParams)kernel;
        else
        if (kernel instanceof IMatter.StructuringElementParams)
            this.kernel2 = (IMatter.StructuringElementParams)kernel;
        else
            logger.error("Unknown kernel type! Support him!");

        makeTab();
    }

    @Override
    public String getTabName() { return "MorphologyEx"; }

    @Override
    protected void applyOpencvFilter() {
        // TODO
        // Source image. The number of channels can be arbitrary. The depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F.

        Mat dst = new Mat();
        Imgproc.morphologyEx(
            imageMat, // src
            dst,
            morphologicalOperation.getVal(),
            kernel.createMat());
        imageMat = dst;
    }

    @Override
    protected void makeOptions(JPanel imagePanel, Box boxCenterLeft) {
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

        boxCenterLeft.add(panel);
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
            radioBtnAlg.setActionCommand(shape.name());
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
