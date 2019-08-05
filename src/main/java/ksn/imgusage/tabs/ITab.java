
package ksn.imgusage.tabs;

import ksn.imgusage.utils.ImgWrapper;

public interface ITab {

    ImgWrapper getImage();
    void updateSource(ITab newSource);
    void resetImage();

}
