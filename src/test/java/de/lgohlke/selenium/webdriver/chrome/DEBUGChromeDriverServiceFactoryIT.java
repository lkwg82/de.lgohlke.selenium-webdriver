package de.lgohlke.selenium.webdriver.chrome;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class DEBUGChromeDriverServiceFactoryIT {
    private final ChromeDriverServiceFactory factory = new ChromeDriverServiceFactory(new ChromeDriverConfiguration());

    @Test
    public void startAndStop() throws IOException {
        ChromeDriverService driverService = factory.createService();

        driverService.start();
        try {
            WebDriver webDriver = factory.createWebDriver(driverService);
            String    url       = "chrome://version";
            webDriver.get(url);
            String currentUrl = webDriver.getCurrentUrl();

            assertThat(currentUrl).isEqualTo(url);
        } finally {
            driverService.stop();
        }
    }
}