
package ksn.catalano.examples.filter.tabs;

import java.awt.Graphics2D;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public interface ITabHandler {

    JTabbedPane getTabPanel();

    void onSourceChanged();
    void onCancel();
    void onAddNewFilter();
    void onRemoveFilter(ITab tab);
    void onImagePanelPaint(JPanel imagePanel, Graphics2D g);

}
