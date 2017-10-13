package de.lgohlke.selenium.webdriver.chrome;

import de.lgohlke.selenium.webdriver.ExecutableFinder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class ChromeDriverLocationStrategyTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private ExecutableFinder             executableFinder = new ExecutableFinder();
    private ChromeDriverLocationStrategy locationStrategy = new ChromeDriverLocationStrategy(executableFinder);

    @Test
    public void shouldFindChromiumDriver() {
        String pathname = "/usr/lib/chromium-browser/chromedriver";
        assertThat(new File(pathname)).describedAs("you should install chromium-chromedriver package")
                                      .isFile();

        environmentVariables.set("DRIVERS_PATH", "");

        assertThat(locationStrategy.findExecutable()
                                   .getPath()).isEqualTo(pathname);
    }
}
