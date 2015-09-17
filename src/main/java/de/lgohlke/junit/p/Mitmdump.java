package de.lgohlke.junit.p;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class Mitmdump extends Programm {
    private final static String PROGRAMM = "mitmdump";
    private final MODE mode;
    private final File trafficFile;
    private final int port;

    @Override
    public boolean isInstalled() throws IOException, InterruptedException {
        return checkInstalled(PROGRAMM);
    }

    @Override
    protected void checkCustom() throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder();

        // use stdout/stderr from parent process
//        pb.inheritIO();

        Process p = pb.command("mitmdump", "--version").start();

        p.waitFor(10, TimeUnit.SECONDS);

        String result = readStderr(p).trim();
        if (!"mitmdump 0.11.3".equals(result)) {
            throw new IllegalStateException("output:" + result + ", please install correct version");
        }

        p.destroyForcibly();

        log.debug("exit:   " + p.exitValue());
        if (p.exitValue() != 0) {
            throw new IllegalStateException("exit code: " + p.exitValue());
        }
    }

    @Override
    public void start() throws IOException, InterruptedException {
        if (mode == MODE.SERVE) {
            log.info("starting in serve mode");
            start(PROGRAMM + " -v -S " + trafficFile + " --norefresh --no-pop -k --no-upstream-cert -p " + port);
        } else {
            throw new NotImplementedException("");
        }
    }

    public enum MODE {
        SERVE, DUMP
    }
}

