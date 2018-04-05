package wagner.stephanie.lizzie.gui;

import wagner.stephanie.lizzie.Lizzie;

import javax.swing.*;
import java.awt.event.*;

public class Input implements MouseListener, KeyListener, MouseWheelListener, MouseMotionListener {
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
        if (e.getKeyCode() == KeyEvent.VK_O && (e.getModifiers() & KeyEvent.CTRL_MASK) != 0
                || e.getKeyCode() == KeyEvent.VK_R) {
            Lizzie.board.leaveTryPlayState();
            Lizzie.loadGameByPrompting();
        } else if (e.getKeyCode() == KeyEvent.VK_S && (e.getModifiers() & KeyEvent.CTRL_MASK) != 0
                || e.getKeyCode() == KeyEvent.VK_W) {
            Lizzie.board.leaveTryPlayState();
            Lizzie.storeGameByPrompting();
        } if (e.getKeyCode() == KeyEvent.VK_C && (e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
            Lizzie.copyGameToClipboardInSgf();
        } if (e.getKeyCode() == KeyEvent.VK_V && (e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
            Lizzie.pasteGameFromClipboardInSgf();
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            Lizzie.board.nextMove();
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            Lizzie.board.previousMove();
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            Lizzie.leelaz.togglePonder();
        } else if (e.getKeyCode() == KeyEvent.VK_P) {
            Lizzie.board.pass();
        } else if (e.getKeyCode() == KeyEvent.VK_N) {
            Lizzie.frame.toggleShowMoveNumber();
        } else if (e.getKeyCode() == KeyEvent.VK_O) {
            Lizzie.optionDialog.setDialogSetting(Lizzie.optionSetting);
            Lizzie.optionDialog.setVisible(true);
        } else if (e.getKeyCode() == KeyEvent.VK_C && (e.getModifiers() & KeyEvent.ALT_MASK) != 0) {
            Lizzie.board.leaveTryPlayState();
            Lizzie.clearBoardAndState();
        } else if (e.getKeyCode() == KeyEvent.VK_G) {
            String inputMoveNumberString = JOptionPane.showInputDialog(Lizzie.frame
                    , "Enter move number you want to go:\ne.g. 78 - Jump to move No. 78\n-15 - move backward 15 steps\n+15 move forward 15 steps.", "Lizzie", JOptionPane.QUESTION_MESSAGE);
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
        }

        Lizzie.frame.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_F1) {
            Lizzie.frame.showControls = false;
            Lizzie.frame.repaint();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() > 0) {
            Lizzie.board.nextMove();
        } else if (e.getWheelRotation() < 0) {
            Lizzie.board.previousMove();
        }

        Lizzie.frame.repaint();
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
