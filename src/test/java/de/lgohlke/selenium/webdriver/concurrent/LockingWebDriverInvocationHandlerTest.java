package de.lgohlke.selenium.webdriver.concurrent;

import de.lgohlke.selenium.webdriver.AbstractWebDriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.StrictAssertions.assertThat;

@Slf4j
public class LockingWebDriverInvocationHandlerTest {

    private static final int              TEST_TIMEOUT     = 3000;
    private final        LockingWebDriver lockingWebdriver = ConcurrentWebDriver.createLocking(new AbstractWebDriver() {
        @Override
        public void close() {
            super.close();
        }
    });

    @Test(timeout = TEST_TIMEOUT)
    public void shouldIndicateIfIsLocked() throws Throwable {

        WebDriver driver = new AbstractWebDriver() {
            @Override
            public void close() {
                super.close();
            }
        };
        LockingWebDriverInvocationHandler invocationHandler = new LockingWebDriverInvocationHandler(driver);

        MethodCaller caller = new MethodCaller(invocationHandler);

        assertThat(caller.call("isLocked", Boolean.class)).isFalse();
        caller.call("lock");
        assertThat(caller.call("isLocked", Boolean.class)).isTrue();
        caller.call("unlock");
        assertThat(caller.call("isLocked", Boolean.class)).isFalse();
    }

    @Test(timeout = TEST_TIMEOUT)
    @Ignore("wip")
    public void shouldHoldLockPerThread() throws InterruptedException {
        List<String> calledCommands = Collections.synchronizedList(new ArrayList<>());

        WebDriver driver = new AbstractWebDriver() {
            @Override
            public void get(String url) {
                log.error("get");
                calledCommands.add("get");
            }

            @Override
            public String getCurrentUrl() {
                log.error("getCurrentUrl");
                calledCommands.add("getCurrentUrl");
                return "";
            }
        };

        LockingWebDriver lockingWebdriver = ConcurrentWebDriver.createLocking(driver);

        Runnable longRunningThread = new Runnable() {
            @Override
            public void run() {
                lockingWebdriver.lock();

                sleep();
                lockingWebdriver.get("a");
                sleep();
                lockingWebdriver.get("b");
                sleep();

                lockingWebdriver.unlock();
            }

            private void sleep() {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable timerThread = lockingWebdriver::getCurrentUrl;

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(longRunningThread);
        TimeUnit.MILLISECONDS.sleep(200);
        executorService.submit(timerThread);

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        executorService.shutdownNow();

        Assertions.assertThat(calledCommands).containsExactly("get", "get", "getCurrentUrl");
    }

    @RequiredArgsConstructor
    private static class MethodCaller {
        private final LockingWebDriverInvocationHandler invocationHandler;

        public Object call(String name) throws Throwable {
            return call(name, Void.class);
        }

        public <T> T call(String name, Class<T> castClass) throws Throwable {

            Method method = LockingWebDriver.class.getDeclaredMethod(name);
            Object invoke = invocationHandler.invoke(new LockingWebDriver() {
                @Override
                public UnlockLockingWebdriver lock() {
                    return () -> {
                        // ok
                    };
                }

                @Override
                public void unlock() {

                }

                @Override
                public boolean isLocked() {
                    return false;
                }

                @Override
                public void get(String url) {

                }

                @Override
                public String getCurrentUrl() {
                    return null;
                }

                @Override
                public String getTitle() {
                    return null;
                }

                @Override
                public List<WebElement> findElements(By by) {
                    return null;
                }

                @Override
                public WebElement findElement(By by) {
                    return null;
                }

                @Override
                public String getPageSource() {
                    return null;
                }

                @Override
                public void close() {

                }

                @Override
                public void quit() {

                }

                @Override
                public Set<String> getWindowHandles() {
                    return null;
                }

                @Override
                public String getWindowHandle() {
                    return null;
                }

                @Override
                public TargetLocator switchTo() {
                    return null;
                }

                @Override
                public Navigation navigate() {
                    return null;
                }

                @Override
                public Options manage() {
                    return null;
                }
            }, method, null);

            if (castClass.equals(Void.class)) {
                return null;
            }
            return (T) invoke;
        }
    }


}