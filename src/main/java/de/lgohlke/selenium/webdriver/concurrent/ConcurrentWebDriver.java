package de.lgohlke.selenium.webdriver.concurrent;

import org.apache.commons.lang3.ClassUtils;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * enables multi-threaded interaction with the webdriver api
 */
public class ConcurrentWebDriver {
    /**
     * enables Webdriver usage of the same from different threads (handles locks internally). This locks an already in use instance.
     * <p/>
     * <i>Difference to <tt>ThreadGuard</tt>:</i> It does not forbid usage of a single webdriver by different threads.
     * <p/>
     * based on {@link org.openqa.selenium.support.ThreadGuard}
     */
    public static WebDriver createSyncronized(WebDriver webDriver) {
        ClassLoader       classLoader       = webDriver.getClass().getClassLoader();
        InvocationHandler invocationHandler = new SynchronizedWebDriverInvocationHandler(webDriver);
        List<Class<?>>    interfaces        = ClassUtils.getAllInterfaces(webDriver.getClass());

        return (WebDriver) Proxy.newProxyInstance(classLoader,
                                                  interfaces.toArray(new Class[interfaces.size()]),
                                                  invocationHandler);
    }

    /**
     * adds capability to lock your webDriver instance, suitable for concurrent usage
     * <p/>
     * <pre>
     *      LockingWebDriver lockingDriver = createLocking(webDriver);
     *
     *      // blocking any other locking request
     *      lockingDriver.lock();
     *
     *      lockingDriver.get("http://www.lgohlke.de");
     *
     *      lockingDriver.unlock();
     * </pre>
     */
    public static LockingWebDriver createLocking(WebDriver webDriver) {

        List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(webDriver.getClass());
        allInterfaces.add(LockingWebDriver.class);

        ClassLoader       classLoader       = webDriver.getClass().getClassLoader();
        Class<?>[]        interfaces        = allInterfaces.toArray(new Class[allInterfaces.size()]);
        InvocationHandler invocationHandler = new LockingWebDriverInvocationHandler(webDriver);

        LockingWebDriver lockingWebDriver = (LockingWebDriver) Proxy.newProxyInstance(classLoader,
                                                                                      interfaces,
                                                                                      invocationHandler);
        return (LockingWebDriver) ConcurrentWebDriver.createSyncronized(lockingWebDriver);
    }

}
