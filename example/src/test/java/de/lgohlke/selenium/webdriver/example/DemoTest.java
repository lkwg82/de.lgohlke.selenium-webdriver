package de.lgohlke.selenium.webdriver.example;

import de.lgohlke.junit.DriverService;
import de.lgohlke.selenium.webdriver.DriverType;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

public class DemoTest {
    @Rule
    private DriverService driverService = new DriverService(DriverType.CHROME);

    @Test
    public void test() throws InterruptedException {
        WebDriver driver = driverService.getDriver();
        driver.get("https://google.de");
        assertThat(driver.getPageSource()).isNotEmpty();
    }
}
