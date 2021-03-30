package ksn.imgusage.model;

import java.text.ParseException;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SliderIntModel implements ISliderModel<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(SliderIntModel.class);
    private final DefaultBoundedRangeModel model;

    public SliderIntModel(int value, int extent, int min, int max) {
        value = Math.max(min, value);
        value = Math.min(max, value);
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
    public Integer getMinimum() {
        return model.getMinimum();
    }

    @Override
    public void setMinimum(Integer min) {
        model.setMinimum(min);
    }

    @Override
    public Integer getMaximum() {
        return model.getMaximum();
    }

    @Override
    public void setMaximum(Integer max) {
        model.setMaximum(max);
    }

    private String formatValue(int value) {
        return Integer.toString(value);
    }

    private int parseValue(String value) throws ParseException {
        return Integer.parseInt(value);
    }

    @Override
    public String getFormatedText() {
        return formatValue(getValue());
    }

    @Override
    public void setFormatedText(String value) {
        try {
            setValue(parseValue(value));
        } catch (Exception ex) {
            logger.error("{}::setFormatedText: {}", getClass().getSimpleName(), ex.getMessage());
        }
    }

    @Override
    public String reformat(String value) {
        try {
            return formatValue(parseValue(value));
        } catch (Exception ex) {
            logger.error("{}::formatText: {}", getClass().getSimpleName(), ex.getMessage());
            return null;
        }
    }

    @Override
    public BoundedRangeModel getWrapped() {
        return model;
    }

}
