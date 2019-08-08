package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

import javax.swing.*;

import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ksn.imgusage.model.ISliderModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.opencv.type.CvArrayType;
import ksn.imgusage.tabs.opencv.type.CvMorphTypes;
import ksn.imgusage.tabs.opencv.type.IMatter;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.UiHelper;

/** <a href='https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#ga67493776e3ad1a3df63883829375201f'>Performs advanced morphological transformations</a> */
public class MorphologyExTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(MorphologyExTab.class);

    private final ITabHandler tabHandler;
    private ITab source;
    private BufferedImage image;
    private boolean boosting = true;
    private Runnable imagePanelInvalidate;
    private CvMorphTypes morphologicalOperation;
    private IMatter kernel;
    private IMatter.CtorParams               kernel1 = new IMatter.CtorParams();
    private IMatter.StructuringElementParams kernel2 = new IMatter.StructuringElementParams();
    private Timer timer;
    private JPanel panelKernel1; // for this.kernel1
    private JPanel panelKernel2; // for this.kernel2

    public MorphologyExTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.morphologicalOperation = CvMorphTypes.MORPH_GRADIENT;
        this.kernel = new IMatter.StructuringElementParams();

        makeTab();
    }

    public MorphologyExTab(ITabHandler tabHandler, ITab source, boolean boosting, CvMorphTypes morphologicalOperation, IMatter kernel) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.boosting = boosting;
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
    public BufferedImage getImage() {
        if (image != null)
            return image;

        BufferedImage src = source.getImage();
        if (src == null)
            return null;

        JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(tabHandler.getTabPanel());
        try {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Mat matSrc = ImgHelper.toMat(src);
            // TODO
            // Source image. The number of channels can be arbitrary. The depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F.

            try {
                Mat matDest = new Mat();
                Imgproc.morphologyEx(
                    matSrc,
                    matDest,
                    morphologicalOperation.getVal(),
                    kernel.createMat());

                image = ImgHelper.toBufferedImage(matDest);
            } catch (CvException ex) {
                logger.error(ex.toString());
                image = ImgHelper.failedImage();
            }
        } finally {
            frame.setCursor(Cursor.getDefaultCursor());
        }
        return image;
    }


    @Override
    public void resetImage() {
        if (image == null)
            return;

        image = null;
        imagePanelInvalidate.run();
        SwingUtilities.invokeLater(() -> tabHandler.onImageChanged(this));
    }

    @Override
    public void updateSource(ITab newSource) {
        this.source = newSource;
        resetImage();
    }

    private void makeTab() {
        UiHelper.makeTab(
             tabHandler,
             this,
             "MorphologyEx",
             true,
             this::makeFilterOptions
         );
    }

    public void makeFilterOptions(JPanel imagePanel, Box boxCenterLeft) {
        imagePanelInvalidate = imagePanel::repaint;

        boxCenterLeft.add(UiHelper.makeAsBoostCheckBox(() -> boosting, b -> boosting = b, this::resetImage));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("MorphologyEx"));

        {
            JComboBox<CvMorphTypes> comboBoxMorphOper = new JComboBox<>(CvMorphTypes.values());
            comboBoxMorphOper.setBorder(BorderFactory.createTitledBorder("Morphological operation"));
            comboBoxMorphOper.setSelectedItem(morphologicalOperation);
            comboBoxMorphOper.setAlignmentX(Component.LEFT_ALIGNMENT);
            comboBoxMorphOper.setToolTipText("Type of a morphological operation");
            comboBoxMorphOper.addActionListener(ev -> {
                morphologicalOperation = (CvMorphTypes)comboBoxMorphOper.getSelectedItem();
                debounceResetImage();
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
        boxKernelSize.add(UiHelper.makeSliderVert(kernel1.getModelRows(), "Rows", "Kernel size Width / number of rows"));
        boxKernelSize.add(Box.createHorizontalStrut(2));
        boxKernelSize.add(UiHelper.makeSliderVert(kernel1.getModelCols(), "Cols", "Kernel size Height / number of columns"));
        boxKernelSize.add(Box.createHorizontalGlue());

        Box boxScalar = Box.createHorizontalBox();
        boxScalar.setBorder(BorderFactory.createTitledBorder("Scalar"));
        boxScalar.setToolTipText("An optional value to initialize each matrix element with");

        boxScalar.add(Box.createHorizontalGlue());
        boxScalar.add(UiHelper.makeSliderVert(kernel1.getModelScalarVal0(), "v0", null));
        boxScalar.add(Box.createHorizontalStrut(0));
        boxScalar.add(UiHelper.makeSliderVert(kernel1.getModelScalarVal1(), "v1", null));
        boxScalar.add(Box.createHorizontalStrut(0));
        boxScalar.add(UiHelper.makeSliderVert(kernel1.getModelScalarVal2(), "v2", null));
        boxScalar.add(Box.createHorizontalStrut(0));
        boxScalar.add(UiHelper.makeSliderVert(kernel1.getModelScalarVal3(), "v3", null));
        boxScalar.add(Box.createHorizontalGlue());

        Box box4ArrayType = Box.createHorizontalBox();
        box4ArrayType.setBorder(BorderFactory.createTitledBorder("Array type"));
        JComboBox<CvArrayType> comboArrayType = new JComboBox<>(CvArrayType.values());
        comboArrayType.setSelectedItem(kernel1.getType());
        comboArrayType.addActionListener(ev -> {
            kernel1.setType((CvArrayType)comboArrayType.getSelectedItem());
            debounceResetImage();
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
                debounceResetImage();
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
    }

    private void debounceResetImage() {
        UiHelper.debounceExecutor(() -> timer, t -> timer = t, 300, this::resetImage, logger);
    }

    @Override
    public void printParams() {
        logger.info("morphologicalOperation={}, kernel={}", morphologicalOperation, kernel);
    }

}
