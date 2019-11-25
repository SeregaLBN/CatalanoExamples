package ksn.imgusage.model;

import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SliderDoubleModel implements ISliderModel<Double> {

    private static final Logger logger = LoggerFactory.getLogger(SliderDoubleModel.class);
    private final DefaultBoundedRangeModel model;
    private final int decimalPrecision;
    private final double coefficient;

    public SliderDoubleModel(double value, double extent, double min, double max) {
        this(value, extent, min, max, 2);
    }
    public SliderDoubleModel(double value, double extent, double min, double max, int decimalPrecision) {
        this.decimalPrecision = decimalPrecision;
        coefficient = 1/Math.pow(10, decimalPrecision); // 1: 0.1;  2: 0.01;  3: 0.001;  4: 0.0001
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
    public Double getMinimum() {
        return model.getMinimum() * coefficient;
    }

    @Override
    public void setMinimum(Double min) {
        model.setMinimum((int)(min / coefficient));
    }

    @Override
    public Double getMaximum() {
        return model.getMaximum() * coefficient;
    }
    @Override
    public void setMaximum(Double max) {
        model.setMaximum((int)(max / coefficient));
    }

    @Override
    public String getFormatedText() {
        String format = "%." + decimalPrecision + "f";
        return String.format(Locale.US, format, getValue());
    }

    @Override
    public void setFormatedText(String value) {
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.US);
            Number number = format.parse(value);
            setValue(number.doubleValue());
        } catch (Exception ex) {
            logger.error("{}::setFormatedText: {}", getClass().getSimpleName(), ex.getMessage());
        }
    }

    @Override
    public BoundedRangeModel getWrapped() {
        return model;
    }

}
