package de.lgohlke.selenium.webdriver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public abstract class DriverServiceFactory<S extends DriverService, T extends DriverConfiguration> {
    @Getter
    private final T driverConfiguration;
    @Setter
    private RemoteWebdriverInitialConnectionRetryer remoteWebdriverInitialConnectionRetryer = new RemoteWebdriverInitialConnectionRetryer(2, TimeUnit.SECONDS, 5);

    public abstract S createService(String... args);

    public WebDriver createWebDriver(S service) throws IOException {
        DriverServiceFactory<S, T> outerFactory = this;

        return remoteWebdriverInitialConnectionRetryer.start(new DriverServiceFactory<S, T>(driverConfiguration) {
            @Override
            public S createService(String... args) {
                return outerFactory.createService(args);
            }

            @Override
            public DriverArgumentsBuilder createServiceArgumentsBuilder() {
                return outerFactory.createServiceArgumentsBuilder();
            }

            @Override
            public WebDriver createWebDriver(S service) throws IOException {
                return outerFactory.createPlainWebDriver(service);
            }
        }, service);
    }

    public WebDriver createPlainWebDriver(S service) throws IOException {
        return new RemoteWebDriver(service.getUrl(), getDriverConfiguration().createCapabilities());
    }

    public abstract DriverArgumentsBuilder createServiceArgumentsBuilder();
}
