package de.lgohlke.selenium.webdriver.chrome;

import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class ChromeDriverConfigurationTest {

    private final ChromeDriverConfiguration configuration = new ChromeDriverConfiguration();

    @Test
    public void testOptionUserDataDir() throws IOException {
        configuration.setUserDir("x");

        Capabilities capabilities = configuration.createCapabilities();
        Object       capability   = capabilities.getCapability(ChromeOptions.CAPABILITY);

        assertThat(capability).isInstanceOf(ChromeOptions.class);
        assertThat(((ChromeOptions) capability).toJson().toString()).isEqualTo("{\"args\":[\"user-data-dir=x\"],\"extensions\":[]}");
    }

}