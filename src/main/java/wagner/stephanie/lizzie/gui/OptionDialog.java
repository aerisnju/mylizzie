/*
 * Created by JFormDesigner on Sun Mar 18 19:01:57 CST 2018
 */

package wagner.stephanie.lizzie.gui;

import org.apache.commons.lang3.StringUtils;
import wagner.stephanie.lizzie.Lizzie;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Aeris
 */
public class OptionDialog extends JDialog {
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel labelVariationLimit;
    private JRadioButton radioButtonV5;
    private JRadioButton radioButtonV10;
    private JRadioButton radioButtonV15;
    private JRadioButton radioButtonV30;
    private JRadioButton radioButtonUnlimited;
    private JLabel labelAxisSetting;
    private JRadioButton radioButtonA1Top;
    private JRadioButton radioButtonA1Bottom;
    private JLabel labelBoardColor;
    private JRadioButton radioButtonColorOriginal;
    private JRadioButton radioButtonColorBright;
    private JRadioButton radioButtonColorPureWhite;
    private JLabel labelAutoHide;
    private JCheckBox checkBoxAutoHideMoveNumber;
    private JLabel labelAnalysisModeOn;
    private JCheckBox checkBoxAnalysisWindowShow;
    private JCheckBox checkBoxMouseMoveShow;
    private JLabel labelLeelazCommandLine;
    private JTextField textFieldLeelazCommandLine;
    private JLabel labelNotes;
    private JButton buttonResetCommandLine;
    private JLabel labelMoveNumber;
    private JCheckBox checkBoxShowMoveNumber;
    private JCheckBox checkBoxMoveNumberLimit;
    private JTextField textFieldMoveNumberLimitCount;
    private JLabel labelMoveNumberLimitLabelTail;
    private JCheckBox checkBoxAutoHideAnalysisSuggession;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    public OptionDialog(Frame owner) {
        super(owner);
        initComponents();
    }

    public OptionDialog(Dialog owner) {
        super(owner);
        initComponents();
    }

    public void setDialogSetting(OptionSetting setting) {
        switch (setting.getVariationLimit()) {
            case 5:
                radioButtonV5.setSelected(true);
                break;
            case 10:
                radioButtonV10.setSelected(true);
                break;
            case 15:
                radioButtonV15.setSelected(true);
                break;
            case 30:
                radioButtonV30.setSelected(true);
                break;
            default:
                radioButtonUnlimited.setSelected(true);
                break;
        }

        if (setting.isA1OnTop()) {
            radioButtonA1Top.setSelected(true);
        } else {
            radioButtonA1Bottom.setSelected(true);
        }

        if (setting.getBoardColor().equals(Color.WHITE)) {
            radioButtonColorPureWhite.setSelected(true);
        } else if (setting.getBoardColor().equals(Color.ORANGE.darker())) {
            radioButtonColorOriginal.setSelected(true);
        } else {
            radioButtonColorBright.setSelected(true);
        }

        checkBoxAutoHideMoveNumber.setSelected(setting.isAutoHideMoveNumber());

        checkBoxAutoHideAnalysisSuggession.setSelected(setting.isAutoHideAnalysisSuggession());

        checkBoxAnalysisWindowShow.setSelected(setting.isAnalysisWindowShow());

        checkBoxMouseMoveShow.setSelected(setting.isMouseOverShowMove());

        textFieldLeelazCommandLine.setText(setting.getLeelazCommandLine());

        checkBoxShowMoveNumber.setSelected(setting.isShowMoveNumber());
        if (setting.getNumberOfLastMovesShown() <= 0) {
            setting.setNumberOfLastMovesShown(new OptionSetting().getNumberOfLastMovesShown());
        }

        if (setting.getNumberOfLastMovesShown() == Integer.MAX_VALUE) {
            checkBoxMoveNumberLimit.setSelected(false);
        } else {
            checkBoxMoveNumberLimit.setSelected(true);
            textFieldMoveNumberLimitCount.setText(String.valueOf(setting.getNumberOfLastMovesShown()));
        }
    }

    public void readDialogSetting(OptionSetting setting) {
        int variationLimit;

        if (radioButtonV5.isSelected()) {
            variationLimit = 5;
        } else if (radioButtonV10.isSelected()) {
            variationLimit = 10;
        } else if (radioButtonV15.isSelected()) {
            variationLimit = 15;
        } else if (radioButtonV30.isSelected()) {
            variationLimit = 30;
        } else {
            variationLimit = Integer.MAX_VALUE;
        }

        Color boardColor;
        if (radioButtonColorBright.isSelected()) {
            boardColor = new Color(0xf0, 0xd2, 0xa0);
        } else if (radioButtonColorOriginal.isSelected()) {
            boardColor = Color.ORANGE.darker();
        } else {
            boardColor = Color.WHITE;
        }

        setting.setVariationLimit(variationLimit);
        setting.setA1OnTop(radioButtonA1Top.isSelected());
        setting.setBoardColor(boardColor);
        setting.setAutoHideMoveNumber(checkBoxAutoHideMoveNumber.isSelected());
        setting.setAutoHideAnalysisSuggession(checkBoxAutoHideAnalysisSuggession.isSelected());
        setting.setAnalysisWindowShow(checkBoxAnalysisWindowShow.isSelected());
        setting.setMouseOverShowMove(checkBoxMouseMoveShow.isSelected());
        String newLeelazCommandLine = textFieldLeelazCommandLine.getText().trim();
        if (StringUtils.isEmpty(newLeelazCommandLine)) {
            setting.setLeelazCommandLine(new OptionSetting().getLeelazCommandLine());
        } else {
            setting.setLeelazCommandLine(newLeelazCommandLine);
        }

        setting.setShowMoveNumber(checkBoxShowMoveNumber.isSelected());
        if (checkBoxMoveNumberLimit.isSelected()) {
            try {
                int moveNumberLimit = Integer.parseInt(textFieldMoveNumberLimitCount.getText());
                if (moveNumberLimit <= 0) {
                    moveNumberLimit = new OptionSetting().getNumberOfLastMovesShown();
                }
                setting.setNumberOfLastMovesShown(moveNumberLimit);
            } catch (NumberFormatException e) {
                setting.setNumberOfLastMovesShown(new OptionSetting().getNumberOfLastMovesShown());
            }
        } else {
            setting.setNumberOfLastMovesShown(Integer.MAX_VALUE);
        }
    }

    private void cancelButtonActionPerformed(ActionEvent e) {
        setVisible(false);
    }

    private void okButtonActionPerformed(ActionEvent e) {
        readDialogSetting(Lizzie.optionSetting);
        Lizzie.analysisDialog.setVisible(Lizzie.optionSetting.isAnalysisWindowShow());
        setVisible(false);
    }

    private void buttonResetCommandLineActionPerformed(ActionEvent e) {
        textFieldLeelazCommandLine.setText(new OptionSetting().getLeelazCommandLine());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        labelVariationLimit = new JLabel();
        radioButtonV5 = new JRadioButton();
        radioButtonV10 = new JRadioButton();
        radioButtonV15 = new JRadioButton();
        radioButtonV30 = new JRadioButton();
        radioButtonUnlimited = new JRadioButton();
        labelAxisSetting = new JLabel();
        radioButtonA1Top = new JRadioButton();
        radioButtonA1Bottom = new JRadioButton();
        labelBoardColor = new JLabel();
        radioButtonColorOriginal = new JRadioButton();
        radioButtonColorBright = new JRadioButton();
        radioButtonColorPureWhite = new JRadioButton();
        labelAutoHide = new JLabel();
        checkBoxAutoHideMoveNumber = new JCheckBox();
        labelAnalysisModeOn = new JLabel();
        checkBoxAnalysisWindowShow = new JCheckBox();
        checkBoxMouseMoveShow = new JCheckBox();
        labelLeelazCommandLine = new JLabel();
        textFieldLeelazCommandLine = new JTextField();
        labelNotes = new JLabel();
        buttonResetCommandLine = new JButton();
        labelMoveNumber = new JLabel();
        checkBoxShowMoveNumber = new JCheckBox();
        checkBoxMoveNumberLimit = new JCheckBox();
        textFieldMoveNumberLimitCount = new JTextField();
        labelMoveNumberLimitLabelTail = new JLabel();
        checkBoxAutoHideAnalysisSuggession = new JCheckBox();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        setTitle("Options");
        setModal(true);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {

                //---- labelVariationLimit ----
                labelVariationLimit.setText("Variation limit:");

                //---- radioButtonV5 ----
                radioButtonV5.setText("5");

                //---- radioButtonV10 ----
                radioButtonV10.setText("10");

                //---- radioButtonV15 ----
                radioButtonV15.setText("15");

                //---- radioButtonV30 ----
                radioButtonV30.setText("30");

                //---- radioButtonUnlimited ----
                radioButtonUnlimited.setText("Unlimited");
                radioButtonUnlimited.setSelected(true);

                //---- labelAxisSetting ----
                labelAxisSetting.setText("Axis setting:");

                //---- radioButtonA1Top ----
                radioButtonA1Top.setText("A1 is on top(Yehu)");

                //---- radioButtonA1Bottom ----
                radioButtonA1Bottom.setText("A1 is on bottom(Yike, Yicheng, Zen, .etc)");
                radioButtonA1Bottom.setSelected(true);

                //---- labelBoardColor ----
                labelBoardColor.setText("Board color:");

                //---- radioButtonColorOriginal ----
                radioButtonColorOriginal.setText("Original");

                //---- radioButtonColorBright ----
                radioButtonColorBright.setText("Bright");
                radioButtonColorBright.setSelected(true);

                //---- radioButtonColorPureWhite ----
                radioButtonColorPureWhite.setText("Pure white");

                //---- labelAutoHide ----
                labelAutoHide.setText("Auto hide when show variation:");

                //---- checkBoxAutoHideMoveNumber ----
                checkBoxAutoHideMoveNumber.setText("Move number");
                checkBoxAutoHideMoveNumber.setSelected(true);

                //---- labelAnalysisModeOn ----
                labelAnalysisModeOn.setText("Analysis mode:");

                //---- checkBoxAnalysisWindowShow ----
                checkBoxAnalysisWindowShow.setText("Show move list");
                checkBoxAnalysisWindowShow.setSelected(true);

                //---- checkBoxMouseMoveShow ----
                checkBoxMouseMoveShow.setText("Mouse over show move");

                //---- labelLeelazCommandLine ----
                labelLeelazCommandLine.setText("Leelaz command line:");

                //---- textFieldLeelazCommandLine ----
                textFieldLeelazCommandLine.setText("-g -t2 -wnetwork");

                //---- labelNotes ----
                labelNotes.setText("Note: Restarting Lizzie is required after changing the leelaz command line");
                labelNotes.setFont(labelNotes.getFont().deriveFont(labelNotes.getFont().getStyle() | Font.BOLD));

                //---- buttonResetCommandLine ----
                buttonResetCommandLine.setText("Reset");
                buttonResetCommandLine.addActionListener(e -> buttonResetCommandLineActionPerformed(e));

                //---- labelMoveNumber ----
                labelMoveNumber.setText("Move number:");

                //---- checkBoxShowMoveNumber ----
                checkBoxShowMoveNumber.setText("Show");
                checkBoxShowMoveNumber.setSelected(true);

                //---- checkBoxMoveNumberLimit ----
                checkBoxMoveNumberLimit.setText("Only show last");

                //---- textFieldMoveNumberLimitCount ----
                textFieldMoveNumberLimitCount.setText("30");

                //---- labelMoveNumberLimitLabelTail ----
                labelMoveNumberLimitLabelTail.setText("move(s).");

                //---- checkBoxAutoHideAnalysisSuggession ----
                checkBoxAutoHideAnalysisSuggession.setText("Analysis suggessions");
                checkBoxAutoHideAnalysisSuggession.setSelected(true);

                GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
                contentPanel.setLayout(contentPanelLayout);
                contentPanelLayout.setHorizontalGroup(
                    contentPanelLayout.createParallelGroup()
                        .addGroup(contentPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(contentPanelLayout.createParallelGroup()
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addComponent(labelLeelazCommandLine)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(textFieldLeelazCommandLine, GroupLayout.PREFERRED_SIZE, 376, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buttonResetCommandLine, GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                                    .addContainerGap())
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addGroup(contentPanelLayout.createParallelGroup()
                                        .addGroup(contentPanelLayout.createSequentialGroup()
                                            .addComponent(labelVariationLimit)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(radioButtonV5)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(radioButtonV10)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(radioButtonV15)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(radioButtonV30)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(radioButtonUnlimited))
                                        .addGroup(contentPanelLayout.createSequentialGroup()
                                            .addComponent(labelAxisSetting)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(radioButtonA1Top)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(radioButtonA1Bottom))
                                        .addGroup(contentPanelLayout.createSequentialGroup()
                                            .addComponent(labelBoardColor)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(radioButtonColorOriginal)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(radioButtonColorBright)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(radioButtonColorPureWhite))
                                        .addGroup(contentPanelLayout.createSequentialGroup()
                                            .addComponent(labelAutoHide)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(checkBoxAutoHideMoveNumber)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(checkBoxAutoHideAnalysisSuggession))
                                        .addGroup(contentPanelLayout.createSequentialGroup()
                                            .addComponent(labelAnalysisModeOn)
                                            .addGap(12, 12, 12)
                                            .addComponent(checkBoxAnalysisWindowShow)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(checkBoxMouseMoveShow))
                                        .addComponent(labelNotes)
                                        .addGroup(contentPanelLayout.createSequentialGroup()
                                            .addComponent(labelMoveNumber)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(checkBoxShowMoveNumber)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(checkBoxMoveNumberLimit)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(textFieldMoveNumberLimitCount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(labelMoveNumberLimitLabelTail)))
                                    .addGap(0, 19, Short.MAX_VALUE))))
                );
                contentPanelLayout.setVerticalGroup(
                    contentPanelLayout.createParallelGroup()
                        .addGroup(contentPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(contentPanelLayout.createParallelGroup()
                                .addComponent(radioButtonUnlimited)
                                .addComponent(radioButtonV30)
                                .addComponent(radioButtonV15)
                                .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(labelVariationLimit)
                                    .addComponent(radioButtonV5))
                                .addComponent(radioButtonV10))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelAxisSetting, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
                                .addComponent(radioButtonA1Top)
                                .addComponent(radioButtonA1Bottom))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup()
                                .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(radioButtonColorOriginal)
                                    .addComponent(labelBoardColor))
                                .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(radioButtonColorBright)
                                    .addComponent(radioButtonColorPureWhite)))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelAutoHide)
                                .addComponent(checkBoxAutoHideMoveNumber)
                                .addComponent(checkBoxAutoHideAnalysisSuggession))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup()
                                .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(checkBoxAnalysisWindowShow)
                                    .addComponent(labelAnalysisModeOn))
                                .addComponent(checkBoxMouseMoveShow))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelLeelazCommandLine)
                                .addComponent(textFieldLeelazCommandLine, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(buttonResetCommandLine))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelMoveNumber)
                                .addComponent(checkBoxShowMoveNumber)
                                .addComponent(checkBoxMoveNumberLimit)
                                .addComponent(textFieldMoveNumberLimitCount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(labelMoveNumberLimitLabelTail))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                            .addComponent(labelNotes)
                            .addContainerGap())
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
                okButton.setText("OK");
                okButton.addActionListener(e -> okButtonActionPerformed(e));
                buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setText("Cancel");
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

        //---- buttonGroupVariationLimit ----
        ButtonGroup buttonGroupVariationLimit = new ButtonGroup();
        buttonGroupVariationLimit.add(radioButtonV5);
        buttonGroupVariationLimit.add(radioButtonV10);
        buttonGroupVariationLimit.add(radioButtonV15);
        buttonGroupVariationLimit.add(radioButtonV30);
        buttonGroupVariationLimit.add(radioButtonUnlimited);

        //---- buttonGroupAxisSetting ----
        ButtonGroup buttonGroupAxisSetting = new ButtonGroup();
        buttonGroupAxisSetting.add(radioButtonA1Top);
        buttonGroupAxisSetting.add(radioButtonA1Bottom);

        //---- buttonGroupBoardColor ----
        ButtonGroup buttonGroupBoardColor = new ButtonGroup();
        buttonGroupBoardColor.add(radioButtonColorOriginal);
        buttonGroupBoardColor.add(radioButtonColorBright);
        buttonGroupBoardColor.add(radioButtonColorPureWhite);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
}
