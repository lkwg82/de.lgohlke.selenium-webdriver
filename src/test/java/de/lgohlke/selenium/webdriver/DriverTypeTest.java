package de.lgohlke.selenium.webdriver;

import de.lgohlke.selenium.webdriver.chrome.ChromeDriverConfiguration;
import de.lgohlke.selenium.webdriver.phantomjs.PhantomJSDriverConfiguration;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;

import static de.lgohlke.selenium.webdriver.DriverType.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DriverTypeTest {
    @Test(expected = IllegalArgumentException.class)
    public void CHROME_shouldCheckConfigurationType_Fail() {
        CHROME.driverServiceFactory(new PhantomJSDriverConfiguration());
    }

    @Test
    public void CHROME_shouldCheckConfigurationType_Ok() {
        CHROME.driverServiceFactory(new ChromeDriverConfiguration());
    }

    @Test(expected = IllegalArgumentException.class)
    public void CHROME_HEADLESS_shouldCheckConfigurationType_Fail() {
        CHROME_HEADLESS.driverServiceFactory(new PhantomJSDriverConfiguration());
    }

    @Test
    public void CHROME_HEADLESS_shouldCheckConfigurationType_Ok() throws IOException {
        DriverServiceFactory factory       = CHROME_HEADLESS.driverServiceFactory(new ChromeDriverConfiguration());
        DriverConfiguration  configuration = factory.getDriverConfiguration();

        Object capability = configuration.createCapabilities().getCapability(ChromeOptions.CAPABILITY);

        assertThat(capability).isInstanceOf(ChromeOptions.class);
        assertThat(((ChromeOptions) capability).toJson()
                                               .toString()).isEqualTo("{\"args\":[\"--headless\",\"--disable-gpu\"],\"extensions\":[]}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void PHANTOMJS_shouldCheckConfigurationType_Fail() {
        PHANTOMJS.driverServiceFactory(new ChromeDriverConfiguration());
    }

    @Test
    public void PHANTOMJS_shouldCheckConfigurationType_Ok() {
        PHANTOMJS.driverServiceFactory(new PhantomJSDriverConfiguration());
    }
}