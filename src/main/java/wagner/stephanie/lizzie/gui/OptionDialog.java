/*
 * Created by JFormDesigner on Sun Mar 18 19:01:57 CST 2018
 */

package wagner.stephanie.lizzie.gui;

import java.util.*;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.StringUtils;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.rules.Board;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Aeris
 */
public class OptionDialog extends JDialog {
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
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
    private JLabel labelBoardDisplay;
    private JRadioButton radioButtonBoardClassic;
    private JRadioButton radioButtonBoardFancy;
    private JLabel labelSuggestion;
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
    private JCheckBox checkBoxPlayoutsInShortForm;
    private JLabel labelTryPlayingMode;
    private JCheckBox checkBoxAutoEnterTryPlayingMode;
    private JLabel labelMainWindow;
    private JCheckBox checkBoxMainWindowAlwaysOnTop;
    private JCheckBox checkBoxShowBlackSuggestion;
    private JLabel labelMaxAnalysisTime;
    private JTextField textFieldMaxAnalysisTime;
    private JLabel labelMinute;
    private JCheckBox checkBoxShowWhiteSuggestion;
    private JLabel labelColorRed;
    private JSpinner spinnerColorRed;
    private JLabel labelColorGreen;
    private JSpinner spinnerColorGreen;
    private JLabel labelColorBlue;
    private JSpinner spinnerColorBlue;
    private JCheckBox checkBoxFancyStones;
    private JLabel labelShadow;
    private JCheckBox checkBoxShowShadow;
    private JSpinner spinnerShadowSize;
    private JCheckBox checkBoxShowNextMove;
    private JButton buttonManage;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    private JPanel panelBasicSettings;
    private JLabel labelBoardSize;
    private JRadioButton radioButtonBoard19x19;
    private JRadioButton radioButtonBoard13x13;
    private JRadioButton radioButtonBoard9x9;
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
        if (setting.getBoardSize().equals(new OptionSetting.BoardSize(9, 9))) {
            radioButtonBoard9x9.setSelected(true);
        } else if (setting.getBoardSize().equals(new OptionSetting.BoardSize(13, 13))) {
            radioButtonBoard13x13.setSelected(true);
        } else {
            radioButtonBoard19x19.setSelected(true);
        }

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

        if (setting.isShowFancyBoard()) {
            radioButtonBoardFancy.setSelected(true);
        } else {
            radioButtonBoardClassic.setSelected(true);
        }

        spinnerColorRed.setValue(setting.getBoardColor().getRed());
        spinnerColorGreen.setValue(setting.getBoardColor().getGreen());
        spinnerColorBlue.setValue(setting.getBoardColor().getBlue());

        checkBoxFancyStones.setSelected(setting.isShowFancyStone());

        checkBoxShowShadow.setSelected(setting.isShowShadow());
        spinnerShadowSize.setValue(Ints.constrainToRange(setting.getShadowSize(), 0, 999));

        checkBoxPlayoutsInShortForm.setSelected(setting.isPlayoutsInShortForm());

        checkBoxShowNextMove.setSelected(setting.isShowNextMove());

        checkBoxAnalysisWindowShow.setSelected(setting.isAnalysisWindowShow());

        checkBoxMouseMoveShow.setSelected(setting.isMouseOverShowMove());

        checkBoxShowBlackSuggestion.setSelected(setting.isShowBlackSuggestion());

        checkBoxShowWhiteSuggestion.setSelected(setting.isShowWhiteSuggestion());

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

        checkBoxAutoEnterTryPlayingMode.setSelected(setting.isAutoEnterTryPlayingMode());

        checkBoxMainWindowAlwaysOnTop.setSelected(setting.isMainWindowAlwaysOnTop());

        textFieldMaxAnalysisTime.setText(String.valueOf(setting.getMaxAnalysisTimeInMinutes()));
    }

    public void readDialogSetting(OptionSetting setting) {
        if (radioButtonBoard9x9.isSelected()) {
            setting.setBoardSize(new OptionSetting.BoardSize(9, 9));
        } else if (radioButtonBoard13x13.isSelected()) {
            setting.setBoardSize(new OptionSetting.BoardSize(13, 13));
        } else {
            setting.setBoardSize(new OptionSetting.BoardSize(19, 19));
        }

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

        setting.setShowFancyBoard(radioButtonBoardFancy.isSelected());
        setting.setBoardColor(new OptionSetting.BoardColor((Integer) spinnerColorRed.getValue(), (Integer) spinnerColorGreen.getValue(), (Integer) spinnerColorBlue.getValue()));
        setting.setShowFancyStone(checkBoxFancyStones.isSelected());
        setting.setShowShadow(checkBoxShowShadow.isSelected());
        setting.setShadowSize((Integer) spinnerShadowSize.getValue());

        setting.setVariationLimit(variationLimit);
        setting.setPlayoutsInShortForm(checkBoxPlayoutsInShortForm.isSelected());
        setting.setShowNextMove(checkBoxShowNextMove.isSelected());
        setting.setA1OnTop(radioButtonA1Top.isSelected());
        setting.setAnalysisWindowShow(checkBoxAnalysisWindowShow.isSelected());
        setting.setMouseOverShowMove(checkBoxMouseMoveShow.isSelected());
        setting.setShowBlackSuggestion(checkBoxShowBlackSuggestion.isSelected());
        setting.setShowWhiteSuggestion(checkBoxShowWhiteSuggestion.isSelected());
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

        setting.setAutoEnterTryPlayingMode(checkBoxAutoEnterTryPlayingMode.isSelected());

        setting.setMainWindowAlwaysOnTop(checkBoxMainWindowAlwaysOnTop.isSelected());

        try {
            int maxAnalysisTime = Integer.parseInt(textFieldMaxAnalysisTime.getText());
            if (maxAnalysisTime > 0) {
                setting.setMaxAnalysisTimeInMinutes(maxAnalysisTime);
            }
        } catch (NumberFormatException e) {
            // no operation
        }
    }

    private void cancelButtonActionPerformed(ActionEvent e) {
        setVisible(false);
    }

    private void okButtonActionPerformed(ActionEvent e) {
        String originalCommandLine = Lizzie.optionSetting.getLeelazCommandLine();

        readDialogSetting(Lizzie.optionSetting);
        if (!Lizzie.optionSetting.isShowBlackSuggestion() || !Lizzie.optionSetting.isShowWhiteSuggestion() || !Lizzie.optionSetting.isAnalysisWindowShow()) {
            Lizzie.analysisFrame.getAnalysisTableModel().clearSelectedMove();
        }

        Lizzie.analysisDialog.setVisible(Lizzie.optionSetting.isAnalysisWindowShow());
        Lizzie.frame.setAlwaysOnTop(Lizzie.optionSetting.isMainWindowAlwaysOnTop());
        Lizzie.frame.getBoardRenderer().forceCachedBackgroundImageRefresh();
        Lizzie.frame.getBoardRenderer().forceCachedStoneImageRefresh();

        if (Board.BOARD_SIZE != Lizzie.optionSetting.getBoardSize().getWidth()) {
            Board.BOARD_SIZE = Lizzie.optionSetting.getBoardSize().getWidth();
            Lizzie.clearBoardAndState();
        }

        Lizzie.frame.repaint();
        setVisible(false);

        if (!StringUtils.equals(originalCommandLine, Lizzie.optionSetting.getLeelazCommandLine())) {
            Lizzie.switchEngineBySetting();
        }
    }

    private void buttonResetCommandLineActionPerformed(ActionEvent e) {
        textFieldLeelazCommandLine.setText(new OptionSetting().getLeelazCommandLine());
    }

    private void buttonManageActionPerformed(ActionEvent e) {
        // Show dialog
        EngineProfileManagerDialog engineProfileManagerDialog = new EngineProfileManagerDialog(this);
        engineProfileManagerDialog.setVisible(true);
        if (engineProfileManagerDialog.isUserApproved()) {
            Lizzie.optionSetting.setEngineProfileList(engineProfileManagerDialog.getProfileList());
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("wagner.stephanie.lizzie.i18n.GuiBundle");
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
        labelBoardDisplay = new JLabel();
        radioButtonBoardClassic = new JRadioButton();
        radioButtonBoardFancy = new JRadioButton();
        labelSuggestion = new JLabel();
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
        checkBoxPlayoutsInShortForm = new JCheckBox();
        labelTryPlayingMode = new JLabel();
        checkBoxAutoEnterTryPlayingMode = new JCheckBox();
        labelMainWindow = new JLabel();
        checkBoxMainWindowAlwaysOnTop = new JCheckBox();
        checkBoxShowBlackSuggestion = new JCheckBox();
        labelMaxAnalysisTime = new JLabel();
        textFieldMaxAnalysisTime = new JTextField();
        labelMinute = new JLabel();
        checkBoxShowWhiteSuggestion = new JCheckBox();
        labelColorRed = new JLabel();
        spinnerColorRed = new JSpinner();
        labelColorGreen = new JLabel();
        spinnerColorGreen = new JSpinner();
        labelColorBlue = new JLabel();
        spinnerColorBlue = new JSpinner();
        checkBoxFancyStones = new JCheckBox();
        labelShadow = new JLabel();
        checkBoxShowShadow = new JCheckBox();
        spinnerShadowSize = new JSpinner();
        checkBoxShowNextMove = new JCheckBox();
        buttonManage = new JButton();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        panelBasicSettings = new JPanel();
        labelBoardSize = new JLabel();
        radioButtonBoard19x19 = new JRadioButton();
        radioButtonBoard13x13 = new JRadioButton();
        radioButtonBoard9x9 = new JRadioButton();

        //======== this ========
        setTitle(bundle.getString("OptionDialog.this.title"));
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
                labelVariationLimit.setText(bundle.getString("OptionDialog.labelVariationLimit.text"));

                //---- radioButtonV5 ----
                radioButtonV5.setText(bundle.getString("OptionDialog.radioButtonV5.text"));

                //---- radioButtonV10 ----
                radioButtonV10.setText(bundle.getString("OptionDialog.radioButtonV10.text"));

                //---- radioButtonV15 ----
                radioButtonV15.setText(bundle.getString("OptionDialog.radioButtonV15.text"));

                //---- radioButtonV30 ----
                radioButtonV30.setText(bundle.getString("OptionDialog.radioButtonV30.text"));

                //---- radioButtonUnlimited ----
                radioButtonUnlimited.setText(bundle.getString("OptionDialog.radioButtonUnlimited.text"));
                radioButtonUnlimited.setSelected(true);

                //---- labelAxisSetting ----
                labelAxisSetting.setText(bundle.getString("OptionDialog.labelAxisSetting.text"));

                //---- radioButtonA1Top ----
                radioButtonA1Top.setText(bundle.getString("OptionDialog.radioButtonA1Top.text"));

                //---- radioButtonA1Bottom ----
                radioButtonA1Bottom.setText(bundle.getString("OptionDialog.radioButtonA1Bottom.text"));
                radioButtonA1Bottom.setSelected(true);

                //---- labelBoardDisplay ----
                labelBoardDisplay.setText(bundle.getString("OptionDialog.labelBoardDisplay.text"));

                //---- radioButtonBoardClassic ----
                radioButtonBoardClassic.setText(bundle.getString("OptionDialog.radioButtonBoardClassic.text"));

                //---- radioButtonBoardFancy ----
                radioButtonBoardFancy.setText(bundle.getString("OptionDialog.radioButtonBoardFancy.text"));
                radioButtonBoardFancy.setSelected(true);

                //---- labelSuggestion ----
                labelSuggestion.setText(bundle.getString("OptionDialog.labelSuggestion.text"));

                //---- labelAnalysisModeOn ----
                labelAnalysisModeOn.setText(bundle.getString("OptionDialog.labelAnalysisModeOn.text"));

                //---- checkBoxAnalysisWindowShow ----
                checkBoxAnalysisWindowShow.setText(bundle.getString("OptionDialog.checkBoxAnalysisWindowShow.text"));
                checkBoxAnalysisWindowShow.setSelected(true);

                //---- checkBoxMouseMoveShow ----
                checkBoxMouseMoveShow.setText(bundle.getString("OptionDialog.checkBoxMouseMoveShow.text"));

                //---- labelLeelazCommandLine ----
                labelLeelazCommandLine.setText(bundle.getString("OptionDialog.labelLeelazCommandLine.text"));

                //---- textFieldLeelazCommandLine ----
                textFieldLeelazCommandLine.setText("-g -t2 -wnetwork -b0");

                //---- labelNotes ----
                labelNotes.setText(bundle.getString("OptionDialog.labelNotes.text"));
                labelNotes.setFont(labelNotes.getFont().deriveFont(labelNotes.getFont().getStyle() | Font.BOLD));

                //---- buttonResetCommandLine ----
                buttonResetCommandLine.setText(bundle.getString("OptionDialog.buttonResetCommandLine.text"));
                buttonResetCommandLine.addActionListener(e -> buttonResetCommandLineActionPerformed(e));

                //---- labelMoveNumber ----
                labelMoveNumber.setText(bundle.getString("OptionDialog.labelMoveNumber.text"));

                //---- checkBoxShowMoveNumber ----
                checkBoxShowMoveNumber.setText(bundle.getString("OptionDialog.checkBoxShowMoveNumber.text"));
                checkBoxShowMoveNumber.setSelected(true);

                //---- checkBoxMoveNumberLimit ----
                checkBoxMoveNumberLimit.setText(bundle.getString("OptionDialog.checkBoxMoveNumberLimit.text"));

                //---- textFieldMoveNumberLimitCount ----
                textFieldMoveNumberLimitCount.setText("30");

                //---- labelMoveNumberLimitLabelTail ----
                labelMoveNumberLimitLabelTail.setText(bundle.getString("OptionDialog.labelMoveNumberLimitLabelTail.text"));

                //---- checkBoxPlayoutsInShortForm ----
                checkBoxPlayoutsInShortForm.setText(bundle.getString("OptionDialog.checkBoxPlayoutsInShortForm.text"));

                //---- labelTryPlayingMode ----
                labelTryPlayingMode.setText(bundle.getString("OptionDialog.labelTryPlayingMode.text"));

                //---- checkBoxAutoEnterTryPlayingMode ----
                checkBoxAutoEnterTryPlayingMode.setText(bundle.getString("OptionDialog.checkBoxAutoEnterTryPlayingMode.text"));

                //---- labelMainWindow ----
                labelMainWindow.setText(bundle.getString("OptionDialog.labelMainWindow.text"));

                //---- checkBoxMainWindowAlwaysOnTop ----
                checkBoxMainWindowAlwaysOnTop.setText(bundle.getString("OptionDialog.checkBoxMainWindowAlwaysOnTop.text"));

                //---- checkBoxShowBlackSuggestion ----
                checkBoxShowBlackSuggestion.setText(bundle.getString("OptionDialog.checkBoxShowBlackSuggestion.text"));
                checkBoxShowBlackSuggestion.setSelected(true);

                //---- labelMaxAnalysisTime ----
                labelMaxAnalysisTime.setText(bundle.getString("OptionDialog.labelMaxAnalysisTime.text"));

                //---- textFieldMaxAnalysisTime ----
                textFieldMaxAnalysisTime.setText("2");

                //---- labelMinute ----
                labelMinute.setText(bundle.getString("OptionDialog.labelMinute.text"));

                //---- checkBoxShowWhiteSuggestion ----
                checkBoxShowWhiteSuggestion.setText(bundle.getString("OptionDialog.checkBoxShowWhiteSuggestion.text"));
                checkBoxShowWhiteSuggestion.setSelected(true);

                //---- labelColorRed ----
                labelColorRed.setText(bundle.getString("OptionDialog.labelColorRed.text"));

                //---- spinnerColorRed ----
                spinnerColorRed.setModel(new SpinnerNumberModel(178, 0, 255, 1));

                //---- labelColorGreen ----
                labelColorGreen.setText(bundle.getString("OptionDialog.labelColorGreen.text"));

                //---- spinnerColorGreen ----
                spinnerColorGreen.setModel(new SpinnerNumberModel(140, 0, 255, 1));

                //---- labelColorBlue ----
                labelColorBlue.setText(bundle.getString("OptionDialog.labelColorBlue.text"));

                //---- spinnerColorBlue ----
                spinnerColorBlue.setModel(new SpinnerNumberModel(0, 0, 255, 1));

                //---- checkBoxFancyStones ----
                checkBoxFancyStones.setText(bundle.getString("OptionDialog.checkBoxFancyStones.text"));
                checkBoxFancyStones.setSelected(true);

                //---- labelShadow ----
                labelShadow.setText(bundle.getString("OptionDialog.labelShadow.text"));

                //---- checkBoxShowShadow ----
                checkBoxShowShadow.setText(bundle.getString("OptionDialog.checkBoxShowShadow.text"));
                checkBoxShowShadow.setSelected(true);

                //---- spinnerShadowSize ----
                spinnerShadowSize.setModel(new SpinnerNumberModel(100, 0, 999, 1));

                //---- checkBoxShowNextMove ----
                checkBoxShowNextMove.setText(bundle.getString("OptionDialog.checkBoxShowNextMove.text"));

                //---- buttonManage ----
                buttonManage.setText(bundle.getString("OptionDialog.buttonManage.text"));
                buttonManage.addActionListener(e -> buttonManageActionPerformed(e));

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
                                    .addComponent(textFieldLeelazCommandLine, GroupLayout.PREFERRED_SIZE, 283, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buttonManage)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buttonResetCommandLine)
                                    .addContainerGap(35, Short.MAX_VALUE))
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
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(radioButtonA1Top)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(radioButtonA1Bottom))
                                        .addGroup(contentPanelLayout.createSequentialGroup()
                                            .addComponent(labelAnalysisModeOn)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(checkBoxAnalysisWindowShow)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(checkBoxMouseMoveShow)
                                            .addGap(18, 18, 18)
                                            .addComponent(labelMaxAnalysisTime)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(textFieldMaxAnalysisTime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(labelMinute))
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
                                            .addComponent(labelMoveNumberLimitLabelTail))
                                        .addGroup(contentPanelLayout.createSequentialGroup()
                                            .addComponent(labelTryPlayingMode)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(checkBoxAutoEnterTryPlayingMode))
                                        .addGroup(contentPanelLayout.createSequentialGroup()
                                            .addComponent(labelMainWindow)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(checkBoxMainWindowAlwaysOnTop))
                                        .addGroup(contentPanelLayout.createSequentialGroup()
                                            .addComponent(labelSuggestion)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(checkBoxShowBlackSuggestion)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(checkBoxShowWhiteSuggestion)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(checkBoxPlayoutsInShortForm)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(checkBoxShowNextMove))
                                        .addGroup(contentPanelLayout.createSequentialGroup()
                                            .addComponent(labelBoardDisplay)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(radioButtonBoardFancy)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(radioButtonBoardClassic)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(labelColorRed)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(spinnerColorRed, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(labelColorGreen)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(spinnerColorGreen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(labelColorBlue)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(spinnerColorBlue, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(checkBoxFancyStones))
                                        .addGroup(contentPanelLayout.createSequentialGroup()
                                            .addComponent(labelShadow)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(checkBoxShowShadow)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(spinnerShadowSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                    .addGap(0, 61, Short.MAX_VALUE))))
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
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelBoardDisplay)
                                .addComponent(radioButtonBoardFancy)
                                .addComponent(radioButtonBoardClassic)
                                .addComponent(labelColorRed)
                                .addComponent(spinnerColorRed, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(labelColorGreen)
                                .addComponent(spinnerColorGreen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(labelColorBlue)
                                .addComponent(spinnerColorBlue, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(checkBoxFancyStones))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelShadow)
                                .addComponent(checkBoxShowShadow)
                                .addComponent(spinnerShadowSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelSuggestion)
                                .addComponent(checkBoxShowBlackSuggestion)
                                .addComponent(checkBoxShowWhiteSuggestion)
                                .addComponent(checkBoxPlayoutsInShortForm)
                                .addComponent(checkBoxShowNextMove))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelAnalysisModeOn)
                                .addComponent(checkBoxAnalysisWindowShow)
                                .addComponent(checkBoxMouseMoveShow)
                                .addComponent(labelMaxAnalysisTime)
                                .addComponent(textFieldMaxAnalysisTime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(labelMinute))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelLeelazCommandLine)
                                .addComponent(textFieldLeelazCommandLine, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(buttonManage)
                                .addComponent(buttonResetCommandLine))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelMoveNumber)
                                .addComponent(checkBoxShowMoveNumber)
                                .addComponent(checkBoxMoveNumberLimit)
                                .addComponent(textFieldMoveNumberLimitCount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(labelMoveNumberLimitLabelTail))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelTryPlayingMode)
                                .addComponent(checkBoxAutoEnterTryPlayingMode))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelMainWindow)
                                .addComponent(checkBoxMainWindowAlwaysOnTop))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                okButton.setText(bundle.getString("OptionDialog.okButton.text"));
                okButton.addActionListener(e -> okButtonActionPerformed(e));
                buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setText(bundle.getString("OptionDialog.cancelButton.text"));
                cancelButton.addActionListener(e -> cancelButtonActionPerformed(e));
                buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);

            //======== panelBasicSettings ========
            {
                panelBasicSettings.setLayout(new FlowLayout(FlowLayout.LEFT));

                //---- labelBoardSize ----
                labelBoardSize.setText(bundle.getString("OptionDialog.labelBoardSize.text"));
                panelBasicSettings.add(labelBoardSize);

                //---- radioButtonBoard19x19 ----
                radioButtonBoard19x19.setText(bundle.getString("OptionDialog.radioButtonBoard19x19.text"));
                radioButtonBoard19x19.setSelected(true);
                panelBasicSettings.add(radioButtonBoard19x19);

                //---- radioButtonBoard13x13 ----
                radioButtonBoard13x13.setText(bundle.getString("OptionDialog.radioButtonBoard13x13.text"));
                panelBasicSettings.add(radioButtonBoard13x13);

                //---- radioButtonBoard9x9 ----
                radioButtonBoard9x9.setText(bundle.getString("OptionDialog.radioButtonBoard9x9.text"));
                panelBasicSettings.add(radioButtonBoard9x9);
            }
            dialogPane.add(panelBasicSettings, BorderLayout.NORTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);

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
        buttonGroupBoardColor.add(radioButtonBoardClassic);
        buttonGroupBoardColor.add(radioButtonBoardFancy);

        //---- buttonGroupBoardSize ----
        ButtonGroup buttonGroupBoardSize = new ButtonGroup();
        buttonGroupBoardSize.add(radioButtonBoard19x19);
        buttonGroupBoardSize.add(radioButtonBoard13x13);
        buttonGroupBoardSize.add(radioButtonBoard9x9);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
}
