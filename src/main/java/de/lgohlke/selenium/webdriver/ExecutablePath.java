package de.lgohlke.selenium.webdriver;

import com.google.common.annotations.VisibleForTesting;

import java.io.File;
import java.nio.file.Paths;

public class ExecutablePath {
    public File buildExecutablePath(String basename) {
        String environmentVariable = "DRIVERS_PATH";
        String driversPath         = getEnvironment(environmentVariable);

        if (driversPath == null) {
            throw new IllegalStateException("please set environment variable " + environmentVariable + " to point to the directory where " + basename + " binary is located");
        }

        boolean is64bit;
        if (System.getProperty("os.name").contains("Windows")) {
            is64bit = getEnvironment("ProgramFiles(x86)") != null;
        } else {
            is64bit = System.getProperty("os.arch").contains("64");
        }
        String filename = basename + "-" + System.getProperty("os.name")
                                                 .toLowerCase() + "-" + (is64bit ? "64" : "32") + "bit";
        return Paths.get(driversPath, filename).toFile();
    }

    @VisibleForTesting
    String getEnvironment(String environmentVariable) {
        return System.getenv(environmentVariable);
    }
}
