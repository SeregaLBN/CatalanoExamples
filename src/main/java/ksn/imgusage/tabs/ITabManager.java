package ksn.imgusage.tabs;

import java.awt.Component;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import org.slf4j.Logger;

public interface ITabManager {

    ITab<?> getPrevTab(ITab<?> self);
    ITab<?> getNextTab(ITab<?> self);

    void onCancel();
    void onAddNewFilter();
    void onRemoveFilter(ITab<?> tab);
    void onImgPanelDraw(JPanel imagePanel, Graphics2D g, Logger logger);

    void onError(Exception ex, ITab<?> tab, Component from);

}
