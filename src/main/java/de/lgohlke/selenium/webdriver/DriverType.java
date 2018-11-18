package de.lgohlke.selenium.webdriver;

import com.google.common.base.Preconditions;
import de.lgohlke.selenium.webdriver.chrome.ChromeDriverConfiguration;
import de.lgohlke.selenium.webdriver.chrome.ChromeDriverServiceFactory;

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
    CHROME_HEADLESS {
        @Override
        public <T extends DriverConfiguration> DriverServiceFactory driverServiceFactory(T driverConfiguration) {
            Preconditions.checkArgument(driverConfiguration instanceof ChromeDriverConfiguration,
                                        "must be instance of " + ChromeDriverConfiguration.class);

            ((ChromeDriverConfiguration) driverConfiguration).enableHeadlessMode();

            return new ChromeDriverServiceFactory((ChromeDriverConfiguration) driverConfiguration);
        }

        @Override
        public Class<? extends DriverConfiguration> getDriverConfigurationClass() {
            return ChromeDriverConfiguration.class;
        }
    };

    abstract public <T extends DriverConfiguration> DriverServiceFactory driverServiceFactory(T driverConfiguration);

    abstract public Class<? extends DriverConfiguration> getDriverConfigurationClass();
}
