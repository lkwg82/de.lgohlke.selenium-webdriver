package de.lgohlke.selenium.webdriver.chrome;

import org.junit.Test;

public class ChromeDriverServiceFactoryTest {

    @Test(expected = IllegalArgumentException.class)
    public void argumentsShouldComeInPairsElseFail() {
        ChromeDriverServiceFactory serviceFactory = new ChromeDriverServiceFactory(new ChromeDriverConfiguration());

        serviceFactory.createService("s");
    }
}