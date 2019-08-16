package ksn.imgusage.tabs;

import java.awt.Component;
import java.awt.image.BufferedImage;

public interface ITab<TTabParams extends ITabParams> {

    void init(ITabHandler tabHandler, ITab<?> source) ;
    Component makeTab(TTabParams params);
    TTabParams getParams();

    BufferedImage getImage();
    void resetImage();
    void updateSource(ITab<?> newSource);

    String getTitle();
    String getName();

}
