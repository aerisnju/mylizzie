package wagner.stephanie.lizzie.gui;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import wagner.stephanie.lizzie.Lizzie;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class OptionSetting {
    //<editor-fold desc="Option elements">
    public static class ColorSetting {
        private int red;
        private int green;
        private int blue;
        private int alpha;

        public ColorSetting() {
            red = 255;
            green = 255;
            blue = 255;
            alpha = 255;
        }

        public ColorSetting(Color color) {
            this(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        }

        public ColorSetting(int red, int green, int blue) {
            this(red, green, blue, 255);
        }

        public ColorSetting(int red, int green, int blue, int alpha) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }

        public int getRed() {
            return red;
        }

        public void setRed(int red) {
            this.red = Ints.constrainToRange(red, 0, 255);
        }

        public int getGreen() {
            return green;
        }

        public void setGreen(int green) {
            this.green = Ints.constrainToRange(green, 0, 255);
        }

        public int getBlue() {
            return blue;
        }

        public void setBlue(int blue) {
            this.blue = Ints.constrainToRange(blue, 0, 255);
        }

        public int getAlpha() {
            return alpha;
        }

        public void setAlpha(int alpha) {
            this.alpha = alpha;
        }

        public Color toColor() {
            return new Color(red, green, blue, alpha);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ColorSetting that = (ColorSetting) o;
            return new EqualsBuilder()
                    .append(red, that.red)
                    .append(green, that.green)
                    .append(blue, that.blue)
                    .append(alpha, that.alpha)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(red)
                    .append(green)
                    .append(blue)
                    .append(alpha)
                    .toHashCode();
        }
    }

    public static class BoardSize {
        public static final int DEFAULT_SIZE = 19;
        private int width;
        private int height;

        public BoardSize() {
            this(DEFAULT_SIZE, DEFAULT_SIZE);
        }

        public BoardSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BoardSize boardSize = (BoardSize) o;
            return new EqualsBuilder()
                    .append(width, boardSize.width)
                    .append(height, boardSize.height)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(width)
                    .append(height)
                    .toHashCode();
        }
    }

    public static class ByoYomiSetting {
        private int byoYomiTime;
        private boolean stopThinkingWhenCountingDown;

        public ByoYomiSetting() {
            byoYomiTime = 30;
            stopThinkingWhenCountingDown = true;
        }

        public int getByoYomiTime() {
            return byoYomiTime;
        }

        public void setByoYomiTime(int byoYomiTime) {
            this.byoYomiTime = byoYomiTime;
        }

        public boolean isStopThinkingWhenCountingDown() {
            return stopThinkingWhenCountingDown;
        }

        public void setStopThinkingWhenCountingDown(boolean stopThinkingWhenCountingDown) {
            this.stopThinkingWhenCountingDown = stopThinkingWhenCountingDown;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ByoYomiSetting that = (ByoYomiSetting) o;
            return new EqualsBuilder()
                    .append(byoYomiTime, that.byoYomiTime)
                    .append(stopThinkingWhenCountingDown, that.stopThinkingWhenCountingDown)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(byoYomiTime)
                    .append(stopThinkingWhenCountingDown)
                    .toHashCode();
        }
    }

    public static class WindowState {
        private boolean maximizedHorizontal;
        private boolean maximizedVertical;
        private boolean iconified;
        private int x;
        private int y;
        private int width;
        private int height;

        public WindowState() {
            this(false, false, false, 100, 100, 600, 400);
        }

        public WindowState(boolean maximizedHorizontal, boolean maximizedVertical, boolean iconified, int x, int y, int width, int height) {
            this.maximizedHorizontal = maximizedHorizontal;
            this.maximizedVertical = maximizedVertical;
            this.iconified = iconified;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public WindowState(Window window) {
            recordStateFrom(window);
        }

        public boolean isMaximizedHorizontal() {
            return maximizedHorizontal;
        }

        public void setMaximizedHorizontal(boolean maximizedHorizontal) {
            this.maximizedHorizontal = maximizedHorizontal;
        }

        public boolean isMaximizedVertical() {
            return maximizedVertical;
        }

        public void setMaximizedVertical(boolean maximizedVertical) {
            this.maximizedVertical = maximizedVertical;
        }

        public boolean isIconified() {
            return iconified;
        }

        public void setIconified(boolean iconified) {
            this.iconified = iconified;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void applyStateTo(Window window) {
            int x = Math.max(this.x, 10);
            int y = Math.max(this.y, 10);
            int width = Math.max(this.width, 100);
            int height = Math.max(this.height, 100);
            window.setBounds(x, y, width, height);

            if (window instanceof Frame) {
                Frame frame = (Frame) window;
                if (iconified) {
                    frame.setExtendedState(frame.getExtendedState() | JFrame.ICONIFIED);
                } else if (maximizedHorizontal && maximizedVertical) {
                    frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
                } else {
                    if (maximizedHorizontal) {
                        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_HORIZ);
                    }
                    if (maximizedVertical) {
                        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_VERT);
                    }
                }
            }
        }

        public void recordStateFrom(Window window) {
            if (window instanceof Frame) {
                Frame frame = (Frame) window;
                int frameState = frame.getExtendedState();
                iconified = (frameState & JFrame.ICONIFIED) != 0;
                maximizedHorizontal = (frameState & JFrame.MAXIMIZED_HORIZ) != 0;
                maximizedVertical = (frameState & JFrame.MAXIMIZED_VERT) != 0;
            } else {
                iconified = false;
                maximizedHorizontal = false;
                maximizedVertical = false;
            }

            x = window.getX();
            y = window.getY();
            width = window.getWidth();
            height = window.getHeight();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WindowState that = (WindowState) o;
            return new EqualsBuilder()
                    .append(maximizedHorizontal, that.maximizedHorizontal)
                    .append(maximizedVertical, that.maximizedVertical)
                    .append(iconified, that.iconified)
                    .append(x, that.x)
                    .append(y, that.y)
                    .append(width, that.width)
                    .append(height, that.height)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(maximizedHorizontal)
                    .append(maximizedVertical)
                    .append(iconified)
                    .append(x)
                    .append(y)
                    .append(width)
                    .append(height)
                    .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("maximizedHorizontal", maximizedHorizontal)
                    .append("maximizedVertical", maximizedVertical)
                    .append("iconified", iconified)
                    .append("x", x)
                    .append("y", y)
                    .append("width", width)
                    .append("height", height)
                    .toString();
        }
    }
    //</editor-fold>

    private int version;
    private BoardSize boardSize;
    private int variationLimit;
    private boolean showAxis;
    private boolean a1OnTop;
    private boolean showFancyBoard;
    private boolean showFancyStone;
    private boolean showShadow;
    private int shadowSize;
    private ColorSetting boardColor;
    private boolean playoutsInShortForm;
    private boolean showNextMove;
    private boolean analysisWindowShow;
    private boolean gtpConsoleWindowShow;
    private boolean mouseOverShowMove;
    private boolean showBlackSuggestion;
    private boolean showWhiteSuggestion;
    private String leelazCommandLine;
    private boolean showMoveNumber;
    private int numberOfLastMovesShown;
    private boolean autoEnterTryPlayingMode;
    private boolean mainWindowAlwaysOnTop;
    private int maxAnalysisTime;
    private List<String> engineProfileList;
    private boolean variationTransparent;
    private boolean autoStartAnalyzingAfterPlacingMoves;
    private ColorSetting bestSuggestionColor;
    private boolean alwaysShowBlackWinrate;
    private boolean showWhiteWinrateWithWhiteFonts;

    private WindowState mainWindowState;
    private WindowState analysisWindowState;
    private WindowState winrateHistogramWindowState;
    private WindowState gtpConsoleWindowState;

    private String lastChooserLocation;
    private boolean winrateHistogramWindowShow;

    private ByoYomiSetting byoYomiSetting;

    public OptionSetting() {
        version = 1;
        boardSize = new BoardSize();
        variationLimit = Integer.MAX_VALUE;
        showAxis = true;
        a1OnTop = false;
        showFancyBoard = true;
        showFancyStone = true;
        showShadow = true;
        shadowSize = 100;
        boardColor = new ColorSetting(178, 140, 0);
        playoutsInShortForm = false;
        showNextMove = false;
        analysisWindowShow = true;
        gtpConsoleWindowShow = false;
        mouseOverShowMove = false;
        showBlackSuggestion = true;
        showWhiteSuggestion = true;
        leelazCommandLine = "./leelaz -g -t2 -wnetwork -b0";
        showMoveNumber = true;
        numberOfLastMovesShown = Integer.MAX_VALUE;
        autoEnterTryPlayingMode = false;
        mainWindowAlwaysOnTop = false;
        maxAnalysisTime = 120;
        engineProfileList = ImmutableList.of();
        variationTransparent = false;
        autoStartAnalyzingAfterPlacingMoves = true;
        bestSuggestionColor = new ColorSetting(Color.RED);
        alwaysShowBlackWinrate = false;
        showWhiteWinrateWithWhiteFonts = false;

        // on 1080p windows screens, this is a good width/height
        mainWindowState = new WindowState(false, false, false, 100, 100, 657, 687);
        analysisWindowState = new WindowState();
        winrateHistogramWindowState = new WindowState();
        gtpConsoleWindowState = new WindowState();

        lastChooserLocation = ".";
        winrateHistogramWindowShow = true;
        byoYomiSetting = new ByoYomiSetting();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public BoardSize getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(BoardSize boardSize) {
        this.boardSize = boardSize;
    }

    public int getVariationLimit() {
        return variationLimit;
    }

    public void setVariationLimit(int variationLimit) {
        this.variationLimit = variationLimit;
    }

    public boolean isShowAxis() {
        return showAxis;
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
    }

    public boolean isA1OnTop() {
        return a1OnTop;
    }

    public void setA1OnTop(boolean a1OnTop) {
        this.a1OnTop = a1OnTop;
    }

    public boolean isShowFancyBoard() {
        return showFancyBoard;
    }

    public void setShowFancyBoard(boolean showFancyBoard) {
        this.showFancyBoard = showFancyBoard;
    }

    public boolean isShowFancyStone() {
        return showFancyStone;
    }

    public void setShowFancyStone(boolean showFancyStone) {
        this.showFancyStone = showFancyStone;
    }

    public boolean isShowShadow() {
        return showShadow;
    }

    public void setShowShadow(boolean showShadow) {
        this.showShadow = showShadow;
    }

    public int getShadowSize() {
        return shadowSize;
    }

    public void setShadowSize(int shadowSize) {
        this.shadowSize = shadowSize;
    }

    public ColorSetting getBoardColor() {
        return boardColor;
    }

    public void setBoardColor(ColorSetting boardColor) {
        this.boardColor = boardColor;
    }

    public boolean isPlayoutsInShortForm() {
        return playoutsInShortForm;
    }

    public void setPlayoutsInShortForm(boolean playoutsInShortForm) {
        this.playoutsInShortForm = playoutsInShortForm;
    }

    public boolean isShowNextMove() {
        return showNextMove;
    }

    public void setShowNextMove(boolean showNextMove) {
        this.showNextMove = showNextMove;
    }

    public boolean isAnalysisWindowShow() {
        return analysisWindowShow;
    }

    public void setAnalysisWindowShow(boolean analysisWindowShow) {
        this.analysisWindowShow = analysisWindowShow;
    }

    public boolean isGtpConsoleWindowShow() {
        return gtpConsoleWindowShow;
    }

    public void setGtpConsoleWindowShow(boolean gtpConsoleWindowShow) {
        this.gtpConsoleWindowShow = gtpConsoleWindowShow;
    }

    public boolean isMouseOverShowMove() {
        return mouseOverShowMove;
    }

    public void setMouseOverShowMove(boolean mouseOverShowMove) {
        this.mouseOverShowMove = mouseOverShowMove;
    }

    public String getLeelazCommandLine() {
        return leelazCommandLine;
    }

    public void setLeelazCommandLine(String leelazCommandLine) {
        this.leelazCommandLine = leelazCommandLine;
    }

    public boolean isShowMoveNumber() {
        return showMoveNumber;
    }

    public void setShowMoveNumber(boolean showMoveNumber) {
        this.showMoveNumber = showMoveNumber;
    }

    public boolean isShowBlackSuggestion() {
        return showBlackSuggestion;
    }

    public void setShowBlackSuggestion(boolean showBlackSuggestion) {
        this.showBlackSuggestion = showBlackSuggestion;
    }

    public boolean isShowWhiteSuggestion() {
        return showWhiteSuggestion;
    }

    public void setShowWhiteSuggestion(boolean showWhiteSuggestion) {
        this.showWhiteSuggestion = showWhiteSuggestion;
    }

    public int getNumberOfLastMovesShown() {
        return numberOfLastMovesShown;
    }

    public void setNumberOfLastMovesShown(int numberOfLastMovesShown) {
        this.numberOfLastMovesShown = numberOfLastMovesShown;
    }

    public boolean isAutoEnterTryPlayingMode() {
        return autoEnterTryPlayingMode;
    }

    public void setAutoEnterTryPlayingMode(boolean autoEnterTryPlayingMode) {
        this.autoEnterTryPlayingMode = autoEnterTryPlayingMode;
    }

    public ColorSetting getBestSuggestionColor() {
        return bestSuggestionColor;
    }

    public void setBestSuggestionColor(ColorSetting bestSuggestionColor) {
        this.bestSuggestionColor = bestSuggestionColor;
    }

    public boolean isMainWindowAlwaysOnTop() {
        return mainWindowAlwaysOnTop;
    }

    public void setMainWindowAlwaysOnTop(boolean mainWindowAlwaysOnTop) {
        this.mainWindowAlwaysOnTop = mainWindowAlwaysOnTop;
    }

    public int getMaxAnalysisTime() {
        return maxAnalysisTime;
    }

    public void setMaxAnalysisTime(int maxAnalysisTime) {
        this.maxAnalysisTime = maxAnalysisTime;
    }

    public List<String> getEngineProfileList() {
        return engineProfileList;
    }

    public void setEngineProfileList(List<String> engineProfileList) {
        this.engineProfileList = engineProfileList;
    }

    public boolean isVariationTransparent() {
        return variationTransparent;
    }

    public void setVariationTransparent(boolean variationTransparent) {
        this.variationTransparent = variationTransparent;
    }

    public boolean isAutoStartAnalyzingAfterPlacingMoves() {
        return autoStartAnalyzingAfterPlacingMoves;
    }

    public void setAutoStartAnalyzingAfterPlacingMoves(boolean autoStartAnalyzingAfterPlacingMoves) {
        this.autoStartAnalyzingAfterPlacingMoves = autoStartAnalyzingAfterPlacingMoves;
    }

    public boolean isAlwaysShowBlackWinrate() {
        return alwaysShowBlackWinrate;
    }

    public void setAlwaysShowBlackWinrate(boolean alwaysShowBlackWinrate) {
        this.alwaysShowBlackWinrate = alwaysShowBlackWinrate;
    }

    public boolean isShowWhiteWinrateWithWhiteFonts() {
        return showWhiteWinrateWithWhiteFonts;
    }

    public void setShowWhiteWinrateWithWhiteFonts(boolean showWhiteWinrateWithWhiteFonts) {
        this.showWhiteWinrateWithWhiteFonts = showWhiteWinrateWithWhiteFonts;
    }

    public WindowState getMainWindowState() {
        return mainWindowState;
    }

    public void setMainWindowState(WindowState mainWindowState) {
        this.mainWindowState = mainWindowState;
    }

    public void setMainWindowState(Window frame) {
        mainWindowState = new WindowState(frame);
    }

    public WindowState getAnalysisWindowState() {
        return analysisWindowState;
    }

    public void setAnalysisWindowState(WindowState analysisWindowState) {
        this.analysisWindowState = analysisWindowState;
    }

    public void setAnalysisWindowState(Window window) {
        analysisWindowState = new WindowState(window);
    }

    public WindowState getWinrateHistogramWindowState() {
        return winrateHistogramWindowState;
    }

    public void setWinrateHistogramWindowState(WindowState winrateHistogramWindowState) {
        this.winrateHistogramWindowState = winrateHistogramWindowState;
    }

    public void setWinrateHistogramWindowState(Window window) {
        winrateHistogramWindowState = new WindowState(window);
    }

    public WindowState getGtpConsoleWindowState() {
        return gtpConsoleWindowState;
    }

    public void setGtpConsoleWindowState(WindowState gtpConsoleWindowState) {
        this.gtpConsoleWindowState = gtpConsoleWindowState;
    }

    public void setGtpConsoleWindowState(Window window) {
        gtpConsoleWindowState = new WindowState(window);
    }

    public String getLastChooserLocation() {
        return lastChooserLocation;
    }

    public void setLastChooserLocation(String lastChooserLocation) {
        this.lastChooserLocation = lastChooserLocation;
    }

    public boolean isWinrateHistogramWindowShow() {
        return winrateHistogramWindowShow;
    }

    public void setWinrateHistogramWindowShow(boolean winrateHistogramWindowShow) {
        this.winrateHistogramWindowShow = winrateHistogramWindowShow;
    }

    public ByoYomiSetting getByoYomiSetting() {
        return byoYomiSetting;
    }

    public void setByoYomiSetting(ByoYomiSetting byoYomiSetting) {
        this.byoYomiSetting = byoYomiSetting;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptionSetting that = (OptionSetting) o;
        return new EqualsBuilder()
                .append(version, that.version)
                .append(variationLimit, that.variationLimit)
                .append(showAxis, that.showAxis)
                .append(a1OnTop, that.a1OnTop)
                .append(showFancyBoard, that.showFancyBoard)
                .append(showFancyStone, that.showFancyStone)
                .append(showShadow, that.showShadow)
                .append(shadowSize, that.shadowSize)
                .append(playoutsInShortForm, that.playoutsInShortForm)
                .append(showNextMove, that.showNextMove)
                .append(analysisWindowShow, that.analysisWindowShow)
                .append(gtpConsoleWindowShow, that.gtpConsoleWindowShow)
                .append(mouseOverShowMove, that.mouseOverShowMove)
                .append(showBlackSuggestion, that.showBlackSuggestion)
                .append(showWhiteSuggestion, that.showWhiteSuggestion)
                .append(showMoveNumber, that.showMoveNumber)
                .append(numberOfLastMovesShown, that.numberOfLastMovesShown)
                .append(autoEnterTryPlayingMode, that.autoEnterTryPlayingMode)
                .append(mainWindowAlwaysOnTop, that.mainWindowAlwaysOnTop)
                .append(maxAnalysisTime, that.maxAnalysisTime)
                .append(variationTransparent, that.variationTransparent)
                .append(autoStartAnalyzingAfterPlacingMoves, that.autoStartAnalyzingAfterPlacingMoves)
                .append(alwaysShowBlackWinrate, that.alwaysShowBlackWinrate)
                .append(showWhiteWinrateWithWhiteFonts, that.showWhiteWinrateWithWhiteFonts)
                .append(winrateHistogramWindowShow, that.winrateHistogramWindowShow)
                .append(boardSize, that.boardSize)
                .append(boardColor, that.boardColor)
                .append(leelazCommandLine, that.leelazCommandLine)
                .append(engineProfileList, that.engineProfileList)
                .append(bestSuggestionColor, that.bestSuggestionColor)
                .append(mainWindowState, that.mainWindowState)
                .append(analysisWindowState, that.analysisWindowState)
                .append(winrateHistogramWindowState, that.winrateHistogramWindowState)
                .append(gtpConsoleWindowState, that.gtpConsoleWindowState)
                .append(lastChooserLocation, that.lastChooserLocation)
                .append(byoYomiSetting, that.byoYomiSetting)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(version)
                .append(boardSize)
                .append(variationLimit)
                .append(showAxis)
                .append(a1OnTop)
                .append(showFancyBoard)
                .append(showFancyStone)
                .append(showShadow)
                .append(shadowSize)
                .append(boardColor)
                .append(playoutsInShortForm)
                .append(showNextMove)
                .append(analysisWindowShow)
                .append(gtpConsoleWindowShow)
                .append(mouseOverShowMove)
                .append(showBlackSuggestion)
                .append(showWhiteSuggestion)
                .append(leelazCommandLine)
                .append(showMoveNumber)
                .append(numberOfLastMovesShown)
                .append(autoEnterTryPlayingMode)
                .append(mainWindowAlwaysOnTop)
                .append(maxAnalysisTime)
                .append(engineProfileList)
                .append(variationTransparent)
                .append(autoStartAnalyzingAfterPlacingMoves)
                .append(bestSuggestionColor)
                .append(alwaysShowBlackWinrate)
                .append(showWhiteWinrateWithWhiteFonts)
                .append(mainWindowState)
                .append(analysisWindowState)
                .append(winrateHistogramWindowState)
                .append(gtpConsoleWindowState)
                .append(lastChooserLocation)
                .append(winrateHistogramWindowShow)
                .append(byoYomiSetting)
                .toHashCode();
    }

    @Override
    public String toString() {
        return Lizzie.gson.toJson(this);
    }
}
