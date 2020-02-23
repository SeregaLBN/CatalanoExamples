package ksn.imgusage.tabs;

import java.awt.Component;
import java.awt.image.BufferedImage;

public interface ITab<TTabParams extends ITabParams> {

    void setHandler(ITabHandler tabHandler) ;
    void setSource(ITab<?> source) ;

    Component makeTab(TTabParams params);
    TTabParams getParams();

    /** get filtered image (used as source for net tab) */
    BufferedImage getImage();
    /** get a filtered image, possibly with an additional rendering layer (used to draw in the current tab) */
    BufferedImage getDrawImage();

    /** Mark the current image to be redrawn (the previous {@link ITab#getImage()} has changed} */
    void invalidate();

    String getTitle();
    String getGroup();
    String getName();
    String getDescription();

}
