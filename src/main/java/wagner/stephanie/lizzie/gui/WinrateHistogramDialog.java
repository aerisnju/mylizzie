/*
 * Created by JFormDesigner on Sat Mar 24 18:06:19 CST 2018
 */

package wagner.stephanie.lizzie.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import wagner.stephanie.lizzie.Lizzie;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.util.ResourceBundle;

/**
 * @author Cao Hu
 */
public class WinrateHistogramDialog extends JDialog {
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panelWinrateHistogram;
    private JScrollPane scrollPaneWinrateHistory;
    private JTable tableWinrateHistory;
    private JPanel panelWinrateHistogramOption;
    private JCheckBox checkBoxShowOnlyHighOscillation;
    private JCheckBox checkBoxHistogramShowBlack;
    private JCheckBox checkBoxHistogramShowWhite;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private ChartPanel histogramChartPanel = null;
    private XYSeriesCollection dataSet = null;

    public WinrateHistogramDialog(Frame owner) {
        super(owner);
        initComponents();
    }

    public WinrateHistogramDialog(Dialog owner) {
        super(owner);
        initComponents();
    }

    public JTable getTableWinrateHistory() {
        return tableWinrateHistory;
    }

    private void initCustomComponents() {
        WinrateHistogramTableModel winrateHistogramTableModel = new WinrateHistogramTableModel();
        tableWinrateHistory.setModel(winrateHistogramTableModel);

        XYSeries blackSeries = new XYSeries("Black");
        XYSeries whiteSeries = new XYSeries("White");
        XYSeries standardSeries = new XYSeries("50%");
        for (int i = 0; i <= 50; ++i) {
            standardSeries.add(i, 50);
        }

        dataSet = new XYSeriesCollection();
        dataSet.addSeries(blackSeries);
        dataSet.addSeries(whiteSeries);
        dataSet.addSeries(standardSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "", // chart title
                "", // x axis label
                "Win%", // y axis label
                dataSet, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );

        chart.setBackgroundPaint(Color.WHITE);

        // get a reference to the plot for further customisation...
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0.0, 100.0);

        histogramChartPanel = new ChartPanel(chart);
        panelWinrateHistogram.add(histogramChartPanel);

        if (Lizzie.optionSetting.getWinrateHistogramWindowWidth() >= 10 && Lizzie.optionSetting.getWinrateHistogramWindowHeight() >= 10) {
            setSize(Lizzie.optionSetting.getWinrateHistogramWindowWidth(), Lizzie.optionSetting.getWinrateHistogramWindowHeight());
        }
        histogramChartPanel.setPreferredSize(new Dimension(getWidth(), (int) (getHeight() * 0.3)));

        winrateHistogramTableModel.setRefreshObserver(model -> {
            blackSeries.clear();
            whiteSeries.clear();
            standardSeries.clear();

            for (int i = 0; i < model.getHistogramEntryList().size(); ++i) {
                WinrateHistogramEntry entry = model.getHistogramEntryList().get(i);

                standardSeries.add(entry.getMoveNumber(), 50);
                if (checkBoxHistogramShowBlack.isSelected()) {
                    blackSeries.add(entry.getMoveNumber(), entry.getBlackWinrate());
                }
                if (checkBoxHistogramShowWhite.isSelected()) {
                    whiteSeries.add(entry.getMoveNumber(), entry.getWhiteWinrate());
                }
            }
            if (model.getHistogramEntryList().size() < 50) {
                for (int i = model.getHistogramEntryList().size() - 1; i <= 50; ++i) {
                    standardSeries.add(i, 50);
                }
            }

            histogramChartPanel.repaint();
        });
    }

    private void checkBoxShowOnlyHighOscillationItemStateChanged(ItemEvent e) {
        JCheckBox jcheckBoxShowOnlyHighOscillation = (JCheckBox) e.getItem();
        WinrateHistogramTableModel tableModel = (WinrateHistogramTableModel) getTableWinrateHistory().getModel();

        tableModel.setProvideFilteredData(jcheckBoxShowOnlyHighOscillation.isSelected());
    }

    private void thisComponentResized(ComponentEvent e) {
        if (histogramChartPanel != null) {
            Dimension preferredSize = new Dimension(getWidth(), (int) (getHeight() * 0.3));
            histogramChartPanel.setPreferredSize(preferredSize);
        }
    }

    private void thisComponentHidden(ComponentEvent e) {
        Lizzie.optionSetting.setWinrateHistogramWindowShow(false);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("wagner.stephanie.lizzie.i18n.GuiBundle");
        panelWinrateHistogram = new JPanel();
        scrollPaneWinrateHistory = new JScrollPane();
        tableWinrateHistory = new JTable();
        panelWinrateHistogramOption = new JPanel();
        checkBoxShowOnlyHighOscillation = new JCheckBox();
        checkBoxHistogramShowBlack = new JCheckBox();
        checkBoxHistogramShowWhite = new JCheckBox();

        //======== this ========
        setTitle(bundle.getString("WinrateHistogramDialog.this.title"));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                thisComponentHidden(e);
            }

            @Override
            public void componentResized(ComponentEvent e) {
                thisComponentResized(e);
            }
        });
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== panelWinrateHistogram ========
        {
            panelWinrateHistogram.setLayout(new FlowLayout());
        }
        contentPane.add(panelWinrateHistogram, BorderLayout.NORTH);

        //======== scrollPaneWinrateHistory ========
        {

            //---- tableWinrateHistory ----
            tableWinrateHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tableWinrateHistory.setFillsViewportHeight(true);
            scrollPaneWinrateHistory.setViewportView(tableWinrateHistory);
        }
        contentPane.add(scrollPaneWinrateHistory, BorderLayout.CENTER);

        //======== panelWinrateHistogramOption ========
        {
            panelWinrateHistogramOption.setLayout(new FlowLayout(FlowLayout.LEADING));

            //---- checkBoxShowOnlyHighOscillation ----
            checkBoxShowOnlyHighOscillation.setText(bundle.getString("WinrateHistogramDialog.checkBoxShowOnlyHighOscillation.text"));
            checkBoxShowOnlyHighOscillation.setSelected(true);
            checkBoxShowOnlyHighOscillation.addItemListener(e -> checkBoxShowOnlyHighOscillationItemStateChanged(e));
            panelWinrateHistogramOption.add(checkBoxShowOnlyHighOscillation);

            //---- checkBoxHistogramShowBlack ----
            checkBoxHistogramShowBlack.setText(bundle.getString("WinrateHistogramDialog.checkBoxHistogramShowBlack.text"));
            checkBoxHistogramShowBlack.setSelected(true);
            panelWinrateHistogramOption.add(checkBoxHistogramShowBlack);

            //---- checkBoxHistogramShowWhite ----
            checkBoxHistogramShowWhite.setText(bundle.getString("WinrateHistogramDialog.checkBoxHistogramShowWhite.text"));
            checkBoxHistogramShowWhite.setSelected(true);
            panelWinrateHistogramOption.add(checkBoxHistogramShowWhite);
        }
        contentPane.add(panelWinrateHistogramOption, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents

        initCustomComponents();
    }
}
