package de.lgohlke.selenium.webdriver.phantomjs;

import com.sun.net.httpserver.HttpServer;
import de.lgohlke.junit.FreeportProber;
import de.lgohlke.selenium.webdriver.DriverArgumentsBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;


public class PhantomJSDriverServiceFactoryIT {
    @Rule
    public FreeportProber proxyPortProber = new FreeportProber();

    private PhantomJSDriverServiceFactory factory = new PhantomJSDriverServiceFactory(new PhantomJSDriverConfiguration());
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
        PhantomJSDriverService driverService = factory.createService();

        driverService.start();
        try {
            WebDriver webDriver = factory.createWebDriver(driverService);
            String url = "http://localhost:" + httpServer.getAddress().getPort() + "/webdriverTest";
            webDriver.get(url);
            String currentUrl = webDriver.getCurrentUrl();

            assertThat(currentUrl).isEqualTo(url);
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

        PhantomJSDriverService service = factory.createService(arguments);
        service.start();
        try {
            WebDriver webDriver = factory.createWebDriver(service);
            webDriver.get("http://www.lgohlke.de");
            assertThat(webDriver.getPageSource().length()).isBetween(24000, 26000);
        } finally {
            service.stop();
        }
    }
    @Ignore
    @Test
    public void testProxyHTTPS() throws IOException {
        String[] arguments = factory.createServiceArgumentsBuilder()
                                    .httpProxyServer("http://localhost:" + proxyPortProber.getPort())
                                    .build();

        PhantomJSDriverService service = factory.createService(arguments);
        service.start();
        try {
            WebDriver webDriver = factory.createWebDriver(service);
            webDriver.get("https://www.google.de");
            assertThat(webDriver.getPageSource().length()).isBetween(100000, 110000);
        } finally {
            service.stop();
        }
    }

    @Test
    public void testServiceArguments() {
        DriverArgumentsBuilder serviceArgumentsBuilder = factory.createServiceArgumentsBuilder();

        String[] strings = serviceArgumentsBuilder.httpProxyServer("http://localhost:8080").build();

        assertThat(strings).contains("--proxy=localhost:8080", "--proxy-type=http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testServiceArgumentBuilder() {
        factory.createServiceArgumentsBuilder().httpProxyServer("httpx://localhost:8080").build();
    }
}