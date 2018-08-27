package featurecat.lizzie.rules;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.math3.util.Precision;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameInfo {
    private static final Logger logger = LogManager.getLogger(GameInfo.class);

    public enum StateChangedItemType {
        BLACK_FIRST, KOMI, BLACK_HANDICAP, WHITE_HANDICAP, HIDDEN_MOVE_COUNT, BLACK_PLAYER_NAME, WHITE_PLAYER_NAME
    }

    private final List<GameInfoChangeListener> listeners = new CopyOnWriteArrayList<>();
    private boolean blackFirst;
    private double komi;
    private int blackHandicap;
    private int whiteHandicap;
    private int hiddenMoveCount;
    private String blackPlayerName;
    private String whitePlayerName;

    public GameInfo() {
        this(true, Board.BOARD_SIZE == 19 ? 7.5 : 6.5, 0, 0, 0, "LeelaZero", "LeelaZero");
    }

    public GameInfo(boolean blackFirst, double komi, int blackHandicap, int whiteHandicap, int hiddenMoveCount, String blackPlayerName, String whitePlayerName) {
        this.blackFirst = blackFirst;
        this.komi = komi;
        this.blackHandicap = blackHandicap;
        this.whiteHandicap = whiteHandicap;
        this.hiddenMoveCount = hiddenMoveCount;
        this.blackPlayerName = blackPlayerName;
        this.whitePlayerName = whitePlayerName;
    }

    public void reset() {
        blackFirst = true;
        komi = Board.BOARD_SIZE == 19 ? 7.5 : 6.5;
        blackHandicap = 0;
        whiteHandicap = 0;
        hiddenMoveCount = 0;
        blackPlayerName = "LeelaZero";
        whitePlayerName = "LeelaZero";

        notifyGameInfoChange(ImmutableSet.of(
                StateChangedItemType.BLACK_FIRST
                , StateChangedItemType.KOMI
                , StateChangedItemType.BLACK_HANDICAP
                , StateChangedItemType.WHITE_HANDICAP
                , StateChangedItemType.HIDDEN_MOVE_COUNT
                , StateChangedItemType.BLACK_PLAYER_NAME
                , StateChangedItemType.WHITE_PLAYER_NAME
        ));
    }

    public boolean isBlackFirst() {
        return blackFirst;
    }

    public void setBlackFirst(boolean blackFirst) {
        boolean notify = this.blackFirst != blackFirst;

        this.blackFirst = blackFirst;

        if (notify) {
            notifyGameInfoChange(ImmutableSet.of(StateChangedItemType.BLACK_FIRST));
        }
    }

    public double getKomi() {
        return komi;
    }

    public void setKomi(double komi) {
        boolean notify = !Precision.equals(this.komi, komi, 0.001);

        this.komi = komi;

        if (notify) {
            notifyGameInfoChange(ImmutableSet.of(StateChangedItemType.KOMI));
        }
    }

    public int getBlackHandicap() {
        return blackHandicap;
    }

    public void setBlackHandicap(int blackHandicap) {
        boolean notify = this.blackHandicap != blackHandicap;

        this.blackHandicap = blackHandicap;

        if (notify) {
            notifyGameInfoChange(ImmutableSet.of(StateChangedItemType.BLACK_HANDICAP));
        }
    }

    public int getWhiteHandicap() {
        return whiteHandicap;
    }

    public void setWhiteHandicap(int whiteHandicap) {
        boolean notify = this.whiteHandicap != whiteHandicap;

        this.whiteHandicap = whiteHandicap;

        if (notify) {
            notifyGameInfoChange(ImmutableSet.of(StateChangedItemType.WHITE_HANDICAP));
        }
    }

    public int getHiddenMoveCount() {
        return hiddenMoveCount;
    }

    public void setHiddenMoveCount(int hiddenMoveCount) {
        boolean notify = this.hiddenMoveCount != hiddenMoveCount;

        this.hiddenMoveCount = hiddenMoveCount;

        if (notify) {
            notifyGameInfoChange(ImmutableSet.of(StateChangedItemType.HIDDEN_MOVE_COUNT));
        }
    }

    public String getBlackPlayerName() {
        return blackPlayerName;
    }

    public void setBlackPlayerName(String blackPlayerName) {
        this.blackPlayerName = blackPlayerName;
        notifyGameInfoChange(ImmutableSet.of(StateChangedItemType.BLACK_PLAYER_NAME));
    }

    public String getWhitePlayerName() {
        return whitePlayerName;
    }

    public void setWhitePlayerName(String whitePlayerName) {
        boolean notify = !Objects.equals(this.whitePlayerName, whitePlayerName);

        this.whitePlayerName = whitePlayerName;

        if (notify) {
            notifyGameInfoChange(ImmutableSet.of(StateChangedItemType.WHITE_PLAYER_NAME));
        }
    }

    private void notifyGameInfoChange(final Set<StateChangedItemType> changedItemTypes) {
        listeners.forEach(listener -> {
            try {
                listener.onGameInfoChanged(GameInfo.this, changedItemTypes);
            } catch (Exception e) {
                logger.error("Exception in listners.", e);
            }
        });
    }

    public void registerGameInfoChangeListener(GameInfoChangeListener listener) {
        listeners.add(listener);
        try {
            listener.onGameInfoChanged(GameInfo.this, ImmutableSet.of(
                    StateChangedItemType.BLACK_FIRST
                    , StateChangedItemType.KOMI
                    , StateChangedItemType.BLACK_HANDICAP
                    , StateChangedItemType.WHITE_HANDICAP
                    , StateChangedItemType.HIDDEN_MOVE_COUNT
                    , StateChangedItemType.BLACK_PLAYER_NAME
                    , StateChangedItemType.WHITE_PLAYER_NAME
            ));
        } catch (Exception e) {
            logger.error("Exception in listners.", e);
        }
    }

    public void unregisterGameInfoChangeListener(GameInfoChangeListener listener) {
        listeners.remove(listener);
    }

    public interface GameInfoChangeListener {
        void onGameInfoChanged(GameInfo info, Set<StateChangedItemType> changedItemTypes);
    }
}
