package featurecat.lizzie.rules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameStatusManager {
    private static final Logger logger = LogManager.getLogger(GameStatusManager.class);

    private GameInfo gameInfo;
    private List<GameInfoChangeListener> listeners;

    public GameStatusManager() {
        gameInfo = new GameInfo();
        listeners = new CopyOnWriteArrayList<>();
    }

    public void updateKomi(double newKomi) {
        gameInfo = gameInfo.newKomi(newKomi);
        notifyGameInfoChange();
    }

    private void notifyGameInfoChange() {
        listeners.forEach(listener -> {
            try {
                listener.gameInfoChanged(gameInfo);
            } catch (Exception e) {
                logger.error("Exception in listners.", e);
            }
        });
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public void registerGameInfoChangeListener(GameInfoChangeListener listener) {
        listeners.add(listener);
        try {
            listener.gameInfoChanged(gameInfo);
        } catch (Exception e) {
            logger.error("Exception in listners.", e);
        }
    }

    public void unregisterGameInfoChangeListener(GameInfoChangeListener listener) {
        listeners.remove(listener);
    }

    public interface GameInfoChangeListener {
        void gameInfoChanged(GameInfo newGameInfo) throws Exception;
    }
}
