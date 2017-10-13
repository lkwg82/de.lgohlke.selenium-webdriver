package de.lgohlke.selenium.webdriver.chrome;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ChromeDriverLocationStrategyTest {

    @Test
    public void test() {
        ChromeDriverLocationStrategy locationStrategy = new ChromeDriverLocationStrategy();

        assertThat(locationStrategy.findExecutable()).isFile();
    }

}
