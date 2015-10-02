package de.lgohlke.selenium.webdriver.concurrent;

import de.lgohlke.selenium.webdriver.AbstractWebDriver;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class SynchronizedWebDriverInvocationHandlerTest {
    @Test(timeout = 500)
    public void shouldNotBlockWhenRunningSequentially() throws Throwable {
        AbstractWebDriver wrappedDriver = new AbstractWebDriver() {
            @Override
            public String getCurrentUrl() {
                return "toll";
            }
        };
        InvocationHandler invocationHandler = new SynchronizedWebDriverInvocationHandler(wrappedDriver);

        Method method = WebDriver.class.getDeclaredMethod("getCurrentUrl");
        invocationHandler.invoke(null, method, null);
        invocationHandler.invoke(null, method, null);

    }

}