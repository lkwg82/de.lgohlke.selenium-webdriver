package de.lgohlke.selenium.webdriver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;

import java.io.IOException;

@RequiredArgsConstructor
public abstract class DriverServiceFactory<S extends DriverService, T extends DriverConfiguration> {
    @Getter
    private final T driverConfiguration;

    public abstract S createService(String... args);

    public WebDriver createWebDriver(S service) throws IOException {
        return new RemoteWebDriver(service.getUrl(), getDriverConfiguration().createCapabilities());
    }

    public abstract DriverArgumentsBuilder createServiceArgumentsBuilder();
}
