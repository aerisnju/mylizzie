package wagner.stephanie.lizzie.gui;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.awt.*;
import java.util.List;

public class OptionSetting {
    public static class BoardColor {
        private int red;
        private int green;
        private int blue;

        public BoardColor() {
            this(178, 140, 0);
        }

        public BoardColor(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
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

        public Color toColor() {
            return new Color(red, green, blue);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BoardColor that = (BoardColor) o;
            return new EqualsBuilder()
                    .append(red, that.red)
                    .append(green, that.green)
                    .append(blue, that.blue)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(red)
                    .append(green)
                    .append(blue)
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

    private int version;
    private BoardSize boardSize;
    private int variationLimit;
    private boolean showAxis;
    private boolean a1OnTop;
    private boolean showFancyBoard;
    private boolean showFancyStone;
    private boolean showShadow;
    private int shadowSize;
    private BoardColor boardColor;
    private boolean playoutsInShortForm;
    private boolean showNextMove;
    private boolean analysisWindowShow;
    private boolean mouseOverShowMove;
    private boolean showBlackSuggestion;
    private boolean showWhiteSuggestion;
    private String leelazCommandLine;
    private boolean showMoveNumber;
    private int numberOfLastMovesShown;
    private boolean autoEnterTryPlayingMode;
    private boolean mainWindowAlwaysOnTop;
    private int maxAnalysisTimeInMinutes;
    private List<String> engineProfileList;
    private boolean variationTransparent;
    private boolean autoStartAnalyzingAfterPlacingMoves;

    private int mainWindowPosX;
    private int mainWindowPosY;
    private int mainWindowWidth;
    private int mainWindowHeight;
    private int analysisWindowPosX;
    private int analysisWindowPosY;
    private int analysisWindowWidth;
    private int analysisWindowHeight;
    private int winrateHistogramWindowPosX;
    private int winrateHistogramWindowPosY;
    private int winrateHistogramWindowWidth;
    private int winrateHistogramWindowHeight;

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
        boardColor = new BoardColor();
        playoutsInShortForm = false;
        showNextMove = false;
        analysisWindowShow = true;
        mouseOverShowMove = false;
        showBlackSuggestion = true;
        showWhiteSuggestion = true;
        leelazCommandLine = "./leelaz -g -t2 -wnetwork -b0";
        showMoveNumber = true;
        numberOfLastMovesShown = Integer.MAX_VALUE;
        autoEnterTryPlayingMode = false;
        mainWindowAlwaysOnTop = false;
        maxAnalysisTimeInMinutes = 2;
        engineProfileList = ImmutableList.of();
        variationTransparent = false;
        autoStartAnalyzingAfterPlacingMoves = true;

        mainWindowPosX = -1;
        mainWindowPosY = -1;
        // on 1080p windows screens, this is a good width/height
        mainWindowWidth = 657;
        mainWindowHeight = 687;
        analysisWindowPosX = -1;
        analysisWindowPosY = -1;
        analysisWindowWidth = -1;
        analysisWindowHeight = -1;

        winrateHistogramWindowPosX = -1;
        winrateHistogramWindowPosY = -1;
        winrateHistogramWindowWidth = -1;
        winrateHistogramWindowHeight = -1;

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

    public BoardColor getBoardColor() {
        return boardColor;
    }

    public void setBoardColor(BoardColor boardColor) {
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

    public boolean isMainWindowAlwaysOnTop() {
        return mainWindowAlwaysOnTop;
    }

    public void setMainWindowAlwaysOnTop(boolean mainWindowAlwaysOnTop) {
        this.mainWindowAlwaysOnTop = mainWindowAlwaysOnTop;
    }

    public int getMaxAnalysisTimeInMinutes() {
        return maxAnalysisTimeInMinutes;
    }

    public void setMaxAnalysisTimeInMinutes(int maxAnalysisTimeInMinutes) {
        this.maxAnalysisTimeInMinutes = maxAnalysisTimeInMinutes;
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

    public int getMainWindowPosX() {
        return mainWindowPosX;
    }

    public void setMainWindowPosX(int mainWindowPosX) {
        this.mainWindowPosX = mainWindowPosX;
    }

    public int getMainWindowPosY() {
        return mainWindowPosY;
    }

    public void setMainWindowPosY(int mainWindowPosY) {
        this.mainWindowPosY = mainWindowPosY;
    }

    public int getMainWindowWidth() {
        return mainWindowWidth;
    }

    public void setMainWindowWidth(int mainWindowWidth) {
        this.mainWindowWidth = mainWindowWidth;
    }

    public int getMainWindowHeight() {
        return mainWindowHeight;
    }

    public void setMainWindowHeight(int mainWindowHeight) {
        this.mainWindowHeight = mainWindowHeight;
    }

    public int getAnalysisWindowPosX() {
        return analysisWindowPosX;
    }

    public void setAnalysisWindowPosX(int analysisWindowPosX) {
        this.analysisWindowPosX = analysisWindowPosX;
    }

    public int getAnalysisWindowPosY() {
        return analysisWindowPosY;
    }

    public void setAnalysisWindowPosY(int analysisWindowPosY) {
        this.analysisWindowPosY = analysisWindowPosY;
    }

    public int getAnalysisWindowWidth() {
        return analysisWindowWidth;
    }

    public void setAnalysisWindowWidth(int analysisWindowWidth) {
        this.analysisWindowWidth = analysisWindowWidth;
    }

    public int getAnalysisWindowHeight() {
        return analysisWindowHeight;
    }

    public void setAnalysisWindowHeight(int analysisWindowHeight) {
        this.analysisWindowHeight = analysisWindowHeight;
    }

    public int getWinrateHistogramWindowPosX() {
        return winrateHistogramWindowPosX;
    }

    public void setWinrateHistogramWindowPosX(int winrateHistogramWindowPosX) {
        this.winrateHistogramWindowPosX = winrateHistogramWindowPosX;
    }

    public int getWinrateHistogramWindowPosY() {
        return winrateHistogramWindowPosY;
    }

    public void setWinrateHistogramWindowPosY(int winrateHistogramWindowPosY) {
        this.winrateHistogramWindowPosY = winrateHistogramWindowPosY;
    }

    public int getWinrateHistogramWindowWidth() {
        return winrateHistogramWindowWidth;
    }

    public void setWinrateHistogramWindowWidth(int winrateHistogramWindowWidth) {
        this.winrateHistogramWindowWidth = winrateHistogramWindowWidth;
    }

    public int getWinrateHistogramWindowHeight() {
        return winrateHistogramWindowHeight;
    }

    public void setWinrateHistogramWindowHeight(int winrateHistogramWindowHeight) {
        this.winrateHistogramWindowHeight = winrateHistogramWindowHeight;
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
                .append(mouseOverShowMove, that.mouseOverShowMove)
                .append(showBlackSuggestion, that.showBlackSuggestion)
                .append(showWhiteSuggestion, that.showWhiteSuggestion)
                .append(showMoveNumber, that.showMoveNumber)
                .append(numberOfLastMovesShown, that.numberOfLastMovesShown)
                .append(autoEnterTryPlayingMode, that.autoEnterTryPlayingMode)
                .append(mainWindowAlwaysOnTop, that.mainWindowAlwaysOnTop)
                .append(maxAnalysisTimeInMinutes, that.maxAnalysisTimeInMinutes)
                .append(variationTransparent, that.variationTransparent)
                .append(autoStartAnalyzingAfterPlacingMoves, that.autoStartAnalyzingAfterPlacingMoves)
                .append(mainWindowPosX, that.mainWindowPosX)
                .append(mainWindowPosY, that.mainWindowPosY)
                .append(mainWindowWidth, that.mainWindowWidth)
                .append(mainWindowHeight, that.mainWindowHeight)
                .append(analysisWindowPosX, that.analysisWindowPosX)
                .append(analysisWindowPosY, that.analysisWindowPosY)
                .append(analysisWindowWidth, that.analysisWindowWidth)
                .append(analysisWindowHeight, that.analysisWindowHeight)
                .append(winrateHistogramWindowPosX, that.winrateHistogramWindowPosX)
                .append(winrateHistogramWindowPosY, that.winrateHistogramWindowPosY)
                .append(winrateHistogramWindowWidth, that.winrateHistogramWindowWidth)
                .append(winrateHistogramWindowHeight, that.winrateHistogramWindowHeight)
                .append(winrateHistogramWindowShow, that.winrateHistogramWindowShow)
                .append(boardSize, that.boardSize)
                .append(boardColor, that.boardColor)
                .append(leelazCommandLine, that.leelazCommandLine)
                .append(engineProfileList, that.engineProfileList)
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
                .append(mouseOverShowMove)
                .append(showBlackSuggestion)
                .append(showWhiteSuggestion)
                .append(leelazCommandLine)
                .append(showMoveNumber)
                .append(numberOfLastMovesShown)
                .append(autoEnterTryPlayingMode)
                .append(mainWindowAlwaysOnTop)
                .append(maxAnalysisTimeInMinutes)
                .append(engineProfileList)
                .append(variationTransparent)
                .append(autoStartAnalyzingAfterPlacingMoves)
                .append(mainWindowPosX)
                .append(mainWindowPosY)
                .append(mainWindowWidth)
                .append(mainWindowHeight)
                .append(analysisWindowPosX)
                .append(analysisWindowPosY)
                .append(analysisWindowWidth)
                .append(analysisWindowHeight)
                .append(winrateHistogramWindowPosX)
                .append(winrateHistogramWindowPosY)
                .append(winrateHistogramWindowWidth)
                .append(winrateHistogramWindowHeight)
                .append(lastChooserLocation)
                .append(winrateHistogramWindowShow)
                .append(byoYomiSetting)
                .toHashCode();
    }
}
