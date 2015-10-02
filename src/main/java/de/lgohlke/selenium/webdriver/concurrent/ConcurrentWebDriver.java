package de.lgohlke.selenium.webdriver.concurrent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.locks.StampedLock;

/**
 * enables Webdriver usage of the same from different threads (handles locks internally)
 * <p/>
 * based on {@link org.openqa.selenium.support.ThreadGuard}
 */
@RequiredArgsConstructor
public class ConcurrentWebDriver {

    public static WebDriver createSyncronized(WebDriver webDriver) {
        ClassLoader       classLoader       = webDriver.getClass().getClassLoader();
        InvocationHandler invocationHandler = new SynchronizedWebDriverInvocationHandler(webDriver);
        List<Class<?>>    interfaces        = ClassUtils.getAllInterfaces(webDriver.getClass());

        return (WebDriver) Proxy.newProxyInstance(classLoader,
                                                  interfaces.toArray(new Class[interfaces.size()]),
                                                  invocationHandler);
    }

    public static LockingWebDriver createLocking(WebDriver webDriver) {
        ClassLoader classLoader = webDriver.getClass().getClassLoader();

        List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(webDriver.getClass());
        allInterfaces.add(LockingWebDriver.class);
        Class<?>[]        interfaces        = allInterfaces.toArray(new Class[allInterfaces.size()]);
        InvocationHandler invocationHandler = new LockingWebDriverInvocationHandler(webDriver);
        Object            proxyInstance     = Proxy.newProxyInstance(classLoader, interfaces, invocationHandler);

        return (LockingWebDriver) proxyInstance;
    }

    @RequiredArgsConstructor
    @Slf4j
    private static class SynchronizedWebDriverInvocationHandler implements InvocationHandler {
        private final StampedLock lock = new StampedLock();
        private final WebDriver wrappedDriver;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            log.info("isLocked {}", lock.isWriteLocked());
            long stamp = lock.writeLock();
            try {
                return method.invoke(wrappedDriver, args);
            } finally {
                lock.unlockWrite(stamp);
            }
        }
    }

    @RequiredArgsConstructor
    @Slf4j
    private static class LockingWebDriverInvocationHandler implements InvocationHandler {
        private final StampedLock lock = new StampedLock();
        private final WebDriver wrappedDriver;
        private long stamp = 0L;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (null == args && "lock".equals(method.getName())) {
                stamp = lock.writeLock();
                log.info("locking with {}", stamp);
                return (UnlockLockingWebdriver) ((LockingWebDriver) proxy)::unlock;
            } else if (null == args && "unlock".equals(method.getName())) {
                log.info("unlocking with {}", stamp);
                lock.unlockWrite(stamp);
                return null;
            } else if (null == args && "isLocked".equals(method.getName())) {
                boolean writeLocked = lock.isWriteLocked();
                log.info("is locked {}", writeLocked);
                return writeLocked;
            } else {
                return method.invoke(wrappedDriver, args);
            }
        }
    }
}
