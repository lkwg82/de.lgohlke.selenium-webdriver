package de.lgohlke.selenium.webdriver;

import org.openqa.selenium.WebDriverException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface SwallowWebdriverException {
    Class<? extends WebDriverException>[] value() default {};
}
