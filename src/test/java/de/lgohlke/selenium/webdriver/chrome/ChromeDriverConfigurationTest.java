package de.lgohlke.selenium.webdriver.chrome;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;


public class ChromeDriverConfigurationTest {

    private  final Gson gson = new GsonBuilder().disableHtmlEscaping()
                                                .create();
    private final ChromeDriverConfiguration configuration = new ChromeDriverConfiguration();

    @Test
    public void testOptionUserDataDir() {
        configuration.setUserDir("x");

        Capabilities capabilities = configuration.createCapabilities();
        Object       capability   = capabilities.getCapability(ChromeOptions.CAPABILITY);

        assertThat(capability).isInstanceOf(TreeMap.class);
        String json = gson.toJson(capability);
        assertThat(json).isEqualTo("{\"args\":[\"--user-data-dir=x\"],\"extensions\":[]}");
    }

    @Test
    public void shouldBeHeadless() {
        configuration.enableHeadlessMode();

        assertThat(configuration.isHeadless()).isTrue();

        Capabilities capabilities = configuration.createCapabilities();
        Object       capability   = capabilities.getCapability(ChromeOptions.CAPABILITY);

        assertThat(capability).isInstanceOf(TreeMap.class);
        String json = gson.toJson(capability);
        assertThat(json).isEqualTo("{\"args\":[\"--headless\",\"--disable-gpu\"],\"extensions\":[]}");
    }
}