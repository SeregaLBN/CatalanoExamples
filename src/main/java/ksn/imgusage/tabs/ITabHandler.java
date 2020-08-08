package ksn.imgusage.tabs;

import java.awt.Component;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import org.slf4j.Logger;

public interface ITabHandler {

    /** User changed image - redraw all next tabs */
    void onImageChanged(ITab<?> tab);
    void onCancel();
    void onAddNewFilter();
    void onRemoveFilter(ITab<?> tab);
    void onImgPanelDraw(JPanel imagePanel, Graphics2D g, Logger logger);

    void onError(Exception ex, ITab<?> tab, Component from);

}
