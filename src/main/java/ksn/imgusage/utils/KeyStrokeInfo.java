package ksn.imgusage.utils;

/** @see {@link javax.swing.KeyStroke} */
public class KeyStrokeInfo {

    /** UI name key combo hint */
    public final String toolTip;

    /** @see param keyCode in {@link javax.swing.KeyStroke#getKeyStroke(int, int)} */
    public final int keyCode;

    /** @see param modifiers in {@link javax.swing.KeyStroke#getKeyStroke(int, int)} */
    public final int modifiers;

    public KeyStrokeInfo(String toolTip, int keyCode, int modifiers) {
        this.toolTip = toolTip;
        this.keyCode = keyCode;
        this.modifiers = modifiers;
    }

}
