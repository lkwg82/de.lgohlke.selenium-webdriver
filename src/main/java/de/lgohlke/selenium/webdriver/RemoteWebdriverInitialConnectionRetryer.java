package de.lgohlke.selenium.webdriver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.service.DriverService;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * retries RemoteWebdriver connection
 * <p/>
 * from time to time it happens the RemoteWebdriver can not connect to the DriverService, unhandled it would break the startup
 */
@Slf4j
@RequiredArgsConstructor
public class RemoteWebdriverInitialConnectionRetryer {
    private final int      timeout;
    private final TimeUnit timeUnit;
    private final int      retries;

    public WebDriver start(DriverServiceFactory factory, DriverService driverService) {
        WebDriver driver = null;
        for (int i = 0; i < retries && driver == null; i++) {
            log.info("trying to start {}/{} (attempt/max attempts)", i, retries);
            driver = startServiceAndCreateWebdriver(driverService, factory);
            log.info("is started: {}", driver == null);
            if (driver == null) {
                try {
                    timeUnit.sleep(2);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return driver;
    }

    private WebDriver startServiceAndCreateWebdriver(DriverService driverService, DriverServiceFactory factory) {
        ExecutorService service = Executors.newFixedThreadPool(1);

        @SuppressWarnings("unchecked")
        Callable<WebDriver> startJob = () -> {
            if (!driverService.isRunning()) {
                log.info("starting");
                driverService.start();
            }
            log.info("try to create webdriver");
            return factory.createWebDriver(driverService);
        };

        Future<WebDriver> startFuture = service.submit(startJob);
        try {
            log.info("waiting for webdriver");
            return startFuture.get(timeout, timeUnit);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            log.warn(e.getMessage(), e);
            if (driverService.isRunning()) {
                driverService.stop();
            }
        } finally {
            service.shutdownNow();
        }
        return null;
    }
}
