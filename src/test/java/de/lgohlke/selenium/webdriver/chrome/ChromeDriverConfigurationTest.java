package de.lgohlke.selenium.webdriver.chrome;

import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;


public class ChromeDriverConfigurationTest {

    private final ChromeDriverConfiguration configuration = new ChromeDriverConfiguration();

    @Test
    public void testOptionUserDataDir() throws IOException {
        configuration.setUserDir("x");

        Capabilities capabilities = configuration.createCapabilities();
        Object       capability   = capabilities.getCapability(ChromeOptions.CAPABILITY);

        TreeMap<String, List<String>> chromeOptions = (TreeMap) capability;
        assertThat(chromeOptions).containsKeys("args");
        assertThat(chromeOptions.get("args")).containsExactly("--user-data-dir=x");
    }

    @Test
    public void shouldBeHeadless() throws IOException {
        configuration.enableHeadlessMode();

        Capabilities capabilities = configuration.createCapabilities();
        Object       capability   = capabilities.getCapability(ChromeOptions.CAPABILITY);

        TreeMap<String, List<String>> chromeOptions = (TreeMap) capability;
        assertThat(chromeOptions).containsKeys("args");
        assertThat(chromeOptions.get("args")).containsExactly("--headless", "--disable-gpu");
    }
}