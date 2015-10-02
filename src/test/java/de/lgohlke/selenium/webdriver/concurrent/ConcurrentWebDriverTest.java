package de.lgohlke.selenium.webdriver.concurrent;

import de.lgohlke.selenium.webdriver.AbstractWebDriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ConcurrentWebDriverTest {
    private static final int TEST_TIMEOUT = 3000;

    private final long             sleepMilliseconds     = TimeUnit.MILLISECONDS.toMillis(1);
    private final LongAdder        invokedCounter        = new LongAdder();
    private final AtomicInteger    maxThreads            = new AtomicInteger();
    private final MyWebdriver      wrappedDriver         = new MyWebdriver(maxThreads,
                                                                           sleepMilliseconds,
                                                                           invokedCounter);
    private final WebDriver        synchronizedWebdriver = ConcurrentWebDriver.createSyncronized(wrappedDriver);
    private final LockingWebDriver lockingWebdriver      = ConcurrentWebDriver.createLocking(wrappedDriver);

    private static void runTest(Runnable action, int threads, int maxRange) throws InterruptedException {
        ExecutorService actionExecutors = Executors.newFixedThreadPool(threads);

        IntStream.range(0, maxRange).parallel().forEach(i -> actionExecutors.submit(action));

        actionExecutors.shutdown();
        actionExecutors.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test(timeout = TEST_TIMEOUT)
    public void regularWebdriverShouldAcceptConcurrentUsage() throws InterruptedException {
        int invocations = 1000;

        runTest(() -> wrappedDriver.get("xyz"), 20, invocations);

        assertThat(maxThreads.intValue()).isGreaterThan(1);
        assertThat(invokedCounter.intValue()).isEqualTo(invocations);
    }

    @Test(timeout = TEST_TIMEOUT)
    public void synchronizedWebdriverShouldAcceptAtMostSingleUsage() throws InterruptedException {
        int invocations = 1000;

        runTest(() -> synchronizedWebdriver.get("xyz"), 200, invocations);

        assertThat(maxThreads.intValue()).isEqualTo(1);
        assertThat(invokedCounter.intValue()).isEqualTo(invocations);
    }

    @Test(timeout = TEST_TIMEOUT)
    public void setLockingWebdriverShouldAcceptAtMostSingleUsageAutocloseable() throws InterruptedException {
        int invocations = 500;

        runTest(() -> {
            try (UnlockLockingWebdriver ignored = lockingWebdriver.lock()) {
                lockingWebdriver.get("xyz");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }, 50, invocations);

        assertThat(maxThreads.intValue()).isEqualTo(1);
        assertThat(invokedCounter.intValue()).isEqualTo(invocations);
    }

    @Test(timeout = TEST_TIMEOUT)
    public void setLockingWebdriverShouldAcceptAtMostSingleUsagePlain() throws InterruptedException {
        int invocations = 500;

        runTest(() -> {
            lockingWebdriver.lock();
            lockingWebdriver.get("xyz");
            lockingWebdriver.unlock();
        }, 50, invocations);

        assertThat(maxThreads.intValue()).isEqualTo(1);
        assertThat(invokedCounter.intValue()).isEqualTo(invocations);
    }

    @RequiredArgsConstructor
    @Slf4j
    static class MyWebdriver extends AbstractWebDriver {
        private final LongAdder counter = new LongAdder();

        private final AtomicInteger max;
        private final long          sleepMilliseconds;
        private final LongAdder     invokedCounter;

        @Override
        public void get(String url) {
            invokedCounter.increment();
            log();
            counter.increment();
            log();

            if (max.intValue() < counter.intValue()) {
                max.set(counter.intValue());
            }

            try {
                TimeUnit.MILLISECONDS.sleep(sleepMilliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            counter.decrement();
            log();
        }

        private void log() {
//            log.info("thread: {} counter: {} ", Thread.currentThread().getId(), counter.intValue());
        }
    }

}
