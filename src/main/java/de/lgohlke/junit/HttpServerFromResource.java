package de.lgohlke.junit;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.rules.ExternalResource;
import org.openqa.selenium.net.PortProber;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@RequiredArgsConstructor
public class HttpServerFromResource extends ExternalResource {
    private final String     pathToServe;
    private       HttpServer httpServer;

    @Override
    protected void before() throws Throwable {
        httpServer = HttpServerFromResourceInner.start(pathToServe);
    }

    @Override
    protected void after() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    public int getPort() {
        return httpServer.getAddress().getPort();
    }

    public String url(String request) {
        return "http://localhost:" + getPort() + request;
    }

    @Slf4j
    private static class HttpServerFromResourceInner {
        private HttpServerFromResourceInner() {
            // ok
        }

        public static HttpServer start(String pathToServe) throws IOException {
            HttpServer httpServer = HttpServer.create();
            httpServer.createContext("/", createHttpExchange(pathToServe));
            httpServer.bind(new InetSocketAddress("localhost", PortProber.findFreePort()), 0);
            httpServer.start();
            return httpServer;
        }

        private static HttpHandler createHttpExchange(String pathToServe) {
            return httpExchange -> {
                String requestUri = httpExchange.getRequestURI().toString();

                String name = pathToServe + requestUri;
                name = name.replaceFirst("/+", "/");
                URL resource = HttpServerFromResourceInner.class.getResource(name);

                try (OutputStream os = httpExchange.getResponseBody()) {
                    if (resource == null) {
                        log.warn("could not find " + requestUri);
                        httpExchange.sendResponseHeaders(404, 0);
                    } else {
                        byte[] bytes = Files.readAllBytes(Paths.get(resource.getFile()));
                        httpExchange.sendResponseHeaders(200, bytes.length);

                        os.write(bytes);
                    }
                }
            };
        }
    }
}
