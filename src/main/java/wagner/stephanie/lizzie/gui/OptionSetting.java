package wagner.stephanie.lizzie.gui;

import java.awt.*;

public class OptionSetting {
    private int variationLimit;
    private boolean a1OnTop;
    private Color boardColor;
    private boolean autoHideMoveNumber;
    private boolean analysisModeOn;

    public OptionSetting(int variationLimit, boolean a1OnTop, Color boardColor, boolean autoHideMoveNumber, boolean analysisModeOn) {
        this.variationLimit = variationLimit;
        this.a1OnTop = a1OnTop;
        this.boardColor = boardColor;
        this.autoHideMoveNumber = autoHideMoveNumber;
        this.analysisModeOn = analysisModeOn;
    }

    public OptionSetting() {
        variationLimit = Integer.MAX_VALUE;
        a1OnTop = true;
        boardColor = new Color(0xf0, 0xd2, 0xa0);
        autoHideMoveNumber = true;
        analysisModeOn = true;
    }

    public Color getBoardColor() {
        return boardColor;
    }

    public int getVariationLimit() {
        return variationLimit;
    }

    public boolean isA1OnTop() {
        return a1OnTop;
    }

    public boolean isAutoHideMoveNumber() {
        return autoHideMoveNumber;
    }

    public boolean isAnalysisModeOn() {
        return analysisModeOn;
    }

    public void setAnalysisModeOn(boolean analysisModeOn) {
        this.analysisModeOn = analysisModeOn;
    }
}
