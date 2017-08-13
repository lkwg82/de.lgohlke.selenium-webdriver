package de.lgohlke.selenium.webdriver.chrome;

import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.service.DriverService;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ChromeDriverServiceFactoryTest {
    private final ChromeDriverServiceFactory serviceFactory = new ChromeDriverServiceFactory(new ChromeDriverConfiguration());

    @Test(expected = IllegalArgumentException.class)
    public void argumentsShouldComeInPairsElseFail() {
        serviceFactory.createService("s");
    }

    @Test
    public void environmentVariableShouldBePropagated() throws NoSuchFieldException, IllegalAccessException {
        ChromeDriverService service = serviceFactory.createService("DISPLAY", "X");

        Field field = DriverService.class.getDeclaredField("environment");
        field.setAccessible(true);
        Map<String, String> env = (Map<String, String>) field.get(service);

        assertThat(env).containsEntry("DISPLAY", "X");
    }
}