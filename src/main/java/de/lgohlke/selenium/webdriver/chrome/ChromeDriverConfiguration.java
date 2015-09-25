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

    public ChromeDriverConfiguration setUserDir(String path) {
        return addCommandlineSwitch("user-data-dir=" + path);
    }

    public ChromeDriverConfiguration addCommandlineSwitch(String switchh) {
        options.addArguments(switchh);
        return this;
    }
}
