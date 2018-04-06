package wagner.stephanie.lizzie.gui;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class WinrateHistogramEntry {
    public static final List<String> COLUMN_NAMES;
    public static final List<Class> COLUMN_CLASSES = ImmutableList.of(Integer.class, String.class, String.class, Double.class, Double.class, Double.class);

    public static final String COLOR_BLACK = "B";
    public static final String COLOR_WHITE = "W";
    public static final String COLOR_NONE = "?";

    static {
        ResourceBundle bundle = ResourceBundle.getBundle("wagner.stephanie.lizzie.i18n.GuiBundle");

        // "Move Number", "Color", "Move", "Black Win%", "White Win%", "Black Win% diff"
        COLUMN_NAMES = ImmutableList.of(
                bundle.getString("WinrateHistogramDialog.histogramTable.title.moveNumber")
                , bundle.getString("WinrateHistogramDialog.histogramTable.title.color")
                , bundle.getString("WinrateHistogramDialog.histogramTable.title.move")
                , bundle.getString("WinrateHistogramDialog.histogramTable.title.blackWinrate")
                , bundle.getString("WinrateHistogramDialog.histogramTable.title.whiteWinrate")
                , bundle.getString("WinrateHistogramDialog.histogramTable.title.blackWinDiff")
        );
    }

    private int moveNumber;
    private String color;
    private String move;
    private double blackWinrate;
    private double blackWindiff;

    public WinrateHistogramEntry(int moveNumber, String color, String move, double blackWinrate, double blackWindiff) {
        this.moveNumber = moveNumber;
        this.color = color;
        this.move = move;
        this.blackWinrate = blackWinrate;
        this.blackWindiff = blackWindiff;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public void setMoveNumber(int moveNumber) {
        this.moveNumber = moveNumber;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMove() {
        return move;
    }

    public void setMove(String move) {
        this.move = move;
    }

    public double getBlackWinrate() {
        return blackWinrate;
    }

    public void setBlackWinrate(double blackWinrate) {
        this.blackWinrate = blackWinrate;
    }

    public double getBlackWindiff() {
        return blackWindiff;
    }

    public void setBlackWindiff(double blackWindiff) {
        this.blackWindiff = blackWindiff;
    }

    public double getWhiteWinrate() {
        return 100 - blackWinrate;
    }

    public void setWhiteWinrate(double whiteWinrate) {
        blackWinrate = 100 - whiteWinrate;
    }

    public double getWhiteWindiff() {
        return -blackWindiff;
    }

    public void setWhiteWindiff(double whiteWindiff) {
        blackWindiff = -whiteWindiff;
    }

    public Object getDataByIndex(int index) {
        switch (index) {
            case 0:
                return getMoveNumber();
            case 1:
                return getColor();
            case 2:
                return getMove();
            case 3:
                return getBlackWinrate();
            case 4:
                return getWhiteWinrate();
            case 5:
                return getBlackWindiff();
            default:
                return "";
        }
    }
}
