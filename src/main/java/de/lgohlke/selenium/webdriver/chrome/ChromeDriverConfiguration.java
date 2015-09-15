package de.lgohlke.selenium.webdriver.chrome;

import de.lgohlke.selenium.webdriver.DriverConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

public class ChromeDriverConfiguration implements DriverConfiguration {
    private final DesiredCapabilities capabilities = DesiredCapabilities.chrome();
    private final ChromeOptions       options      = new ChromeOptions();

    @Override
    public Capabilities createCapabilities() {
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        return capabilities;
    }

    public DriverConfiguration setUserDir(String path) {
        options.addArguments("user-data-dir=" + path);
        return this;
    }
}
