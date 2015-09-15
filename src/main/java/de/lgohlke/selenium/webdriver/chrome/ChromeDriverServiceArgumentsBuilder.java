package de.lgohlke.selenium.webdriver.chrome;

import de.lgohlke.selenium.webdriver.DriverArgumentsBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Setter
@Accessors(fluent = true)
@Slf4j
public class ChromeDriverServiceArgumentsBuilder implements DriverArgumentsBuilder {
    private static final Pattern PROXY_SERVER = Pattern.compile("https?://[^:]*(:\\d+)?");

    private String httpProxyServer;

    public String[] build() {
        List<String> arguments = new ArrayList<>();

        if (httpProxyServer != null) {
            if (PROXY_SERVER.matcher(httpProxyServer).matches()) {
                arguments.add("HTTP_PROXY");
                arguments.add(httpProxyServer);
                arguments.add("HTTPS_PROXY");
                arguments.add(httpProxyServer);
            } else {
                throw new IllegalArgumentException("httpProxyServer must match https?://... with ':<port>'");
            }
        }

        if (!arguments.isEmpty()) {
            log.info("arguments: {}", arguments);
        }
        return arguments.toArray(new String[arguments.size()]);
    }
}
