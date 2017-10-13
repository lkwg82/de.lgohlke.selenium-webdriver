package de.lgohlke.selenium.webdriver.chrome;

import com.google.common.base.Preconditions;
import de.lgohlke.logging.LogLevel;
import de.lgohlke.logging.LoggingOutputStream;
import de.lgohlke.selenium.webdriver.DriverArgumentsBuilder;
import de.lgohlke.selenium.webdriver.DriverServiceFactory;
import de.lgohlke.selenium.webdriver.ExecutableFinder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeDriverService.Builder;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@Slf4j
public class ChromeDriverServiceFactory extends DriverServiceFactory<ChromeDriverService, ChromeDriverConfiguration> {
    static {
        LogManager.getLogManager()
                  .reset();
        Logger.getLogger("global")
              .setLevel(Level.WARNING);
        SLF4JBridgeHandler.install();
    }

    @Setter
    private ChromeDriverLocationStrategy locationStrategy = new ChromeDriverLocationStrategy(new ExecutableFinder());

    public ChromeDriverServiceFactory(ChromeDriverConfiguration config) {
        super(config);
    }

    public DriverArgumentsBuilder createServiceArgumentsBuilder() {
        return new ChromeDriverServiceArgumentsBuilder();
    }

    public ChromeDriverService createService(String... args) {
        Preconditions.checkArgument(args.length % 2 == 0, "arguments should be pairs");

        Map<String, String> environment = new HashMap<>();
        for (int i = 1; i < args.length; i += 2) {
            environment.put(args[i - 1], args[i]);
        }

        handleDISPLAYonLinux(environment);

        ChromeDriverService service = new Builder()
                .usingDriverExecutable(locationStrategy.findExecutable())
                .withVerbose(log.isDebugEnabled())
                .withEnvironment(environment)
                .usingAnyFreePort()
                .build();

        LoggingOutputStream loggingOutputStream = new LoggingOutputStream(log, LogLevel.INFO);
        service.sendOutputTo(loggingOutputStream);

        return service;
    }

    private void handleDISPLAYonLinux(Map<String, String> environment) {
        if ("Linux".equals(System.getenv("os.name")) && !environment.containsKey("DISPLAY")) {
            String displayFromEnvironment = System.getenv("DISPLAY");
            if (null != displayFromEnvironment) {
                log.info("using DISPLAY {}", displayFromEnvironment);
                environment.put("DISPLAY", displayFromEnvironment);
            } else {
                log.info("set default DISPLAY=:0");
                environment.put("DISPLAY", ":0");
            }
        }
    }
}