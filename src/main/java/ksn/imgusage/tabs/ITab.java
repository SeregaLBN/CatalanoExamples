
package ksn.imgusage.tabs;

import java.awt.image.BufferedImage;

public interface ITab<TTabParams extends ITabParams> {

    BufferedImage getImage();
    void updateSource(ITab<?> newSource);
    void resetImage();

    TTabParams getParams();

}
