package de.lgohlke.selenium.webdriver.concurrent;

@FunctionalInterface
public interface UnlockLockingWebdriver extends AutoCloseable {
    default void unlock() throws Exception {
        close();
    }
}
