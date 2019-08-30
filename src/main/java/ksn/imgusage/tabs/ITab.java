package ksn.imgusage.tabs;

import java.awt.Component;
import java.awt.image.BufferedImage;

public interface ITab<TTabParams extends ITabParams> {

    void setHandler(ITabHandler tabHandler) ;
    void setSource(ITab<?> source) ;

    Component makeTab(TTabParams params);
    TTabParams getParams();

    /** get filtered image */
    BufferedImage getImage();
    /** get a filtered image, possibly with an additional rendering layer */
    BufferedImage getDrawImage();
    void resetImage(boolean debounce);
    void resetImage();

    String getTitle();
    String getName();
    String getDescription();

}
