package wagner.stephanie.lizzie.gui;

import org.apache.commons.lang3.ArrayUtils;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.analysis.BestMoveObserver;
import wagner.stephanie.lizzie.analysis.MoveData;
import wagner.stephanie.lizzie.rules.*;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class BoardRenderer {
    private static final double MARGIN = 0.03; // percentage of the boardLength to offset before drawing black lines
    private static final double MARGIN_WITH_COORDS = 0.06;
    private static final double STARPOINT_DIAMETER = 0.015;

    private static final AlphaComposite COMPOSITE_6 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
    private static final AlphaComposite COMPOSITE_75 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f);
    private static final AlphaComposite COMPOSITE_5 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

    private int x, y;
    private int boardLength;

    private int scaledMargin, availableLength, squareLength, stoneRadius;
    private AtomicReference<List<MoveData>> bestMovesUpdated = new AtomicReference<>();
    private MoveData branch = null;

    private double[] influences = null;

    private BufferedImage cachedBackgroundImage = null;
    private AtomicBoolean cachedBackgroundImageForceRefresh = new AtomicBoolean(false);

    private BufferedImage cachedStonesImage = null;
    private BufferedImage cachedStonesShadowImage = null;
    private AtomicBoolean cachedStonesImageForceRefresh = new AtomicBoolean(false);
    private Zobrist cachedZhash = new Zobrist(); // defaults to an empty board

    private BufferedImage branchStonesImage = null;
    private BufferedImage branchStonesShadowImage = null;

    private BestMoveObserver bestMoveObserver;
    private BoardStateChangeObserver boardStateChangeObserver;

    public BoardRenderer() {
        // register repaint events
        bestMoveObserver = new BestMoveObserver() {
            @Override
            public void bestMovesUpdated(int boardStateCount, List<MoveData> newBestMoves) {
                bestMovesUpdated.set(newBestMoves);

                if (Lizzie.frame != null) {
                    Lizzie.frame.repaint();
                }
            }

            @Override
            public void engineRestarted() {
                if (Lizzie.frame != null) {
                    Lizzie.frame.repaint();
                }
            }
        };

        boardStateChangeObserver = new BoardStateChangeObserver() {
            @Override
            public void mainStreamAppended(BoardHistoryNode newNodeBegin, BoardHistoryNode head) {
                if (Lizzie.frame != null) {
                    Lizzie.frame.repaint();
                }
            }

            @Override
            public void mainStreamCut(BoardHistoryNode nodeBeforeCutPoint, BoardHistoryNode head) {
                if (Lizzie.frame != null) {
                    Lizzie.frame.repaint();
                }
            }

            @Override
            public void headMoved(BoardHistoryNode oldHead, BoardHistoryNode newHead) {
                if (Lizzie.frame != null) {
                    Lizzie.frame.repaint();
                }
            }

            @Override
            public void boardCleared() {
                if (Lizzie.frame != null) {
                    Lizzie.frame.repaint();
                }
            }
        };

        Lizzie.leelaz.registerBestMoveObserver(bestMoveObserver);
        Lizzie.board.registerBoardStateChangeObserver(boardStateChangeObserver);
    }

    public void forceCachedBackgroundImageRefresh() {
        cachedBackgroundImageForceRefresh.set(true);
    }

    public void forceCachedStoneImageRefresh() {
        cachedStonesImageForceRefresh.set(true);
    }

    public void updateInfluences(double[] influences) {
        this.influences = influences;
    }

    /**
     * Draw a go board
     */
    public void draw(Graphics2D g) {
        if (Lizzie.frame == null || Lizzie.board == null)
            return;

        setupSizeParameters();

//        Stopwatch timer = new Stopwatch();
        drawBackground(g);
//        timer.lap("background");
        drawStones();
//        timer.lap("stones");
        drawBranch();
//        timer.lap("branch");

        renderImages(g);
//        timer.lap("rendering images");

        drawMoveNumbers(g);
//        timer.lap("movenumbers");
        if (!Lizzie.frame.isPlayingAgainstLeelaz)
            drawLeelazSuggestions(g);
//        timer.lap("leelaz");

        drawInfluences(g);
//        timer.lap("influences");

//        timer.print();
    }

    /**
     * Calculate good values for boardLength, scaledMargin, availableLength, and squareLength
     */
    private void setupSizeParameters() {
        int[] calculatedPixelMargins = calculatePixelMargins();
        boardLength = calculatedPixelMargins[0];
        scaledMargin = calculatedPixelMargins[1];
        availableLength = calculatedPixelMargins[2];

        squareLength = calculateSquareLength(availableLength);
        stoneRadius = squareLength / 2 - 1;
    }

    /**
     * Draw the green background and go board with lines. We cache the image for a performance boost.
     */
    private void drawBackground(Graphics2D g0) {
        // draw the cached background image if frame size changes
        if (cachedBackgroundImage == null || cachedBackgroundImage.getWidth() != Lizzie.frame.getWidth() ||
                cachedBackgroundImage.getHeight() != Lizzie.frame.getHeight() ||
                cachedBackgroundImageForceRefresh.getAndSet(false)) {

            cachedBackgroundImage = new BufferedImage(Lizzie.frame.getWidth(), Lizzie.frame.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = cachedBackgroundImage.createGraphics();

            // draw the wooden background
            drawWoodenBoard(g);

            // draw the lines
            g.setColor(Color.BLACK);
            for (int i = 0; i < Board.BOARD_SIZE; i++) {
                g.drawLine(x + scaledMargin, y + scaledMargin + squareLength * i,
                        x + scaledMargin + availableLength - 1, y + scaledMargin + squareLength * i);
            }
            for (int i = 0; i < Board.BOARD_SIZE; i++) {
                g.drawLine(x + scaledMargin + squareLength * i, y + scaledMargin,
                        x + scaledMargin + squareLength * i, y + scaledMargin + availableLength - 1);
            }

            // draw the star points
            drawStarPoints(g);

            // draw coordinates if enabled
            if (Lizzie.frame.showCoordinates) {
                g.setColor(Color.BLACK);
                String alphabet = "ABCDEFGHJKLMNOPQRST";
                for (int i = 0; i < Board.BOARD_SIZE; i++) {
                    drawString(g, x + scaledMargin + squareLength * i, y + scaledMargin / 2, "Open Sans", "" + alphabet.charAt(i), stoneRadius * 4 / 5, stoneRadius);
                    drawString(g, x + scaledMargin + squareLength * i, y - scaledMargin / 2 + boardLength, "Open Sans", "" + alphabet.charAt(i), stoneRadius * 4 / 5, stoneRadius);
                }
                for (int i = 0; i < Board.BOARD_SIZE; i++) {
                    if (Lizzie.optionSetting.isA1OnTop()) {
                        drawString(g, x + scaledMargin / 2, y + scaledMargin + squareLength * i, "Open Sans", "" + (i + 1), stoneRadius * 4 / 5, stoneRadius);
                        drawString(g, x - scaledMargin / 2 + +boardLength, y + scaledMargin + squareLength * i, "Open Sans", "" + (i + 1), stoneRadius * 4 / 5, stoneRadius);
                    } else {
                        drawString(g, x + scaledMargin / 2, y + scaledMargin + squareLength * i, "Open Sans", "" + (Board.BOARD_SIZE - i), stoneRadius * 4 / 5, stoneRadius);
                        drawString(g, x - scaledMargin / 2 + +boardLength, y + scaledMargin + squareLength * i, "Open Sans", "" + (Board.BOARD_SIZE - i), stoneRadius * 4 / 5, stoneRadius);
                    }
                }
            }
            g.dispose();
        }

        g0.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g0.drawImage(cachedBackgroundImage, 0, 0, null);
    }

    /**
     * Draw the star points
     *
     * @param g graphics2d to draw
     */
    private void drawStarPoints(Graphics2D g) {
        if (Board.BOARD_SIZE == 9) {
            drawStarPoints9x9(g);
        } else if (Board.BOARD_SIZE == 13) {
            drawStarPoints13x13(g);
        } else {
            drawStarPoints19x19(g);
        }
    }

    private void drawStarPoints19x19(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int starPointRadius = (int) (STARPOINT_DIAMETER * boardLength) / 2;
        final int NUM_STARPOINTS = 3;
        final int STARPOINT_EDGE_OFFSET = 3;
        final int STARPOINT_GRID_DISTANCE = 6;
        for (int i = 0; i < NUM_STARPOINTS; i++) {
            for (int j = 0; j < NUM_STARPOINTS; j++) {
                int centerX = x + scaledMargin + squareLength * (STARPOINT_EDGE_OFFSET + STARPOINT_GRID_DISTANCE * i);
                int centerY = y + scaledMargin + squareLength * (STARPOINT_EDGE_OFFSET + STARPOINT_GRID_DISTANCE * j);
                fillCircle(g, centerX, centerY, starPointRadius);
            }
        }
    }

    private void drawStarPoints13x13(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int starPointRadius = (int) (STARPOINT_DIAMETER * boardLength) / 2;
        final int NUM_STARPOINTS = 2;
        final int STARPOINT_EDGE_OFFSET = 3;
        final int STARPOINT_GRID_DISTANCE = 6;
        for (int i = 0; i < NUM_STARPOINTS; i++) {
            for (int j = 0; j < NUM_STARPOINTS; j++) {
                int centerX = x + scaledMargin + squareLength * (STARPOINT_EDGE_OFFSET + STARPOINT_GRID_DISTANCE * i);
                int centerY = y + scaledMargin + squareLength * (STARPOINT_EDGE_OFFSET + STARPOINT_GRID_DISTANCE * j);
                fillCircle(g, centerX, centerY, starPointRadius);
            }
        }

        // Draw center
        int centerX = x + scaledMargin + squareLength * STARPOINT_GRID_DISTANCE;
        int centerY = y + scaledMargin + squareLength * STARPOINT_GRID_DISTANCE;
        fillCircle(g, centerX, centerY, starPointRadius);
    }

    private void drawStarPoints9x9(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int starPointRadius = (int) (STARPOINT_DIAMETER * boardLength) / 2;
        final int NUM_STARPOINTS = 2;
        final int STARPOINT_EDGE_OFFSET = 2;
        final int STARPOINT_GRID_DISTANCE = 4;
        for (int i = 0; i < NUM_STARPOINTS; i++) {
            for (int j = 0; j < NUM_STARPOINTS; j++) {
                int centerX = x + scaledMargin + squareLength * (STARPOINT_EDGE_OFFSET + STARPOINT_GRID_DISTANCE * i);
                int centerY = y + scaledMargin + squareLength * (STARPOINT_EDGE_OFFSET + STARPOINT_GRID_DISTANCE * j);
                fillCircle(g, centerX, centerY, starPointRadius);
            }
        }

        // Draw center
        int centerX = x + scaledMargin + squareLength * STARPOINT_GRID_DISTANCE;
        int centerY = y + scaledMargin + squareLength * STARPOINT_GRID_DISTANCE;
        fillCircle(g, centerX, centerY, starPointRadius);
    }

    /**
     * Draw the stones. We cache the image for a performance boost.
     */
    private void drawStones() {
        // draw a new image if frame size changes or board state changes
        if (cachedStonesImage == null || cachedStonesImage.getWidth() != boardLength ||
                cachedStonesImage.getHeight() != boardLength ||
                !cachedZhash.equals(Lizzie.board.getData().getZobrist()) ||
                cachedStonesImageForceRefresh.getAndSet(false)) {

            cachedStonesImage = new BufferedImage(boardLength, boardLength, BufferedImage.TYPE_INT_ARGB);
            cachedStonesShadowImage = new BufferedImage(boardLength, boardLength, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = cachedStonesImage.createGraphics();
            Graphics2D gShadow = cachedStonesShadowImage.createGraphics();

            // we need antialiasing to make the stones pretty. Java is a bit slow at antialiasing; that's why we want the cache
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gShadow.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = 0; i < Board.BOARD_SIZE; i++) {
                for (int j = 0; j < Board.BOARD_SIZE; j++) {
                    int stoneX = scaledMargin + squareLength * i;
                    int stoneY = scaledMargin + squareLength * j;
                    drawStone(g, gShadow, stoneX, stoneY, Lizzie.board.getStones()[Board.getIndex(i, j)]);
                }
            }

            cachedZhash = Lizzie.board.getData().getZobrist();
            g.dispose();
            gShadow.dispose();
        }
    }

    /**
     * Draw the 'ghost stones' which show a variation Leelaz is thinking about
     */
    private void drawBranch() {
        branchStonesImage = new BufferedImage(boardLength, boardLength, BufferedImage.TYPE_INT_ARGB);
        branchStonesShadowImage = new BufferedImage(boardLength, boardLength, BufferedImage.TYPE_INT_ARGB);

        if (Lizzie.frame.isPlayingAgainstLeelaz) {
            return;
        }

        branch = Lizzie.analysisFrame.getAnalysisTableModel().getSelectedMove();
        if (branch == null) {
            return;
        }

        Graphics2D g = (Graphics2D) branchStonesImage.getGraphics();
        Graphics2D gShadow = (Graphics2D) branchStonesShadowImage.getGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Stone color = Lizzie.board.getData().getLastMoveColor();
        if (color == Stone.EMPTY) {
            color = Stone.WHITE;
        }

        int variationCount = 0;
        for (String variation : branch.getVariation()) {
            color = color.opposite();
            ++variationCount;
            // limit variation stones
            // note that move number display is in another function
            if (variationCount > Lizzie.optionSetting.getVariationLimit()) {
                break;
            }

            int[] coords = Board.convertNameToCoordinates(variation);
            if (Board.isValid(coords[0], coords[1])) {
                int stoneX = scaledMargin + squareLength * coords[0];
                int stoneY = scaledMargin + squareLength * coords[1];

                // check if board is empty to prevent overwriting stones if there are under-the-stones situations
                if (Lizzie.board.getStones()[Board.getIndex(coords[0], coords[1])] == Stone.EMPTY)
                    drawVariationStone(g, gShadow, stoneX, stoneY, color.unGhosted());
            }
        }

        g.dispose();
        gShadow.dispose();
    }

    /**
     * render the shadows and stones in correct background-foreground order
     */
    private void renderImages(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.drawImage(cachedStonesShadowImage, x, y, null);
        g.drawImage(branchStonesShadowImage, x, y, null);
        g.drawImage(cachedStonesImage, x, y, null);
        g.drawImage(branchStonesImage, x, y, null);
    }

    /**
     * Draw move numbers and/or mark the last played move
     */
    private void drawMoveNumbers(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int[] lastMove = Lizzie.board.getLastMove();
        int lastMoveNumber = Lizzie.board.getData().getMoveNumber();

        // mark last coordinate with a small circle
        if (!Lizzie.board.isInTryPlayState() && (!Lizzie.optionSetting.isShowMoveNumber() || branch != null)
                || Lizzie.board.isInTryPlayState() && lastMoveNumber <= Lizzie.board.getTryPlayStateBeginMoveNumber()) {
            if (lastMove != null) {
                // mark the last coordinate
                int lastMoveMarkerRadius = stoneRadius / 2;
                int stoneX = x + scaledMargin + squareLength * lastMove[0];
                int stoneY = y + scaledMargin + squareLength * lastMove[1];

                // set color to the opposite color of whatever is on the board
                g.setColor(Lizzie.board.getStones()[Board.getIndex(lastMove[0], lastMove[1])].isWhite() ?
                        Color.BLACK : Color.WHITE);
                drawCircle(g, stoneX, stoneY, lastMoveMarkerRadius);
            } else if (lastMoveNumber != 0) {
                // mark pass
                g.setColor(Lizzie.board.getData().isBlackToPlay() ? new Color(255, 255, 255, 150) : new Color(0, 0, 0, 150));
                g.fillOval(x + boardLength / 2 - 4 * stoneRadius, y + boardLength / 2 - 4 * stoneRadius, stoneRadius * 8, stoneRadius * 8);
                g.setColor(Lizzie.board.getData().isBlackToPlay() ? new Color(0, 0, 0, 255) : new Color(255, 255, 255, 255));
                drawString(g, x + boardLength / 2, y + boardLength / 2, "Open Sans", "pass", stoneRadius * 4, stoneRadius * 6);
            }
        }

        if (!Lizzie.board.isInTryPlayState() && Lizzie.optionSetting.isShowMoveNumber() && branch == null
                || Lizzie.board.isInTryPlayState()) { // at this time, isShowMoveNumber is true, or is in try play state
            // draw existing stones
            int[] moveNumberList = Lizzie.board.getMoveNumberList();
            int moveNumberBaseFix = 0;
            if (Lizzie.board.isInTryPlayState()) {
                moveNumberBaseFix = Lizzie.board.getTryPlayStateBeginMoveNumber();
            }

            for (int i = 0; i < Board.BOARD_SIZE; i++) {
                for (int j = 0; j < Board.BOARD_SIZE; j++) {
                    int stoneX = x + scaledMargin + squareLength * i;
                    int stoneY = y + scaledMargin + squareLength * j;

                    int index = Board.getIndex(i, j);
                    if (lastMoveNumber - moveNumberList[index] >= Lizzie.optionSetting.getNumberOfLastMovesShown()) {
                        continue;
                    }

                    Stone stoneAtThisPoint = Lizzie.board.getStones()[index];
                    // don't write the move number if either: the move number is 0, or there will already be playout information written
                    if (moveNumberList[index] - moveNumberBaseFix > 0) {
                        if (lastMove != null && i == lastMove[0] && j == lastMove[1])
                            g.setColor(Color.RED.brighter());//stoneAtThisPoint.isBlack() ? Color.RED.brighter() : Color.BLUE.brighter());
                        else
                            g.setColor(stoneAtThisPoint.isBlack() ? Color.WHITE : Color.BLACK);

                        String moveNumberString = String.valueOf(moveNumberList[index] - moveNumberBaseFix);
                        drawString(g, stoneX, stoneY, "Open Sans", moveNumberString, (float) (stoneRadius * 1.4), (int) (stoneRadius * 1.4));
                    }
                }
            }

            // draw pass with number
            if (lastMove == null && lastMoveNumber != 0 && lastMoveNumber - moveNumberBaseFix >= 0) {
                g.setColor(Lizzie.board.getData().isBlackToPlay() ? new Color(255, 255, 255, 150) : new Color(0, 0, 0, 150));
                g.fillOval(x + boardLength / 2 - 4 * stoneRadius, y + boardLength / 2 - 4 * stoneRadius, stoneRadius * 8, stoneRadius * 8);
                g.setColor(Color.RED);
                drawString(g, x + boardLength / 2, y + boardLength / 2, "Open Sans", Font.PLAIN, String.valueOf(lastMoveNumber - moveNumberBaseFix), stoneRadius * 4, stoneRadius * 6, 1);
                g.setColor(Lizzie.board.getData().isBlackToPlay() ? new Color(0, 0, 0, 255) : new Color(255, 255, 255, 255));
                drawString(g, x + boardLength / 2, y + boardLength / 2 + stoneRadius, "Open Sans", "pass", stoneRadius * 4, stoneRadius * 6);
            }
        }

        if (branch != null) {
            int variationBase = 0;
            if (Lizzie.board.isInTryPlayState()) {
                variationBase = lastMoveNumber - Lizzie.board.getTryPlayStateBeginMoveNumber();
                if (variationBase < 0) {
                    variationBase = 0;
                }
            }
            // draw branch number
            int nextVariationNumber = 0;
            if (Lizzie.board.isInTryPlayState()) {
                // try play state: show successive move number
                nextVariationNumber = variationBase;
            }

            Stone nextStone = Lizzie.board.getData().getLastMoveColor();
            if (nextStone == Stone.EMPTY) {
                nextStone = Stone.WHITE;
            }
            for (String move : branch.getVariation()) {
                ++nextVariationNumber;

                // Flip stone
                nextStone = nextStone.opposite();

                if (nextVariationNumber == variationBase + 1) {
                    // we do not draw the first variation as its winrate should be displayed
                    continue;
                }

                // limit variation number to settings
                // note that stone drawing is at another place
                if (nextVariationNumber - variationBase > Lizzie.optionSetting.getVariationLimit()) {
                    break;
                }

                int[] coords = Board.convertNameToCoordinates(move);
                int i = coords[0], j = coords[1];
                if (Board.isValid(i, j)) {
                    int stoneX = x + scaledMargin + squareLength * i;
                    int stoneY = y + scaledMargin + squareLength * j;

                    // Draw variation move number
                    g.setColor(nextStone.equals(Stone.BLACK) ? Color.WHITE : Color.BLACK);
                    String moveNumberString = String.valueOf(nextVariationNumber);
                    drawString(g, stoneX, stoneY, "Open Sans", moveNumberString, (float) (stoneRadius * 1.4), (int) (stoneRadius * 1.4));
                }
            }
        }
    }

    private final int MIN_ALPHA = 32;
    private final int MIN_ALPHA_TO_DISPLAY_TEXT = 64;
    private final int MAX_ALPHA = 240;
    private final double HUE_SCALING_FACTOR = 3.0;
    private final double ALPHA_SCALING_FACTOR = 5.0;

    /**
     * Draw all of Leelaz's suggestions as colored stones with winrate/playout statistics overlayed
     */
    private void drawLeelazSuggestions(Graphics2D g) {
        List<MoveData> bestMoves = bestMovesUpdated.get();
        if ((Lizzie.board.getData().isBlackToPlay() && Lizzie.optionSetting.isShowBlackSuggestion()
                || !Lizzie.board.getData().isBlackToPlay() && Lizzie.optionSetting.isShowWhiteSuggestion()) && !bestMoves.isEmpty()) {
            int maxPlayouts = bestMoves.stream().max(Comparator.comparingInt(MoveData::getPlayouts)).get().getPlayouts();
            for (MoveData move : bestMoves) {
                boolean isBestMove = bestMoves.get(0) == move;
                if (move.getPlayouts() == 0) // this actually can happen
                    continue;

                double percentPlayouts = (double) move.getPlayouts() / maxPlayouts;

                int[] coordinates = Board.convertNameToCoordinates(move.getCoordinate());
                int suggestionX = x + scaledMargin + squareLength * coordinates[0];
                int suggestionY = y + scaledMargin + squareLength * coordinates[1];

                // -0.32 = Greenest hue, 0 = Reddest hue
                float hue = (float) (-0.32 * Math.max(0, Math.log(percentPlayouts) / HUE_SCALING_FACTOR + 1));
                float saturation = 0.75f; //saturation
                float brightness = 0.85f; //brightness
                int alpha = (int) (MIN_ALPHA + (MAX_ALPHA - MIN_ALPHA) * Math.max(0, Math.log(percentPlayouts) /
                        ALPHA_SCALING_FACTOR + 1));
//                    if (uiConfig.getBoolean("shadows-enabled"))
//                        alpha = 255;

                Color hsbColor = Color.getHSBColor(hue, saturation, brightness);
                Color color = new Color(hsbColor.getRed(), hsbColor.getBlue(), hsbColor.getGreen(), alpha);

                if (branch == null) {
                    drawShadow(g, suggestionX, suggestionY, true, (float) alpha / 255);
                    g.setColor(color);
                    fillCircle(g, suggestionX, suggestionY, stoneRadius);
                }

                if (branch == null || isBestMove && !branch.getCoordinate().equalsIgnoreCase("Pass")
                        && Arrays.equals(Board.convertNameToCoordinates(branch.getCoordinate()), coordinates)) {
                    // highlight LeelaZero's top recommended move
                    int strokeWidth = 1;
                    if (isBestMove) { // this is the best move
                        strokeWidth = 2;
                        g.setColor(Color.RED);
                        g.setStroke(new BasicStroke(strokeWidth));
                    } else {
                        g.setColor(color.darker());
                    }
                    drawCircle(g, suggestionX, suggestionY, stoneRadius - strokeWidth / 2);
                    g.setStroke(new BasicStroke(1));
                }


                if (branch == null && alpha >= MIN_ALPHA_TO_DISPLAY_TEXT || branch != null && !branch.getCoordinate().equalsIgnoreCase("Pass")
                        && Arrays.equals(Board.convertNameToCoordinates(branch.getCoordinate()), coordinates)) {
                    double roundedWinrate = Math.round(move.getWinrate() * 10) / 10.0;
                    g.setColor(Color.BLACK);
                    if (branch != null && Lizzie.board.getData().isBlackToPlay())
                        g.setColor(Color.WHITE);

                    drawString(g, suggestionX, suggestionY, "Open Sans Semibold", Font.PLAIN, String.format("%.1f", roundedWinrate), stoneRadius, stoneRadius * 1.5, 1);
                    drawString(g, suggestionX, suggestionY + stoneRadius * 2 / 5, "Open Sans", getPlayoutsString(move.getPlayouts()), (float) (stoneRadius * 0.8), stoneRadius * 1.4);
                }
            }

            int[] nextMove = Lizzie.board.getNextMoveCoordinate();
            if (Lizzie.optionSetting.isShowNextMove() && nextMove != null) {
                if (Lizzie.board.getData().isBlackToPlay()) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(Color.WHITE);
                }
                int moveX = x + scaledMargin + squareLength * nextMove[0];
                int moveY = y + scaledMargin + squareLength * nextMove[1];
                drawCircle(g, moveX, moveY, stoneRadius + 1); // slightly outside best move circle
            }
        }
    }

    private void drawInfluences(Graphics2D g) {
        if (ArrayUtils.isNotEmpty(influences) && influences.length == Board.BOARD_SIZE * Board.BOARD_SIZE) {
            Composite oldComposite = g.getComposite();
            g.setComposite(COMPOSITE_6);
            try {
                for (int i = 0; i < Board.BOARD_SIZE; i++) {
                    for (int j = 0; j < Board.BOARD_SIZE; j++) {
                        int influenceX = x + scaledMargin + squareLength * i;
                        int influenceY = y + scaledMargin + squareLength * j;
                        drawInfluence(g, influenceX, influenceY, influences[Board.getIndex(i, j)]);
                    }
                }
            } finally {
                g.setComposite(oldComposite);
            }
        }
    }

    private static final Color COLOR_INFLUENCE_BLACK = Color.DARK_GRAY;
    private static final Color COLOR_INFLUENCE_WHITE = Color.WHITE;

    private void drawInfluence(Graphics2D g, int influenceX, int influenceY, double influence) {
        if (Math.abs(influence) < 0.01) {
            return;
        }

        if (influence > 0) {
            g.setColor(COLOR_INFLUENCE_BLACK);
        } else {
            g.setColor(COLOR_INFLUENCE_WHITE);
        }
        int radius = (int) (stoneRadius * 0.5 * Math.abs(influence));
        fillSquare(g, influenceX, influenceY, radius);
    }

    private void drawWoodenBoard(Graphics2D g) {
        if (Lizzie.optionSetting.isShowFancyBoard()) {
            // fancy version
            try {
                int shadowRadius = (int) (boardLength * MARGIN / 6);
                g.drawImage(AssetsManager.getAssetsManager().getImageAsset("assets/board.png"), x - 2 * shadowRadius, y - 2 * shadowRadius, boardLength + 4 * shadowRadius, boardLength + 4 * shadowRadius, null);
                g.setStroke(new BasicStroke(shadowRadius * 2));
                // draw border
                g.setColor(new Color(0, 0, 0, 50));
                g.drawRect(x - shadowRadius, y - shadowRadius, boardLength + 2 * shadowRadius, boardLength + 2 * shadowRadius);
                g.setStroke(new BasicStroke(1));
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            // simple version
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setColor(Lizzie.optionSetting.getBoardColor().toColor());
            g.fillRect(x, y, boardLength, boardLength);
        }
    }

    /**
     * Calculates the lengths and pixel margins from a given boardLength.
     *
     * @param boardLength go board's length in pixels; must be boardLength >= BOARD_SIZE - 1
     * @return an array containing the three outputs: new boardLength, scaledMargin, availableLength
     */
    private static int[] calculatePixelMargins(int boardLength) {
        if (boardLength < Board.BOARD_SIZE - 1)
            throw new IllegalArgumentException("boardLength may not be less than " + (Board.BOARD_SIZE - 1) + ", but was " + boardLength);

        int scaledMargin;
        int availableLength;

        // decrease boardLength until the availableLength will result in square board intersections
        double margin = (Lizzie.frame.showCoordinates ? MARGIN_WITH_COORDS : MARGIN) / Board.BOARD_SIZE * 19.0;
        boardLength++;
        do {
            boardLength--;
            scaledMargin = (int) (margin * boardLength);
            availableLength = boardLength - 2 * scaledMargin;
        }
        while (!((availableLength - 1) % (Board.BOARD_SIZE - 1) == 0));
        // this will be true if BOARD_SIZE - 1 square intersections, plus one line, will fit

        return new int[]{boardLength, scaledMargin, availableLength};
    }

    private void drawShadow(Graphics2D g, int centerX, int centerY, boolean isGhost) {
        drawShadow(g, centerX, centerY, isGhost, 1);
    }

    private void drawShadow(Graphics2D g, int centerX, int centerY, boolean isGhost, float shadowStrength) {
        if (!Lizzie.optionSetting.isShowShadow())
            return;

        final int shadowSize = (int) (stoneRadius * 0.3 * Lizzie.optionSetting.getShadowSize() / 100);
        final int fartherShadowSize = (int) (stoneRadius * 0.17 * Lizzie.optionSetting.getShadowSize() / 100);


        final Paint TOP_GRADIENT_PAINT;
        final Paint LOWER_RIGHT_GRADIENT_PAINT;

        if (isGhost) {
            TOP_GRADIENT_PAINT = new RadialGradientPaint(new Point2D.Float(centerX, centerY),
                    stoneRadius + shadowSize, new float[]{((float) stoneRadius / (stoneRadius + shadowSize)) - 0.0001f, ((float) stoneRadius / (stoneRadius + shadowSize)), 1.0f}, new Color[]{
                    new Color(0, 0, 0, 0), new Color(50, 50, 50, (int) (120 * shadowStrength)), new Color(0, 0, 0, 0)
            });

            LOWER_RIGHT_GRADIENT_PAINT = new RadialGradientPaint(new Point2D.Float(centerX + shadowSize * 2 / 3, centerY + shadowSize * 2 / 3),
                    stoneRadius + fartherShadowSize, new float[]{0.6f, 1.0f}, new Color[]{
                    new Color(0, 0, 0, 180), new Color(0, 0, 0, 0)
            });
        } else {
            TOP_GRADIENT_PAINT = new RadialGradientPaint(new Point2D.Float(centerX, centerY),
                    stoneRadius + shadowSize, new float[]{0.3f, 1.0f}, new Color[]{
                    new Color(50, 50, 50, 150), new Color(0, 0, 0, 0)
            });
            LOWER_RIGHT_GRADIENT_PAINT = new RadialGradientPaint(new Point2D.Float(centerX + shadowSize, centerY + shadowSize),
                    stoneRadius + fartherShadowSize, new float[]{0.6f, 1.0f}, new Color[]{
                    new Color(0, 0, 0, 140), new Color(0, 0, 0, 0)
            });
        }

        final Paint originalPaint = g.getPaint();

        g.setPaint(TOP_GRADIENT_PAINT);
        fillCircle(g, centerX, centerY, stoneRadius + shadowSize);
        if (!isGhost) {
            g.setPaint(LOWER_RIGHT_GRADIENT_PAINT);
            fillCircle(g, centerX + shadowSize, centerY + shadowSize, stoneRadius + fartherShadowSize);
        }
        g.setPaint(originalPaint);
    }

    /**
     * Draws a stone centered at (centerX, centerY)
     */
    private void drawStone(Graphics2D g, Graphics2D gShadow, int centerX, int centerY, Stone color) {
//        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
//                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // if no shadow graphics is supplied, just draw onto the same graphics
        if (gShadow == null)
            gShadow = g;

        switch (color) {
            case BLACK:
                if (Lizzie.optionSetting.isShowFancyStone()) {
                    drawShadow(gShadow, centerX, centerY, false);
                    try {
                        g.drawImage(AssetsManager.getAssetsManager().getImageAsset("assets/black0.png"), centerX - stoneRadius, centerY - stoneRadius, stoneRadius * 2 + 1, stoneRadius * 2 + 1, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    drawShadow(gShadow, centerX, centerY, true);
                    g.setColor(Color.BLACK);
                    fillCircle(g, centerX, centerY, stoneRadius);
                }
                break;

            case WHITE:
                if (Lizzie.optionSetting.isShowFancyStone()) {
                    drawShadow(gShadow, centerX, centerY, false);
                    try {
                        g.drawImage(AssetsManager.getAssetsManager().getImageAsset("assets/white0.png"), centerX - stoneRadius, centerY - stoneRadius, stoneRadius * 2 + 1, stoneRadius * 2 + 1, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    drawShadow(gShadow, centerX, centerY, true);
                    g.setColor(Color.WHITE);
                    fillCircle(g, centerX, centerY, stoneRadius);
                    g.setColor(Color.BLACK);
                    drawCircle(g, centerX, centerY, stoneRadius);
                }
                break;

            case BLACK_GHOST:
                if (Lizzie.optionSetting.isShowFancyStone()) {
                    drawShadow(gShadow, centerX, centerY, true);
                    try {
                        g.drawImage(AssetsManager.getAssetsManager().getImageAsset("assets/black0.png"), centerX - stoneRadius, centerY - stoneRadius, stoneRadius * 2 + 1, stoneRadius * 2 + 1, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    drawShadow(gShadow, centerX, centerY, true);
                    g.setColor(new Color(0, 0, 0));//, uiConfig.getInt("branch-stone-alpha")));
                    fillCircle(g, centerX, centerY, stoneRadius);
                }
                break;

            case WHITE_GHOST:
                if (Lizzie.optionSetting.isShowFancyStone()) {
                    drawShadow(gShadow, centerX, centerY, true);
                    try {
                        g.drawImage(AssetsManager.getAssetsManager().getImageAsset("assets/white0.png"), centerX - stoneRadius, centerY - stoneRadius, stoneRadius * 2 + 1, stoneRadius * 2 + 1, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    drawShadow(gShadow, centerX, centerY, true);
                    g.setColor(new Color(255, 255, 255));//, uiConfig.getInt("branch-stone-alpha")));
                    fillCircle(g, centerX, centerY, stoneRadius);
                    g.setColor(new Color(0, 0, 0));//, uiConfig.getInt("branch-stone-alpha")));
                    drawCircle(g, centerX, centerY, stoneRadius);
                }
                break;

            default:
        }
    }

    /**
     * Draws a stone centered at (centerX, centerY)
     */
    private void drawVariationStone(Graphics2D g, Graphics2D gShadow, int centerX, int centerY, Stone color) {
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // if no shadow graphics is supplied, just draw onto the same graphics
        if (gShadow == null)
            gShadow = g;

        Composite oldComposite = g.getComposite();
        if (Lizzie.optionSetting.isVariationTransparent()) {
            if (color == Stone.BLACK) {
                g.setComposite(COMPOSITE_5);
            } else {
                g.setComposite(COMPOSITE_75);
            }
        }

        try {
            switch (color) {
                case BLACK:
                    if (Lizzie.optionSetting.isShowFancyStone()) {
                        drawShadow(gShadow, centerX, centerY, false);
                        try {
                            g.drawImage(AssetsManager.getAssetsManager().getImageAssetFallThrough("assets/black1.png", "assets/black0.png"), centerX - stoneRadius, centerY - stoneRadius, stoneRadius * 2 + 1, stoneRadius * 2 + 1, null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        drawShadow(gShadow, centerX, centerY, true);
                        g.setColor(Color.BLACK);
                        fillCircle(g, centerX, centerY, stoneRadius);
                    }
                    break;

                case WHITE:
                    if (Lizzie.optionSetting.isShowFancyStone()) {
                        drawShadow(gShadow, centerX, centerY, false);
                        try {
                            g.drawImage(AssetsManager.getAssetsManager().getImageAssetFallThrough("assets/white1.png", "assets/white0.png"), centerX - stoneRadius, centerY - stoneRadius, stoneRadius * 2 + 1, stoneRadius * 2 + 1, null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        drawShadow(gShadow, centerX, centerY, true);
                        g.setColor(Color.WHITE);
                        fillCircle(g, centerX, centerY, stoneRadius);
                        g.setColor(Color.BLACK);
                        drawCircle(g, centerX, centerY, stoneRadius);
                    }
                    break;
                default:
                    break;
            }
        } finally {
            g.setComposite(oldComposite);
        }
    }

    /**
     * Fills in a circle centered at (centerX, centerY) with radius $radius$
     */
    private void fillCircle(Graphics2D g, int centerX, int centerY, int radius) {
        g.fillOval(centerX - radius, centerY - radius, 2 * radius + 1, 2 * radius + 1);
    }

    /**
     * Draws the outline of a circle centered at (centerX, centerY) with radius $radius$
     */
    private void drawCircle(Graphics2D g, int centerX, int centerY, int radius) {
        g.drawOval(centerX - radius, centerY - radius, 2 * radius + 1, 2 * radius + 1);
    }

    /**
     * Fills in a square centered at (centerX, centerY) with radius $radius$
     */
    private void fillSquare(Graphics2D g, int centerX, int centerY, int radius) {
        g.fillRect(centerX - radius, centerY - radius, 2 * radius + 1, 2 * radius + 1);
    }

    /**
     * Draws the outline of a squre centered at (centerX, centerY) with radius $radius$
     */
    private void drawSquare(Graphics2D g, int centerX, int centerY, int radius) {
        g.drawRect(centerX - radius, centerY - radius, 2 * radius + 1, 2 * radius + 1);
    }

    /**
     * Draws a string centered at (x, y) of font $fontString$, whose contents are $string$.
     * The maximum/default fontsize will be $maximumFontHeight$, and the length of the drawn string will be at most maximumFontWidth.
     * The resulting actual size depends on the length of $string$.
     * aboveOrBelow is a param that lets you set:
     * aboveOrBelow = -1 -> y is the top of the string
     * aboveOrBelow = 0  -> y is the vertical center of the string
     * aboveOrBelow = 1  -> y is the bottom of the string
     */
    private void drawString(Graphics2D g, int x, int y, String fontString, int style, String string, float maximumFontHeight, double maximumFontWidth, int aboveOrBelow) {

        Font font = makeFont(fontString, style);

        // set maximum size of font
        font = font.deriveFont((float) (font.getSize2D() * maximumFontWidth / g.getFontMetrics(font).stringWidth(string)));
        font = font.deriveFont(Math.min(maximumFontHeight, font.getSize()));
        g.setFont(font);

        FontMetrics metrics = g.getFontMetrics(font);

        int height = metrics.getAscent() - metrics.getDescent();
        int verticalOffset;
        switch (aboveOrBelow) {
            case -1:
                verticalOffset = height / 2;
                break;

            case 1:
                verticalOffset = -height / 2;
                break;

            default:
                verticalOffset = 0;
        }
        // bounding box for debugging
        // g.drawRect(x-(int)maximumFontWidth/2, y - height/2 + verticalOffset, (int)maximumFontWidth, height+verticalOffset );
        g.drawString(string, x - metrics.stringWidth(string) / 2, y + height / 2 + verticalOffset);
    }

    private void drawString(Graphics2D g, int x, int y, String fontString, String string, float maximumFontHeight, double maximumFontWidth) {
        drawString(g, x, y, fontString, Font.PLAIN, string, maximumFontHeight, maximumFontWidth, 0);
    }

    /**
     * @return a font with kerning enabled
     */
    private Font makeFont(String fontString, int style) {
        Font font = new Font(fontString, style, 100);
        Map<TextAttribute, Object> atts = new HashMap<>();
        atts.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        return font.deriveFont(atts);
    }


    /**
     * @return a shorter, rounded string version of playouts. e.g. 345 -> 345, 1265 -> 1.3k, 44556 -> 45k, 133523 -> 134k, 1234567 -> 1.2m
     */
    private String getPlayoutsString(int playouts) {
        if (Lizzie.optionSetting.isPlayoutsInShortForm()) {
            if (playouts >= 1_000_000) {
                double playoutsDouble = (double) playouts / 100_000; // 1234567 -> 12.34567
                return Math.round(playoutsDouble) / 10.0 + "m";
            } else if (playouts >= 10_000) {
                double playoutsDouble = (double) playouts / 1_000; // 13265 -> 13.265
                return Math.round(playoutsDouble) + "k";
            } else if (playouts >= 1_000) {
                double playoutsDouble = (double) playouts / 100; // 1265 -> 12.65
                return Math.round(playoutsDouble) / 10.0 + "k";
            } else {
                return String.valueOf(playouts);
            }
        } else {
            return String.valueOf(playouts);
        }
    }


    private int[] calculatePixelMargins() {
        return calculatePixelMargins(boardLength);
    }

    /**
     * Set the location to render the board
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Set the maximum boardLength to render the board
     *
     * @param boardLength the boardLength of the board
     */
    public void setBoardLength(int boardLength) {
        this.boardLength = boardLength;
    }

    /**
     * Converts a location on the screen to a location on the board
     *
     * @param x x pixel coordinate
     * @param y y pixel coordinate
     * @return if there is a valid coordinate, an array (x, y) where x and y are between 0 and BOARD_SIZE - 1. Otherwise, returns null
     */
    public int[] convertScreenToCoordinates(int x, int y) {
        int marginLength; // the pixel width of the margins
        int boardLengthWithoutMargins; // the pixel width of the game board without margins

        // calculate a good set of boardLength, scaledMargin, and boardLengthWithoutMargins to use
        int[] calculatedPixelMargins = calculatePixelMargins();
        boardLength = calculatedPixelMargins[0];
        marginLength = calculatedPixelMargins[1];
        boardLengthWithoutMargins = calculatedPixelMargins[2];

        int squareSize = calculateSquareLength(boardLengthWithoutMargins);

        // transform the pixel coordinates to board coordinates
        x = (x - this.x - marginLength + squareSize / 2) / squareSize;
        y = (y - this.y - marginLength + squareSize / 2) / squareSize;

        // return these values if they are valid board coordinates
        if (Board.isValid(x, y))
            return new int[]{x, y};
        else
            return null;
    }

    /**
     * Calculate the boardLength of each intersection square
     *
     * @param availableLength the pixel board length of the game board without margins
     * @return the board length of each intersection square
     */
    private int calculateSquareLength(int availableLength) {
        return availableLength / (Board.BOARD_SIZE - 1);
    }
}