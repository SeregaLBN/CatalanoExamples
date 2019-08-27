package ksn.imgusage.tabs.catalano;

import java.awt.Component;
import java.awt.Container;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.Rotate;
import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.type.dto.catalano.RotateTabParams;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/Rotate.java'>Rotate image</a> */
public class RotateTab extends CatalanoFilterTab<RotateTabParams> {

    public static final String TAB_TITLE = Rotate.class.getSimpleName();
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Rotate image";

    private static final double MIN =   0;
    private static final double MAX = 360;

    private RotateTabParams params;

    public RotateTab() {
        super(false);
    }

    @Override
    public Component makeTab(RotateTabParams params) {
        if (params == null)
            params = new RotateTabParams();
        this.params = params;

        return makeTab();
    }

    @Override
    public String getTitle() { return TAB_TITLE; }
    @Override
    public String getName() { return TAB_NAME; }
    @Override
    public String getDescription() { return TAB_DESCRIPTION; }

    @Override
    protected void applyCatalanoFilter() {
        if (imageFBmp.isARGB()) // rotate filter form Catalano-Framework don`t supported argb
            imageFBmp.toRGB();
        new Rotate(params.angle, params.keepSize, params.algorithm)
            .applyInPlace(imageFBmp);
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderDoubleModel modelAngle = new SliderDoubleModel(params.angle, 0, MIN, MAX);
        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));

        Container box2 = makeBoxedCheckBox(
            () -> params.keepSize,
            v  -> params.keepSize = v,
            "",
            "Keep size",
            "params.keepSize",
            "Keep the original size", null);

        Component box4Alg = makeBoxedRadioButtons(
            Stream.of(Rotate.Algorithm.values()),
            () -> params.algorithm,
            v  -> params.algorithm = v,
            "Algorithm",
            "params.algorithm",
            "Interpolation algorithm",
            null,
            v -> "Interpolation algorithm",
            null);
        box2.add(box4Alg);

        boxOptions.add(Box.createHorizontalGlue());
        boxOptions.add(makeSliderVert(modelAngle, "Angle", "Angle"));
        boxOptions.add(Box.createHorizontalStrut(8));
        boxOptions.add(box2);
        boxOptions.add(Box.createHorizontalGlue());

        box4Options.add(boxOptions);

        addChangeListener("modelAngle", modelAngle, v -> params.angle = v);

        return box4Options;
    }

    @Override
    public RotateTabParams getParams() {
        return params;
    }

}
