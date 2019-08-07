package ksn.imgusage.tabs.opencv.type;

/** @see <a href='https://docs.opencv.org/3.4.2/d2/de8/group__core__array.html#ga209f2f4869e304c82d07739337eae7c5'>Various border types, image boundaries are denoted with `|`</a> */
public enum CvBorderTypes {

    /** `iiiiii|abcdefgh|iiiiiii`  with some specified `i` */
    BORDER_CONSTANT   (0),

    /** `aaaaaa|abcdefgh|hhhhhhh` */
    BORDER_REPLICATE  (1),

    /** `fedcba|abcdefgh|hgfedcb` */
    BORDER_REFLECT    (2),

    /** `cdefgh|abcdefgh|abcdefg` */
    BORDER_WRAP       (3),

    /** `gfedcb|abcdefgh|gfedcba` */
    BORDER_REFLECT_101(4),

    /** `uvwxyz|abcdefgh|ijklmno` */
    BORDER_TRANSPARENT(5),

  ///** same as BORDER_REFLECT_101 */
  //BORDER_REFLECT101 (BORDER_REFLECT_101), //!<

    /** same as {@link #BORDER_REFLECT_101} */
    BORDER_DEFAULT    (BORDER_REFLECT_101.val),

    /** do not look outside of ROI (Region Of Interest) */
    BORDER_ISOLATED   (16);


    private final int val;
    private CvBorderTypes(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

}
