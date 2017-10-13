package de.lgohlke.selenium.webdriver;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExecutableFinderTest {

    @Test
    public void shouldFindProgrammInPath_date() {
        ExecutableFinder executableFinder = new ExecutableFinder();
        String           path             = executableFinder.find("date");

        assertThat(path).isEqualTo("/bin/date");
    }

    @Test
    public void shouldFindProgrammInPath_whenAddedNewPath() {
        ExecutableFinder executableFinder = new ExecutableFinder();
        executableFinder.addPath("/usr/lib/chromium-browser");
        String path = executableFinder.find("chromedriver");

        assertThat(path).isEqualTo("/usr/lib/chromium-browser/chromedriver");
    }

    @Test
    public void shouldNotFindProgrammInPath() {
        ExecutableFinder executableFinder = new ExecutableFinder();
        String           path             = executableFinder.find("chromedriver");

        assertThat(path).isNullOrEmpty();
    }

}
