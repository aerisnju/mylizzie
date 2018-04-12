/*
 * Created by JFormDesigner on Fri Apr 06 16:22:22 CST 2018
 */

package wagner.stephanie.lizzie.gui;

import com.google.common.collect.ImmutableList;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.collections4.CollectionUtils;
import wagner.stephanie.lizzie.Lizzie;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * @author Cao Hu
 */
public class EngineProfileManagerDialog extends JDialog {
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel labelProfile1;
    private JTextField textFieldProfile1;
    private JLabel labelProfile2;
    private JTextField textFieldProfile2;
    private JLabel labelProfile3;
    private JTextField textFieldProfile3;
    private JLabel labelProfile4;
    private JTextField textFieldProfile4;
    private JLabel labelProfile5;
    private JTextField textFieldProfile5;
    private JLabel labelProfile6;
    private JTextField textFieldProfile6;
    private JLabel labelProfile7;
    private JTextField textFieldProfile7;
    private JLabel labelProfile8;
    private JTextField textFieldProfile8;
    private JLabel labelProfile9;
    private JTextField textFieldProfile9;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    private JPanel panelNotes;
    private JLabel labelProfileManagerNote;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private boolean userApproved;
    private List<String> profileList;
    private List<JTextField> profileTextFields;

    public EngineProfileManagerDialog(Frame owner) {
        super(owner);
        initComponents();
        initVariables();
    }

    public EngineProfileManagerDialog(Dialog owner) {
        super(owner);
        initComponents();
        initVariables();
    }

    public boolean isUserApproved() {
        return userApproved;
    }

    public List<String> getProfileList() {
        return profileList;
    }

    private void initVariables() {
        userApproved = false;
        profileList = Lizzie.optionSetting.getEngineProfileList();
        profileTextFields = ImmutableList.of(textFieldProfile1, textFieldProfile2, textFieldProfile3, textFieldProfile4
                , textFieldProfile5, textFieldProfile6, textFieldProfile7, textFieldProfile8, textFieldProfile9);

        if (CollectionUtils.isNotEmpty(profileList)) {
            for (int i = 0; i < profileList.size(); ++i) {
                profileTextFields.get(i).setText(profileList.get(i));
            }
        }
    }

    private void cancelButtonActionPerformed(ActionEvent e) {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void okButtonActionPerformed(ActionEvent e) {
        userApproved = true;
        profileList = profileTextFields.stream().map(JTextField::getText).collect(Collectors.toCollection(ArrayList::new));
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("wagner.stephanie.lizzie.i18n.GuiBundle");
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        labelProfile1 = new JLabel();
        textFieldProfile1 = new JTextField();
        labelProfile2 = new JLabel();
        textFieldProfile2 = new JTextField();
        labelProfile3 = new JLabel();
        textFieldProfile3 = new JTextField();
        labelProfile4 = new JLabel();
        textFieldProfile4 = new JTextField();
        labelProfile5 = new JLabel();
        textFieldProfile5 = new JTextField();
        labelProfile6 = new JLabel();
        textFieldProfile6 = new JTextField();
        labelProfile7 = new JLabel();
        textFieldProfile7 = new JTextField();
        labelProfile8 = new JLabel();
        textFieldProfile8 = new JTextField();
        labelProfile9 = new JLabel();
        textFieldProfile9 = new JTextField();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        panelNotes = new JPanel();
        labelProfileManagerNote = new JLabel();

        //======== this ========
        setTitle(bundle.getString("EngineProfileManagerDialog.this.title"));
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(Borders.createEmptyBorder("7dlu, 7dlu, 7dlu, 7dlu"));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new FormLayout(
                        "default, $lcgap, [200dlu,pref]",
                        "8*(default, $lgap), default"));

                //---- labelProfile1 ----
                labelProfile1.setText(bundle.getString("EngineProfileManagerDialog.labelProfile1.text"));
                contentPanel.add(labelProfile1, CC.xy(1, 1));
                contentPanel.add(textFieldProfile1, CC.xy(3, 1));

                //---- labelProfile2 ----
                labelProfile2.setText(bundle.getString("EngineProfileManagerDialog.labelProfile2.text"));
                contentPanel.add(labelProfile2, CC.xy(1, 3));
                contentPanel.add(textFieldProfile2, CC.xy(3, 3));

                //---- labelProfile3 ----
                labelProfile3.setText(bundle.getString("EngineProfileManagerDialog.labelProfile3.text"));
                contentPanel.add(labelProfile3, CC.xy(1, 5));
                contentPanel.add(textFieldProfile3, CC.xy(3, 5));

                //---- labelProfile4 ----
                labelProfile4.setText(bundle.getString("EngineProfileManagerDialog.labelProfile4.text"));
                contentPanel.add(labelProfile4, CC.xy(1, 7));
                contentPanel.add(textFieldProfile4, CC.xy(3, 7));

                //---- labelProfile5 ----
                labelProfile5.setText(bundle.getString("EngineProfileManagerDialog.labelProfile5.text"));
                contentPanel.add(labelProfile5, CC.xy(1, 9));
                contentPanel.add(textFieldProfile5, CC.xy(3, 9));

                //---- labelProfile6 ----
                labelProfile6.setText(bundle.getString("EngineProfileManagerDialog.labelProfile6.text"));
                contentPanel.add(labelProfile6, CC.xy(1, 11));
                contentPanel.add(textFieldProfile6, CC.xy(3, 11));

                //---- labelProfile7 ----
                labelProfile7.setText(bundle.getString("EngineProfileManagerDialog.labelProfile7.text"));
                contentPanel.add(labelProfile7, CC.xy(1, 13));
                contentPanel.add(textFieldProfile7, CC.xy(3, 13));

                //---- labelProfile8 ----
                labelProfile8.setText(bundle.getString("EngineProfileManagerDialog.labelProfile8.text"));
                contentPanel.add(labelProfile8, CC.xy(1, 15));
                contentPanel.add(textFieldProfile8, CC.xy(3, 15));

                //---- labelProfile9 ----
                labelProfile9.setText(bundle.getString("EngineProfileManagerDialog.labelProfile9.text"));
                contentPanel.add(labelProfile9, CC.xy(1, 17));
                contentPanel.add(textFieldProfile9, CC.xy(3, 17));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(Borders.createEmptyBorder("5dlu, 0dlu, 0dlu, 0dlu"));
                buttonBar.setLayout(new FormLayout(
                        "$glue, $button, $rgap, $button",
                        "pref"));

                //---- okButton ----
                okButton.setText(bundle.getString("EngineProfileManagerDialog.okButton.text"));
                okButton.addActionListener(e -> okButtonActionPerformed(e));
                buttonBar.add(okButton, CC.xy(2, 1));

                //---- cancelButton ----
                cancelButton.setText(bundle.getString("EngineProfileManagerDialog.cancelButton.text"));
                cancelButton.addActionListener(e -> cancelButtonActionPerformed(e));
                buttonBar.add(cancelButton, CC.xy(4, 1));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);

            //======== panelNotes ========
            {
                panelNotes.setLayout(new FlowLayout(FlowLayout.LEFT));
                ((FlowLayout) panelNotes.getLayout()).setAlignOnBaseline(true);

                //---- labelProfileManagerNote ----
                labelProfileManagerNote.setText(bundle.getString("EngineProfileManagerDialog.labelProfileManagerNote.text"));
                panelNotes.add(labelProfileManagerNote);
            }
            dialogPane.add(panelNotes, BorderLayout.NORTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
}
