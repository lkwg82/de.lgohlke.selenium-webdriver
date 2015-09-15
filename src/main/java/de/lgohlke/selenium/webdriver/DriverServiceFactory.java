package de.lgohlke.selenium.webdriver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.service.DriverService;

import java.io.IOException;


public interface DriverServiceFactory<S extends DriverService> {
    S createService(String... args);
    WebDriver createWebDriver(S service) throws IOException;
    DriverArgumentsBuilder createServiceArgumentsBuilder();
}
