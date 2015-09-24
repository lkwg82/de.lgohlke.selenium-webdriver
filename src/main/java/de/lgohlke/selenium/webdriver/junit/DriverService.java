package de.lgohlke.selenium.webdriver.junit;

import de.lgohlke.selenium.webdriver.DriverConfiguration;
import de.lgohlke.selenium.webdriver.DriverServiceFactory;
import de.lgohlke.selenium.webdriver.DriverType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.rules.ExternalResource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class DriverService extends ExternalResource {
    private final DriverType                                       drivertype;
    private final List<WebDriverEventListener>                     listeners;
    private final DriverConfiguration                              configuration;
    private       org.openqa.selenium.remote.service.DriverService service;

    @Getter
    private       WebDriver                                        driver;

    public DriverService(DriverType drivertype, WebDriverEventListener... listeners) {
        this.drivertype = drivertype;
        this.listeners = Arrays.asList(listeners);

        try {
            this.configuration = drivertype.getDriverConfigurationClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public DriverService(DriverType drivertype, List<WebDriverEventListener> listeners, DriverConfiguration configuration) {
        this.drivertype = drivertype;
        this.listeners = listeners;
        this.configuration = configuration;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void before() throws Throwable {
        DriverServiceFactory driverServiceFactory = drivertype.driverServiceFactory(configuration);
        service = driverServiceFactory.createService();

        log.info("starting service");
        service.start();

        driver = driverServiceFactory.createWebDriver(service);

        if (!listeners.isEmpty()) {
            EventFiringWebDriver eventFiringWebDriver = new EventFiringWebDriver(driver);
            listeners.forEach(eventFiringWebDriver::register);
            driver = eventFiringWebDriver;
        }
    }

    @Override
    protected void after() {
        log.info("stopping service");
        service.stop();
    }
}
