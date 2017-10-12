package de.lgohlke.selenium.webdriver;

import de.lgohlke.selenium.webdriver.chrome.ChromeDriverConfiguration;
import de.lgohlke.selenium.webdriver.phantomjs.PhantomJSDriverConfiguration;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

import static de.lgohlke.selenium.webdriver.DriverType.CHROME;
import static de.lgohlke.selenium.webdriver.DriverType.CHROME_HEADLESS;
import static de.lgohlke.selenium.webdriver.DriverType.PHANTOMJS;
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

        Object capability = configuration.createCapabilities()
                                         .getCapability(ChromeOptions.CAPABILITY);

        TreeMap<String,List<String>> chromeOptions = (TreeMap) capability;
        assertThat(chromeOptions).containsKeys("args");
        assertThat(chromeOptions.get("args")).containsExactly("--headless","--disable-gpu");
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