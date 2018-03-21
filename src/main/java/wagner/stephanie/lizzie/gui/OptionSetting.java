package wagner.stephanie.lizzie.gui;

import java.awt.*;
import java.util.Objects;

public class OptionSetting {
    private int variationLimit;
    private boolean a1OnTop;
    private Color boardColor;
    private boolean autoHideMoveNumber;
    private boolean analysisModeOn;

    private int mainWindowPosX;
    private int mainWindowPosY;
    private int mainWindowWidth;
    private int mainWindowHeight;
    private int analysisWindowPosX;
    private int analysisWindowPosY;
    private int analysisWindowWidth;
    private int analysisWindowHeight;

    public OptionSetting() {
        variationLimit = Integer.MAX_VALUE;
        a1OnTop = true;
        boardColor = new Color(0xf0, 0xd2, 0xa0);
        autoHideMoveNumber = true;
        analysisModeOn = true;

        mainWindowPosX = -1;
        mainWindowPosY = -1;
        // on 1080p windows screens, this is a good width/height
        mainWindowWidth = 657;
        mainWindowHeight = 687;
        analysisWindowPosX = -1;
        analysisWindowPosY = -1;
        analysisWindowWidth = -1;
        analysisWindowHeight = -1;
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

    public boolean isAutoHideMoveNumber() {
        return autoHideMoveNumber;
    }

    public void setAutoHideMoveNumber(boolean autoHideMoveNumber) {
        this.autoHideMoveNumber = autoHideMoveNumber;
    }

    public boolean isAnalysisModeOn() {
        return analysisModeOn;
    }

    public void setAnalysisModeOn(boolean analysisModeOn) {
        this.analysisModeOn = analysisModeOn;
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
}
