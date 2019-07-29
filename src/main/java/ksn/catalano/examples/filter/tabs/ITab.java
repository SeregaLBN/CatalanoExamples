
package ksn.catalano.examples.filter.tabs;

import Catalano.Imaging.FastBitmap;

public interface ITab {

    FastBitmap getImage();
    void updateSource(ITab newSource);
    void resetImage();

}
