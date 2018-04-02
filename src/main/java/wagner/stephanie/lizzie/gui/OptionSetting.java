package wagner.stephanie.lizzie.gui;

import java.awt.*;

public class OptionSetting {
    private int variationLimit;
    private boolean a1OnTop;
    private Color boardColor;
    private boolean playoutsInShortForm;
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

    public OptionSetting() {
        variationLimit = Integer.MAX_VALUE;
        a1OnTop = false;
        boardColor = new Color(0xf0, 0xd2, 0xa0);
        playoutsInShortForm = false;
        analysisWindowShow = true;
        mouseOverShowMove = false;
        showBlackSuggestion = true;
        showWhiteSuggestion = true;
        leelazCommandLine = "-g -t2 -wnetwork -b0";
        showMoveNumber = true;
        numberOfLastMovesShown = Integer.MAX_VALUE;
        autoEnterTryPlayingMode = false;
        mainWindowAlwaysOnTop = false;
        maxAnalysisTimeInMinutes = 2;

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
    }

    public int getVariationLimit() {
        return variationLimit;
    }

    public void setVariationLimit(int variationLimit) {
        this.variationLimit = variationLimit;
    }

    public boolean isA1OnTop() {
        return a1OnTop;
    }

    public void setA1OnTop(boolean a1OnTop) {
        this.a1OnTop = a1OnTop;
    }

    public Color getBoardColor() {
        return boardColor;
    }

    public void setBoardColor(Color boardColor) {
        this.boardColor = boardColor;
    }

    public boolean isPlayoutsInShortForm() {
        return playoutsInShortForm;
    }

    public void setPlayoutsInShortForm(boolean playoutsInShortForm) {
        this.playoutsInShortForm = playoutsInShortForm;
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
}
