package featurecat.lizzie.analysis;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractGtpBasedAnalyzer extends AbstractAnalyzer {
    protected GtpClient gtpClient;
    protected boolean owningTheClient;

    public AbstractGtpBasedAnalyzer(GtpClient gtpClient, boolean owningTheClient) {
        this.gtpClient = gtpClient;
        this.owningTheClient = owningTheClient;
    }

    public GtpClient getGtpClient() {
        return gtpClient;
    }

    @Override
    protected ListenableFuture<List<String>> postRawGtpCommand(String command) {
        return gtpClient.postCommand(command);
    }

    @Override
    protected void doShutdown(long timeout, TimeUnit timeUnit) {
        if (owningTheClient && gtpClient != null) {
            gtpClient.shutdown(timeout, timeUnit);
            owningTheClient = false;
            gtpClient = null;
        }
    }
}
