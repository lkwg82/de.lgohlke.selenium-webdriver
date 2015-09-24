package de.lgohlke.selenium.webdriver;

import com.google.common.base.Preconditions;
import de.lgohlke.selenium.webdriver.chrome.ChromeDriverConfiguration;
import de.lgohlke.selenium.webdriver.chrome.ChromeDriverServiceFactory;
import de.lgohlke.selenium.webdriver.phantomjs.PhantomJSDriverConfiguration;
import de.lgohlke.selenium.webdriver.phantomjs.PhantomJSDriverServiceFactory;

public enum DriverType {
    CHROME {
        @Override
        public <T extends DriverConfiguration> DriverServiceFactory driverServiceFactory(T driverConfiguration) {
            Preconditions.checkArgument(driverConfiguration instanceof ChromeDriverConfiguration,
                                        "must be instance of " + ChromeDriverConfiguration.class);
            return new ChromeDriverServiceFactory((ChromeDriverConfiguration) driverConfiguration);
        }

        @Override
        public Class<? extends DriverConfiguration> getDriverConfigurationClass() {
            return ChromeDriverConfiguration.class;
        }
    },
    PHANTOMJS {
        @Override
        public <T extends DriverConfiguration> DriverServiceFactory driverServiceFactory(T driverConfiguration) {
            Preconditions.checkArgument(driverConfiguration instanceof PhantomJSDriverConfiguration,
                                        "must be instance of " + PhantomJSDriverConfiguration.class);
            return new PhantomJSDriverServiceFactory((PhantomJSDriverConfiguration) driverConfiguration);
        }

        @Override
        public Class<? extends DriverConfiguration> getDriverConfigurationClass() {
            return PhantomJSDriverConfiguration.class;
        }
    };

    abstract public <T extends DriverConfiguration> DriverServiceFactory driverServiceFactory(T driverConfiguration);

    abstract public Class<? extends DriverConfiguration> getDriverConfigurationClass();
}
