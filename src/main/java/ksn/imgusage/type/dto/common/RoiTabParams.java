package ksn.imgusage.type.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.commons.RoiTab;
import ksn.imgusage.type.Rect;
import ksn.imgusage.type.Size;

/** Init parameters for {@link RoiTab} */
public class RoiTabParams implements ITabParams {

    /** Ratio to source image */
    public Size ratio;

    /** rectangle - Region Of Interest */
    public Rect roi;

    public RoiTabParams() {}

    public RoiTabParams(Size ratio, Rect roi) {
        this.ratio = ratio;
        this.roi = roi;
    }

    @Override
    public String toString() {
        return "{ ratio=" + ratio + ", roi=" + roi + " }";
    }





    @Deprecated
    private static final int BAD_SIZE = -1;

    @Deprecated
    @JsonProperty("padding")
    public void setPadding(Padding pad) {
        roi = new Rect(pad.left, pad.right, BAD_SIZE, BAD_SIZE);
    }

    @Deprecated
    public static class Padding {

        public int left;
        public int top;
        public int right;
        public int bottom;

        public Padding() {}

        public Padding(int left, int top, int right, int bottom) {
            this.left   = left;
            this.top    = top;
            this.right  = right;
            this.bottom = bottom;
        }

        @Override
        public String toString() {
            return "{ left=" + left + ", right=" + right + ", top=" + top + ", bottom=" + bottom + " }";
        }

    }

}
