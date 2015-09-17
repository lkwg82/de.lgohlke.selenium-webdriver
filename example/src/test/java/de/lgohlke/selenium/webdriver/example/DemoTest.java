package de.lgohlke.selenium.webdriver.example;

import de.lgohlke.junit.DriverService;
import de.lgohlke.selenium.webdriver.DriverType;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class DemoTest {
    @Rule
    public DriverService driverService = new DriverService(DriverType.CHROME);

    @Test
    public void test() throws InterruptedException {
        WebDriver driver = driverService.getDriver();
        driver.get("https://google.de");
        TimeUnit.SECONDS.sleep(5);
        assertThat(driver.getPageSource()).isNotEmpty();
    }
}
