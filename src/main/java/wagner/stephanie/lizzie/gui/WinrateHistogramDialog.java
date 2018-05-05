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
import java.util.function.Consumer;

/**
 * @author Cao Hu
 */
public class WinrateHistogramDialog extends JDialog {
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panelWinrateHistogramOption;
    private JCheckBox checkBoxShowOnlyHighOscillation;
    private JCheckBox checkBoxHistogramShowBlack;
    private JCheckBox checkBoxHistogramShowWhite;
    private JSplitPane splitPaneHistogram;
    private JPanel panelWinrateHistogram;
    private JScrollPane scrollPaneWinrateHistory;
    private JTable tableWinrateHistory;
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

        winrateHistogramTableModel.setRefreshObserver(new Consumer<WinrateHistogramTableModel>() {
            private long lastRefreshTime = System.currentTimeMillis();

            @Override
            public void accept(WinrateHistogramTableModel model) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastRefreshTime < 250L) {
                    return;
                }

                lastRefreshTime = currentTime;

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

            }
        });

        if (Lizzie.optionSetting.getWinrateHistogramWindowWidth() >= 10 && Lizzie.optionSetting.getWinrateHistogramWindowHeight() >= 10) {
            setPreferredSize(new Dimension(Lizzie.optionSetting.getWinrateHistogramWindowWidth(), Lizzie.optionSetting.getWinrateHistogramWindowHeight()));
        }
        splitPaneHistogram.setDividerLocation(0.3);
        pack();

        histogramChartPanel.setPreferredSize(new Dimension(panelWinrateHistogram.getWidth(), panelWinrateHistogram.getHeight()));
        histogramChartPanel.setSize(panelWinrateHistogram.getWidth(), panelWinrateHistogram.getHeight());
        pack();
    }

    private void checkBoxShowOnlyHighOscillationItemStateChanged(ItemEvent e) {
        JCheckBox jcheckBoxShowOnlyHighOscillation = (JCheckBox) e.getItem();
        WinrateHistogramTableModel tableModel = (WinrateHistogramTableModel) getTableWinrateHistory().getModel();

        tableModel.setProvideFilteredData(jcheckBoxShowOnlyHighOscillation.isSelected());
    }

    private void thisComponentHidden(ComponentEvent e) {
        Lizzie.optionSetting.setWinrateHistogramWindowShow(false);
    }

    private void panelWinrateHistogramComponentResized(ComponentEvent e) {
        if (histogramChartPanel != null) {
            Dimension preferredSize = new Dimension(e.getComponent().getWidth(), e.getComponent().getHeight());
            histogramChartPanel.setPreferredSize(preferredSize);
            histogramChartPanel.setSize(e.getComponent().getWidth(), e.getComponent().getHeight());
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("wagner.stephanie.lizzie.i18n.GuiBundle");
        panelWinrateHistogramOption = new JPanel();
        checkBoxShowOnlyHighOscillation = new JCheckBox();
        checkBoxHistogramShowBlack = new JCheckBox();
        checkBoxHistogramShowWhite = new JCheckBox();
        splitPaneHistogram = new JSplitPane();
        panelWinrateHistogram = new JPanel();
        scrollPaneWinrateHistory = new JScrollPane();
        tableWinrateHistory = new JTable();

        //======== this ========
        setTitle(bundle.getString("WinrateHistogramDialog.this.title"));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                thisComponentHidden(e);
            }
        });
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

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

        //======== splitPaneHistogram ========
        {
            splitPaneHistogram.setOrientation(JSplitPane.VERTICAL_SPLIT);
            splitPaneHistogram.setDividerLocation(10);

            //======== panelWinrateHistogram ========
            {
                panelWinrateHistogram.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        panelWinrateHistogramComponentResized(e);
                    }
                });
                panelWinrateHistogram.setLayout(new FlowLayout());
            }
            splitPaneHistogram.setTopComponent(panelWinrateHistogram);

            //======== scrollPaneWinrateHistory ========
            {

                //---- tableWinrateHistory ----
                tableWinrateHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                tableWinrateHistory.setFillsViewportHeight(true);
                scrollPaneWinrateHistory.setViewportView(tableWinrateHistory);
            }
            splitPaneHistogram.setBottomComponent(scrollPaneWinrateHistory);
        }
        contentPane.add(splitPaneHistogram, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents

        initCustomComponents();
    }
}
