package de.lgohlke.selenium.webdriver;

import de.lgohlke.selenium.webdriver.chrome.ChromeDriverServiceFactory;
import de.lgohlke.selenium.webdriver.phantomjs.PhantomJSDriverServiceFactory;

public enum DriverType {
    CHROME {
        @Override
        public DriverServiceFactory driverServiceFactory() {
            return new ChromeDriverServiceFactory();
        }

        @Override
        public String[] arguments(int proxyPort) {
            return new ChromeDriverServiceFactory().createServiceArgumentsBuilder()
                                                   .httpProxyServer("http://localhost:" + proxyPort)
                                                   .build();
        }
    },
    PHANTOMJS {
        @Override
        public DriverServiceFactory driverServiceFactory() {
            return new PhantomJSDriverServiceFactory();
        }

        @Override
        public String[] arguments(int proxyPort) {
            return new PhantomJSDriverServiceFactory().createServiceArgumentsBuilder().httpProxyServer(
                    "http://localhost:" + proxyPort).build();
        }
    };

    abstract public DriverServiceFactory driverServiceFactory();

    abstract public String[] arguments(int proxyPort);
}
