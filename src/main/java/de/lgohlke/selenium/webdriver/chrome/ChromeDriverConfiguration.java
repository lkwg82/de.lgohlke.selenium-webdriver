package de.lgohlke.selenium.webdriver.chrome;

import de.lgohlke.selenium.webdriver.DriverConfiguration;
import lombok.Getter;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

public class ChromeDriverConfiguration implements DriverConfiguration {
    private final DesiredCapabilities capabilities = DesiredCapabilities.chrome();
    @Getter
    private final ChromeOptions       options      = new ChromeOptions();

    @Getter
    private boolean headless;

    @Override
    public Capabilities createCapabilities() {
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        return capabilities;
    }

    public ChromeDriverConfiguration setUserDir(String path) {
        return addCommandlineSwitch("--user-data-dir=" + path);
    }

    public ChromeDriverConfiguration addCommandlineSwitch(String switchh) {
        options.addArguments(switchh);
        return this;
    }

    public ChromeDriverConfiguration enableHeadlessMode() {
        headless = true;
        return addCommandlineSwitch("--headless")
                .addCommandlineSwitch("--disable-gpu");
    }
}
