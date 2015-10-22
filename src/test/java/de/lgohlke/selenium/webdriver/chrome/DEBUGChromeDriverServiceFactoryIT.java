package de.lgohlke.selenium.webdriver.chrome;

import de.lgohlke.selenium.webdriver.FailedServiceStartRestarterIT;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class DEBUGChromeDriverServiceFactoryIT {
    private final ChromeDriverServiceFactory factory = new ChromeDriverServiceFactory(new ChromeDriverConfiguration());

    @Test
    public void startAndStop() throws IOException {
        FailedServiceStartRestarterIT.FailedServiceStartRestarter serviceStartRestarter = new FailedServiceStartRestarterIT.FailedServiceStartRestarter(
                5,
                TimeUnit.SECONDS);

        ChromeDriverService driverService = (ChromeDriverService) serviceStartRestarter.start(factory);

        try {
            log.warn("try to create webdriver");
            WebDriver webDriver = factory.createWebDriver(driverService);
            log.warn("created webdriver");
            String    url       = "chrome://version/";
            webDriver.get(url);
            String currentUrl = webDriver.getCurrentUrl();

            assertThat(currentUrl).isEqualTo(url);
        } finally {
            driverService.stop();
        }
    }
}