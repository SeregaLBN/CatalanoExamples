package ksn.catalano.examples.filter.model;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

public class SliderIntModel implements ISliderModel<Integer> {

    private final DefaultBoundedRangeModel model;

    public SliderIntModel(int value, int extent, int min, int max) {
        model = new DefaultBoundedRangeModel(value, extent, min, max);
    }

    @Override
    public Integer getValue() {
        return model.getValue();
    }
    @Override
    public void setValue(Integer value) {
        model.setValue(value);
    }

    @Override
    public String getFormatedText() {
        return Integer.toString(getValue());
    }

    @Override
    public BoundedRangeModel getWrapped() {
        return model;
    }

}
