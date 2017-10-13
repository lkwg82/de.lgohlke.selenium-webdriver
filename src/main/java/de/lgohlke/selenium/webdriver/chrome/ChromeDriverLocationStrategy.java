package de.lgohlke.selenium.webdriver.chrome;

import de.lgohlke.selenium.webdriver.ExecutableFinder;
import de.lgohlke.selenium.webdriver.ExecutablePath;
import lombok.RequiredArgsConstructor;

import java.io.File;
@RequiredArgsConstructor
class ChromeDriverLocationStrategy {
    private final ExecutableFinder finder;

    File findExecutable() {
        String chromedriver = "chromedriver";
        try {
            return new ExecutablePath().buildExecutablePath(chromedriver);
        } catch (IllegalStateException e) {
            finder.addPath("/usr/lib/chromium-browser");
            String path = finder.find(chromedriver);
            if (null == path) {
                throw e;
            } else {
                return new File(path);
            }
        }
    }
}
