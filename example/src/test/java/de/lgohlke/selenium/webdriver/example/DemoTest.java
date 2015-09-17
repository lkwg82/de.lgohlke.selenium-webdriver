package de.lgohlke.selenium.webdriver.example;

import de.lgohlke.junit.DriverService;
import de.lgohlke.selenium.webdriver.DriverType;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

public class DemoTest {
    @Rule
    public DriverService driverService = new DriverService(DriverType.CHROME);
    @Rule
    public DriverService driverService2 = new DriverService(DriverType.PHANTOMJS);

    @Test
    public void test() throws InterruptedException {
        WebDriver driver = driverService.getDriver();
        driver.get("https://google.de");
        assertThat(driver.getPageSource()).isNotEmpty();
    }

    @Test
    public void test2() throws InterruptedException {
        WebDriver driver = driverService2.getDriver();
        driver.get("https://google.de");
        assertThat(driver.getPageSource()).isNotEmpty();
    }
}
