package de.lgohlke.logging;

public interface LogLevelFilter {
    enum USE{
        SYSERR,SYSOUT,BOTH
    }

    boolean apply(String message);
    LogLevel level();
    USE useFor();
}
