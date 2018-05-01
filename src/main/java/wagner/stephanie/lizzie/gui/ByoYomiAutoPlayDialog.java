/*
 * Created by JFormDesigner on Thu Apr 12 11:15:25 CST 2018
 */

package wagner.stephanie.lizzie.gui;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.math.NumberUtils;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.rules.BoardHistoryNode;
import wagner.stephanie.lizzie.rules.BoardStateChangeObserver;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Cao Hu
 */
public class ByoYomiAutoPlayDialog extends JDialog {
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panelNotes;
    private JLabel labelNotes;
    private JLabel labelSettings;
    private JCheckBox checkBoxBlackCountdown;
    private JCheckBox checkBoxWhiteCountdown;
    private JLabel labelCountdownTime;
    private JSpinner spinnerCountdownTime;
    private JLabel labelSeconds;
    private JPanel panelCountdown;
    private JLabel labelBlack;
    private JLabel labelCountdownIndicator;
    private JLabel labelWhite;
    private JLabel labelBlackToPlay;
    private JLabel labelCountdownValue;
    private JLabel labelWhiteToPlay;
    private JProgressBar progressBarCountdown;
    private JPanel panelButtonArea;
    private JButton buttonStartPause;
    private JButton buttonReset;
    private JButton buttonExit;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private Timer timer;
    private BoardStateChangeObserver boardStateChangeObserver;
    private AtomicBoolean nextBlack;
    private boolean countdownSystemStarted;

    public ByoYomiAutoPlayDialog(Frame owner) {
        super(owner);
        initComponents();
        initOtherComponents();
    }

    public ByoYomiAutoPlayDialog(Dialog owner) {
        super(owner);
        initComponents();
        initOtherComponents();
    }

    private void initOtherComponents() {
        timer = new Timer(1000, e -> {
            int remaining = NumberUtils.toInt(labelCountdownValue.getText(), (Integer) spinnerCountdownTime.getValue());
            if (remaining <= 1) {
                countdownEnds();
                resetCountdown();
            } else {
                --remaining;
                labelCountdownValue.setText(String.valueOf(remaining));
                progressBarCountdown.setValue(progressBarCountdown.getMaximum() - remaining);
            }
        });
        timer.setRepeats(true);

        countdownSystemStarted = false;

        nextBlack = new AtomicBoolean(Lizzie.board.getData().isBlackToPlay());

        boardStateChangeObserver = new BoardStateChangeObserver() {
            @Override
            public void mainStreamAppended(BoardHistoryNode newNodeBegin, BoardHistoryNode head) {
                boolean newState = head.getData().isBlackToPlay();
                boolean originalState = nextBlack.getAndSet(newState);
                if (newState != originalState) {
                    boardPlayerChanged();
                }
            }

            @Override
            public void mainStreamCut(BoardHistoryNode nodeBeforeCutPoint, BoardHistoryNode head) {
                boolean newState = head.getData().isBlackToPlay();
                boolean originalState = nextBlack.getAndSet(newState);
                if (newState != originalState) {
                    boardPlayerChanged();
                }
            }

            @Override
            public void headMoved(BoardHistoryNode oldHead, BoardHistoryNode newHead) {
                boolean newState = newHead.getData().isBlackToPlay();
                boolean originalState = nextBlack.getAndSet(newState);
                if (newState != originalState) {
                    boardPlayerChanged();
                }
            }

            @Override
            public void boardCleared() {
                nextBlack.set(true);
                boardPlayerChanged();
            }
        };
        Lizzie.board.registerBoardStateChangeObserver(boardStateChangeObserver);
    }

    private void boardPlayerChanged() {
        refreshPlayer();

        stopCountdown();
        if (isCountdownSystemStarted()) {
            if (checkBoxBlackCountdown.isSelected() && nextBlack.get() || checkBoxWhiteCountdown.isSelected() && !nextBlack.get()) {
                resetCountdown();
                startOrResumeCountdown();

                Lizzie.leelaz.startThinking();
            } else {
                Lizzie.leelaz.stopThinking();

                resetCountdown();
            }
        } else {
            resetCountdown();
        }
    }

    private void refreshPlayer() {
        if (nextBlack.get()) {
            labelBlackToPlay.setText("●");
            labelWhiteToPlay.setText("");
        } else {
            labelBlackToPlay.setText("");
            labelWhiteToPlay.setText("○");
        }
    }

    private void countdownEnds() {
        Lizzie.miscExecutor.execute(() -> Lizzie.board.playBestMove());
    }

    private void resetCountdown() {
        int countdownValue = (int) spinnerCountdownTime.getValue();
        labelCountdownValue.setText(String.valueOf(countdownValue));
        progressBarCountdown.setMaximum(countdownValue);
        progressBarCountdown.setValue(0);
    }

    private void stopCountdown() {
        if (timer.isRunning()) {
            timer.stop();
        }
    }

    private void startOrResumeCountdown() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    private void buttonExitActionPerformed(ActionEvent e) {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void thisWindowClosing(WindowEvent e) {
        countdownSystemStop();
        stopCountdown();
        Lizzie.board.unregisterBoardStateChangeObserver(boardStateChangeObserver);
    }

    private void buttonResetActionPerformed(ActionEvent e) {
        refreshPlayer();

        countdownSystemStop();
        stopCountdown();
        resetCountdown();
    }

    private void buttonStartPauseActionPerformed(ActionEvent e) {
        refreshPlayer();

        if (isCountdownSystemStarted()) {
            countdownSystemStop();
            stopCountdown();
        } else {
            countdownSystemStart();
            if (checkBoxBlackCountdown.isSelected() && nextBlack.get() || checkBoxWhiteCountdown.isSelected() && !nextBlack.get()) {
                startOrResumeCountdown();
            }
        }
    }

    private void spinnerCountdownTimeStateChanged(ChangeEvent e) {
        countdownSystemStop();
        stopCountdown();
        resetCountdown();
    }

    private void countdownSystemStart() {
        countdownSystemStarted = true;
        refreshPlayer();
    }

    private void countdownSystemStop() {
        countdownSystemStarted = false;
        labelBlackToPlay.setText("");
        labelWhiteToPlay.setText("");
    }

    public boolean isCountdownSystemStarted() {
        return countdownSystemStarted;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("wagner.stephanie.lizzie.i18n.GuiBundle");
        panelNotes = new JPanel();
        labelNotes = new JLabel();
        labelSettings = new JLabel();
        checkBoxBlackCountdown = new JCheckBox();
        checkBoxWhiteCountdown = new JCheckBox();
        labelCountdownTime = new JLabel();
        spinnerCountdownTime = new JSpinner();
        labelSeconds = new JLabel();
        panelCountdown = new JPanel();
        labelBlack = new JLabel();
        labelCountdownIndicator = new JLabel();
        labelWhite = new JLabel();
        labelBlackToPlay = new JLabel();
        labelCountdownValue = new JLabel();
        labelWhiteToPlay = new JLabel();
        progressBarCountdown = new JProgressBar();
        panelButtonArea = new JPanel();
        buttonStartPause = new JButton();
        buttonReset = new JButton();
        buttonExit = new JButton();

        //======== this ========
        setTitle(bundle.getString("ByoYomiAutoPlayDialog.this.title"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                thisWindowClosing(e);
            }
        });
        Container contentPane = getContentPane();

        //======== panelNotes ========
        {
            panelNotes.setBackground(Color.white);

            //---- labelNotes ----
            labelNotes.setText(bundle.getString("ByoYomiAutoPlayDialog.labelNotes.text"));
            labelNotes.setVerticalAlignment(SwingConstants.TOP);

            GroupLayout panelNotesLayout = new GroupLayout(panelNotes);
            panelNotes.setLayout(panelNotesLayout);
            panelNotesLayout.setHorizontalGroup(
                    panelNotesLayout.createParallelGroup()
                            .addGroup(panelNotesLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(labelNotes, GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                                    .addContainerGap())
            );
            panelNotesLayout.setVerticalGroup(
                    panelNotesLayout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, panelNotesLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(labelNotes, GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                                    .addContainerGap())
            );
        }

        //---- labelSettings ----
        labelSettings.setText(bundle.getString("ByoYomiAutoPlayDialog.labelSettings.text"));

        //---- checkBoxBlackCountdown ----
        checkBoxBlackCountdown.setText(bundle.getString("ByoYomiAutoPlayDialog.checkBoxBlackCountdown.text"));

        //---- checkBoxWhiteCountdown ----
        checkBoxWhiteCountdown.setText(bundle.getString("ByoYomiAutoPlayDialog.checkBoxWhiteCountdown.text"));

        //---- labelCountdownTime ----
        labelCountdownTime.setText(bundle.getString("ByoYomiAutoPlayDialog.labelCountdownTime.text"));

        //---- spinnerCountdownTime ----
        spinnerCountdownTime.setModel(new SpinnerNumberModel(30, 1, 600, 1));
        spinnerCountdownTime.addChangeListener(e -> spinnerCountdownTimeStateChanged(e));

        //---- labelSeconds ----
        labelSeconds.setText(bundle.getString("ByoYomiAutoPlayDialog.labelSeconds.text"));

        //======== panelCountdown ========
        {
            panelCountdown.setLayout(new MigLayout(
                    "fill,hidemode 3",
                    // columns
                    "[fill]" +
                            "[fill]" +
                            "[fill]",
                    // rows
                    "[]" +
                            "[]" +
                            "[]"));

            //---- labelBlack ----
            labelBlack.setText(bundle.getString("ByoYomiAutoPlayDialog.labelBlack.text"));
            labelBlack.setHorizontalAlignment(SwingConstants.CENTER);
            panelCountdown.add(labelBlack, "cell 0 0");

            //---- labelCountdownIndicator ----
            labelCountdownIndicator.setText(bundle.getString("ByoYomiAutoPlayDialog.labelCountdownIndicator.text"));
            labelCountdownIndicator.setHorizontalAlignment(SwingConstants.CENTER);
            labelCountdownIndicator.setFont(labelCountdownIndicator.getFont().deriveFont(labelCountdownIndicator.getFont().getSize() + 5f));
            panelCountdown.add(labelCountdownIndicator, "cell 1 0");

            //---- labelWhite ----
            labelWhite.setText(bundle.getString("ByoYomiAutoPlayDialog.labelWhite.text"));
            labelWhite.setHorizontalAlignment(SwingConstants.CENTER);
            panelCountdown.add(labelWhite, "cell 2 0");

            //---- labelBlackToPlay ----
            labelBlackToPlay.setHorizontalAlignment(SwingConstants.CENTER);
            labelBlackToPlay.setFont(labelBlackToPlay.getFont().deriveFont(labelBlackToPlay.getFont().getSize() + 12f));
            panelCountdown.add(labelBlackToPlay, "cell 0 1");

            //---- labelCountdownValue ----
            labelCountdownValue.setText(bundle.getString("ByoYomiAutoPlayDialog.labelCountdownValue.text"));
            labelCountdownValue.setHorizontalAlignment(SwingConstants.CENTER);
            labelCountdownValue.setFont(labelCountdownValue.getFont().deriveFont(labelCountdownValue.getFont().getSize() + 12f));
            panelCountdown.add(labelCountdownValue, "cell 1 1");

            //---- labelWhiteToPlay ----
            labelWhiteToPlay.setFont(labelWhiteToPlay.getFont().deriveFont(labelWhiteToPlay.getFont().getSize() + 12f));
            labelWhiteToPlay.setHorizontalAlignment(SwingConstants.CENTER);
            panelCountdown.add(labelWhiteToPlay, "cell 2 1");

            //---- progressBarCountdown ----
            progressBarCountdown.setPreferredSize(new Dimension(146, 20));
            progressBarCountdown.setMaximum(30);
            panelCountdown.add(progressBarCountdown, "cell 1 2");
        }

        //======== panelButtonArea ========
        {
            panelButtonArea.setLayout(new MigLayout(
                    "fill,hidemode 3",
                    // columns
                    "[fill]" +
                            "[fill]" +
                            "[fill]",
                    // rows
                    "[]"));

            //---- buttonStartPause ----
            buttonStartPause.setText(bundle.getString("ByoYomiAutoPlayDialog.buttonStartPause.text"));
            buttonStartPause.addActionListener(e -> buttonStartPauseActionPerformed(e));
            panelButtonArea.add(buttonStartPause, "cell 0 0");

            //---- buttonReset ----
            buttonReset.setText(bundle.getString("ByoYomiAutoPlayDialog.buttonReset.text"));
            buttonReset.addActionListener(e -> buttonResetActionPerformed(e));
            panelButtonArea.add(buttonReset, "cell 1 0");

            //---- buttonExit ----
            buttonExit.setText(bundle.getString("ByoYomiAutoPlayDialog.buttonExit.text"));
            buttonExit.addActionListener(e -> buttonExitActionPerformed(e));
            panelButtonArea.add(buttonExit, "cell 2 0");
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addComponent(panelNotes, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(panelCountdown, GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addComponent(panelButtonArea, GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                                .addComponent(labelSettings)
                                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(checkBoxBlackCountdown)
                                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(checkBoxWhiteCountdown))
                                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                                .addComponent(labelCountdownTime)
                                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(spinnerCountdownTime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(labelSeconds)))
                                                                .addGap(0, 195, Short.MAX_VALUE)))
                                                .addContainerGap())))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addComponent(panelNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(labelSettings)
                                        .addComponent(checkBoxBlackCountdown)
                                        .addComponent(checkBoxWhiteCountdown))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(labelCountdownTime)
                                        .addComponent(spinnerCountdownTime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(labelSeconds))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(panelCountdown, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(panelButtonArea, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
}
