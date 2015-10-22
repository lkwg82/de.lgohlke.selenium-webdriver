package de.lgohlke.selenium.webdriver;

import de.lgohlke.selenium.webdriver.chrome.ChromeDriverConfiguration;
import de.lgohlke.selenium.webdriver.chrome.ChromeDriverServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.os.ProcessUtils;
import org.openqa.selenium.remote.service.DriverService;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

@Slf4j
public class FailedServiceStartRestarterIT {
    ChromeDriverConfiguration  config  = new ChromeDriverConfiguration();
    ChromeDriverServiceFactory factory = new ChromeDriverServiceFactory(config);

    @Ignore
    @Test
    public void test() throws Exception {
        FailedServiceStartRestarter restarter = new FailedServiceStartRestarter(5, TimeUnit.SECONDS);
        IntStream.range(0, 100).parallel().forEach(i -> {
            ChromeDriverService service = factory.createService();
            try {
                restarter.start(factory, service);
            } finally {
                service.stop();
            }
        });
    }

    @RequiredArgsConstructor
    public static class FailedServiceStartRestarter {
        private final int      timeout;
        private final TimeUnit timeUnit;

        public WebDriver start(DriverServiceFactory factory, DriverService driverService) {
            WebDriver driver    = null;
            boolean   isStarted = false;
            for (int i = 0; i < 10 && driver == null; i++) {
                log.error("trying to start {}", i);
                driver = startServiceAndCreateWebdriver(driverService, factory);
                log.warn("is started: {}", isStarted);
                if (driver == null) {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return driver;
        }

        private WebDriver startServiceAndCreateWebdriver(DriverService driverService, DriverServiceFactory factory) {
            ExecutorService service = Executors.newFixedThreadPool(1);

            Callable<WebDriver> startJob = () -> {
                log.warn("starting");
                driverService.start();
                log.warn("try to create webdriver");
                return factory.createWebDriver(driverService);
            };

            Future<WebDriver> startFuture = service.submit(startJob);
            try {
                try {
                    log.warn("waiting for result");
                    return startFuture.get(5, TimeUnit.SECONDS);
                } catch (ExecutionException | InterruptedException e) {
                    log.error(e.getMessage(), e);
                    if (driverService.isRunning()) {
                        driverService.stop();
                    }
                    return null;
                }
            } catch (TimeoutException e) {
                log.error(e.getMessage(), e);

                Field processField = null;
                try {
                    processField = driverService.getClass().getField("process");
                } catch (NoSuchFieldException e1) {
                    log.error(e1.getMessage(), e1);
                    throw new IllegalStateException(e1);
                }
                processField.setAccessible(true);
                Process process = null;
                try {
                    process = (Process) processField.get(driverService);
                } catch (IllegalAccessException e1) {
                    log.error(e1.getMessage(), e1);
                    throw new IllegalStateException(e1);
                }
                log.error("try to kill the process: {}", process);
                int exitCode = ProcessUtils.killProcess(process);
                log.error("killed the process, exit : {}", exitCode);
                return null;
            } finally {
                service.shutdownNow();
            }
        }
    }
}
