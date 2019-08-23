package ksn.imgusage.tabs;

import java.awt.Component;
import java.awt.Graphics2D;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

public interface ITabHandler {

    JFrame getFrame();
    File getCurrentDir();

    ITab<?> getFirstTab();

    void onImageChanged(ITab<?> tab);
    void onCancel();
    void onAddNewFilter();
    void onRemoveFilter(ITab<?> tab);
    void onImagePanelPaint(JPanel imagePanel, Graphics2D g);

    void onSavePipeline();
    void onLoadPipeline();

    void onError(String message, ITab<?> tab, Component from);

}
