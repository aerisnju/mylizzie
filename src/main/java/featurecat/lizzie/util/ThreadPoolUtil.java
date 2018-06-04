package featurecat.lizzie.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {
    private static final Logger logger = LogManager.getLogger(ThreadPoolUtil.class);

    public static void shutdownAndAwaitTermination(ExecutorService pool) {
        shutdownAndAwaitTermination(pool, 60, TimeUnit.SECONDS);
    }

    public static void shutdownAndAwaitTermination(ExecutorService pool, long timeoutEachTrial, TimeUnit unit) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(timeoutEachTrial, unit)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(timeoutEachTrial, unit)) {
                    logger.error("Error: Pool not shutdown!");
                }
            }
        } catch (InterruptedException e) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
