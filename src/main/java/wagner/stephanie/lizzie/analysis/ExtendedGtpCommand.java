package wagner.stephanie.lizzie.analysis;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.function.Consumer;

public interface ExtendedGtpCommand extends GtpCommand {
    default ListenableFuture<List<String>> postCommand(String command) {
        return postCommand(command, null);
    }

    ListenableFuture<List<String>> postCommand(String command, Consumer<String> continuousOutputConsumer);
}
