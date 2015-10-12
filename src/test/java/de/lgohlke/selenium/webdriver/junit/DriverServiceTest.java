package de.lgohlke.selenium.webdriver.junit;

import de.lgohlke.selenium.webdriver.DriverType;
import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class DriverServiceTest {
    private DriverService driverService;

    @After
    public void after() {
        if (driverService != null) {
            driverService.after();
        }
    }

    @Test
    public void testStart() throws Throwable {
        driverService = new DriverService(DriverType.PHANTOMJS);
        driverService.before();

        WebDriver driver = driverService.getDriver();
        assertThat(driver).isInstanceOf(RemoteWebDriver.class);
        assertThat(driver.toString()).startsWith("RemoteWebDriver: phantomjs on ");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldBeWrappedInEventFiringWebdriver() throws Throwable {
        AbstractWebDriverEventListener eventListener = new AbstractWebDriverEventListener() {
        };
        driverService = new DriverService(DriverType.PHANTOMJS, eventListener);

        driverService.before();

        WebDriver driver = driverService.getDriver();
        assertThat(driver).isInstanceOf(EventFiringWebDriver.class);

        EventFiringWebDriver eventFiringWebDriver = (EventFiringWebDriver) driver;
        Field                field                = EventFiringWebDriver.class.getDeclaredField("eventListeners");
        field.setAccessible(true);

        assertThat(((List<WebDriverEventListener>) field.get(eventFiringWebDriver)).size()).isEqualTo(1);
    }
}