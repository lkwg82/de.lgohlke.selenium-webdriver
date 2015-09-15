package de.lgohlke.junit.p;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class Programm {
    @Setter(AccessLevel.PROTECTED)
    private Process        process;
    @Setter(AccessLevel.PROTECTED)
    private ProcessBuilder processBuilder;
    @Setter(AccessLevel.PROTECTED)
    private File workingDirectory = new File(System.getProperty("user.dir"));

    private StreamGobbler errorStreamReader;
    private StreamGobbler outStreamReader;

    protected static String readStdout(Process p) throws IOException {
        return readFromInputStream(p.getInputStream());
    }

    protected static String readStderr(Process p) throws IOException {
        return readFromInputStream(p.getErrorStream());
    }

    private static String readFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader  = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder  builder = new StringBuilder();
        String         line    = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }
        return builder.toString();
    }

    protected boolean checkInstalled(String programm) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder();

        // use stdout/stderr from parent process
//        pb.inheritIO();

        pb.directory(workingDirectory);

        String  command = "which " + programm;
        Process p       = pb.command("bash", "-c", command).start();
        p.waitFor(10, TimeUnit.SECONDS);
        p.destroyForcibly();

        log.debug("exit:   " + p.exitValue());

        return p.exitValue() == 0;
    }

    protected void checkCustom() throws IOException, InterruptedException {
    }

    protected void start(String command) throws IOException, InterruptedException {

        if (!isInstalled()) {
            throw new IllegalStateException("could not start, missing programm");
        }

        checkCustom();

        ProcessBuilder pb = new ProcessBuilder();
        setProcessBuilder(pb);

        // use stdout/stderr from parent process
//        pb.inheritIO();

        Path errorFile = Files.createTempFile("err", "log");
        Path outFile = Files.createTempFile("out", "log");

        errorStreamReader = new StreamGobbler(errorFile, log::error);
        errorStreamReader.start();

        outStreamReader = new StreamGobbler(outFile, log::debug);
        outStreamReader.start();

        log.debug("running command '{}'", command);
        process = pb
                .directory(new File(System.getProperty("user.dir")))
                .redirectError(errorFile.toFile())
                .redirectOutput(outFile.toFile())
                .command("bash", "-c", command).start();

        new Thread(() -> {
            try {
                int exitValue = process.waitFor();
                if (exitValue != 0) {
                    log.error("exit:" + exitValue);
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }).start();
    }

    public void stop() throws IOException, InterruptedException {
        log.info("stopping");
        process.destroy();
        process.waitFor(30, TimeUnit.SECONDS);
        process.destroyForcibly();

        errorStreamReader.finish();
        outStreamReader.finish();

    }

    public abstract boolean isInstalled() throws IOException, InterruptedException;

    public abstract void start() throws IOException, InterruptedException;
}
