package de.lgohlke.selenium.webdriver;

import de.lgohlke.selenium.webdriver.chrome.ChromeDriverServiceFactory;
import de.lgohlke.selenium.webdriver.phantomjs.PhantomJSDriverServiceFactory;

public enum DriverType {
    CHROME {
        @Override
        public DriverServiceFactory driverServiceFactory() {
            return new ChromeDriverServiceFactory();
        }
    },
    PHANTOMJS {
        @Override
        public DriverServiceFactory driverServiceFactory() {
            return new PhantomJSDriverServiceFactory();
        }
    };

    abstract public DriverServiceFactory driverServiceFactory();
}
