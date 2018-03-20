package wagner.stephanie.lizzie;

import com.toomasr.sgf4j.Sgf;
import com.toomasr.sgf4j.SgfParseException;
import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;
import com.toomasr.sgf4j.parser.Util;
import org.apache.commons.lang3.StringUtils;
import wagner.stephanie.lizzie.analysis.Leelaz;
import wagner.stephanie.lizzie.gui.AnalysisFrame;
import wagner.stephanie.lizzie.gui.LizzieFrame;
import wagner.stephanie.lizzie.gui.OptionDialog;
import wagner.stephanie.lizzie.gui.OptionSetting;
import wagner.stephanie.lizzie.rules.Board;
import wagner.stephanie.lizzie.rules.BoardHistoryList;
import wagner.stephanie.lizzie.rules.BoardHistoryNode;
import wagner.stephanie.lizzie.rules.Stone;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.sgf", "SGF");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(filter);
        chooser.setMultiSelectionEnabled(false);
        int state = chooser.showOpenDialog(frame);
        if (state == JFileChooser.APPROVE_OPTION) {
            loadGameByFile(chooser.getSelectedFile().toPath());
        }
    }

    public static void loadGameByFile(Path gameFilePath) {
        try {
            Game game = Sgf.createFromPath(gameFilePath);
            GameNode node = game.getRootNode();

            if (game.getProperty("GM") != null && !Objects.equals(game.getProperty("GM"), "1")) {
                JOptionPane.showMessageDialog(frame, "Error: Not a go game.", "Lizzie", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (game.getProperty("MULTIGOGM") != null && !Objects.equals(game.getProperty("MULTIGOGM"), "1")) {
                JOptionPane.showMessageDialog(frame, "Error: Not a go game.", "Lizzie", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (game.getProperty("SZ") != null && !Objects.equals(game.getProperty("SZ"), "19")) {
                JOptionPane.showMessageDialog(frame, "Error: Board size is not 19x19.", "Lizzie", JOptionPane.ERROR_MESSAGE);
                return;
            }

            clearBoardAndState();

            // Process handicap
            String handicap = game.getProperty("HA");
            if (handicap != null) {
                String preStoneString = game.getProperty("AB");
                if (preStoneString != null) {
                    List<int []> preStones = Arrays.stream(preStoneString.split(","))
                            .map(String::trim)
                            .map(Util::alphaToCoords)
                            .collect(Collectors.toList());

                    for (int[] preStone : preStones) {
                        Lizzie.board.place(preStone[0], preStone[1]);
                        Lizzie.board.pass();
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Warning: Does not know handicap stones.", "Lizzie", JOptionPane.WARNING_MESSAGE);
                }
            }

            do {
                if (node.getMoveNo() < 0 || StringUtils.isEmpty(node.getMoveString())) {
                    continue;
                }
                int[] coords = node.getCoords();
                if (coords[0] >= 19 || coords[0] < 0 || coords[1] >= 19 || coords[1] < 0) {
                    Lizzie.board.pass();
                } else {
                    Lizzie.board.place(coords[0], coords[1]);
                }
            }
            while ((node = node.getNextNode()) != null);
        } catch (SgfParseException e) {
            JOptionPane.showMessageDialog(frame, "Error: cannot load sgf: " + e.getMessage(), "Lizzie", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void storeGameByPrompting() {
        Game game = new Game();

        game.addProperty("FF", "4"); // SGF version: 4
        game.addProperty("KM", "7.5"); // Lz only support fixed komi
        game.addProperty("GM", "1"); // Go game
        game.addProperty("SZ", "19");
        game.addProperty("CA", "UTF-8");

        BoardHistoryList historyList = board.getHistory();
        BoardHistoryNode initialNode = historyList.getInitialNode();

        GameNode previousNode = null;
        for (BoardHistoryNode p = initialNode; p != null; p = p.next()) {
            GameNode gameNode = new GameNode(previousNode);

            if (previousNode == null) {
                game.setRootNode(gameNode);
            }

            // Move node
            if (Objects.equals(p.getData().lastMoveColor, Stone.BLACK) || Objects.equals(p.getData().lastMoveColor, Stone.WHITE)) {
                int x, y;

                if (p.getData().lastMove == null) {
                    // Pass
                    x = 19;
                    y = 19;
                } else {
                    x = p.getData().lastMove[0];
                    y = p.getData().lastMove[1];

                    if (x < 0 || x >= 19 || y < 0 || y >= 19) {
                        x = 19;
                        y = 19;
                    }
                }

                String moveKey = Objects.equals(p.getData().lastMoveColor, Stone.BLACK) ? "B" : "W";
                String moveValue = Util.coordToAlpha.get(x) + Util.coordToAlpha.get(y);

                gameNode.addProperty(moveKey, moveValue);
            }

            if (p.getData().moveNumber > 0) {
                gameNode.setMoveNo(p.getData().moveNumber);
            }

            if (previousNode != null) {
                previousNode.addChild(gameNode);
            }

            previousNode = gameNode;
        }

        game.postProcess();

        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.sgf", "SGF");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(filter);
        chooser.setMultiSelectionEnabled(false);
        int result = chooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file.exists()) {
                int ret = JOptionPane.showConfirmDialog(frame, "The SGF file is exists, do you want replace it?", "Warning", JOptionPane.OK_CANCEL_OPTION);
                if (ret == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
            if (!file.getPath().toLowerCase().endsWith(".sgf")) {
                file = new File(file.getPath() + ".sgf");
            }
            try {
                Sgf.writeToFile(game, file.toPath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Error: cannot save sgf: " + e.getMessage(), "Lizzie", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
