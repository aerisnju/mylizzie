package wagner.stephanie.lizzie;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.toomasr.sgf4j.Sgf;
import com.toomasr.sgf4j.SgfParseException;
import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;
import com.toomasr.sgf4j.parser.Util;
import org.apache.commons.collections4.CollectionUtils;
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
import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Main class.
 */
public class Lizzie {
    public static final String SETTING_FILE = "mylizzie.json";
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static LizzieFrame frame;
    public static JDialog analysisDialog;
    public static AnalysisFrame analysisFrame;
    public static Leelaz leelaz;
    public static Board board;
    public static OptionDialog optionDialog;
    public static OptionSetting optionSetting;

    static {
        readSettingFile();
        // Sometimes gson will fail to parse the file
        if (optionSetting == null || optionSetting.getBoardColor() == null) {
            optionSetting = new OptionSetting();
        }
    }

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
        optionDialog.setDialogSetting(optionSetting);

        setGuiPosition();

        analysisDialog.setVisible(optionSetting.isAnalysisModeOn());
    }

    public static void clearBoardAndState() {
        leelaz.clearBoard();
        board = new Board();
    }

    public static void loadGameByPrompting() {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.sgf", "SGF");
        JFileChooser chooser = new JFileChooser(optionSetting.getLastChooserLocation());
        chooser.setFileFilter(filter);
        chooser.setMultiSelectionEnabled(false);
        int state = chooser.showOpenDialog(frame);
        if (state == JFileChooser.APPROVE_OPTION) {
            optionSetting.setLastChooserLocation(chooser.getSelectedFile().toPath().getParent().toString());
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

            // Process pre-placed stones
            String prePlacedBlackStoneString = game.getProperty("AB");
            String prePlacedWhiteStoneString = game.getProperty("AW");
            List<int[]> prePlacedBlackStones = Collections.emptyList(), prePlacedWhiteStones = Collections.emptyList();
            if (StringUtils.isNotEmpty(prePlacedBlackStoneString)) {
                prePlacedBlackStones = Arrays.stream(prePlacedBlackStoneString.split(","))
                        .map(String::trim)
                        .map(Util::alphaToCoords)
                        .collect(Collectors.toList());
            }
            if (StringUtils.isNotEmpty(prePlacedWhiteStoneString)) {
                prePlacedWhiteStones = Arrays.stream(prePlacedBlackStoneString.split(","))
                        .map(String::trim)
                        .map(Util::alphaToCoords)
                        .collect(Collectors.toList());
            }

            if (CollectionUtils.isNotEmpty(prePlacedBlackStones) || CollectionUtils.isNotEmpty(prePlacedWhiteStones)) {
                int maxLength = Math.max(prePlacedBlackStones.size(), prePlacedWhiteStones.size());
                for (int i = 0; i < maxLength; ++i) {
                    if (i < prePlacedBlackStones.size()) {
                        Lizzie.board.place(prePlacedBlackStones.get(i)[0], prePlacedBlackStones.get(i)[1]);
                    } else {
                        Lizzie.board.pass();
                    }
                    if (i < prePlacedWhiteStones.size()) {
                        Lizzie.board.place(prePlacedWhiteStones.get(i)[0], prePlacedWhiteStones.get(i)[1]);
                    } else {
                        Lizzie.board.pass();
                    }
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
            optionSetting.setLastChooserLocation(file.getParent());
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

    public static void readGuiPosition() {
        readMainFramePosition();
        readAnalysisWindowPosition();
    }

    public static void readAnalysisWindowPosition() {
        optionSetting.setAnalysisWindowPosX(analysisDialog.getX());
        optionSetting.setAnalysisWindowPosY(analysisDialog.getY());
        optionSetting.setAnalysisWindowWidth(analysisDialog.getWidth());
        optionSetting.setAnalysisWindowHeight(analysisDialog.getHeight());
    }

    public static void readMainFramePosition() {
        optionSetting.setMainWindowPosX(frame.getX());
        optionSetting.setMainWindowPosY(frame.getY());
        optionSetting.setMainWindowWidth(frame.getWidth());
        optionSetting.setMainWindowHeight(frame.getHeight());
    }

    public static void setGuiPosition() {
        setMainWindowPositino();
        setAnalysisWindowPosition();
    }

    public static void setAnalysisWindowPosition() {
        if (optionSetting.getAnalysisWindowPosX() >= 10 && optionSetting.getAnalysisWindowPosY() >= 10) {
            analysisDialog.setLocation(optionSetting.getAnalysisWindowPosX(), optionSetting.getAnalysisWindowPosY());
        }
        if (optionSetting.getAnalysisWindowWidth() >= 10 && optionSetting.getAnalysisWindowHeight() >= 10) {
            analysisDialog.setSize(optionSetting.getAnalysisWindowWidth(), optionSetting.getAnalysisWindowHeight());
        }
    }

    public static void setMainWindowPositino() {
        if (optionSetting.getMainWindowPosX() >= 10 && optionSetting.getMainWindowPosY() >= 10) {
            frame.setLocation(optionSetting.getMainWindowPosX(), optionSetting.getMainWindowPosY());
        }
        if (optionSetting.getMainWindowWidth() >= 10 && optionSetting.getMainWindowHeight() >= 10) {
            frame.setSize(optionSetting.getMainWindowWidth(), optionSetting.getMainWindowHeight());
        }
    }

    public static void readSettingFile() {
        try (Reader reader = new FileReader(SETTING_FILE)) {
            optionSetting = gson.fromJson(reader, OptionSetting.class);
        } catch (FileNotFoundException e) {
            // Do nothing
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeSettingFile() {
        try (Writer writer = new FileWriter(SETTING_FILE)) {
            writer.write(gson.toJson(optionSetting));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
