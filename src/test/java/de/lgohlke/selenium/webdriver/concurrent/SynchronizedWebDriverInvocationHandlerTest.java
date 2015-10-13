package de.lgohlke.selenium.webdriver.concurrent;

import de.lgohlke.selenium.webdriver.AbstractWebDriver;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void shouldNotThrowInvocationTargetException() throws NoSuchMethodException {

        AbstractWebDriver driver = new AbstractWebDriver() {
            @Override
            public String getWindowHandle() {
                throw new WebDriverException("test");
            }
        };
        InvocationHandler invocationHandler = new SynchronizedWebDriverInvocationHandler(driver);
        Method method = WebDriver.class.getDeclaredMethod("getWindowHandle");

        try {
            invocationHandler.invoke(null,method,null);
            fail("should not pass");
        } catch (Throwable e) {
            assertThat(e).isInstanceOf(WebDriverException.class);
        }
    }
}