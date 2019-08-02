package ksn.imgusage.model;

import javax.swing.BoundedRangeModel;

public interface ISliderModel<T extends Number> {

    T getValue();
    void setValue(T value);

    String getFormatedText();
    void setFormatedText(String value);

    BoundedRangeModel getWrapped();

}
