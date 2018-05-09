package wagner.stephanie.lizzie.analysis;

import com.google.common.util.concurrent.ListenableFuture;
import com.zaxxer.nuprocess.NuAbstractProcessHandler;
import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import com.zaxxer.nuprocess.NuProcessHandler;
import org.jtrim2.utils.ObjectFinalizer;
import wagner.stephanie.lizzie.util.ArgumentTokenizer;
import wagner.stephanie.lizzie.util.ThreadPoolUtil;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class GeneralGtpClient implements GtpClient {
    protected class GeneralGtpProcessHandler extends NuAbstractProcessHandler implements Closeable {
        private final ObjectFinalizer objectFinalizer;

        protected ExecutorService stdoutProcessor;
        protected ExecutorService stderrProcessor;
        protected ExecutorService miscProcessor;

        protected StringBuilder stdoutLineBuilder;
        protected StringBuilder stderrLineBuilder;
        protected boolean inCommandResponse;

        public GeneralGtpProcessHandler() {
            objectFinalizer = new ObjectFinalizer(this::doCleanup, "GtpClientHandler.cleanup");

            stdoutProcessor = Executors.newSingleThreadExecutor();
            stderrProcessor = Executors.newSingleThreadExecutor();
            miscProcessor = Executors.newSingleThreadExecutor();
            stdoutLineBuilder = new StringBuilder(2048);
            stderrLineBuilder = new StringBuilder(2048);
            inCommandResponse = false;
        }

        @Override
        public void onStart(final NuProcess nuProcess) {
            miscProcessor.execute(() -> engineStartedObserverList.forEach(observer -> observer.accept(nuProcess.getPID())));
        }

        @Override
        public void onExit(final int statusCode) {
            GeneralGtpFuture future;

            while ((future = runningCommandQueue.poll()) != null) {
                future.markComplete();
            }

            while ((future = stagineCommandQueue.poll()) != null) {
                future.markComplete();
            }

            miscProcessor.execute(() -> engineExitObserverList.forEach(observer -> observer.accept(statusCode)));
        }

        @Override
        public void onStdout(final ByteBuffer buffer, final boolean closed) {
            if (!closed) {
                final byte[] bytes = new byte[buffer.remaining()];
                // You must update buffer.position() before returning (either implicitly,
                // like this, or explicitly) to indicate how many bytes your handler has consumed.
                buffer.get(bytes);

                stdoutProcessor.execute(() -> {
                    for (byte b : bytes) {
                        stdoutLineBuilder.append((char) b);
                        if (b == '\n') {
                            onEngineStdoutLine(stdoutLineBuilder.toString());
                            stdoutLineBuilder = new StringBuilder(2048);
                        }
                    }
                });
            }
        }

        @Override
        public void onStderr(final ByteBuffer buffer, final boolean closed) {
            if (!closed) {
                final byte[] bytes = new byte[buffer.remaining()];
                // You must update buffer.position() before returning (either implicitly,
                // like this, or explicitly) to indicate how many bytes your handler has consumed.
                buffer.get(bytes);

                stderrProcessor.execute(() -> {
                    for (byte b : bytes) {
                        stderrLineBuilder.append((char) b);
                        if (b == '\n') {
                            onEngineStderrLine(stderrLineBuilder.toString());
                            stderrLineBuilder = new StringBuilder(2048);
                        }
                    }
                });
            }
        }

        @Override
        public boolean onStdinReady(final ByteBuffer buffer) {
            GeneralGtpFuture future = stagineCommandQueue.poll();
            if (future != null) {
                buffer.put(future.getCommand().getBytes());
                if (!future.getCommand().endsWith("\n")) {
                    buffer.put((byte) '\n');
                }
                buffer.flip();

                runningCommandQueue.offer(future);

                return !stagineCommandQueue.isEmpty();
            }

            return false;
        }

        protected void onEngineStdoutLine(final String line) {
            engineStdoutLineConsumerList.forEach(consumer -> consumer.accept(line));
            if (inCommandResponse) {
                GeneralGtpFuture future = Objects.requireNonNull(runningCommandQueue.peek());
                List<String> response = future.getResponse();
                if (line.equals("\n") || line.equals("\r\n") || line.equals("\r")) {
                    runningCommandQueue.poll();

                    response.add(line.trim());
                    future.markComplete();

                    inCommandResponse = false;
                } else {
                    response.add(line.trim());
                }
            } else if (line.startsWith("=") || line.startsWith("?")) {
                inCommandResponse = true;
                GeneralGtpFuture future = Objects.requireNonNull(runningCommandQueue.peek());
                List<String> response = future.getResponse();
                response.add(line.trim());
            } else {
                onEngineDiagnosticLine(line);
            }
        }

        protected void onEngineStderrLine(final String line) {
            engineStderrLineConsumerList.forEach(consumer -> consumer.accept(line));
        }

        protected void onEngineDiagnosticLine(final String line) {
            engineDiagnosticLineConsumerList.forEach(consumer -> consumer.accept(line));
        }

        private void doCleanup() {
            if (stdoutProcessor != null) {
                ThreadPoolUtil.shutdownAndAwaitTermination(stdoutProcessor);
                stdoutProcessor = null;
            }

            if (stderrProcessor != null) {
                ThreadPoolUtil.shutdownAndAwaitTermination(stderrProcessor);
                stderrProcessor = null;
            }

            if (miscProcessor != null) {
                ThreadPoolUtil.shutdownAndAwaitTermination(miscProcessor);
                miscProcessor = null;
            }
        }

        @Override
        public void close() {
            objectFinalizer.doFinalize();
        }
    }

    private final ObjectFinalizer objectFinalizer;
    private List<String> gtpCommandLine;
    private NuProcessHandler gtpProcessHandler;
    private NuProcess gtpProcess;
    private ConcurrentLinkedQueue<GeneralGtpFuture> stagineCommandQueue;
    private ConcurrentLinkedQueue<GeneralGtpFuture> runningCommandQueue;
    private List<Consumer<String>> engineDiagnosticLineConsumerList;
    private List<Consumer<String>> engineStdoutLineConsumerList;
    private List<Consumer<String>> engineStderrLineConsumerList;
    private List<Consumer<Integer>> engineStartedObserverList;
    private List<Consumer<Integer>> engineExitObserverList;

    public GeneralGtpClient(String commandLine) {
        this(ArgumentTokenizer.tokenize(commandLine));
    }

    public GeneralGtpClient(List<String> commandLine) {
        objectFinalizer = new ObjectFinalizer(this::doCleanup, "GtpClient.cleanup");

        gtpCommandLine = commandLine;
        stagineCommandQueue = new ConcurrentLinkedQueue<>();
        runningCommandQueue = new ConcurrentLinkedQueue<>();
        engineDiagnosticLineConsumerList = new CopyOnWriteArrayList<>();
        engineStdoutLineConsumerList = new CopyOnWriteArrayList<>();
        engineStderrLineConsumerList = new CopyOnWriteArrayList<>();
        engineStartedObserverList = new CopyOnWriteArrayList<>();
        engineExitObserverList = new CopyOnWriteArrayList<>();
    }

    public void registerDiagnosticLineConsumer(Consumer<String> consumer) {
        engineDiagnosticLineConsumerList.add(consumer);
    }

    public void unregisterDiagnosticLineConsumer(Consumer<String> consumer) {
        engineDiagnosticLineConsumerList.remove(consumer);
    }

    @Override
    public void registerStdoutLineConsumer(Consumer<String> consumer) {
        engineStdoutLineConsumerList.add(consumer);
    }

    @Override
    public void unregisterStdoutLineConsumer(Consumer<String> consumer) {
        engineStdoutLineConsumerList.remove(consumer);
    }

    @Override
    public void registerStderrLineConsumer(Consumer<String> consumer) {
        engineStderrLineConsumerList.add(consumer);
    }

    @Override
    public void unregisterStderrLineConsumer(Consumer<String> consumer) {
        engineStderrLineConsumerList.remove(consumer);
    }

    public boolean cancelCommand(GeneralGtpFuture future) {
        return stagineCommandQueue.remove(future);
    }

    @Override
    public ListenableFuture<List<String>> postCommand(String command) {
        GeneralGtpFuture future = new GeneralGtpFuture(command, this);
        stagineCommandQueue.offer(future);
        gtpProcess.wantWrite();

        return future;
    }

    @Override
    public void start() {
        NuProcessBuilder processBuilder = new NuProcessBuilder(gtpCommandLine);

        gtpProcessHandler = provideProcessHandler();
        processBuilder.setProcessListener(gtpProcessHandler);
        setUpOtherProcessParameters(processBuilder);

        gtpProcess = processBuilder.start();
    }

    protected void setUpOtherProcessParameters(NuProcessBuilder processBuilder) {
    }

    protected NuProcessHandler provideProcessHandler() {
        return this.new GeneralGtpProcessHandler();
    }

    private int doShutdown(long timeout, TimeUnit timeUnit) {
        int exitCode = Integer.MIN_VALUE;
        if (gtpProcess != null && gtpProcess.isRunning()) {
            try {
                postCommand("quit");
                exitCode = gtpProcess.waitFor(timeout, timeUnit);
                if (exitCode == Integer.MIN_VALUE) {
                    gtpProcess.destroy(false);
                    exitCode = gtpProcess.waitFor(timeout, timeUnit);
                    if (exitCode == Integer.MIN_VALUE) {
                        gtpProcess.destroy(true);
                    }
                }
            } catch (InterruptedException e) {
                gtpProcess.destroy(true);
            }

            gtpProcess = null;

            if (gtpProcessHandler instanceof Closeable) {
                try {
                    ((Closeable) gtpProcessHandler).close();
                } catch (IOException e) {
                    // Do nothing
                }
            }
        }

        return exitCode;
    }

    @Override
    public int shutdown(long timeout, TimeUnit timeUnit) {
        int exitCode = doShutdown(timeout, timeUnit);
        objectFinalizer.markFinalized();

        return exitCode;
    }

    @Override
    public boolean isRunning() {
        return gtpProcess != null && gtpProcess.isRunning();
    }

    @Override
    public void registerEngineStartedObserver(Consumer<Integer> observer) {
        engineStartedObserverList.add(observer);
    }

    @Override
    public void unregisterEngineStartedObserver(Consumer<Integer> observer) {
        engineStartedObserverList.remove(observer);
    }

    @Override
    public void registerEngineExitObserver(Consumer<Integer> observer) {
        engineExitObserverList.add(observer);
    }

    @Override
    public void unregisterEngineExitObserver(Consumer<Integer> observer) {
        engineExitObserverList.remove(observer);
    }

    private void doCleanup() {
        doShutdown(60, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        objectFinalizer.doFinalize();
    }

    public static void main(String[] args) throws Exception {
        final GtpClient gtpClient = new GeneralGtpClient("leelaz.exe -g -t2 -wnetwork");
        gtpClient.start();

        Future<List<String>> timeResult = gtpClient.postCommand("888 time_settings 0 10 1");

        new Thread(() -> {
            Future<List<String>> nameResult = gtpClient.postCommand("1 name");
            for (int i = 0; i < 100; ++i) {
                gtpClient.postCommand(100 + i + " name");
                gtpClient.postCommand(200 + i + " name");
            }

            try {
                System.out.println(nameResult.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            Future<List<String>> errorResult = gtpClient.postCommand("1234 estimate_score");
            for (int i = 0; i < 145; ++i) {
                gtpClient.postCommand(300 + i + " name");
                gtpClient.postCommand(400 + i + " list_commands");
            }
            try {
                System.out.println(errorResult.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }).start();

        gtpClient.postCommand("5 name");
        gtpClient.postCommand("5 name");
        Future<List<String>> nameResult = gtpClient.postCommand("6 name");
        gtpClient.postCommand("list_commands");
        Future<List<String>> genMoveResult = gtpClient.postCommand("100 genmove b");
        Future<List<String>> listResult = gtpClient.postCommand("7 list_commands");
        gtpClient.postCommand("8 list_commands");
        gtpClient.postCommand("8 list_commands");
        gtpClient.postCommand("111111 list_commands");

        System.out.println(genMoveResult.get());
        System.out.println(listResult.get());
        System.out.println(nameResult.get());
        System.out.println(timeResult.get());

        Thread.sleep(5000);

        final int[] count = new int[1];
        final long start = System.currentTimeMillis();

        gtpClient.registerStderrLineConsumer(line -> {
            if (++count[0] % 1000 == 0) {
                System.out.println(System.currentTimeMillis() - start);
            }
        });

        Future<List<String>> ponderResult = gtpClient.postCommand("time_left b 0 0");
        System.out.println(ponderResult.get());

        Thread.sleep(30000);

        gtpClient.shutdown(60, TimeUnit.SECONDS);
    }
}
