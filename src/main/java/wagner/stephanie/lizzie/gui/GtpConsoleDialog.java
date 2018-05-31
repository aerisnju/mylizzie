/*
 * Created by JFormDesigner on Sun May 27 00:05:33 CST 2018
 */

package wagner.stephanie.lizzie.gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.Sets;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.analysis.GtpClient;
import wagner.stephanie.lizzie.util.TextLineManager;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class GtpConsoleDialog extends JDialog {
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JTextField textFieldGtpCommandInput;
    private JScrollPane scrollPaneGtpConsole;
    private JTextPane textPaneGtpConsole;
    private JPanel panelInfoUtil;
    private JToolBar toolBarConsole;
    private JButton buttonClear;
    private JLabel labelInfo;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private static final ImmutableSet<String> ANALYZE_COMMAND = Sets.immutable.of("lz-analyze", "lz-analyze_genmove");
    private static final ImmutableSet<String> CLASSIC_ANALYZE_COMMAND = Sets.immutable.of("time_left");

    private final TextLineManager textLineManager;
    private final SimpleAttributeSet grayText;
    private final Consumer<String> commandConsumer, stdoutConsumer, stderrConsumer;
    private final Consumer<Integer> exitObserver;
    private GtpClient gtpClient;
    private volatile boolean enableStdoutDisplay;
    private volatile boolean enableStderrDisplay;

    public GtpConsoleDialog(Window owner) {
        super(owner);
        initComponents();

        textLineManager = new TextLineManager(textPaneGtpConsole.getStyledDocument(), 10000);
        grayText = new SimpleAttributeSet();
        StyleConstants.setForeground(grayText, Color.GRAY);
        commandConsumer = command -> {
            SwingUtilities.invokeLater(() -> textLineManager.appendBoldLine("GTP> " + command));

            enableStdoutDisplay = !isAnalysisCommand(command);
            enableStderrDisplay = !isClassicAnalysisCommand(command);
        };
        stdoutConsumer = line -> {
            if (enableStdoutDisplay) {
                SwingUtilities.invokeLater(() -> textLineManager.appendNormalLine(line));
            }
        };
        stderrConsumer = line -> {
            if (enableStderrDisplay) {
                SwingUtilities.invokeLater(() -> textLineManager.appendLine(line, grayText));
            }
        };
        exitObserver = exitCode -> unlinkGtpClient();
        gtpClient = null;
        enableStdoutDisplay = true;
        enableStderrDisplay = true;

        getRootPane().registerKeyboardAction(e -> setVisible(!isVisible()),
                KeyStroke.getKeyStroke(KeyEvent.VK_E, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private static boolean isAnalysisCommand(final String command) {
        return ANALYZE_COMMAND.anySatisfy(each -> StringUtils.containsIgnoreCase(command, each));
    }

    private static boolean isClassicAnalysisCommand(final String command) {
        return CLASSIC_ANALYZE_COMMAND.anySatisfy(each -> StringUtils.containsIgnoreCase(command, each));
    }

    public void unlinkGtpClient() {
        if (gtpClient != null) {
            gtpClient.unregisterGtpCommandObserver(commandConsumer);
            gtpClient.unregisterStdoutLineConsumer(stdoutConsumer);
            gtpClient.unregisterStderrLineConsumer(stderrConsumer);
            gtpClient.unregisterEngineExitObserver(exitObserver);

            gtpClient = null;
        }
    }

    public void linkToGtpClient(GtpClient gtpClient) {
        unlinkGtpClient();

        this.gtpClient = gtpClient;

        this.gtpClient.registerGtpCommandObserver(commandConsumer);
        this.gtpClient.registerStdoutLineConsumer(stdoutConsumer);
        this.gtpClient.registerStderrLineConsumer(stderrConsumer);
        this.gtpClient.registerEngineExitObserver(exitObserver);
    }

    private void textFieldGtpCommandInputActionPerformed(ActionEvent e) {
        String command = textFieldGtpCommandInput.getText();
        textFieldGtpCommandInput.setText("");

        if (gtpClient != null) {
            gtpClient.postCommand(command);
        }
    }

    private void thisComponentHidden(ComponentEvent e) {
        Lizzie.optionSetting.setGtpConsoleWindowShow(false);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("wagner.stephanie.lizzie.i18n.GuiBundle");
        textFieldGtpCommandInput = new JTextField();
        scrollPaneGtpConsole = new JScrollPane();
        textPaneGtpConsole = new JTextPane();
        panelInfoUtil = new JPanel();
        toolBarConsole = new JToolBar();
        buttonClear = new JButton();
        labelInfo = new JLabel();

        //======== this ========
        setTitle(bundle.getString("GtpConsoleDialog.this.title"));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                thisComponentHidden(e);
            }
        });
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //---- textFieldGtpCommandInput ----
        textFieldGtpCommandInput.setFont(textFieldGtpCommandInput.getFont().deriveFont(textFieldGtpCommandInput.getFont().getSize() + 2f));
        textFieldGtpCommandInput.addActionListener(e -> textFieldGtpCommandInputActionPerformed(e));
        contentPane.add(textFieldGtpCommandInput, BorderLayout.SOUTH);

        //======== scrollPaneGtpConsole ========
        {

            //---- textPaneGtpConsole ----
            textPaneGtpConsole.setEditable(false);
            textPaneGtpConsole.setBackground(Color.white);
            textPaneGtpConsole.setFont(textPaneGtpConsole.getFont().deriveFont(textPaneGtpConsole.getFont().getSize() + 2f));
            scrollPaneGtpConsole.setViewportView(textPaneGtpConsole);
        }
        contentPane.add(scrollPaneGtpConsole, BorderLayout.CENTER);

        //======== panelInfoUtil ========
        {
            panelInfoUtil.setLayout(new TableLayout(new double[][] {
                {TableLayout.FILL},
                {TableLayout.PREFERRED, TableLayout.PREFERRED}}));
            ((TableLayout)panelInfoUtil.getLayout()).setHGap(2);
            ((TableLayout)panelInfoUtil.getLayout()).setVGap(2);

            //======== toolBarConsole ========
            {
                toolBarConsole.setFloatable(false);

                //---- buttonClear ----
                buttonClear.setIcon(new ImageIcon(getClass().getResource("/icon/eraser-tool-icon.png")));
                buttonClear.setToolTipText(bundle.getString("GtpConsoleDialog.buttonClear.toolTipText"));
                toolBarConsole.add(buttonClear);
            }
            panelInfoUtil.add(toolBarConsole, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

            //---- labelInfo ----
            labelInfo.setText(bundle.getString("GtpConsoleDialog.labelInfo.text"));
            panelInfoUtil.add(labelInfo, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
        }
        contentPane.add(panelInfoUtil, BorderLayout.NORTH);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
}
