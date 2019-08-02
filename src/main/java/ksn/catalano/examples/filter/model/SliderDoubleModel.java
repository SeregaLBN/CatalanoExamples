package ksn.catalano.examples.filter.model;

import java.util.Locale;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

public class SliderDoubleModel implements ISliderModel<Double> {

    private final DefaultBoundedRangeModel model;
    private final double coefficient;

    public SliderDoubleModel(double value, double extent, double min, double max) {
        coefficient = 0.01;
        model = new DefaultBoundedRangeModel(
                (int)(value  / coefficient),
                (int)(extent / coefficient),
                (int)(min    / coefficient),
                (int)(max    / coefficient));
    }

    @Override
    public Double getValue() {
        return model.getValue() * coefficient;
    }
    @Override
    public void setValue(Double value) {
        model.setValue((int)(value / coefficient));
    }

    @Override
    public String getFormatedText() {
        return String.format(Locale.US, "%.2f", getValue() * coefficient);
    }

    @Override
    public BoundedRangeModel getWrapped() {
        return model;
    }

}
