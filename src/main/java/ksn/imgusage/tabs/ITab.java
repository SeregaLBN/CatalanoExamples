
package ksn.imgusage.tabs;

import Catalano.Imaging.FastBitmap;

public interface ITab {

    FastBitmap getImage();
    void updateSource(ITab newSource);
    void resetImage();

}
