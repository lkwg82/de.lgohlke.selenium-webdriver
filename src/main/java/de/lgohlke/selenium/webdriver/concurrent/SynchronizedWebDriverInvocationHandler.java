package de.lgohlke.selenium.webdriver.concurrent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.locks.StampedLock;

@RequiredArgsConstructor
@Slf4j
class SynchronizedWebDriverInvocationHandler implements InvocationHandler {
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
