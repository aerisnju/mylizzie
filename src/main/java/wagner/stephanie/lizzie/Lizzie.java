package wagner.stephanie.lizzie;

import com.toomasr.sgf4j.Sgf;
import com.toomasr.sgf4j.SgfParseException;
import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;
import org.apache.commons.lang3.StringUtils;
import wagner.stephanie.lizzie.analysis.Leelaz;
import wagner.stephanie.lizzie.gui.AnalysisFrame;
import wagner.stephanie.lizzie.gui.LizzieFrame;
import wagner.stephanie.lizzie.gui.OptionDialog;
import wagner.stephanie.lizzie.gui.OptionSetting;
import wagner.stephanie.lizzie.rules.Board;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.io.IOException;
import java.nio.file.Path;

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

    public static void clearBoardAndState() {
        leelaz.clearBoard();
        board = new Board();
    }

    public static void loadGameByPrompting() {
        JFileChooser chooser = new JFileChooser();
        int state = chooser.showOpenDialog(frame);
        if (state == JFileChooser.APPROVE_OPTION) {
            loadGameByFile(chooser.getSelectedFile().toPath());
        }
    }

    public static void loadGameByFile(Path gameFilePath) {
        try {
            Game game = Sgf.createFromPath(gameFilePath);
            GameNode node = game.getRootNode();

            clearBoardAndState();
            do {
                if (node.getMoveNo() < 0 || StringUtils.isEmpty(node.getMoveString())) {
                    continue;
                }
                int[] coords = node.getCoords();
                if (coords[0] >= 19 || coords[0] < 0 || coords[1] >= 19 || coords[1] < 0) {
                    System.out.printf("%s: Pass\n", node.getColor());
                    Lizzie.board.pass();
                } else {
                    System.out.printf("%s: %d %d\n", node.getColor(), coords[0], coords[1]);
                    Lizzie.board.place(coords[0], coords[1]);
                }
            }
            while ((node = node.getNextNode()) != null);
        } catch (SgfParseException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error: cannot load sgf.", "Lizzie", JOptionPane.ERROR_MESSAGE);
        }
    }
}
