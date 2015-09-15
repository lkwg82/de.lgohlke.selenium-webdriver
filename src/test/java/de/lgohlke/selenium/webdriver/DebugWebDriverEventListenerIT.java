package de.lgohlke.selenium.webdriver;

import de.lgohlke.junit.DriverService;
import de.lgohlke.junit.HttpServerFromResource;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.StrictAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.By;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class DebugWebDriverEventListenerIT {
    @Rule
    public TemporaryFolder        testFolder    = new TemporaryFolder();
    @Rule
    public DriverService          driverService = new DriverService(DriverType.CHROME);
    @Rule
    public HttpServerFromResource httpServer    = new HttpServerFromResource("/");

    private EventFiringWebDriver driver;
    private File                 folder;

    @Before
    public void beforeEachTest() throws IOException {
        folder = testFolder.getRoot();
        driver = new EventFiringWebDriver(driverService.getDriver());
    }

    @Test
    public void createFolderIfNotExists() throws IOException {
        String path     = testFolder.getRoot().getPath();
        String pathFull = path + "/test" + System.currentTimeMillis();

        DebugWebDriverEventListener listener = new DebugWebDriverEventListener(pathFull);
        listener.takeScreenshot(driver, new String[]{"text"});

        StrictAssertions.assertThat(new File(pathFull)).exists();
    }

    @Test
    public void createBaseFolderPerRun() throws IOException {
        String path = testFolder.getRoot().getPath();

        IntStream.of(0, 1, 2).forEach(i -> new DebugWebDriverEventListener(path).takeScreenshot(driver, new String[]{"text"}));

        StrictAssertions.assertThat(testFolder.getRoot().list()).hasSize(3);
    }

    @Test
    public void doScreenshotsAtEachInteraction() throws IOException {
        driver.register(new DebugWebDriverEventListener(folder.getPath()));

        driver.get(httpServer.url("/index.html"));
        driver.findElement(By.tagName("body"));
        driver.get(httpServer.url("/about.html"));
        driver.findElement(By.tagName("body"));

        List<String> list = Arrays.asList(folder.listFiles()[0].list());
        assertThat(list).hasSize(8);

        List<String> pngs  = list.stream().filter(entry -> entry.matches(".*\\.png$")).collect(Collectors.toList());
        List<String> htmls = list.stream().filter(entry -> entry.matches(".*\\.html$")).collect(Collectors.toList());
        assertThat(pngs).hasSize(4);
        assertThat(htmls).hasSize(4);
    }

    @Test
    public void acceptStaleElementExceptionWhenThrownInAn_AfterXY_Hook() {
        driver.register(new DebugWebDriverEventListener(folder.getPath()));

        driver.get(httpServer.url("/form.html"));
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }
}