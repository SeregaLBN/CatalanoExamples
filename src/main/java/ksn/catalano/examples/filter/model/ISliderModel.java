package ksn.catalano.examples.filter.model;

import javax.swing.BoundedRangeModel;

public interface ISliderModel<T extends Number> {

    T getValue();
    void setValue(T value);

    String getFormatedText();

    BoundedRangeModel getWrapped();

}
