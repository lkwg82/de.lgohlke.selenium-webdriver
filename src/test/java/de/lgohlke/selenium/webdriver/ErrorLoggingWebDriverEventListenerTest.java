package de.lgohlke.selenium.webdriver;

import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class ErrorLoggingWebDriverEventListenerTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void createFolderIfMissing() throws IOException {
        String                             path     = temporaryFolder.newFolder("xx").getPath();
        ErrorLoggingWebDriverEventListener listener = new ErrorLoggingWebDriverEventListener(path);

        listener.onException(new NullPointerException("test"), new MyDriver());

        assertThat(new File(path)).exists();
    }

    @Slf4j
    @SuppressWarnings("unchecked")
    private static class MyDriver implements WebDriver, TakesScreenshot {

        @Override
        public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
            try {
                return (X) File.createTempFile("xasdasd", "x");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return null;
            }
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
            return "empty";
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
    }
}