package featurecat.lizzie.gui;

import featurecat.lizzie.Lizzie;
import featurecat.lizzie.rules.BoardData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

/**
 * Dialog for editting game info
 */
public class GameInfoDialog extends JDialog {
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("featurecat.lizzie.i18n.GuiBundle");

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel labelBlackPlayerName;
    private JLabel labelWhitePlayerName;
    private JTextField textFieldBlackPlayerName;
    private JTextField textFieldWhitePlayerName;
    private JLabel labelKomi;
    private JTextField textFieldKomi;
    private JLabel labelDoNotShowFirstNMoveNumbers;
    private JSpinner spinnerMoveNumberStartsAt;
    private JLabel labelMoveNumberHint;
    private JButton buttonAutoDetermine;
    private JLabel labelKomiNote;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    public GameInfoDialog(Window owner) {
        super(owner);
        initComponents();
        initOthers();
    }

    private void determineFirstMove() {
        int firstMove = 1;
        for (BoardData data : Lizzie.board.getHistory()) {
            if (data.isWhite().orElse(false) && !data.isPass()) {
                firstMove = data.getMoveNumber();
                break;
            }
        }

        if (firstMove <= 2) {
            firstMove = 1;
        }

        spinnerMoveNumberStartsAt.setValue(firstMove);
    }

    private void initOthers() {
        textFieldBlackPlayerName.setText(Lizzie.gameInfo.getBlackPlayerName());
        textFieldWhitePlayerName.setText(Lizzie.gameInfo.getWhitePlayerName());
        textFieldKomi.setText(String.valueOf(Lizzie.gameInfo.getKomi()));
        spinnerMoveNumberStartsAt.setValue(Lizzie.gameInfo.getHiddenMoveCount() + 1);
    }

    private void buttonAutoDetermineActionPerformed(ActionEvent e) {
        determineFirstMove();
    }

    private void okButtonActionPerformed(ActionEvent e) {
        Lizzie.gameInfo.setHiddenMoveCount((int) spinnerMoveNumberStartsAt.getValue() - 1);
        try {
            double newKomi = Double.parseDouble(textFieldKomi.getText().trim());
            Lizzie.gameInfo.setKomi(newKomi);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, resourceBundle.getString("GameInfoDialog.prompt.komiFormatError"), "Lizzie", JOptionPane.ERROR_MESSAGE);
        }

        Lizzie.frame.repaint();
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void cancelButtonActionPerformed(ActionEvent e) {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("featurecat.lizzie.i18n.GuiBundle");
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        labelBlackPlayerName = new JLabel();
        labelWhitePlayerName = new JLabel();
        textFieldBlackPlayerName = new JTextField();
        textFieldWhitePlayerName = new JTextField();
        labelKomi = new JLabel();
        textFieldKomi = new JTextField();
        labelDoNotShowFirstNMoveNumbers = new JLabel();
        spinnerMoveNumberStartsAt = new JSpinner();
        labelMoveNumberHint = new JLabel();
        buttonAutoDetermine = new JButton();
        labelKomiNote = new JLabel();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        setTitle(bundle.getString("GameInfoDialog.this.title"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {

                //---- labelBlackPlayerName ----
                labelBlackPlayerName.setText(bundle.getString("GameInfoDialog.labelBlackPlayerName.text"));

                //---- labelWhitePlayerName ----
                labelWhitePlayerName.setText(bundle.getString("GameInfoDialog.labelWhitePlayerName.text"));

                //---- textFieldBlackPlayerName ----
                textFieldBlackPlayerName.setEnabled(false);

                //---- textFieldWhitePlayerName ----
                textFieldWhitePlayerName.setEnabled(false);

                //---- labelKomi ----
                labelKomi.setText(bundle.getString("GameInfoDialog.labelKomi.text"));

                //---- textFieldKomi ----
                textFieldKomi.setText("7.5");

                //---- labelDoNotShowFirstNMoveNumbers ----
                labelDoNotShowFirstNMoveNumbers.setText(bundle.getString("GameInfoDialog.labelDoNotShowFirstNMoveNumbers.text"));

                //---- spinnerMoveNumberStartsAt ----
                spinnerMoveNumberStartsAt.setModel(new SpinnerNumberModel(1, 1, 361, 1));

                //---- labelMoveNumberHint ----
                labelMoveNumberHint.setText(bundle.getString("GameInfoDialog.labelMoveNumberHint.text"));

                //---- buttonAutoDetermine ----
                buttonAutoDetermine.setText(bundle.getString("GameInfoDialog.buttonAutoDetermine.text"));
                buttonAutoDetermine.addActionListener(e -> buttonAutoDetermineActionPerformed(e));

                //---- labelKomiNote ----
                labelKomiNote.setText(bundle.getString("GameInfoDialog.labelKomiNote.text"));

                GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
                contentPanel.setLayout(contentPanelLayout);
                contentPanelLayout.setHorizontalGroup(
                    contentPanelLayout.createParallelGroup()
                        .addGroup(contentPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(contentPanelLayout.createParallelGroup()
                                .addComponent(labelMoveNumberHint, GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(labelBlackPlayerName)
                                        .addComponent(labelWhitePlayerName, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(labelKomi, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(contentPanelLayout.createParallelGroup()
                                        .addComponent(textFieldBlackPlayerName)
                                        .addComponent(textFieldWhitePlayerName)
                                        .addComponent(textFieldKomi)))
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addComponent(labelDoNotShowFirstNMoveNumbers)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(spinnerMoveNumberStartsAt, GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buttonAutoDetermine))
                                .addComponent(labelKomiNote, GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE))
                            .addContainerGap())
                );
                contentPanelLayout.setVerticalGroup(
                    contentPanelLayout.createParallelGroup()
                        .addGroup(contentPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelBlackPlayerName)
                                .addComponent(textFieldBlackPlayerName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelWhitePlayerName)
                                .addComponent(textFieldWhitePlayerName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(textFieldKomi, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(labelKomi))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelDoNotShowFirstNMoveNumbers)
                                .addComponent(spinnerMoveNumberStartsAt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(buttonAutoDetermine))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(labelMoveNumberHint)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(labelKomiNote)
                            .addContainerGap(106, Short.MAX_VALUE))
                );
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

                //---- okButton ----
                okButton.setText(bundle.getString("GameInfoDialog.okButton.text"));
                okButton.addActionListener(e -> okButtonActionPerformed(e));
                buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setText(bundle.getString("GameInfoDialog.cancelButton.text"));
                cancelButton.addActionListener(e -> cancelButtonActionPerformed(e));
                buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
}
