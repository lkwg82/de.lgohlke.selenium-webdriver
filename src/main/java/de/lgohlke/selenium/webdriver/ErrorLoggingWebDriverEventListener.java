package de.lgohlke.selenium.webdriver;

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.openqa.selenium.support.ui.FluentWait;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class ErrorLoggingWebDriverEventListener extends AbstractWebDriverEventListener {
    private static final String DATE_FORMAT = "YYYYMMdd_HHmmss_SSS";

    private final Map<String, Object> contextInformation = new HashMap<>();
    @Getter
    private final CircularFifoBuffer  history            = new CircularFifoBuffer(100);
    private final HistoryRecordUtil   util               = new HistoryRecordUtil(history, contextInformation);
    private final String path;

    @Override
    public void beforeFindBy(By by, WebElement element, WebDriver driver) {
        util.add(driver, "beforeFindBy", by.toString());
    }

    @Override
    public void beforeClickOn(WebElement element, WebDriver driver) {
        String type = Joiner.on("|").join(element.getTagName(),
                                          element.getSize(),
                                          element.getLocation(),
                                          element.getText());
        util.add(driver, "beforeClickOn", type);
    }

    @Override
    public void beforeChangeValueOf(WebElement element, WebDriver driver, CharSequence[] keysToSend) {
        String type = Joiner.on("|").join(element.getTagName(),
                                          element.getSize(),
                                          element.getLocation(),
                                          element.getText());
        util.add(driver, "beforeChangeValueOf (keys:["+keysToSend+"])", type);
    }

    @Override
    public void beforeNavigateTo(String url, WebDriver driver) {
        util.add(driver, "beforeNavigateTo", url);
    }

    @Override
    public void onException(Throwable throwable, WebDriver driver) {
        String timestamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());

        if (isExceptionFromFluentWait(throwable)
                || isExceptionFromWebDriverEventListerAfterXYHook(throwable)
                || isExceptionFromMethodWithSwallowExceptionAnnotation(throwable)) {
            return;
        }

        log.error(throwable.getMessage(), throwable);
        takeScreenshot(timestamp, driver);

        try {
            Files.write(Paths.get(path, timestamp + "_source.html"), driver.getPageSource().getBytes());
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }

        List<String> lines = new ArrayList<>();
        lines.add("url:" + driver.getCurrentUrl());
        lines.add("last");
        lines.add("  by             : " + get("by"));
        lines.add("  clickOn        : " + get("clickOn"));
        lines.add("  changeValueOf  : " + get("changeValueOf"));
        lines.add("  message        : " + throwable.getMessage());
        lines.add("");
        lines.add("---- history ---- ");
        lines.add("");
        for (Object o : history) {
            lines.add(o.toString());
        }

        lines.add("");
        lines.add("---- console logs ---- ");
        Logs logs = driver.manage().logs();
        logs.getAvailableLogTypes().forEach(type -> {
            lines.add(type + " : ");
            logs.get(type)
                .getAll()
                .forEach(entry -> lines.add(" " + entry.getTimestamp() + entry.getLevel() + entry.getMessage()));
        });

        try {
            Files.write(Paths.get(path, timestamp + "_logs.log"), lines);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    private boolean isExceptionFromMethodWithSwallowExceptionAnnotation(Throwable throwable) {
        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            String className = stackTraceElement.getClassName();
            Class<?> clazz = null;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                log.debug(e.getMessage(), e);
            }

            // some sun classes are not accessible
            if (clazz == null) {
                continue;
            }

            String methodName = stackTraceElement.getMethodName();
            List<Method> matchingMethods = Arrays.asList(clazz.getDeclaredMethods())
                                                 .stream()
                                                 .filter(method -> method.getName().equals(
                                                         methodName))
                                                 .filter(m -> m.getAnnotation(SwallowWebdriverException.class) != null)
                                                 .filter(m -> {
                                                     SwallowWebdriverException annotation = m.getAnnotation(
                                                             SwallowWebdriverException.class);
                                                     Class<?>[] acceptedExceptions = annotation.value();

                                                     if (acceptedExceptions.length == 0) {
                                                         return true;
                                                     } else {
                                                         return Arrays.asList(acceptedExceptions)
                                                                      .contains(throwable.getClass());
                                                     }
                                                 })
                                                 .collect(toList());

            if (!matchingMethods.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isExceptionFromWebDriverEventListerAfterXYHook(Throwable throwable) {
        if (throwable instanceof StaleElementReferenceException) {
            for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                String className = stackTraceElement.getClassName();
                Class<?> clazz = null;
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    log.debug(e.getMessage(), e);
                }

                // some sun classes are not accessible
                if (clazz == null) {
                    continue;
                }

                if (WebDriverEventListener.class.isAssignableFrom(clazz)
                        && stackTraceElement.getMethodName().startsWith("after")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isExceptionFromFluentWait(Throwable throwable) {
        if (!(throwable instanceof NoSuchElementException)) {
            return false;
        }

        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            if (FluentWait.class.getName().equals(stackTraceElement.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private String get(String key) {
        return contextInformation.containsKey(key) ? (String) contextInformation.get(key) : "";
    }

    private void takeScreenshot(String screenshotName, WebDriver driver) {
        if (driver instanceof TakesScreenshot) {
            File tempFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            try {
                File destFile = new File(path + "/" + screenshotName + ".png");
                FileUtils.copyFile(tempFile, destFile);
                log.info(destFile + "");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new IllegalStateException(e);
            }
        } else {
            log.error("need driver with capability to make screenshots");
        }
    }

    @Getter
    @RequiredArgsConstructor
    static class HistoryRecord {
        private final Date date = new Date();
        private final String url;
        private final String type;

        public String toString() {
            return new SimpleDateFormat(DATE_FORMAT).format(date) + " url:" + url + ", type:" + type;
        }
    }

    @RequiredArgsConstructor
    static class HistoryRecordUtil {
        private final CircularFifoBuffer  history;
        private final Map<String, Object> contextInformation;

        void add(WebDriver driver, String contextKey, String description) {
            contextInformation.put(contextKey, description);
            history.add(new HistoryRecord(driver.getCurrentUrl(), String.format("%20s | ", contextKey) + description));
        }
    }
}
