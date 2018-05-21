package wagner.stephanie.lizzie.analysis;

import com.zaxxer.nuprocess.NuProcessBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import wagner.stephanie.lizzie.util.ArgumentTokenizer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class GtpClientBuilder {
    private List<String> commandLine = null;
    private Map<String, String> environment = null;
    private Path initialCurrentDirectory = null;

    public GtpClientBuilder setCommandLine(List<String> commandLine) {
        this.commandLine = commandLine;
        return this;
    }

    public GtpClientBuilder setCommandLine(String commandLine) {
        this.commandLine = ArgumentTokenizer.tokenize(commandLine);
        return this;
    }

    public GtpClientBuilder setEnvironment(Map<String, String> environment) {
        this.environment = environment;
        return this;
    }

    public GtpClientBuilder setInitialCurrentDirectory(String initialCurrentDirectory) {
        this.initialCurrentDirectory = Paths.get(initialCurrentDirectory).normalize();
        return this;
    }

    public GtpClientBuilder setInitialCurrentDirectory(Path initialCurrentDirectory) {
        this.initialCurrentDirectory = initialCurrentDirectory.normalize();
        return this;
    }

    public GtpClient build() {
        if (CollectionUtils.isEmpty(commandLine)) {
            throw new IllegalArgumentException("No command line for a GTP process.");
        }

        GeneralGtpClient gtpClient;

        final boolean environmentSet = MapUtils.isNotEmpty(environment);
        final boolean initialCurrentDirectorySet = initialCurrentDirectory != null && Files.exists(initialCurrentDirectory);
        if (environmentSet || initialCurrentDirectorySet) {
            gtpClient = new GeneralGtpClient(commandLine) {
                @Override
                protected void setUpOtherProcessParameters(NuProcessBuilder processBuilder) {
                    super.setUpOtherProcessParameters(processBuilder);
                    if (initialCurrentDirectorySet) {
                        processBuilder.setCwd(initialCurrentDirectory);
                    }
                    if (environmentSet) {
                        processBuilder.environment().putAll(environment);
                    }
                }
            };
        } else {
            gtpClient = new GeneralGtpClient(commandLine);
        }

        return gtpClient;
    }
}
