package de.lgohlke.selenium.webdriver.phantomjs;

import de.lgohlke.selenium.webdriver.DriverArgumentsBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Setter
@Accessors(fluent = true)
@Slf4j
public class PhantomJSDriverServiceArgumentsBuilder implements DriverArgumentsBuilder {
    private static final Pattern PROXY_SERVER = Pattern.compile("https?://[^:]*:\\d+?");

    private String  httpProxyServer;
    private boolean debug;

    public String[] build() {
        List<String> arguments = new ArrayList<>();

        if (httpProxyServer != null) {
            if (PROXY_SERVER.matcher(httpProxyServer).matches()) {
                URL url;
                try {
                    url = new URL(httpProxyServer);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(e);
                }
                arguments.add("--proxy=" + url.getHost() + ":" + url.getPort());
                arguments.add("--proxy-type=" + url.getProtocol().toLowerCase());
            } else {
                throw new IllegalArgumentException("httpProxyServer must match https?://...  with ':<port>'");
            }
        }

        if (log.isDebugEnabled()) {
            debug = true;
        }

        if (debug) {
            arguments.add("--debug=true");
        } else {
            arguments.add("--debug=false");
        }

        return arguments.toArray(new String[arguments.size()]);
    }
}
