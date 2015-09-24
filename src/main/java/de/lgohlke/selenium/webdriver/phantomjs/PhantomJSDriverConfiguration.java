package de.lgohlke.selenium.webdriver.phantomjs;

import de.lgohlke.selenium.webdriver.DriverConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

public class PhantomJSDriverConfiguration implements DriverConfiguration {
    @Override
    public Capabilities createCapabilities() {
        DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
        capabilities.setCapability("phantomjs.page.settings.resourceTimeout", 5000);
        return capabilities;
    }
}
