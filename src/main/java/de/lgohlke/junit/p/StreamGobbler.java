package de.lgohlke.junit.p;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

@RequiredArgsConstructor
@Slf4j
class StreamGobbler extends Thread {
    private final Path      logFile;
    private final LogMethod logRoutine;

    private volatile boolean finishReading;

    public void finish() {
        finishReading = true;
    }

    @Override
    public void run() {
        try (FileInputStream fileInputStream = new FileInputStream(logFile.toFile())) {
            try (InputStreamReader in = new InputStreamReader(fileInputStream)) {
                passReadLineToLog(in);
            }
        } catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
        }

        log.debug("finished: " + this);
    }

    private void passReadLineToLog(InputStreamReader in) throws IOException {
        try (BufferedReader br = new BufferedReader(in)) {
            while (!finishReading) {
                String line = br.readLine();
                if (line == null) {
                    //wait until there is more of the file for us to read
                    Thread.sleep(50);
                } else {
                    logRoutine.log(line);
                }
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
