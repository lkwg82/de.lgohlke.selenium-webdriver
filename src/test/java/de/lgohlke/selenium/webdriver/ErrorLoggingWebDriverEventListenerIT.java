package de.lgohlke.selenium.webdriver;

import de.lgohlke.junit.HttpServerFromResource;
import de.lgohlke.selenium.webdriver.junit.DriverService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import static de.lgohlke.selenium.webdriver.DriverType.CHROME_HEADLESS;
import static de.lgohlke.selenium.webdriver.DriverType.PHANTOMJS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
public class ErrorLoggingWebDriverEventListenerIT {
    @Rule
    public TemporaryFolder        testFolder             = new TemporaryFolder();
    @Rule
    public DriverService          driverServicePhantomJS = new DriverService(PHANTOMJS);
    @Rule
    public DriverService          driverServiceChrome    = new DriverService(CHROME_HEADLESS);
    @Rule
    public HttpServerFromResource httpServer             = new HttpServerFromResource("/");

    private WebDriver                          driverPhantomJS;
    private WebDriver                          driverChrome;
    private ErrorLoggingWebDriverEventListener chromeEventListener;
    private File                               tempFolder;

    @Before
    public void beforeEachTest() throws IOException {
        tempFolder = testFolder.newFolder();
        driverPhantomJS = new EventFiringWebDriver(driverServicePhantomJS.getDriver())
                .register(new ErrorLoggingWebDriverEventListener(tempFolder.getPath()));
        chromeEventListener = new ErrorLoggingWebDriverEventListener(tempFolder.getPath());
        driverChrome = new EventFiringWebDriver(driverServiceChrome.getDriver())
                .register(chromeEventListener);
    }

    @Test
    public void testScreenshotsPhantomJS() throws IOException {
        testWithDriver(driverPhantomJS);
    }

    @Test
    public void testScreenshotsChrome() throws IOException {
        testWithDriver(driverChrome);
    }

    @Test
    public void checkHistory() {
        driverChrome.get(httpServer.url("/index.html"));
        driverChrome.findElement(By.tagName("body"));
        driverChrome.get(httpServer.url("/about.html"));
        driverChrome.findElement(By.tagName("body"));

        assertThat(chromeEventListener.getHistory().size()).isEqualTo(4);
    }

    @Test
    public void webdriverWaitShouldNotTriggerErrorHandler() {
        By                            locator   = By.tagName("bodyy");
        ExpectedCondition<WebElement> condition = ExpectedConditions.presenceOfElementLocated(locator);

        try {
            new WebDriverWait(driverChrome, 1).pollingEvery(1, TimeUnit.SECONDS)
                                              .until(condition);
        } catch (TimeoutException e) {
            // ok
        }

        assertThat(tempFolder.listFiles()).hasSize(0);
    }

    private void testWithDriver(WebDriver driver) throws IOException {
        driver.get(httpServer.url("/index.html"));
        try {
            driver.findElement(By.id("xx"));
        } catch (NoSuchElementException e) {
            // ok
        }

        File[] files = tempFolder.listFiles();
        assertThat(files).hasSize(3);
    }

    @Test
    public void acceptStaleElementExceptionWhenThrownInAn_AfterXY_Hook() {
        EventFiringWebDriver driver = new EventFiringWebDriver(driverServiceChrome.getDriver());
        driver.register(chromeEventListener)
              .register(new AbstractWebDriverEventListener() {
                  @Override
                  public void afterClickOn(WebElement element, WebDriver driver) {
                      try {
                          element.getText();
                      } catch (StaleElementReferenceException e) {
                          // can happen
                      }
                  }
              });

        driver.get(httpServer.url("/form.html"));
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        assertThat(tempFolder.listFiles()).hasSize(0);
    }

    @Test
    public void testSwallowWebdriverExceptionWhenMethodHasAnnotationWithSpecific() {
        driverChrome.get(httpServer.url("/form.html"));

        By selector = By.cssSelector("a");
        new Helper().helpWithSwallow(driverChrome, selector);

        assertThat(tempFolder.listFiles()).hasSize(0);
    }

    @Test
    public void testSwallowWebdriverExceptionWhenMethodHasAnnotationForAll() {
        driverChrome.get(httpServer.url("/form.html"));

        By selector = By.cssSelector("a");
        new Helper().helpWithSwallowAll(driverChrome, selector);

        assertThat(tempFolder.listFiles()).hasSize(0);
    }

    @Test
    public void testSwallowWebdriverExceptionWhenMethodHasAnnotationWithWrongException() {
        driverChrome.get(httpServer.url("/form.html"));

        By selector = By.cssSelector("form");
        new Helper().helpWithSwallowWrongException(driverChrome, selector);

        assertThat(tempFolder.listFiles()).hasSize(3);
    }

    @Test
    public void testSwallowWebdriverExceptionWhenMethodHasNone() {
        driverChrome.get(httpServer.url("/form.html"));

        By selector = By.cssSelector("a");
        new Helper().helpWithSwallowNone(driverChrome, selector);

        assertThat(tempFolder.listFiles()).hasSize(3);
    }

    @Test
    public void shouldLogJSErrors() throws IOException {
        driverChrome.get(httpServer.url("/form.html"));

        try {
            ((JavascriptExecutor) driverChrome).executeScript("NOT_FOUND();");
            fail("should fail");
        } catch (WebDriverException e) {
            // ok
        }

        File[] files = tempFolder.listFiles((FileFilter) new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".log");
            }
        });
        assertThat(files.length).isEqualTo(1);
        String contentOfLogFile = FileUtils.readFileToString(files[0], Charset.defaultCharset());
        assertThat(contentOfLogFile).contains("unknown error: NOT_FOUND is not defined");
    }

    private static class Helper {

        @SwallowWebdriverException(NoSuchElementException.class)
        void helpWithSwallow(WebDriver driver, By by) {
            try {
                driver.findElement(by).getSize();
            } catch (NoSuchElementException e) {
                // ok
            }
        }

        @SwallowWebdriverException(NoSuchElementException.class)
        void helpWithSwallowWrongException(WebDriver driver, By by) {
            try {
                WebElement element = driver.findElement(by);
                driver.get("file:///");
                element.getSize();
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                // ok
            }
        }

        @SwallowWebdriverException
        void helpWithSwallowAll(WebDriver driver, By by) {
            try {
                driver.findElement(by).getSize();
            } catch (NoSuchElementException e) {
                // ok
            }
        }

        void helpWithSwallowNone(WebDriver driver, By by) {
            try {
                driver.findElement(by).getSize();
            } catch (NoSuchElementException e) {
                // ok
            }
        }
    }
}