package de.lgohlke.selenium.webdriver.concurrent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

@RequiredArgsConstructor
@Slf4j
class LockingWebDriverInvocationHandler implements InvocationHandler {
    private final ReentrantLock threadLock  = new ReentrantLock();
    private final StampedLock   runningLock = new StampedLock();

    private final WebDriver wrappedDriver;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // only if no thread or this thread already holds this lock
        threadLock.lock();
        try {
            // pass not more than one call at a time
            long runningStamp = runningLock.writeLock();
            try {
                return synchronizedInvoke((LockingWebDriver) proxy, method, args);
            } finally {
                runningLock.unlockWrite(runningStamp);
            }
        } finally {
            threadLock.unlock();
        }
    }

    private Object synchronizedInvoke(LockingWebDriver proxy, Method method, Object[] args) throws IllegalAccessException, InvocationTargetException {
        if (null == args && "lock".equals(method.getName())) {
            threadLock.lock();
            log.info("locking thread {}", Thread.currentThread().getId());
            return (UnlockLockingWebdriver) proxy::unlock;
        } else if (null == args && "unlock".equals(method.getName())) {
            log.info("release lock of thread {} with holdcount {}", Thread.currentThread().getId(), threadLock.getHoldCount());
            threadLock.unlock();
            return null;
        } else if (null == args && "isLocked".equals(method.getName())) {
            boolean locked = threadLock.isLocked();
            log.info("is locked {}", locked);
            return locked && threadLock.getHoldCount() > 1;
        } else {
            log.info("calling {}", method.getName());
            return method.invoke(wrappedDriver, args);
        }
    }
}
