package de.lgohlke.selenium.webdriver.phantomjs;

import de.lgohlke.logging.LogLevel;
import de.lgohlke.logging.LogLevelFilter;
import de.lgohlke.logging.LogLevelFilterFactory;
import de.lgohlke.logging.SysStreamsLogger;
import de.lgohlke.selenium.webdriver.DriverArgumentsBuilder;
import de.lgohlke.selenium.webdriver.DriverServiceFactory;
import de.lgohlke.selenium.webdriver.ExecutablePath;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@Slf4j
public class PhantomJSDriverServiceFactory implements DriverServiceFactory<PhantomJSDriverService> {

    private static final File EXECUTABLE = new ExecutablePath().buildExecutablePath("phantomjs");

    static {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
        Logger.getLogger("global").setLevel(Level.FINEST);

        LogLevelFilter defaultErrorFilter = LogLevelFilterFactory.createAll(LogLevel.INFO, LogLevelFilter.USE.BOTH);
        SysStreamsLogger.bindSystemStreams(defaultErrorFilter);
    }

    public WebDriver createWebDriver(PhantomJSDriverService service) throws IOException {
        DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
        capabilities.setCapability("phantomjs.page.settings.resourceTimeout", 5000);
        return new RemoteWebDriver(service.getUrl(), capabilities);
    }

    public PhantomJSDriverService createService(String... arguments) {

        List<String> argList = new ArrayList<>();
        argList.add("--web-security=false");
        argList.add("--webdriver-loglevel=TRACE");
        argList.add("--load-images=false");
        argList.add("--ignore-ssl-errors=true");
        argList.add("--ssl-protocol=any");
        argList.addAll(Arrays.asList(arguments));

        EXECUTABLE.setExecutable(true);
        return new PhantomJSDriverService.Builder()
                .usingPhantomJSExecutable(EXECUTABLE)
                .usingCommandLineArguments(argList.toArray(new String[argList.size()]))
                .usingAnyFreePort().build();
    }

    public DriverArgumentsBuilder createServiceArgumentsBuilder() {
        return new PhantomJSDriverServiceArgumentsBuilder();
    }
}