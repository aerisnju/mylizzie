package wagner.stephanie.lizzie.analysis;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.lang3.StringUtils;
import wagner.stephanie.lizzie.util.GenericLizzieException;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class GtpBasedAnalyzerBuilder {
    public static final String REASON = "REASON";
    public static final String ENGINE_NOT_FUNCTION = "ENGINE_NOT_FUNCTION";
    public static final String ENGINE_NOT_SUPPORTED = "ENGINE_NOT_SUPPORTED";

    private GtpClient gtpClient;

    public GtpBasedAnalyzerBuilder setGtpClient(GtpClient gtpClient) {
        this.gtpClient = gtpClient;

        return this;
    }

    public AbstractGtpBasedAnalyzer build() {
        if (!gtpClient.isRunning()) {
            gtpClient.start();
        }

        // Check for engine ready
        ListenableFuture<List<String>> future = gtpClient.postCommand("name");
        List<String> nameResponse = null;
        try {
            nameResponse = future.get(60, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            // Do nothing
        }

        if (!GtpCommand.isSuccessfulResponse(nameResponse)) {
            throw new GenericLizzieException(ImmutableMap.of(REASON, ENGINE_NOT_FUNCTION));
        }
        String name = GtpCommand.getLineWithoutResponseHeader(nameResponse, 0).trim();
        if (name.equals("Leela Zero")) {
            if (isNewOfficialEngine()) {
                return new OfficialLeelazAnalyzer(gtpClient);
            } else {
                detectCorrectModifiedLeelazEngine();
                return new ClassicModifiedLeelazAnalyzer(gtpClient);
            }
        } else {
            throw new GenericLizzieException(ImmutableMap.of(REASON, ENGINE_NOT_SUPPORTED));
        }
    }

    private boolean isNewOfficialEngine() {
        List<String> listCommandsResponse = null;
        ListenableFuture<List<String>> future = gtpClient.postCommand("list_commands");
        try {
            listCommandsResponse = future.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            // Do nothing
        }

        if (!GtpCommand.isSuccessfulResponse(listCommandsResponse)) {
            throw new GenericLizzieException(ImmutableMap.of(REASON, ENGINE_NOT_FUNCTION));
        }

        GtpCommand.removeResponseHeaderInPlace(listCommandsResponse);
        assert listCommandsResponse != null;
        return listCommandsResponse.stream().anyMatch(s -> StringUtils.equalsIgnoreCase(s.trim(), "lz-analyze"));
    }

    private void detectCorrectModifiedLeelazEngine() {
        CorrectModifiedLeelazEngineDetector detector = new CorrectModifiedLeelazEngineDetector();
        gtpClient.registerStderrLineConsumer(detector);
        try {
            gtpClient.postCommand("time_left b 0 0");
            if (!detector.latch.await(8, TimeUnit.SECONDS)) {
                throw new GenericLizzieException(ImmutableMap.of(REASON, ENGINE_NOT_SUPPORTED));
            }
            gtpClient.sendCommand("name"); // Stop
        } catch (InterruptedException e) {
            // Do nothing
        } finally {
            gtpClient.unregisterStderrLineConsumer(detector);
        }
    }

    private static class CorrectModifiedLeelazEngineDetector implements Consumer<String> {
        private CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void accept(String line) {
            if (line.startsWith("~begin")) {
                latch.countDown();
            }
        }
    }
}
