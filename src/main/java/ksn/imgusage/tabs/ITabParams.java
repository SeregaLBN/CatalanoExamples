
package ksn.imgusage.tabs;

import java.util.Locale;

public interface ITabParams {

    public static String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

}
