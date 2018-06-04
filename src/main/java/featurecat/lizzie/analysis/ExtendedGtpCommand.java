package featurecat.lizzie.analysis;

import java.util.function.Consumer;

public interface ExtendedGtpCommand extends GtpCommand {
    default GtpFuture postCommand(String command) {
        return postCommand(command, null);
    }

    default GtpFuture postCommand(String command, Consumer<String> commandOutputConsumer) {
        return postCommand(command, false, commandOutputConsumer);
    }

    GtpFuture postCommand(String command, boolean continuous, Consumer<String> commandOutputConsumer);
}
