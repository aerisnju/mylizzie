package wagner.stephanie.lizzie.gui;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.analysis.DetailedScoreEstimator;
import wagner.stephanie.lizzie.rules.Board;

import javax.swing.*;
import java.awt.event.*;
import java.util.ResourceBundle;

public class Input implements MouseListener, KeyListener, MouseWheelListener, MouseMotionListener {
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("wagner.stephanie.lizzie.i18n.GuiBundle");
    private static final ImmutableMap<String, String> COLOR_DISPLAY_STRING = ImmutableMap.of(
            "B", resourceBundle.getString("LizzieFrame.prompt.colorBlack")
            , "b", resourceBundle.getString("LizzieFrame.prompt.colorBlack")
            , "W", resourceBundle.getString("LizzieFrame.prompt.colorWhite")
            , "w", resourceBundle.getString("LizzieFrame.prompt.colorWhite")
    );

    private ByoYomiAutoPlayDialog byoYomiAutoPlayDialog = null;

    public Input() {
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Lizzie.analysisFrame.getAnalysisTableModel().setSelectedMove(null);

        int x = e.getX();
        int y = e.getY();

        if (e.getButton() == MouseEvent.BUTTON3) { // right mouse click
            Lizzie.board.previousMove(); // interpret as undo
        } else if (e.getButton() == MouseEvent.BUTTON1) { // left mouse click
            if (e.getClickCount() == 2) {
                Lizzie.frame.onDoubleClicked(x, y);
            } else {
                Lizzie.frame.onClicked(x, y);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0 || (e.getModifiers() & KeyEvent.META_MASK) != 0) && KeyEvent.VK_0 <= e.getKeyCode() && e.getKeyCode() <= KeyEvent.VK_9) {
            if (e.getKeyCode() == KeyEvent.VK_0) {
                Lizzie.switchEngineBySetting();
            } else {
                int profileIndex = e.getKeyCode() - KeyEvent.VK_1;
                Lizzie.switchEngineByProfileIndex(profileIndex);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_O && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0 || (e.getModifiers() & KeyEvent.META_MASK) != 0)
                || e.getKeyCode() == KeyEvent.VK_R) {
            Lizzie.board.leaveTryPlayState();
            Lizzie.loadGameByPrompting();
        } else if (e.getKeyCode() == KeyEvent.VK_S && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0 || (e.getModifiers() & KeyEvent.META_MASK) != 0)
                || e.getKeyCode() == KeyEvent.VK_W) {
            Lizzie.board.leaveTryPlayState();
            Lizzie.storeGameByPrompting();
        } else if (e.getKeyCode() == KeyEvent.VK_C && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0 || (e.getModifiers() & KeyEvent.META_MASK) != 0)) {
            Lizzie.board.leaveTryPlayState();
            Lizzie.copyGameToClipboardInSgf();
        } else if (e.getKeyCode() == KeyEvent.VK_V && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0 || (e.getModifiers() & KeyEvent.META_MASK) != 0)) {
            Lizzie.board.leaveTryPlayState();
            Lizzie.pasteGameFromClipboardInSgf();
        } else if (e.getKeyCode() == KeyEvent.VK_C && (e.getModifiers() & KeyEvent.ALT_MASK) != 0) {
            Lizzie.board.leaveTryPlayState();
            Lizzie.clearBoardAndState();
        } else if (e.getKeyCode() == KeyEvent.VK_C) {
            Lizzie.promptForChangeExistingMove();
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            Lizzie.board.nextMove();
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            Lizzie.board.previousMove();
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (Lizzie.optionSetting.isAutoStartAnalyzingAfterPlacingMoves()) {
                Lizzie.leelaz.togglePonder();
            } else {
                Lizzie.leelaz.toggleThinking();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_P) {
            Lizzie.board.pass();
        } else if (e.getKeyCode() == KeyEvent.VK_N) {
            Lizzie.frame.toggleShowMoveNumber();
        } else if (e.getKeyCode() == KeyEvent.VK_O) {
            Lizzie.optionDialog.setDialogSetting(Lizzie.optionSetting);
            Lizzie.optionDialog.setVisible(true);
        } else if (e.getKeyCode() == KeyEvent.VK_G) {
            promptForGotoMove();
        } else if (e.getKeyCode() == KeyEvent.VK_V) {
            if (Lizzie.board.isInTryPlayState()) {
                Lizzie.board.leaveTryPlayState();
            } else {
                Lizzie.board.enterTryPlayState();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_X) {
            Lizzie.board.leaveTryPlayState();
            Lizzie.board.dropSuccessiveMoves();
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            Lizzie.optionSetting.setAnalysisWindowShow(!Lizzie.optionSetting.isAnalysisWindowShow());
            Lizzie.analysisDialog.setVisible(Lizzie.optionSetting.isAnalysisWindowShow());
        } else if (e.getKeyCode() == KeyEvent.VK_H) {
            Lizzie.optionSetting.setWinrateHistogramWindowShow(!Lizzie.optionSetting.isWinrateHistogramWindowShow());
            Lizzie.winrateHistogramDialog.setVisible(Lizzie.optionSetting.isWinrateHistogramWindowShow());
        } else if (e.getKeyCode() == KeyEvent.VK_F1) {
            if (!Lizzie.frame.showControls) {
                Lizzie.frame.showControls = true;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_HOME) {
            Lizzie.board.gotoMove(0);
        } else if (e.getKeyCode() == KeyEvent.VK_END) {
            Lizzie.board.gotoMove(Integer.MAX_VALUE);
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            Lizzie.board.playBestMove();
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            if (Lizzie.board.getData().isBlackToPlay()) {
                Lizzie.optionSetting.setShowBlackSuggestion(!Lizzie.optionSetting.isShowBlackSuggestion());
            } else {
                Lizzie.optionSetting.setShowWhiteSuggestion(!Lizzie.optionSetting.isShowWhiteSuggestion());
            }
        } else if (e.getKeyCode() == KeyEvent.VK_E) {
            Lizzie.optionSetting.setGtpConsoleWindowShow(!Lizzie.optionSetting.isGtpConsoleWindowShow());
            Lizzie.gtpConsole.setVisible(Lizzie.optionSetting.isGtpConsoleWindowShow());
        } else if (e.getKeyCode() == KeyEvent.VK_B) {
            if (byoYomiAutoPlayDialog == null || !byoYomiAutoPlayDialog.isDisplayable()) {
                byoYomiAutoPlayDialog = new ByoYomiAutoPlayDialog(Lizzie.frame);
                byoYomiAutoPlayDialog.setVisible(true);
            } else {
                byoYomiAutoPlayDialog.dispatchEvent(new WindowEvent(byoYomiAutoPlayDialog, WindowEvent.WINDOW_CLOSING));
                byoYomiAutoPlayDialog = null;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_T) {
            scoreGame();
        }

        if (e.getKeyCode() != KeyEvent.VK_T
                && e.getKeyCode() != KeyEvent.VK_O
                && e.getKeyCode() != KeyEvent.VK_W
                && e.getKeyCode() != KeyEvent.VK_S
                && e.getKeyCode() != KeyEvent.VK_R) {
            Lizzie.frame.getBoardRenderer().updateInfluences(null);
        }

        Lizzie.frame.repaint();
    }

    public void scoreGame() {
        if (Lizzie.scoreEstimator == null || !Lizzie.scoreEstimator.isRunning()) {
            JOptionPane.showMessageDialog(Lizzie.frame, resourceBundle.getString("LizzieFrame.prompt.noEstimatorEngine"), "Lizzie", JOptionPane.ERROR_MESSAGE);
        } else {
            Lizzie.frame.getBoardRenderer().updateInfluences(Lizzie.scoreEstimator.estimateInfluences());

            try {
                DetailedScoreEstimator detailedScoreEstimator = (DetailedScoreEstimator) Lizzie.scoreEstimator;
                DetailedScoreEstimator.DetailedScore detailedScore = detailedScoreEstimator.estimateDetailedScore();

                String colorDescription = COLOR_DISPLAY_STRING.getOrDefault(detailedScore.getScore() > 0 ? "B" : "W", "?");
                double absoluteScore = Math.abs(detailedScore.getScore());

                String detailedScoreDescription = String.format(
                        resourceBundle.getString("LizzieFrame.prompt.detailedScoreEstimation")
                        , Lizzie.scoreEstimator.getEstimatorName(), Board.BOARD_SIZE == 19 ? 7.5 : 6.5, colorDescription, absoluteScore
                        , detailedScore.getBlackTerritoryCount()
                        , detailedScore.getWhiteTerritoryCount()
                        , detailedScore.getBlackDeadCount()
                        , detailedScore.getWhiteDeadCount()
                        , detailedScore.getBlackPrisonerCount()
                        , detailedScore.getWhitePrisonerCount()
                );
                JOptionPane.showMessageDialog(Lizzie.frame
                        , detailedScoreDescription
                        , "Lizzie"
                        , JOptionPane.INFORMATION_MESSAGE);
            } catch (ClassCastException e) {
                ImmutablePair<String, Double> estimatedScore = Lizzie.scoreEstimator.estimateScore();
                String colorDescription = COLOR_DISPLAY_STRING.getOrDefault(estimatedScore.getLeft(), "?");
                double score = estimatedScore.getRight();
                JOptionPane.showMessageDialog(Lizzie.frame
                        , String.format(resourceBundle.getString("LizzieFrame.prompt.scoreEstimation"), Lizzie.scoreEstimator.getEstimatorName(), Board.BOARD_SIZE == 19 ? 7.5 : 6.5, colorDescription, score)
                        , "Lizzie"
                        , JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    void promptForGotoMove() {
        String inputMoveNumberString = JOptionPane.showInputDialog(Lizzie.frame
                , resourceBundle.getString("LizzieFrame.prompt.gotoDialogMessage"), "Lizzie", JOptionPane.QUESTION_MESSAGE);
        if (inputMoveNumberString != null && !(inputMoveNumberString = inputMoveNumberString.trim()).isEmpty()) {
            try {
                int moveNumber = Integer.parseInt(inputMoveNumberString);
                if (inputMoveNumberString.startsWith("+") || inputMoveNumberString.startsWith("-")) {
                    Lizzie.board.gotoMoveByDiff(moveNumber);
                } else {
                    // Cannot be minus number
                    Lizzie.board.gotoMove(moveNumber);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(Lizzie.frame, "Number format error.", "Lizzie", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_F1) {
            Lizzie.frame.showControls = false;
            Lizzie.frame.repaint();
        }
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        Lizzie.miscExecutor.execute(() -> {
            if (e.getWheelRotation() > 0) {
                Lizzie.board.nextMove();
            } else if (e.getWheelRotation() < 0) {
                Lizzie.board.previousMove();
            }

            Lizzie.frame.repaint();
        });
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        Lizzie.frame.onMouseMove(x, y);
    }
}
