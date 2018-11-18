package de.lgohlke.selenium.webdriver;

import de.lgohlke.selenium.webdriver.chrome.ChromeDriverConfiguration;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;

import static de.lgohlke.selenium.webdriver.DriverType.CHROME;
import static de.lgohlke.selenium.webdriver.DriverType.CHROME_HEADLESS;
import static org.assertj.core.api.Assertions.assertThat;

public class DriverTypeTest {
    @Test
    public void CHROME_shouldCheckConfigurationType_Ok() {
        CHROME.driverServiceFactory(new ChromeDriverConfiguration());
    }

    @Test
    public void CHROME_HEADLESS_shouldCheckConfigurationType_Ok() throws IOException {
        DriverServiceFactory factory = CHROME_HEADLESS.driverServiceFactory(new ChromeDriverConfiguration());
        DriverConfiguration configuration = factory.getDriverConfiguration();

        Object capability = configuration.createCapabilities()
                                         .getCapability(ChromeOptions.CAPABILITY);

        assertThat(capability).isInstanceOf(ChromeOptions.class);
        assertThat(((ChromeOptions) capability).toJson()
                                               .toString()).isEqualTo(
                "{\"args\":[\"--headless\",\"--disable-gpu\"],\"extensions\":[]}");
    }
}