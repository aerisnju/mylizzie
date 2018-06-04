/*
 * Created by JFormDesigner on Wed May 09 21:55:40 CST 2018
 */

package featurecat.lizzie.gui;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;
import featurecat.lizzie.Lizzie;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

/**
 * About dialog
 */
public class AboutDialog extends JDialog {
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel labelMyLizzie;
    private JLabel labelVersion;
    private JLabel labelAuthor;
    private JPanel buttonBar;
    private JButton okButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    public AboutDialog(Window owner) {
        super(owner);
        initComponents();
        initOtherComponents();
    }

    private void okButtonActionPerformed(ActionEvent e) {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("featurecat.lizzie.i18n.GuiBundle");
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        labelMyLizzie = new JLabel();
        labelVersion = new JLabel();
        labelAuthor = new JLabel();
        buttonBar = new JPanel();
        okButton = new JButton();

        //======== this ========
        setTitle(bundle.getString("AboutDialog.this.title"));
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new MigLayout(
                    "insets dialog,hidemode 3,alignx center",
                    // columns
                    "[fill]",
                    // rows
                    "[fill]" +
                    "[fill]" +
                    "[fill]"));

                //---- labelMyLizzie ----
                labelMyLizzie.setText(bundle.getString("AboutDialog.labelMyLizzie.text"));
                labelMyLizzie.setHorizontalAlignment(SwingConstants.CENTER);
                labelMyLizzie.setFont(labelMyLizzie.getFont().deriveFont(labelMyLizzie.getFont().getSize() + 10f));
                contentPanel.add(labelMyLizzie, "cell 0 0");

                //---- labelVersion ----
                labelVersion.setText(bundle.getString("AboutDialog.labelVersion.text"));
                labelVersion.setHorizontalAlignment(SwingConstants.CENTER);
                labelVersion.setFont(labelVersion.getFont().deriveFont(labelVersion.getFont().getSize() + 5f));
                contentPanel.add(labelVersion, "cell 0 1");

                //---- labelAuthor ----
                labelAuthor.setText(bundle.getString("AboutDialog.labelAuthor.text"));
                labelAuthor.setVerticalAlignment(SwingConstants.TOP);
                contentPanel.add(labelAuthor, "cell 0 2");
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setLayout(new MigLayout(
                    "insets dialog,alignx right",
                    // columns
                    "[button,fill]",
                    // rows
                    null));

                //---- okButton ----
                okButton.setText(bundle.getString("AboutDialog.okButton.text"));
                okButton.addActionListener(e -> okButtonActionPerformed(e));
                buttonBar.add(okButton, "cell 0 0");
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    private void initOtherComponents() {
        String version = Lizzie.getLizzieVersion();
        if (StringUtils.isNotEmpty(version)) {
            labelVersion.setText(version);
        }

        getRootPane().registerKeyboardAction(e -> dispatchEvent(new WindowEvent(AboutDialog.this, WindowEvent.WINDOW_CLOSING)),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> dispatchEvent(new WindowEvent(AboutDialog.this, WindowEvent.WINDOW_CLOSING)),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
