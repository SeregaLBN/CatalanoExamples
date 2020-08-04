package ksn.imgusage.type.dto.opencv;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.CascadeClassifierTab;
import ksn.imgusage.tabs.opencv.CascadeClassifierTab.EHaarcascade;

/** Init parameters for {@link CascadeClassifierTab} */
public class CascadeClassifierTabParams implements ITabParams {

    public EHaarcascade first  = EHaarcascade.frontalface_alt;
    public EHaarcascade second = EHaarcascade.eye_tree_eyeglasses;

    @Override
    public String toString() {
        return "{ first=" + first + ", second=" + second + " }";
    }

}
