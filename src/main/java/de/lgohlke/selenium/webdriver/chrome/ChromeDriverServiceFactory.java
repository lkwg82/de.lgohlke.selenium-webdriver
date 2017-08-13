package de.lgohlke.selenium.webdriver.chrome;

import de.lgohlke.logging.LogLevel;
import de.lgohlke.logging.LogLevelFilter;
import de.lgohlke.logging.LogLevelFilterFactory;
import de.lgohlke.logging.SysStreamsLogger;
import de.lgohlke.selenium.webdriver.DriverArgumentsBuilder;
import de.lgohlke.selenium.webdriver.DriverServiceFactory;
import de.lgohlke.selenium.webdriver.ExecutablePath;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeDriverService.Builder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ChromeDriverServiceFactory extends DriverServiceFactory<ChromeDriverService, ChromeDriverConfiguration> {
    private static final File EXECUTABLE = new ExecutablePath().buildExecutablePath("chromedriver");

    static {
        LogLevelFilter defaultErrorFilter = LogLevelFilterFactory.createAll(LogLevel.INFO, LogLevelFilter.USE.SYSERR);
        LogLevelFilter warnErrorFilter = new LogLevelFilter() {
            @Override
            public boolean apply(String message) {
                return message.contains("[WARNING]") && !message.contains(
                        "PAC support disabled because there is no logging implementation");
            }

            @Override
            public LogLevel level() {
                return LogLevel.WARN;
            }

            @Override
            public USE useFor() {
                return USE.SYSERR;
            }
        };

        SysStreamsLogger.bindSystemStreams(defaultErrorFilter, warnErrorFilter);
    }

    public ChromeDriverServiceFactory(ChromeDriverConfiguration config) {
        super(config);
    }

    public DriverArgumentsBuilder createServiceArgumentsBuilder() {
        return new ChromeDriverServiceArgumentsBuilder();
    }

    public ChromeDriverService createService(String... args) {

        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("arguments should be pairs");
        }

        Map<String, String> environment = new HashMap<>();
        for (int i = 1; i < args.length; i += 2) {
            environment.put(args[i - 1], args[i]);
        }

        return new Builder()
                .usingDriverExecutable(EXECUTABLE)
                .withVerbose(log.isInfoEnabled())
                .withEnvironment(environment)
                .usingAnyFreePort().build();
    }
}