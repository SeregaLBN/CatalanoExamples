package ksn.imgusage.tabs;

import java.awt.Component;
import java.awt.image.BufferedImage;

public interface ITab<TTabParams extends ITabParams> extends AutoCloseable {

    void setManager(ITabManager tabHandler) ;

    Component makeTab(TTabParams params);
    TTabParams getParams();

    /** get filtered image (used as source for next tab) */
    BufferedImage getImage();
    /** get a filtered image, possibly with an additional rendering layer (used to draw in the current tab) */
    BufferedImage getDrawImage();

    /** Mark the current image to be redrawn */
    void invalidate();

    String getTitle();
    String getGroup();
    String getName();
    String getDescription();

    @Override
    void close();

}
