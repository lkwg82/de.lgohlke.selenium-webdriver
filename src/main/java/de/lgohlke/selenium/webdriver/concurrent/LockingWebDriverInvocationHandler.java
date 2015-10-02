package de.lgohlke.selenium.webdriver.concurrent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.locks.StampedLock;

@RequiredArgsConstructor
@Slf4j
class LockingWebDriverInvocationHandler implements InvocationHandler {
    private final StampedLock lock = new StampedLock();
    private final WebDriver wrappedDriver;
    private long stamp               = 0L;
    private long threadIdHoldingLock = 0;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (null == args && "lock".equals(method.getName())) {
            stamp = lock.writeLock();
            threadIdHoldingLock = Thread.currentThread().getId();
            log.info("locking with {}", stamp);
            return (UnlockLockingWebdriver) ((LockingWebDriver) proxy)::unlock;
        } else if (null == args && "unlock".equals(method.getName())) {
            log.info("unlocking with {}", stamp);
            lock.unlockWrite(stamp);
            threadIdHoldingLock = 0;
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
