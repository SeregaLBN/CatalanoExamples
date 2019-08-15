package ksn.imgusage.tabs;

import java.awt.Component;
import java.awt.image.BufferedImage;

public interface ITab<TTabParams extends ITabParams> {

    void init(ITabHandler tabHandler, ITab<?> source) ;
    Component makeTab(TTabParams params);

    BufferedImage getImage();
    void updateSource(ITab<?> newSource);
    void resetImage();

    String getTabName();
    String getTabFullName();
    TTabParams getParams();

}
