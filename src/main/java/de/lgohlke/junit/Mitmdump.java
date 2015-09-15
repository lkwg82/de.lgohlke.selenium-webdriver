package de.lgohlke.junit;

import lombok.extern.slf4j.Slf4j;
import org.junit.rules.ExternalResource;

import java.io.File;
import java.io.IOException;

import static de.lgohlke.junit.p.Mitmdump.MODE;

@Slf4j
public class Mitmdump extends ExternalResource {
    private final MODE           mode;
    private final File           trafficFile;
    private       int            port;
    private       FreeportProber freeportProber;

    private de.lgohlke.junit.p.Mitmdump mitmdump;

    public Mitmdump(MODE mode, String trafficFile, FreeportProber freeportProber) {
        this(mode, new File(Mitmdump.class.getResource(trafficFile).getFile()), freeportProber);
    }

    public Mitmdump(MODE mode, File trafficFile, FreeportProber freeportProber) {
        this.mode = mode;
        this.trafficFile = trafficFile;
        this.freeportProber = freeportProber;
    }

    @Override
    protected void before() throws Throwable {
        if (port == 0) {
            port = freeportProber.getPort();
        }

        mitmdump = new de.lgohlke.junit.p.Mitmdump(mode, trafficFile, port);
        log.info("starting at port {}", port);
        mitmdump.start();
        log.info("started, serving from {}", trafficFile.getPath());
    }

    @Override
    protected void after() {
        try {
            log.info("stopping");
            mitmdump.stop();
            log.info("stopped");
        } catch (InterruptedException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
