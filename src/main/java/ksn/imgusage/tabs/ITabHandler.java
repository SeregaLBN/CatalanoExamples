package ksn.imgusage.tabs;

import java.awt.Component;
import java.awt.Graphics2D;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.slf4j.Logger;

public interface ITabHandler {

    JFrame getFrame();
    File getCurrentDir();

    ITab<?> getFirstTab();

    void onImageChanged(ITab<?> tab);
    void onCancel();
    void onAddNewFilter();
    void onRemoveFilter(ITab<?> tab);
    void onImgPanelDraw(JPanel imagePanel, Graphics2D g, Logger logger);

    void onSavePipeline();
    void onLoadPipeline();

    void onError(String message, ITab<?> tab, Component from);

}
