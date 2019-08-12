package ksn.imgusage.tabs.catalano;

import java.awt.event.ItemEvent;

import javax.swing.*;

import Catalano.Imaging.Filters.Rotate;
import Catalano.Imaging.Filters.Rotate.Algorithm;
import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/Rotate.java'>Rotate image</a> */
public class RotateTab extends CatalanoFilterTab {

    public static final String TAB_NAME = Rotate.class.getSimpleName();
    public static final String TAB_DESCRIPTION = "Rotate image";

    private static final double MIN =   0;
    private static final double MAX = 360;

    private boolean keepSize;
    private Rotate.Algorithm algorithm;
    private final SliderDoubleModel modelAngle;

    public RotateTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, 0, true, Algorithm.BICUBIC);
    }

    public RotateTab(ITabHandler tabHandler, ITab source, double angle, boolean keepSize, Rotate.Algorithm algorithm) {
        super(tabHandler, source, false);
        this.modelAngle = new SliderDoubleModel(angle, 0, MIN, MAX);
        this.keepSize = keepSize;
        this.algorithm = algorithm;

        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyCatalanoFilter() {
        if (imageFBmp.isARGB()) // rotate filter form Catalano-Framework don`t supported argb
            imageFBmp.toRGB();
        new Rotate(modelAngle.getValue(), keepSize, algorithm)
            .applyInPlace(imageFBmp);
    }

    @Override
    protected void makeOptions(Box box4Options) {
        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTabName() + " options"));

        Box box2 = Box.createVerticalBox();
        box2.setBorder(BorderFactory.createTitledBorder(""));

        JCheckBox btnKeepSize = new JCheckBox("Keep size", keepSize);
        btnKeepSize.setToolTipText("Keep the original size");
        btnKeepSize.addActionListener(ev -> {
            keepSize  = btnKeepSize.isSelected();
            resetImage();
        });
        box2.add(btnKeepSize);

        Box box4Alg = Box.createVerticalBox();
        box4Alg.setBorder(BorderFactory.createTitledBorder("Algorithm"));
        box4Alg.setToolTipText("Interpolation algorithm");
        ButtonGroup radioGroup = new ButtonGroup();
        for (Rotate.Algorithm alg : Rotate.Algorithm.values()) {
            JRadioButton radioBtnAlg = new JRadioButton(alg.name(), alg == this.algorithm);
            radioBtnAlg.setToolTipText("Interpolation algorithm");
            radioBtnAlg.addItemListener(ev -> {
                if (ev.getStateChange() == ItemEvent.SELECTED) {
                    this.algorithm = alg;
                    logger.trace("algorithm changed to {}", alg);
                    resetImage();
                }
            });
            box4Alg.add(radioBtnAlg);
            radioGroup.add(radioBtnAlg);
        }
        box2.add(box4Alg);

        boxOptions.add(Box.createHorizontalGlue());
        boxOptions.add(makeSliderVert(modelAngle, "Angle", "Angle"));
        boxOptions.add(Box.createHorizontalStrut(8));
        boxOptions.add(box2);
        boxOptions.add(Box.createHorizontalGlue());

        box4Options.add(boxOptions);

        modelAngle.getWrapped().addChangeListener(ev -> {
            logger.trace("modelAngle: value={}", modelAngle.getFormatedText());
            resetImage();
        });
    }

    @Override
    public void printParams() {
        logger.info("angle={}, keepSize={}, rotateAlgorithm={}",
            modelAngle.getFormatedText(),
            keepSize,
            algorithm);
    }

}
