package de.lgohlke.selenium.webdriver;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class ExecutablePathTest {
    @Rule
    public final  EnvironmentVariables environmentVariables = new EnvironmentVariables();
    private final ExecutablePath       executablePath       = new ExecutablePath();

    @Test
    public void test() {
        environmentVariables.set("DRIVERS_PATH", "/tmp");

        System.setProperty("os.name", "Linux");
        System.setProperty("os.arch", "x64");

        assertThat(executablePath.buildExecutablePath("p")).isEqualTo(new File("/tmp/p-linux-64bit"));
    }

    @Test(expected = IllegalStateException.class)
    public void emptyDriversPathShouldRaiseException() {
        environmentVariables.set("DRIVERS_PATH", "");

        executablePath.buildExecutablePath("p");
    }
}