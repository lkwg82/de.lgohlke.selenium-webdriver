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
    @SuppressWarnings("unchecked")
    public void environmentVariableShouldBePropagated() throws NoSuchFieldException, IllegalAccessException {
        ChromeDriverService service = serviceFactory.createService("DISPLAY", "X");

        Field field = DriverService.class.getDeclaredField("environment");
        field.setAccessible(true);
        Map<String, String> env = (Map<String, String>) field.get(service);

        assertThat(env).containsEntry("DISPLAY", "X");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onLinux_shouldSetDisplayToZEROIfUnset() throws NoSuchFieldException, IllegalAccessException {
        System.setProperty("os.name", "Linux");

        ChromeDriverService service = serviceFactory.createService();

        Field field = DriverService.class.getDeclaredField("environment");
        field.setAccessible(true);
        Map<String, String> env = (Map<String, String>) field.get(service);

        assertThat(env).containsEntry("DISPLAY", ":0");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onOtherOs_shouldNotSetDisplay() throws NoSuchFieldException, IllegalAccessException {
        System.setProperty("os.name", "NotLinux");

        ChromeDriverService service = serviceFactory.createService();

        Field field = DriverService.class.getDeclaredField("environment");
        field.setAccessible(true);
        Map<String, String> env = (Map<String, String>) field.get(service);

        assertThat(env).doesNotContainKey("DISPLAY");
    }
}