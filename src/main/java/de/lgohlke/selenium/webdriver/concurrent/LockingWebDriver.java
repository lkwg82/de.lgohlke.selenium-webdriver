package de.lgohlke.selenium.webdriver.concurrent;

import org.openqa.selenium.WebDriver;

public interface LockingWebDriver extends WebDriver {
    UnlockLockingWebdriver lock();

    void unlock();

    boolean isLocked();
}
