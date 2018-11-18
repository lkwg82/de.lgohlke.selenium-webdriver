package de.lgohlke.selenium.webdriver;

import com.google.gson.Gson;
import de.lgohlke.selenium.webdriver.chrome.ChromeDriverConfiguration;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.TreeMap;

import static de.lgohlke.selenium.webdriver.DriverType.CHROME;
import static de.lgohlke.selenium.webdriver.DriverType.CHROME_HEADLESS;
import static org.assertj.core.api.Assertions.assertThat;

public class DriverTypeTest {
    @Test
    public void CHROME_shouldCheckConfigurationType_Ok() {
        CHROME.driverServiceFactory(new ChromeDriverConfiguration());
    }

    @Test
    public void CHROME_HEADLESS_shouldCheckConfigurationType_Ok() {
        DriverServiceFactory factory = CHROME_HEADLESS.driverServiceFactory(new ChromeDriverConfiguration());
        DriverConfiguration configuration = factory.getDriverConfiguration();

        Object capability = configuration.createCapabilities()
                                         .getCapability(ChromeOptions.CAPABILITY);

        assertThat(capability).isInstanceOf(TreeMap.class);
        String json = new Gson().toJson(capability);
        assertThat(json).isEqualTo("{\"args\":[\"--headless\",\"--disable-gpu\"],\"extensions\":[]}");
    }
}