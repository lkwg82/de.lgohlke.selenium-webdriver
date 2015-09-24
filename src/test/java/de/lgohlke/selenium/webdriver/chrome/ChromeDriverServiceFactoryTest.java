package de.lgohlke.selenium.webdriver.chrome;

import org.junit.Test;

public class ChromeDriverServiceFactoryTest {

    @Test(expected = IllegalArgumentException.class)
    public void argumentsShouldComeInPairsElseFail() {
        new ChromeDriverServiceFactory(new ChromeDriverConfiguration()).createService("s");
    }
}