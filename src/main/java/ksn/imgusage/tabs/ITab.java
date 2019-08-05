
package ksn.imgusage.tabs;

import java.awt.image.BufferedImage;

public interface ITab {

    BufferedImage getImage();
    void updateSource(ITab newSource);
    void resetImage();

}
