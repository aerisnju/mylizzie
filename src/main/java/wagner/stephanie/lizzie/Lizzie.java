package wagner.stephanie.lizzie;

import wagner.stephanie.lizzie.analysis.Leelaz;
import wagner.stephanie.lizzie.gui.AnalysisFrame;
import wagner.stephanie.lizzie.gui.LizzieFrame;
import wagner.stephanie.lizzie.gui.OptionDialog;
import wagner.stephanie.lizzie.gui.OptionSetting;
import wagner.stephanie.lizzie.rules.Board;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.io.IOException;

/**
 * Main class.
 */
public class Lizzie {
    public static LizzieFrame frame;
    public static JDialog analysisDialog;
    public static AnalysisFrame analysisFrame;
    public static Leelaz leelaz;
    public static Board board;
    public static OptionDialog optionDialog;
    public static OptionSetting optionSetting = new OptionSetting();

    /**
     * Launches the game window, and runs the game.
     */
    public static void main(String[] args) throws IOException {
        // Use Nimbus look and feel which looks better
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }

        leelaz = new Leelaz();
        leelaz.ponder();

        board = new Board();
        frame = new LizzieFrame();

        analysisDialog = AnalysisFrame.createAnalysisDialog(frame);
        analysisFrame = (AnalysisFrame) analysisDialog.getContentPane();

        optionDialog = new OptionDialog(frame);
        optionSetting = optionDialog.readDialogSetting();

        analysisDialog.setVisible(optionSetting.isAnalysisModeOn());
    }
}
