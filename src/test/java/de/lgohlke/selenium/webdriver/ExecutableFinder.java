package de.lgohlke.selenium.webdriver;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Field;

/**
 * extends search path
 */
@Slf4j
public class ExecutableFinder extends org.openqa.selenium.os.ExecutableFinder {

    public void addPath(String path) {
        try {
            Field field = org.openqa.selenium.os.ExecutableFinder.class.getDeclaredField("pathSegmentBuilder");
            field.setAccessible(true);
            ImmutableSet.Builder<String> pathSegmentsBuilder = (ImmutableSet.Builder<String>) field.get(this);
            try {
                _addPath(path, pathSegmentsBuilder);
            } finally {
                field.setAccessible(false);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    private void _addPath(String path, ImmutableSet.Builder<String> pathSegmentsBuilder) {
        if (pathSegmentsBuilder.build()
                               .contains(path)) {
            log.info("path '{}' already in seaerch list", path);
        } else {
            File newPath = new File(path);
            if (newPath.isDirectory()) {
                pathSegmentsBuilder.add(path);
            } else {
                log.warn("path '{}' is not a directory", path);
            }
        }
    }
}
