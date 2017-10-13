package de.lgohlke.selenium.webdriver.chrome;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.service.DriverService;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ChromeDriverServiceFactoryTest {
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private final ChromeDriverServiceFactory serviceFactory = new ChromeDriverServiceFactory(new ChromeDriverConfiguration());

    @Test(expected = IllegalArgumentException.class)
    public void argumentsShouldComeInPairsElseFail() {
        serviceFactory.createService("s");
    }

    @Test
    public void environmentVariableShouldBePropagated() throws NoSuchFieldException, IllegalAccessException {
        ChromeDriverService service = serviceFactory.createService("DISPLAY", "X");

        Map<String, String> env = getEnvFromDriverService(service);

        assertThat(env).containsEntry("DISPLAY", "X");
    }

    @Test
    public void onLinux_shouldSetDisplayToZEROIfUnset() throws NoSuchFieldException, IllegalAccessException {
        environmentVariables.set("DISPLAY",null);
        environmentVariables.set("os.name", "Linux");

        ChromeDriverService service = serviceFactory.createService();

        Map<String, String> env = getEnvFromDriverService(service);

        assertThat(env).containsEntry("DISPLAY", ":0");
    }

    @Test
    public void onLinux_shouldUseDisplayFromEnvironment() throws NoSuchFieldException, IllegalAccessException {
        environmentVariables.set("DISPLAY", "Y");
        environmentVariables.set("os.name", "Linux");

        ChromeDriverService service = serviceFactory.createService();

        Map<String, String> env = getEnvFromDriverService(service);

        assertThat(env).containsEntry("DISPLAY", "Y");
    }

    @Test
    public void onOtherOs_shouldNotSetDisplay() throws NoSuchFieldException, IllegalAccessException {
        environmentVariables.set("os.name", "NotLinux");

        ChromeDriverService service = serviceFactory.createService();

        Map<String, String> env = getEnvFromDriverService(service);

        assertThat(env).doesNotContainKey("DISPLAY");
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getEnvFromDriverService(ChromeDriverService service) throws NoSuchFieldException, IllegalAccessException {
        Field field = DriverService.class.getDeclaredField("environment");
        field.setAccessible(true);
        return (Map<String, String>) field.get(service);
    }
}