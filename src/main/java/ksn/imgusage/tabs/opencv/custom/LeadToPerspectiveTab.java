package ksn.imgusage.tabs.opencv.custom;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.opencv.core.Mat;

import ksn.imgusage.type.dto.opencv.custom.LeadToPerspectiveTabParams;

/** Find the optimal perspective binded to an existing outer rectangle */
public class LeadToPerspectiveTab extends CustomTab<LeadToPerspectiveTabParams> {

    public static final String TAB_TITLE = "LeadToPerspective";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Find the optimal perspective binded to an existing outer rectangle";

    private LeadToPerspectiveTabParams params;
    private Mat matStarted;

    @Override
    public Component makeTab(LeadToPerspectiveTabParams params) {
        if (params == null)
            params = new LeadToPerspectiveTabParams();
        this.params = params;

        return makeTab();
    }

    @Override
    public String getTitle() { return TAB_TITLE; }
    @Override
    public String getName() { return TAB_NAME; }
    @Override
    public String getDescription() { return TAB_DESCRIPTION; }

    @Override
    protected void applyOpencvFilter() {
        matStarted = imageMat;

        SwingUtilities.invokeLater(this::nextIteration);
    }

    private void nextIteration() {
    }


    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        JButton btnRepeat = new JButton("Repeat...");
        btnRepeat.addActionListener(ev -> resetImage());
        box4Options.add(btnRepeat);

        return box4Options;
    }

    @Override
    public LeadToPerspectiveTabParams getParams() {
        return params;
    }

}
