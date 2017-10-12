package de.lgohlke.selenium.webdriver.chrome;

import com.sun.net.httpserver.HttpServer;
import de.lgohlke.junit.FreeportProber;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.net.PortProber;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ChromeDriverServiceFactoryIT {

    private final ChromeDriverServiceFactory factory         = new ChromeDriverServiceFactory(new ChromeDriverConfiguration().enableHeadlessMode());
    @Rule
    public        TemporaryFolder            temporaryFolder = new TemporaryFolder();
    @Rule
    public        FreeportProber             proxyPortProber = new FreeportProber();

    private HttpServer          httpServer;
    private ChromeDriverService driverService;

    @Before
    public void beforeEachTest() throws IOException {
        httpServer = HttpServer.create();
        httpServer.createContext("/webdriverTest", httpExchange -> {
            String response = "Welcome Real's HowTo test page";
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        httpServer.bind(new InetSocketAddress("localhost", PortProber.findFreePort()), 0);
        httpServer.start();
    }

    @After
    public void cleanup() {
        httpServer.stop(1);
        if (driverService != null) {
            driverService.stop();
        }
    }

    @Test
    public void startAndStop() throws IOException {
        ChromeDriverService driverService = factory.createService();

        try {
            driverService.start();

            WebDriver webDriver = factory.createWebDriver(driverService);

            String url = "http://localhost:" + httpServer.getAddress()
                                                         .getPort() + "/webdriverTest";
            webDriver.get(url);

            assertThat(webDriver.getCurrentUrl()).isEqualTo(url);
        } finally {
            driverService.stop();
        }
    }

    @Ignore("profiles in headless mode broken")
    @Test
    public void testUsingCustomUserprofile() throws IOException, InterruptedException {

        ChromeDriverConfiguration  driverConfiguration = new ChromeDriverConfiguration().enableHeadlessMode();
        ChromeDriverServiceFactory factory             = new ChromeDriverServiceFactory(driverConfiguration);

        driverConfiguration.setUserDir(temporaryFolder.newFolder()
                                                      .getAbsolutePath());

        String url = "http://localhost:" + httpServer.getAddress()
                                                     .getPort() + "/webdriverTest";

        driverService = factory.createService();
        driverService.start();
        try {
            WebDriver driver = factory.createWebDriver(driverService);
            driver.get(url);

            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.localStorage.setItem('x','x');");
        } finally {
            driverService.stop();
        }


        driverService.start();
        try {
            WebDriver driver = factory.createWebDriver(driverService);
            driver.get(url);
            JavascriptExecutor js   = (JavascriptExecutor) driver;
            String             item = (String) js.executeScript("return window.localStorage.getItem('x');");
            assertThat(item).isEqualTo("x");
        } finally {
            driverService.stop();
        }
    }

    @Ignore
    @Test
    public void testProxyHTTP() throws IOException {
        String[] arguments = factory.createServiceArgumentsBuilder()
                                    .httpProxyServer("http://localhost:" + proxyPortProber.getPort())
                                    .build();
        driverService = factory.createService(arguments);
        driverService.start();

        WebDriver webDriver = factory.createWebDriver(driverService);
        webDriver.get("http://www.lgohlke.de");
        assertThat(webDriver.getPageSource()
                            .length()).isBetween(24000, 26000);
    }

    @Ignore
    @Test
    public void testProxyHTTPS() throws IOException {
        String[] arguments = factory.createServiceArgumentsBuilder()
                                    .httpProxyServer("http://localhost:" + proxyPortProber.getPort())
                                    .build();
        driverService = factory.createService(arguments);
        driverService.start();

        WebDriver webDriver = factory.createWebDriver(driverService);
        webDriver.get("https://www.google.de");
        assertThat(webDriver.getPageSource()
                            .length()).isBetween(100000, 120022);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testServiceArgumentBuilder() {
        factory.createServiceArgumentsBuilder()
               .httpProxyServer("httpx://localhost:8080")
               .build();
    }
}