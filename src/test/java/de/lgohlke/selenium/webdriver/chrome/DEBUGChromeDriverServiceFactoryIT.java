package de.lgohlke.selenium.webdriver.chrome;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.net.PortProber;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class DEBUGChromeDriverServiceFactoryIT {
    private final ChromeDriverServiceFactory factory = new ChromeDriverServiceFactory(new ChromeDriverConfiguration());
    private HttpServer httpServer;

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
    }

    @Test
    public void startAndStop() throws IOException {
        ChromeDriverService driverService = factory.createService();

        driverService.start();
        try {
            WebDriver webDriver = factory.createWebDriver(driverService);
            String    url       = "http://localhost:" + httpServer.getAddress().getPort() + "/webdriverTest";
            webDriver.get(url);
            String currentUrl = webDriver.getCurrentUrl();

            assertThat(currentUrl).isEqualTo(url);
        } finally {
            driverService.stop();
        }
    }
}