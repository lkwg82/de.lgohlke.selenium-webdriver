package de.lgohlke.selenium.webdriver;

import de.lgohlke.selenium.webdriver.chrome.ChromeDriverServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Slf4j
public class RemoteWebdriverInitialConnectionRetryerTest {
    private RemoteWebdriverInitialConnectionRetryer restarter     = new RemoteWebdriverInitialConnectionRetryer(500,
                                                                                                                TimeUnit.MILLISECONDS,
                                                                                                                5);
    private ChromeDriverService                     driverService = mock(ChromeDriverService.class);
    private ChromeDriverServiceFactory              factory       = mock(ChromeDriverServiceFactory.class);

    @Before
    public void beforeEachTest() throws IOException {
        doAnswer(invocationOnMock -> null).when(driverService).start();
        when(driverService.isRunning()).thenReturn(true);
    }

    @Test
    public void shouldTimeoutWhileConnecting() throws Exception {
        Logger logger = LogManager.getLogger(RemoteWebdriverInitialConnectionRetryer.class);
        ((org.apache.logging.log4j.core.Logger) logger).setLevel(Level.ERROR);

        when(driverService.isRunning()).thenReturn(true);
        when(factory.createWebDriver(driverService)).thenAnswer(invocationOnMock -> {
            TimeUnit.MILLISECONDS.sleep(600);
            return null;
        });

        WebDriver webDriver = restarter.start(factory, driverService);

        assertThat(webDriver).isNull();
        verify(driverService, times(10)).isRunning();
        verify(driverService, times(5)).stop();

        ((org.apache.logging.log4j.core.Logger) logger).setLevel(Level.WARN);
    }

    @Test
    public void shouldStartDriverService() throws Exception {
        when(factory.createWebDriver(driverService)).thenAnswer(invocationOnMock -> mock(WebDriver.class));
        when(driverService.isRunning()).thenReturn(false);

        restarter.start(factory, driverService);

        verify(driverService, times(1)).start();
    }

    @Test
    public void shouldSucceedOn2ndTry() throws Exception {
        LongAdder counter = new LongAdder();
        when(factory.createWebDriver(driverService)).thenAnswer(invocationOnMock -> {
            counter.increment();
            return counter.intValue() == 2 ? mock(WebDriver.class) : null;
        });

        WebDriver webDriver = restarter.start(factory, driverService);

        assertThat(webDriver).isNotNull();
        assertThat(counter.intValue()).isEqualTo(2);
    }

}
