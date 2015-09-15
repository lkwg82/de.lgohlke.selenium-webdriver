package de.lgohlke.junit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.rules.ExternalResource;
import org.openqa.selenium.net.PortProber;

@RequiredArgsConstructor
@Slf4j
public class FreeportProber extends ExternalResource {
    @Getter
    private int port;

    @Override
    protected void before() throws Throwable {
        port=PortProber.findFreePort();
        log.info("found free port: {}",port);
    }
}
