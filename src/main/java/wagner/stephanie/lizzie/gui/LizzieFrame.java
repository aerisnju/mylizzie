package wagner.stephanie.lizzie.gui;

import com.jhlabs.image.GaussianFilter;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.rules.Board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The window used to display the game.
 */
public class LizzieFrame extends JFrame {
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("wagner.stephanie.lizzie.i18n.GuiBundle");

    private static final String[] commands = {
            resourceBundle.getString("LizzieFrame.controls.leftArrow")
            , resourceBundle.getString("LizzieFrame.controls.rightArrow")
            , resourceBundle.getString("LizzieFrame.controls.space")
            , resourceBundle.getString("LizzieFrame.controls.rightClick")
            , resourceBundle.getString("LizzieFrame.controls.mouseWheelScroll")
            , resourceBundle.getString("LizzieFrame.controls.keyP")
            , resourceBundle.getString("LizzieFrame.controls.keyN")
            , resourceBundle.getString("LizzieFrame.controls.keyO")
            , resourceBundle.getString("LizzieFrame.controls.keyAltC")
            , resourceBundle.getString("LizzieFrame.controls.keyCtrlO")
            , resourceBundle.getString("LizzieFrame.controls.keyCtrlS")
            , resourceBundle.getString("LizzieFrame.controls.keyCtrlC")
            , resourceBundle.getString("LizzieFrame.controls.keyCtrlV")
            , resourceBundle.getString("LizzieFrame.controls.keyG")
            , resourceBundle.getString("LizzieFrame.controls.keyV")
            , resourceBundle.getString("LizzieFrame.controls.keyX")
            , resourceBundle.getString("LizzieFrame.controls.keyA")
            , resourceBundle.getString("LizzieFrame.controls.keyH")
            , resourceBundle.getString("LizzieFrame.controls.keyHome")
            , resourceBundle.getString("LizzieFrame.controls.keyEnd")
            , resourceBundle.getString("LizzieFrame.controls.keyS")
            , resourceBundle.getString("LizzieFrame.controls.keyC")
            , resourceBundle.getString("LizzieFrame.controls.keyE")
            , resourceBundle.getString("LizzieFrame.controls.keyB")
            , resourceBundle.getString("LizzieFrame.controls.keyEnter")
    };
    public static final String LIZZIE_TITLE = "MyLizzie - Leela Zero Interface";
    public static final String LIZZIE_TRY_PLAY_TITLE = resourceBundle.getString("LizzieFrame.title.tryPlayingMode");

    static {
        // load fonts
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, LizzieFrame.class.getResourceAsStream("/fonts/OpenSans-Regular.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, LizzieFrame.class.getResourceAsStream("/fonts/OpenSans-Semibold.ttf")));
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    }

    private String engineProfile = Lizzie.optionSetting.getLeelazCommandLine();

    private BufferedImage cachedImage;
    private BoardRenderer boardRenderer;
    private BufferStrategy bs;

    // TODO: Clean this
    public boolean showControls = false;
    public boolean showCoordinates = true;
    public boolean isPlayingAgainstLeelaz = false;

    /**
     * Creates a window and refreshes the game state at FPS.
     */
    public LizzieFrame() {
        super();
        setTitle(LIZZIE_TITLE + " - [" + engineProfile + "]");

        boardRenderer = new BoardRenderer();

        setVisible(true);

        createBufferStrategy(2);
        bs = getBufferStrategy();

        Input input = new Input();
        this.addMouseListener(input);
        this.addKeyListener(input);
        this.addMouseWheelListener(input);
        this.addMouseMotionListener(input);

        this.setAlwaysOnTop(Lizzie.optionSetting.isMainWindowAlwaysOnTop());

        // shut down leelaz, then shut down the program when the window is closed
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Lizzie.leelaz.setNormalExit(true);
                Lizzie.readGuiPosition();
                Lizzie.writeSettingFile();
                Lizzie.leelaz.shutdown();

                System.exit(0);
            }
        });
    }

    public BoardRenderer getBoardRenderer() {
        return boardRenderer;
    }

    public BufferedImage getCachedImage() {
        return cachedImage;
    }

    // Toggle show/hide move number
    public void toggleShowMoveNumber() {
        Lizzie.optionSetting.setShowMoveNumber(!Lizzie.optionSetting.isShowMoveNumber());
    }

    public void setEngineProfile(String engineProfile) {
        this.engineProfile = engineProfile;
        if (getTitle().startsWith(LIZZIE_TITLE)) {
            setTitle(LIZZIE_TITLE + " - [" + engineProfile + "]");
        }
    }

    /**
     * Draws the game board and interface
     *
     * @param g0 not used
     */
    public void paint(Graphics g0) {
        if (bs == null)
            return;

        // initialize
        cachedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) cachedImage.getGraphics();

        int topInset = this.getInsets().top;

        if (Lizzie.optionSetting.isShowFancyBoard()) {
            try {
                BufferedImage background = AssetsManager.getAssetsManager().getImageAsset("assets/background.jpg");
                int drawWidth = Math.max(background.getWidth(), getWidth());
                int drawHeight = Math.max(background.getHeight(), getHeight());
                g.drawImage(background, 0, 0, drawWidth, drawHeight, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            g.setColor(Color.GREEN.darker().darker());
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        int maxSize = (int) (Math.min(getWidth(), getHeight() - topInset) * 0.98);
        maxSize = Math.max(maxSize, Board.BOARD_SIZE + 5); // don't let maxWidth become too small

        drawCommandString(g);

        int boardX = (getWidth() - maxSize) / 2;
        int boardY = topInset + (getHeight() - topInset - maxSize) / 2 + 3;
        boardRenderer.setLocation(boardX, boardY);
        boardRenderer.setBoardLength(maxSize);
        boardRenderer.draw(g);

        // cleanup
        g.dispose();

        // draw the control hint
        if (showControls) {
            drawControls();
        }

        // draw the image
        Graphics2D bsGraphics = (Graphics2D) bs.getDrawGraphics();
        bsGraphics.drawImage(cachedImage, 0, 0, null);

        // cleanup
        bsGraphics.dispose();
        bs.show();
    }

    private GaussianFilter filter = new GaussianFilter(15);

    /**
     * Display the controls
     */
    private void drawControls() {
        userAlreadyKnowsAboutCommandString = true;

        Graphics2D g = (Graphics2D) cachedImage.getGraphics();
        int maxSize = Math.min(getWidth(), getHeight());

        Font font = new Font(new JLabel().getFont().getName(), Font.PLAIN, (int) (maxSize * 0.03));
        g.setFont(font);
        int lineHeight = (int) (font.getSize() * 1.15);

        int boxWidth = (int) (maxSize * 0.85);
        int boxHeight = (int) (commands.length * lineHeight);

        int commandsX = (int) (getWidth() / 2 - boxWidth / 2);
        int commandsY = (int) (getHeight() / 2 - boxHeight / 2);

        BufferedImage result = new BufferedImage(boxWidth, boxHeight, BufferedImage.TYPE_INT_ARGB);
        filter.filter(cachedImage.getSubimage(commandsX, commandsY, boxWidth, boxHeight), result);
        g.drawImage(result, commandsX, commandsY, null);

        g.setColor(new Color(0, 0, 0, 130));
        g.fillRect(commandsX, commandsY, boxWidth, boxHeight);
        int strokeRadius = 2;
        g.setStroke(new BasicStroke(2 * strokeRadius));
        g.setColor(new Color(0, 0, 0, 60));
        g.drawRect(commandsX + strokeRadius, commandsY + strokeRadius, boxWidth - 2 * strokeRadius, boxHeight - 2 * strokeRadius);

        int verticalLineX = (int) (commandsX + boxWidth * 0.3);
        g.setColor(new Color(0, 0, 0, 60));
        g.drawLine(verticalLineX, commandsY + 2 * strokeRadius, verticalLineX, commandsY + boxHeight - 2 * strokeRadius);


        g.setStroke(new BasicStroke(1));

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics metrics = g.getFontMetrics(font);

        g.setColor(Color.WHITE);
        for (int i = 0; i < commands.length; i++) {
            String[] split = commands[i].split("\\|");
            g.drawString(split[0], verticalLineX - metrics.stringWidth(split[0]) - strokeRadius * 4, font.getSize() + (int) (commandsY + i * lineHeight));
            g.drawString(split[1], verticalLineX + strokeRadius * 4, font.getSize() + (int) (commandsY + i * lineHeight));
        }
    }

    private boolean userAlreadyKnowsAboutCommandString = false;

    private void drawCommandString(Graphics2D g) {
        if (userAlreadyKnowsAboutCommandString)
            return;

        int maxSize = (int) (Math.min(getWidth(), getHeight()) * 0.98);

        Font font = new Font(new JLabel().getFont().getName(), Font.PLAIN, (int) (maxSize * 0.03));
        String commandString = resourceBundle.getString("LizzieFrame.controls.keyF1");
        int strokeRadius = 2;

        int showCommandsHeight = (int) (font.getSize() * 1.1);
        int showCommandsWidth = g.getFontMetrics(font).stringWidth(commandString) + 4 * strokeRadius;
        int showCommandsX = this.getInsets().left;
        int showCommandsY = getHeight() - showCommandsHeight - this.getInsets().bottom;
        g.setColor(new Color(0, 0, 0, 130));
        g.fillRect(showCommandsX, showCommandsY, showCommandsWidth, showCommandsHeight);
        g.setStroke(new BasicStroke(2 * strokeRadius));
        g.setColor(new Color(0, 0, 0, 60));
        g.drawRect(showCommandsX + strokeRadius, showCommandsY + strokeRadius, showCommandsWidth - 2 * strokeRadius, showCommandsHeight - 2 * strokeRadius);
        g.setStroke(new BasicStroke(1));

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.setFont(font);
        g.drawString(commandString, showCommandsX + 2 * strokeRadius, showCommandsY + font.getSize());
    }

    /**
     * Checks whether or not something was clicked and performs the appropriate action
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public void onClicked(int x, int y) {
        // check for board click
        int[] boardCoordinates = boardRenderer.convertScreenToCoordinates(x, y);

        if (boardCoordinates != null) {
            Lizzie.board.place(boardCoordinates[0], boardCoordinates[1]);
        }
    }

    private AtomicReference<int[]> lastBoardCoordinates = new AtomicReference<>();

    public void onMouseMove(int x, int y) {
        if (Lizzie.optionSetting.isMouseOverShowMove() && (Lizzie.board.getData().isBlackToPlay() && Lizzie.optionSetting.isShowBlackSuggestion()
                || !Lizzie.board.getData().isBlackToPlay() && Lizzie.optionSetting.isShowWhiteSuggestion())) {
            // check for board click
            int[] boardCoordinates = boardRenderer.convertScreenToCoordinates(x, y);
            int[] previousCoordinates = lastBoardCoordinates.getAndSet(boardCoordinates);
            if (!Arrays.equals(previousCoordinates, boardCoordinates)) {
                Lizzie.analysisFrame.getAnalysisTableModel().selectOrDeselectMoveByCoord(boardCoordinates);
                repaint();
            }
        }
    }

    public void onDoubleClicked(int x, int y) {
        int[] boardCoordinates = boardRenderer.convertScreenToCoordinates(x, y);

        if (boardCoordinates != null) {
            int moveNumber = Lizzie.board.getMoveNumber(boardCoordinates[0], boardCoordinates[1]);
            if (moveNumber > 0) {
                Lizzie.board.gotoMove(moveNumber);
            }
        }
    }

    public void showTryPlayTitle() {
        setTitle(LIZZIE_TRY_PLAY_TITLE);
    }

    public void restoreDefaultTitle() {
        setTitle(LIZZIE_TITLE + " - [" + engineProfile + "]");
    }
}
