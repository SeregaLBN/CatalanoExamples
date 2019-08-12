package ksn.imgusage.model;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SliderIntModel implements ISliderModel<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(SliderIntModel.class);
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
    public Integer getMaximum() {
        return model.getMaximum();
    }
    @Override
    public void setMaximum(Integer max) {
        model.setMaximum(max);
    }

    @Override
    public String getFormatedText() {
        return Integer.toString(getValue());
    }
    @Override
    public void setFormatedText(String value) {
        try {
            setValue(Integer.parseInt(value));
        } catch (Exception ex) {
            logger.error("{}::setFormatedText: {}", getClass().getSimpleName(), ex.getMessage());
        }
    }

    @Override
    public BoundedRangeModel getWrapped() {
        return model;
    }

}
