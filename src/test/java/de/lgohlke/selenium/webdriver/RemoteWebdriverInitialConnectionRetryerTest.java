package de.lgohlke.selenium.webdriver;

import de.lgohlke.selenium.webdriver.chrome.ChromeDriverServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class RemoteWebdriverInitialConnectionRetryerTest {
    private RemoteWebdriverInitialConnectionRetryer restarter     = new RemoteWebdriverInitialConnectionRetryer(500, TimeUnit.MILLISECONDS, 5);
    private ChromeDriverService                     driverService = mock(ChromeDriverService.class);
    private ChromeDriverServiceFactory              factory       = mock(ChromeDriverServiceFactory.class);

    @Before
    public void beforeEachTest() throws IOException {
        doAnswer(invocationOnMock -> null).when(driverService).start();
        when(driverService.isRunning()).thenReturn(true);
    }

    @Test
    public void tryUntilReachLimit() throws Exception {
        when(factory.createWebDriver(driverService)).thenAnswer(invocationOnMock -> null);

        WebDriver webDriver = restarter.start(factory, driverService);

        assertThat(webDriver).isNull();
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
