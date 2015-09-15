package de.lgohlke.junit;

import de.lgohlke.selenium.webdriver.DriverServiceFactory;
import de.lgohlke.selenium.webdriver.DriverType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.rules.ExternalResource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;

@Slf4j
public class DriverService extends ExternalResource {
    private final DriverType                                       drivertype;
    private final WebDriverEventListener[]                         listeners;
    private       org.openqa.selenium.remote.service.DriverService service;
    @Getter
    private       WebDriver                                        driver;

    public DriverService(DriverType drivertype, WebDriverEventListener... listeners) {
        this.drivertype = drivertype;
        this.listeners = listeners;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void before() throws Throwable {
        DriverServiceFactory driverServiceFactory = drivertype.driverServiceFactory();
        service = driverServiceFactory.createService();

        log.info("starting service");
        service.start();

        driver = driverServiceFactory.createWebDriver(service);

        if (listeners.length > 0) {
            EventFiringWebDriver eventFiringWebDriver = new EventFiringWebDriver(driver);
            for (WebDriverEventListener listener : listeners) {
                eventFiringWebDriver.register(listener);
            }
            driver = eventFiringWebDriver;
        }
    }

    @Override
    protected void after() {
        log.info("stopping service");
        service.stop();
    }
}
