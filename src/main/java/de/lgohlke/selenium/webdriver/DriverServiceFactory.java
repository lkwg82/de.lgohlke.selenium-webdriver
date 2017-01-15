package de.lgohlke.selenium.webdriver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * the idea is to have a restartable driver configuration
 */
@RequiredArgsConstructor
public abstract class DriverServiceFactory<S extends DriverService, T extends DriverConfiguration> {
    @Getter
    private final T driverConfiguration;
    @Setter
    private RemoteWebdriverInitialConnectionRetryer connectionRetryer = new RemoteWebdriverInitialConnectionRetryer(
            2,
            TimeUnit.SECONDS,
            5);

    public abstract S createService(String... args);

    public abstract DriverArgumentsBuilder createServiceArgumentsBuilder();

    public WebDriver createWebDriver(S service) throws IOException {
        DriverServiceFactory<S, T> outerFactory = this;
        DriverServiceFactory<S, T> factory = new DriverServiceFactory<S, T>(driverConfiguration) {
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
        };

        return connectionRetryer.start(factory, service);
    }

    private WebDriver createPlainWebDriver(S service) throws IOException {
        return new RemoteWebDriver(service.getUrl(), getDriverConfiguration().createCapabilities());
    }
}
