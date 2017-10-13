package de.lgohlke.selenium.webdriver.phantomjs;

import de.lgohlke.logging.LogLevel;
import de.lgohlke.logging.LoggingOutputStream;
import de.lgohlke.selenium.webdriver.DriverArgumentsBuilder;
import de.lgohlke.selenium.webdriver.DriverServiceFactory;
import de.lgohlke.selenium.webdriver.ExecutablePath;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@Slf4j
public class PhantomJSDriverServiceFactory extends DriverServiceFactory<PhantomJSDriverService, PhantomJSDriverConfiguration> {

    private static final File EXECUTABLE = new ExecutablePath().buildExecutablePath("phantomjs");

    static {
        LogManager.getLogManager()
                  .reset();
        SLF4JBridgeHandler.install();
        Logger.getLogger("global")
              .setLevel(Level.FINEST);
    }

    public PhantomJSDriverServiceFactory(PhantomJSDriverConfiguration driverConfiguration) {
        super(driverConfiguration);
    }

    public PhantomJSDriverService createService(String... arguments) {

        List<String> argList = new ArrayList<>();
        argList.add("--web-security=false");
        argList.add("--webdriver-loglevel=TRACE");
        argList.add("--load-images=false");
        argList.add("--ignore-ssl-errors=true");
        argList.add("--ssl-protocol=any");
        argList.addAll(Arrays.asList(arguments));

        PhantomJSDriverService service = new PhantomJSDriverService.Builder()
                .usingPhantomJSExecutable(EXECUTABLE)
                .usingCommandLineArguments(argList.toArray(new String[argList.size()]))
                .usingAnyFreePort()
                .build();

        LoggingOutputStream loggingOutputStream = new LoggingOutputStream(log, LogLevel.INFO);
        service.sendOutputTo(loggingOutputStream);

        return service;
    }

    public DriverArgumentsBuilder createServiceArgumentsBuilder() {
        return new PhantomJSDriverServiceArgumentsBuilder();
    }
}