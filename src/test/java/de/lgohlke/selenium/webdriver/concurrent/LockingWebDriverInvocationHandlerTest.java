package de.lgohlke.selenium.webdriver.concurrent;

import com.google.common.collect.Lists;
import de.lgohlke.selenium.webdriver.AbstractWebDriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class LockingWebDriverInvocationHandlerTest {
    private static final int TEST_TIMEOUT = 3000;

    @Test(timeout = TEST_TIMEOUT)
    public void shouldIndicateIfIsLocked() {

        WebDriver driver = new AbstractWebDriver() {
            @Override
            public void close() {
                super.close();
            }
        };
        LockingWebDriverInvocationHandler invocationHandler = new LockingWebDriverInvocationHandler(driver);
        MethodCaller                      caller            = new MethodCaller(invocationHandler);

        assertThat(caller.call("isLocked", Boolean.class)).isFalse();
        caller.call("lock");
        assertThat(caller.call("isLocked", Boolean.class)).isTrue();
        caller.call("unlock");
        assertThat(caller.call("isLocked", Boolean.class)).isFalse();
    }

    @Test(timeout = TEST_TIMEOUT * 10)
    public void shouldHoldLockPerThread() throws InterruptedException {
        List<String> calledCommands = Collections.synchronizedList(new ArrayList<>());

        WebDriver driver = new AbstractWebDriver() {
            @Override
            public void get(String url) {
                calledCommands.add("get " + url);
            }

            @Override
            public String getCurrentUrl() {
                calledCommands.add("getCurrentUrl");
                return "";
            }

            @Override
            public String getPageSource() {
                calledCommands.add("getPageSource");
                return "";
            }
        };

        LockingWebDriverInvocationHandler invocationHandler = new LockingWebDriverInvocationHandler(driver);
        MethodCaller                      caller            = new MethodCaller(invocationHandler);

        Runnable longRunningThread = new Runnable() {
            @Override
            public void run() {
                caller.call("lock");
                // webdriver calls and sleeps in between
                sleep(1);
                caller.call("get", Void.class, "a");
                sleep(1);
                caller.call("get", Void.class, "b");
                sleep(1);
                caller.call("unlock");
            }

            private void sleep(int microseconds) {
                try {
                    TimeUnit.MICROSECONDS.sleep(microseconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable timerThread  = () -> caller.call("getCurrentUrl", String.class);
        Runnable timer2Thread = () -> caller.call("getPageSource");

        List<Runnable> actions    = Lists.newArrayList(longRunningThread, timerThread, timer2Thread);
        List<Runnable> allACtions = new ArrayList<>();
        IntStream.range(0, 1000).forEach(i -> actions.forEach(allACtions::add));

        Collections.shuffle(allACtions, new SecureRandom());

        ExecutorService executorService = Executors.newFixedThreadPool(200);

        allACtions.parallelStream().forEach(executorService::submit);

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
        executorService.shutdownNow();

        int found = 0;
        int size  = calledCommands.size();
        for (int i = 0; i < size; i++) {
            if ((i + 1) < size && calledCommands.get(i).equals("get a")) {
                assertThat(calledCommands.get(i + 1)).describedAs(calledCommands + "").isEqualTo("get b");
                found++;
            }
        }
        assertThat(found).isEqualTo(1000);
    }

    @RequiredArgsConstructor
    private static class MethodCaller {
        private static final LockingWebDriver PROXY = new LockingWebDriver() {
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
        };
        private final LockingWebDriverInvocationHandler invocationHandler;

        public Object call(String name) {
            return call(name, Void.class);
        }

        @SuppressWarnings("unchecked")
        public <T> T call(String name, Class<T> castClass, Object... args) {

            List<Class> parameterTypes = new ArrayList<>();
            Arrays.asList(args).forEach(a -> parameterTypes.add(a.getClass()));

            try {
                Method method;
                try {
                    method = LockingWebDriver.class.getDeclaredMethod(name);
                } catch (NoSuchMethodException e) {
                    if (parameterTypes.isEmpty()) {
                        method = WebDriver.class.getDeclaredMethod(name);
                    } else {
                        method = WebDriver.class.getDeclaredMethod(name,
                                                                   parameterTypes.toArray(new Class[parameterTypes.size()]));
                    }
                }
                Object invoke = invocationHandler.invoke(PROXY, method, args.length == 0 ? null : args);

                if (castClass.equals(Void.class)) {
                    return null;
                }
                return (T) invoke;
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
    }


}