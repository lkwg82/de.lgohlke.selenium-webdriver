package de.lgohlke.selenium.webdriver.chrome;

import com.sun.net.httpserver.HttpServer;
import de.lgohlke.junit.FreeportProber;
import de.lgohlke.junit.Mitmdump;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.net.PortProber;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static de.lgohlke.junit.p.Mitmdump.MODE.SERVE;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ChromeDriverServiceFactoryIT {
    private final static ChromeDriverConfiguration config = new ChromeDriverConfiguration();
    private final static ChromeDriverServiceFactory factory         = new ChromeDriverServiceFactory(config);

    static {
        config.addCommandlineSwitch("--no-sandbox");
    }

    @Rule
    public               TemporaryFolder            temporaryFolder = new TemporaryFolder();
    private              FreeportProber             proxyPortProber = new FreeportProber();
    private              Mitmdump                   mitmdump        = new Mitmdump(SERVE,
                                                                                   "/proxy.flow",
                                                                                   proxyPortProber);
    @Rule
    public               TestRule                   chain           = RuleChain.outerRule(proxyPortProber).around(
            mitmdump);
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

        driverService.start();

        WebDriver webDriver = factory.createWebDriver(driverService);
        String    url       = "http://localhost:" + httpServer.getAddress().getPort() + "/webdriverTest";
        webDriver.get(url);
        String currentUrl = webDriver.getCurrentUrl();

        assertThat(currentUrl).isEqualTo(url);

        driverService.stop();
    }

    @Test
    public void testUsingCustomUserprofile() throws IOException {

        ChromeDriverConfiguration  driverConfiguration = new ChromeDriverConfiguration();
        ChromeDriverServiceFactory factory             = new ChromeDriverServiceFactory(driverConfiguration);

        driverConfiguration.setUserDir(temporaryFolder.newFolder().getAbsolutePath());

        driverService = factory.createService();
        driverService.start();

        String url      = "chrome://version";
        By     selector = By.id("profile_path");

        WebDriver driver1 = factory.createWebDriver(driverService);
        driver1.get(url);
        String profilePath1 = driver1.findElement(selector).getText();
        driverService.stop();

        driverService.start();
        WebDriver driver2 = factory.createWebDriver(driverService);
        driver2.get(url);
        String profilePath2 = driver2.findElement(selector).getText();

        assertThat(profilePath1).isEqualTo(profilePath2);
    }

    @Test
    public void testProxyHTTP() throws IOException {
        String[] arguments = factory.createServiceArgumentsBuilder()
                                    .httpProxyServer("http://localhost:" + proxyPortProber.getPort())
                                    .build();
        driverService = factory.createService(arguments);
        driverService.start();

        WebDriver webDriver = factory.createWebDriver(driverService);
        webDriver.get("http://www.lgohlke.de");
        assertThat(webDriver.getPageSource().length()).isBetween(24000, 26000);
    }

    @Test
    public void testProxyHTTPS() throws IOException {
        String[] arguments = factory.createServiceArgumentsBuilder()
                                    .httpProxyServer("http://localhost:" + proxyPortProber.getPort())
                                    .build();
        driverService = factory.createService(arguments);
        driverService.start();

        WebDriver webDriver = factory.createWebDriver(driverService);
        webDriver.get("https://www.google.de");
        assertThat(webDriver.getPageSource().length()).isBetween(110000, 120022);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testServiceArgumentBuilder() {
        factory.createServiceArgumentsBuilder().httpProxyServer("httpx://localhost:8080").build();
    }
}