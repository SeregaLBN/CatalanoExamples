package ksn.imgusage.tabs;

import java.awt.Graphics2D;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public interface ITabHandler {

    JTabbedPane getTabPanel();

    void onImageChanged(ITab tab);
    void onCancel();
    void onAddNewFilter();
    void onRemoveFilter(ITab tab);
    void onImagePanelPaint(JPanel imagePanel, Graphics2D g);

}