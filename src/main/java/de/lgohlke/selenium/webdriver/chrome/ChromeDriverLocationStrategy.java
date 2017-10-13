package de.lgohlke.selenium.webdriver.chrome;

import de.lgohlke.selenium.webdriver.ExecutableFinder;
import de.lgohlke.selenium.webdriver.ExecutablePath;

import java.io.File;

class ChromeDriverLocationStrategy {
    File findExecutable() {
        String chromedriver = "chromedriver";
        try {
            return new ExecutablePath().buildExecutablePath(chromedriver);
        } catch (IllegalStateException e) {
            ExecutableFinder finder = new ExecutableFinder();
            finder.addPath("/usr/lib/chromium-browser/chromedriver");
            String path = finder.find(chromedriver);
            if (null == path) {
                throw e;
            } else {
                return new File(path);
            }
        }
    }
}
