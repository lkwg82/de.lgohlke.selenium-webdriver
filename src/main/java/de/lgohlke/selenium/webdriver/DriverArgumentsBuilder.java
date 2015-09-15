package de.lgohlke.selenium.webdriver;

public interface DriverArgumentsBuilder {
    DriverArgumentsBuilder httpProxyServer(String httpProxy);
    String[] build();
}
