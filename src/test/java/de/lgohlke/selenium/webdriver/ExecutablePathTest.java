package de.lgohlke.selenium.webdriver;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class ExecutablePathTest {
    @Test
    public void test() {
        ImmutableMap<String, String> environment = ImmutableMap.of("DRIVERS_PATH", "/tmp");
        ExecutablePathMock           executablePath = new ExecutablePathMock(environment);

        System.setProperty("os.name","Linux");
        System.setProperty("os.arch","x64");

        assertThat(executablePath.buildExecutablePath("p")).isEqualTo(new File("/tmp/p-linux-64bit"));
    }

    @RequiredArgsConstructor
    private static class ExecutablePathMock extends ExecutablePath {
        private final Map<String, String> environment;

        @Override
        String getEnvironment(String environmentVariable) {
            return environment.get(environmentVariable);
        }
    }
}