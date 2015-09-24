package de.lgohlke.selenium.webdriver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.service.DriverService;

import java.io.IOException;

@RequiredArgsConstructor
public abstract class DriverServiceFactory<S extends DriverService, T extends DriverConfiguration> {
    @Getter
    private final T driverConfiguration;

    public abstract S createService(String... args);

    public abstract WebDriver createWebDriver(S service) throws IOException;

    public abstract DriverArgumentsBuilder createServiceArgumentsBuilder();
}
