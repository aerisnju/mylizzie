package wagner.stephanie.lizzie.analysis;

import java.util.function.Consumer;

public interface ExtendedGtpCommand extends GtpCommand {
    default GtpFuture postCommand(String command) {
        return postCommand(command, null);
    }

    GtpFuture postCommand(String command, Consumer<String> continuousOutputConsumer);
}
