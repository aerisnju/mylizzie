package wagner.stephanie.lizzie;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.toomasr.sgf4j.Sgf;
import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;
import com.toomasr.sgf4j.parser.Util;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import wagner.stephanie.lizzie.analysis.Leelaz;
import wagner.stephanie.lizzie.gui.*;
import wagner.stephanie.lizzie.rules.*;
import wagner.stephanie.lizzie.util.ThreadPoolUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Main class.
 */
public class Lizzie {
    private static final Logger logger = LogManager.getLogger(Lizzie.class);
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("wagner.stephanie.lizzie.i18n.GuiBundle");

    public static final String SETTING_FILE = "mylizzie.json";
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static LizzieFrame frame;
    public static JDialog analysisDialog;
    public static AnalysisFrame analysisFrame;
    public static Leelaz leelaz;
    public static Board board;
    public static OptionDialog optionDialog;
    public static OptionSetting optionSetting;
    public static WinrateHistogramDialog winrateHistogramDialog;
    public static ExecutorService miscExecutor = Executors.newSingleThreadExecutor();

    static {
        readSettingFile();
        // Sometimes gson will fail to parse the file
        if (optionSetting == null
                || optionSetting.getBoardColor() == null
                || StringUtils.isEmpty(optionSetting.getLeelazCommandLine())) {
            optionSetting = new OptionSetting();
        }
    }

    /**
     * Launches the game window, and runs the game.
     */
    public static void main(String[] args) throws IOException {
        // Cleanup for misc executor
        Runtime.getRuntime().addShutdownHook(new Thread(() -> ThreadPoolUtil.shutdownAndAwaitTermination(Lizzie.miscExecutor)));

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

        leelaz = new Leelaz(optionSetting.getLeelazCommandLine());
        leelaz.ponder();

        board = new Board();
        frame = new LizzieFrame();

        analysisDialog = AnalysisFrame.createAnalysisDialog(frame);
        analysisFrame = (AnalysisFrame) analysisDialog.getContentPane();

        optionDialog = new OptionDialog(frame);
        optionDialog.setDialogSetting(optionSetting);

        winrateHistogramDialog = new WinrateHistogramDialog(frame);

        setGuiPosition();

        analysisDialog.setVisible(optionSetting.isAnalysisWindowShow());
        winrateHistogramDialog.setVisible(optionSetting.isWinrateHistogramWindowShow());
    }

    public static void clearBoardAndState() {
        board.clear();
        leelaz.clearBoard();
    }

    public static void loadGameByPrompting() {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.sgf", "SGF");
        final JFileChooser chooser = new JFileChooser(optionSetting.getLastChooserLocation());
        chooser.addChoosableFileFilter(filter);
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(false);

        setFileChooserAutoFocusOnTextField(chooser);

        int state = chooser.showOpenDialog(frame);
        if (state == JFileChooser.APPROVE_OPTION) {
            optionSetting.setLastChooserLocation(chooser.getSelectedFile().toPath().getParent().toString());

            File file = chooser.getSelectedFile();
            if (!file.getPath().toLowerCase().endsWith(".sgf")) {
                file = new File(file.getPath() + ".sgf");
            }

            loadGameByFile(file.toPath());
        }
    }

    private static void setFileChooserAutoFocusOnTextField(JFileChooser chooser) {
        chooser.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent he) {
                grabFocusForTextField(chooser.getComponents());
            }

            // Loop to find the JTextField, the first
            // JTextField in JFileChooser
            // Even if you setAccessory which contains a JTextField
            // or which is JTextField itself, it will not get focus
            private void grabFocusForTextField(Component[] components) {
                for (Component component : components) {
                    if (component instanceof JTextField) {
                        JTextField textField = (JTextField) component;
                        textField.grabFocus();
                        break;
                    } else if (component instanceof JPanel) {
                        JPanel panel = (JPanel) component;
                        grabFocusForTextField(panel.getComponents());
                    }
                }
            }
        });
    }

    public static void copyGameToClipboardInSgf() {
        try {
            Game game = snapshotCurrentGame();
            String sgfContent = writeSgfToString(game);

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferableString = new StringSelection(sgfContent);
            clipboard.setContents(transferableString, null);
        } catch (Exception e) {
            logger.error("Error in copying game to clipboard.");
        }
    }

    public static void pasteGameFromClipboardInSgf() {
        try {
            String sgfContent = null;
            // Read from clipboard
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable clipboardContents = clipboard.getContents(null);
            if (clipboardContents != null) {
                if (clipboardContents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    sgfContent = (String) clipboardContents.getTransferData(DataFlavor.stringFlavor);
                }
            }

            if (StringUtils.isNotEmpty(sgfContent)) {
                Game game = Sgf.createFromString(sgfContent);
                loadGameToBoard(game);
            }
        } catch (Exception e) {
            logger.error("Error in copying game from clipboard.");
        }
    }

    public static void promptForChangeExistingMove() {
        ChangeMoveDialog changeMoveDialog = new ChangeMoveDialog(frame);
        changeMoveDialog.setVisible(true);
        if (changeMoveDialog.isUserApproved()) {
            int moveNumber = changeMoveDialog.getMoveNumber();
            String correctedMove = changeMoveDialog.getCorrectedMove();
            int[] convertedCoords = Board.convertDisplayNameToCoordinates(correctedMove);
            if (Board.isValid(convertedCoords)) {
                Lizzie.miscExecutor.execute(() -> board.changeMove(moveNumber, convertedCoords));
            } else {
                JOptionPane.showMessageDialog(frame, resourceBundle.getString("Lizzie.prompt.invalidCoordinates"), "Lizzie", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void switchEngineByProfileIndex(int profileIndex) {
        String newCommandLine = CollectionUtils.isEmpty(Lizzie.optionSetting.getEngineProfileList()) ? "" : Lizzie.optionSetting.getEngineProfileList().get(profileIndex);
        if (StringUtils.isNotEmpty(newCommandLine)) {
            Lizzie.optionSetting.setLeelazCommandLine(newCommandLine);

            switchEngineBySetting();
        }
    }

    public static void switchEngineBySetting() {
        try {
            int moveNumber = board.getData().getMoveNumber();
            leelaz.restartEngine(Lizzie.optionSetting.getLeelazCommandLine());
            board.gotoMove(0);
            board.gotoMove(moveNumber);
            SwingUtilities.invokeLater(() -> frame.setEngineProfile(Lizzie.optionSetting.getLeelazCommandLine()));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class MoveReplayer {
        private boolean nextIsBlack;

        public MoveReplayer() {
            nextIsBlack = true;
        }

        public void playMove(boolean isBlack, int x, int y) {
            if (nextIsBlack == isBlack) {
                Lizzie.board.place(x, y);
                nextIsBlack = !nextIsBlack;
            } else {
                Lizzie.board.pass();
                Lizzie.board.place(x, y);
            }
        }
    }

    public static void loadGameByFile(Path gameFilePath) {
        try {
            Game game = Sgf.createFromPath(gameFilePath);
            loadGameToBoard(game);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: cannot load sgf: " + e.getMessage(), "Lizzie", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void loadGameToBoard(Game game) {
        clearBoardAndState();

        GameNode node = game.getRootNode();
        MoveReplayer replayer = new MoveReplayer();

        // Process pre-placed stones
        placePreplacedMove(replayer, game.getProperty("AB"), game.getProperty("AW"));

        do {
            String preplacedBlack = node.getProperty("AB");
            String preplacedWhite = node.getProperty("AW");
            if (StringUtils.isNotEmpty(preplacedBlack) || StringUtils.isNotEmpty(preplacedWhite)) {
                placePreplacedMove(replayer, preplacedBlack, preplacedWhite);
            }
            if (node.isMove()) {
                if (StringUtils.isNotEmpty(node.getProperty("B"))) {
                    int[] coords = node.getCoords();
                    if (coords != null && coords[0] < 19 && coords[0] >= 0 && coords[1] < 19 && coords[1] >= 0) {
                        replayer.playMove(true, coords[0], coords[1]);
                    }
                }
                if (StringUtils.isNotEmpty(node.getProperty("W"))) {
                    int[] coords = node.getCoords();
                    if (coords != null && coords[0] < 19 && coords[0] >= 0 && coords[1] < 19 && coords[1] >= 0) {
                        replayer.playMove(false, coords[0], coords[1]);
                    }
                }
            }
        }
        while ((node = node.getNextNode()) != null);
    }

    private static void placePreplacedMove(MoveReplayer replayer, String preplacedBlackStoneString, String preplacedWhiteStoneString) {
        List<int[]> preplacedBlackStones = Collections.emptyList(), preplacedWhiteStones = Collections.emptyList();
        if (StringUtils.isNotEmpty(preplacedBlackStoneString)) {
            preplacedBlackStones = Arrays.stream(preplacedBlackStoneString.split(","))
                    .map(String::trim)
                    .map(Util::alphaToCoords)
                    .collect(Collectors.toList());
        }
        if (StringUtils.isNotEmpty(preplacedWhiteStoneString)) {
            preplacedWhiteStones = Arrays.stream(preplacedWhiteStoneString.split(","))
                    .map(String::trim)
                    .map(Util::alphaToCoords)
                    .collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(preplacedBlackStones) || CollectionUtils.isNotEmpty(preplacedWhiteStones)) {
            int maxLength = Math.max(preplacedBlackStones.size(), preplacedWhiteStones.size());
            for (int i = 0; i < maxLength; ++i) {
                if (i < preplacedBlackStones.size()) {
                    replayer.playMove(true, preplacedBlackStones.get(i)[0], preplacedBlackStones.get(i)[1]);
                }
                if (i < preplacedWhiteStones.size()) {
                    replayer.playMove(false, preplacedWhiteStones.get(i)[0], preplacedWhiteStones.get(i)[1]);
                }
            }
        }
    }

    public static void storeGameByFile(Path filePath) {
        try {
            Game game = snapshotCurrentGame();
            writeSgfToFile(game, filePath);
        } catch (Exception e) {
            if (StringUtils.isEmpty(e.getMessage())) {
                JOptionPane.showMessageDialog(frame, "Error: cannot save sgf: " + e.getMessage(), "Lizzie", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Error: cannot save sgf", "Lizzie", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @NotNull
    private static Game snapshotCurrentGame() {
        final int BOARD_SIZE = Lizzie.optionSetting.getBoardSize().getWidth();
        Game game = new Game();

        game.addProperty("FF", "4"); // SGF version: 4
        game.addProperty("KM", "7.5"); // Lz only support fixed komi
        game.addProperty("GM", "1"); // Go game
        game.addProperty("SZ", String.valueOf(BOARD_SIZE));
        game.addProperty("CA", "UTF-8");
        game.addProperty("AP", "MyLizzie");

        BoardHistoryList historyList = board.getHistory();
        BoardHistoryNode initialNode = historyList.getInitialNode();

        GameNode previousSgfNode = null;
        BoardHistoryNode previousNode = null;
        for (BoardHistoryNode p = initialNode.getNext(); p != null; p = p.getNext()) {
            GameNode gameNode = new GameNode(previousSgfNode);

            // Move node
            if (Objects.equals(p.getData().getLastMoveColor(), Stone.BLACK) || Objects.equals(p.getData().getLastMoveColor(), Stone.WHITE)) {
                int x, y;

                if (p.getData().getLastMove() == null) {
                    // Pass
                    x = BOARD_SIZE;
                    y = BOARD_SIZE;
                } else {
                    x = p.getData().getLastMove()[0];
                    y = p.getData().getLastMove()[1];

                    if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
                        x = BOARD_SIZE;
                        y = BOARD_SIZE;
                    }
                }

                String moveKey = Objects.equals(p.getData().getLastMoveColor(), Stone.BLACK) ? "B" : "W";
                String moveValue = Util.coordToAlpha.get(x) + Util.coordToAlpha.get(y);

                gameNode.addProperty(moveKey, moveValue);
                if (p.getData().getCalculationCount() > 100) {
                    gameNode.addProperty("C", String.format("Black: %.1f; White: %.1f", p.getData().getBlackWinrate(), p.getData().getWhiteWinrate()));
                }
            }

            if (p.getData().getMoveNumber() > 0) {
                gameNode.setMoveNo(p.getData().getMoveNumber());
            }

            if (previousSgfNode != null) {
                previousSgfNode.addChild(gameNode);
                // Ensure we have already added child. The previousNode is not null here.
                //addVariationTrees(previousSgfNode, previousNode.getData());
                addTryPlayTrees(previousSgfNode, previousNode);
            } else {
                game.setRootNode(gameNode);
            }

            previousSgfNode = gameNode;
            previousNode = p;
        }

        // Ignore the last node
        // addVariationTree(previousNode, previousData);

        return game;
    }

    private static void addTryPlayTrees(GameNode baseSgfNode, BoardHistoryNode baseNode) {
        if (CollectionUtils.isEmpty(baseNode.getTryPlayHistory())) {
            return;
        }

        for (BoardHistoryNode node : baseNode.getTryPlayHistory()) {
            addTryPlayTree(baseSgfNode, baseNode, node);
        }
    }

    private static void addTryPlayTree(GameNode baseSgfNode, BoardHistoryNode baseNode, BoardHistoryNode tryPlayBeginNode) {
        final int BOARD_SIZE = Lizzie.optionSetting.getBoardSize().getWidth();
        GameNode previousSgfNode = baseSgfNode;

        for (BoardHistoryNode p = tryPlayBeginNode; p != null; p = p.getNext()) {
            GameNode gameNode = new GameNode(previousSgfNode);

            // Move node
            if (Objects.equals(p.getData().getLastMoveColor(), Stone.BLACK) || Objects.equals(p.getData().getLastMoveColor(), Stone.WHITE)) {
                int x, y;

                if (p.getData().getLastMove() == null) {
                    // Pass
                    x = BOARD_SIZE;
                    y = BOARD_SIZE;
                } else {
                    x = p.getData().getLastMove()[0];
                    y = p.getData().getLastMove()[1];

                    if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
                        x = BOARD_SIZE;
                        y = BOARD_SIZE;
                    }
                }

                String moveKey = Objects.equals(p.getData().getLastMoveColor(), Stone.BLACK) ? "B" : "W";
                String moveValue = Util.coordToAlpha.get(x) + Util.coordToAlpha.get(y);

                gameNode.addProperty(moveKey, moveValue);
                if (p.getData().getCalculationCount() > 100) {
                    gameNode.addProperty("C", String.format("Black: %.1f; White: %.1f", p.getData().getBlackWinrate(), p.getData().getWhiteWinrate()));
                }
            }

            if (p.getData().getMoveNumber() > 0) {
                gameNode.setMoveNo(p.getData().getMoveNumber());
            }

            if (previousSgfNode != null) {
                previousSgfNode.addChild(gameNode);
            }

            previousSgfNode = gameNode;
        }
    }

    private static void addVariationTrees(GameNode baseNode, BoardData data) {
        if (CollectionUtils.isEmpty(data.getVariationDataList())) {
            return;
        }

        int treeCount = 0;
        for (VariationData variationData : data.getVariationDataList()) {
            // We only add variation whose playouts is greater than 200
            if (variationData.getPlayouts() > 200) {
                addVariationTree(baseNode, variationData);

                ++treeCount;
                // We only care for 5 or less variations
                if (treeCount > 5) {
                    break;
                }
            }
        }
    }

    private static void addVariationTree(GameNode baseNode, VariationData variationData) {
        final int BOARD_SIZE = Lizzie.optionSetting.getBoardSize().getWidth();
        Stone baseColor = baseNode.isBlack() ? Stone.BLACK : Stone.WHITE;
        GameNode previousNode = baseNode;

        int variationMoveCount = 0;
        for (int[] variation : variationData.getVariation()) {
            GameNode gameNode = new GameNode(previousNode);

            int x, y;
            if (variation == null) {
                // Pass
                x = BOARD_SIZE;
                y = BOARD_SIZE;
            } else {
                x = variation[0];
                y = variation[1];

                if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
                    x = BOARD_SIZE;
                    y = BOARD_SIZE;
                }
            }

            String moveKey = Objects.equals(baseColor.opposite(), Stone.BLACK) ? "B" : "W";
            String moveValue = Util.coordToAlpha.get(x) + Util.coordToAlpha.get(y);
            gameNode.addProperty(moveKey, moveValue);

            if (previousNode == baseNode && variationData.getPlayouts() > 100) {
                double blackWinrate, whiteWinrate;
                if (moveKey.equals("B")) {
                    blackWinrate = variationData.getWinrate();
                    whiteWinrate = 100 - blackWinrate;
                } else {
                    whiteWinrate = variationData.getWinrate();
                    blackWinrate = 100 - whiteWinrate;
                }

                gameNode.addProperty("C", String.format("Black: %.1f; White: %.1f", blackWinrate, whiteWinrate));
            }

            previousNode.addChild(gameNode);

            previousNode = gameNode;
            baseColor = baseColor.opposite();

            ++variationMoveCount;
            if (variationMoveCount >= Lizzie.optionSetting.getVariationLimit()) {
                break;
            }
        }
    }

    public static String writeSgfToString(Game game) {
        try (StringWriter writer = new StringWriter()) {
            writeSgfToStream(game, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeSgfToFile(Game game, Path destination) {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(destination.toFile()), Charset.forName("UTF-8"))) {
            writeSgfToStream(game, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeSgfToStream(Game game, Writer writer) throws IOException {
        writer.write('(');

        // lets write all the root node properties
        Map<String, String> props = game.getProperties();
        if (props.size() > 0) {
            writer.write(";");
        }

        for (Map.Entry<String, String> entry : props.entrySet()) {
            writer.write(entry.getKey());
            writer.write('[');
            writer.write(entry.getValue());
            writer.write(']');
        }

        // write sgf nodes
        GameNode node = game.getRootNode();
        writeSgfSubTree(writer, node, 0);
        writer.write(')');
    }

    private static void writeSgfSubTree(Writer writer, GameNode subtreeRoot, int level) throws IOException {
        if (level > 0) {
            writer.write('(');
        }

        // DF pre-order traversal
        // Write root
        writer.write(';');
        writeNodeProperties(writer, subtreeRoot);

        // Write children
        GameNode mainChild;
        if ((mainChild = subtreeRoot.getNextNode()) != null) {
            writeSgfSubTree(writer, mainChild, level + 1);

            for (GameNode otherChild : subtreeRoot.getChildren()) {
                writeSgfSubTree(writer, otherChild, level + 1);
            }
        }

        if (level > 0) {
            writer.write(')');
        }
    }

    private static void writeNodeProperties(Writer writer, GameNode node) throws IOException {
        for (Map.Entry<String, String> entry : node.getProperties().entrySet()) {
            writer.write(entry.getKey());
            writer.write('[');
            writer.write(entry.getValue());
            writer.write(']');
        }
    }

    private static void storeBoardByFile(Path filePath) {
        BufferedImage bufferedImage = Lizzie.frame.getCachedImage();
        SVGGraphics2D svgGraphics2D = new SVGGraphics2D(bufferedImage.getWidth(), bufferedImage.getHeight());
        try {
            svgGraphics2D.drawImage(bufferedImage, 0, 0, null);
            String fileContent = svgGraphics2D.getSVGDocument();
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                writer.write(fileContent);
            } catch (Exception e) {
                if (StringUtils.isEmpty(e.getMessage())) {
                    JOptionPane.showMessageDialog(frame, "Error: cannot save svg: " + e.getMessage(), "Lizzie", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Error: cannot save svg", "Lizzie", JOptionPane.ERROR_MESSAGE);
                }
            }
        } finally {
            svgGraphics2D.dispose();
        }
    }

    private static void storeBoardPngImageByFile(Path filePath) {
        BufferedImage bufferedImage = Lizzie.frame.getCachedImage();
        try (FileOutputStream stream = new FileOutputStream(filePath.toFile())) {
            ImageIO.write(bufferedImage, "PNG", stream);
        } catch (Exception e) {
            if (StringUtils.isEmpty(e.getMessage())) {
                JOptionPane.showMessageDialog(frame, "Error: cannot save png: " + e.getMessage(), "Lizzie", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Error: cannot save png", "Lizzie", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void storeGameByPrompting() {
        FileNameExtensionFilter sgfFilter = new FileNameExtensionFilter("*.sgf", "SGF");
        FileNameExtensionFilter svgFilter = new FileNameExtensionFilter("*.svg", "SVG");
        FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("*.png", "PNG");

        JFileChooser chooser = new JFileChooser(optionSetting.getLastChooserLocation());
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(sgfFilter);
        chooser.addChoosableFileFilter(svgFilter);
        chooser.addChoosableFileFilter(pngFilter);
        chooser.setMultiSelectionEnabled(false);

        setFileChooserAutoFocusOnTextField(chooser);

        int result = chooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            optionSetting.setLastChooserLocation(file.getParent());

            if (!file.getPath().toLowerCase().endsWith(".sgf") && !file.getPath().toLowerCase().endsWith(".svg") && !file.getPath().toLowerCase().endsWith(".png")) {
                if (chooser.getFileFilter().equals(sgfFilter)) {
                    file = new File(file.getPath() + ".sgf");
                } else if (chooser.getFileFilter().equals(svgFilter)) {
                    file = new File(file.getPath() + ".svg");
                } else {
                    file = new File(file.getPath() + ".png");
                }
            }

            if (file.exists()) {
                int ret = JOptionPane.showConfirmDialog(frame, "The target file is exists, do you want replace it?", "Warning", JOptionPane.OK_CANCEL_OPTION);
                if (ret == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }

            if (file.getPath().toLowerCase().endsWith(".sgf")) {
                storeGameByFile(file.toPath());
            } else if (file.getPath().toLowerCase().endsWith(".svg")) {
                storeBoardByFile(file.toPath());
            } else {
                storeBoardPngImageByFile(file.toPath());
            }
        }
    }

    public static void readGuiPosition() {
        readMainFramePosition();
        readAnalysisWindowPosition();
        readWinrateHistogramWindowPosition();
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

    public static void readWinrateHistogramWindowPosition() {
        optionSetting.setWinrateHistogramWindowPosX(winrateHistogramDialog.getX());
        optionSetting.setWinrateHistogramWindowPosY(winrateHistogramDialog.getY());
        optionSetting.setWinrateHistogramWindowWidth(winrateHistogramDialog.getWidth());
        optionSetting.setWinrateHistogramWindowHeight(winrateHistogramDialog.getHeight());
    }

    public static void setGuiPosition() {
        setMainWindowPosition();
        setAnalysisWindowPosition();
        setWinrateHistogramWindowPosition();
    }

    public static void setAnalysisWindowPosition() {
        if (optionSetting.getAnalysisWindowPosX() >= 0 && optionSetting.getAnalysisWindowPosY() >= 0) {
            analysisDialog.setLocation(optionSetting.getAnalysisWindowPosX(), optionSetting.getAnalysisWindowPosY());
        }
        if (optionSetting.getAnalysisWindowWidth() >= 10 && optionSetting.getAnalysisWindowHeight() >= 10) {
            analysisDialog.setSize(optionSetting.getAnalysisWindowWidth(), optionSetting.getAnalysisWindowHeight());
        }
    }

    public static void setMainWindowPosition() {
        if (optionSetting.getMainWindowPosX() >= 0 && optionSetting.getMainWindowPosY() >= 0) {
            frame.setLocation(optionSetting.getMainWindowPosX(), optionSetting.getMainWindowPosY());
        }
        if (optionSetting.getMainWindowWidth() >= 10 && optionSetting.getMainWindowHeight() >= 10) {
            frame.setSize(optionSetting.getMainWindowWidth(), optionSetting.getMainWindowHeight());
        }
    }

    public static void setWinrateHistogramWindowPosition() {
        if (optionSetting.getWinrateHistogramWindowPosX() >= 0 && optionSetting.getWinrateHistogramWindowPosY() >= 0) {
            winrateHistogramDialog.setLocation(optionSetting.getWinrateHistogramWindowPosX(), optionSetting.getWinrateHistogramWindowPosY());
        }
        if (optionSetting.getWinrateHistogramWindowWidth() >= 10 && optionSetting.getWinrateHistogramWindowHeight() >= 10) {
            winrateHistogramDialog.setSize(optionSetting.getWinrateHistogramWindowWidth(), optionSetting.getWinrateHistogramWindowHeight());
        }
    }

    public static void readSettingFile() {
        try (Reader reader = new FileReader(SETTING_FILE)) {
            optionSetting = gson.fromJson(reader, OptionSetting.class);
        } catch (FileNotFoundException e) {
            // Do nothing
        } catch (Exception e) {
            logger.error("Error in reading setting file.", e);
        }
    }

    public static void writeSettingFile() {
        try (Writer writer = new FileWriter(SETTING_FILE)) {
            writer.write(gson.toJson(optionSetting));
        } catch (Exception e) {
            logger.error("Error in writing setting file.", e);
        }
    }
}
