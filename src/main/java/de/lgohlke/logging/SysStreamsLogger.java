package de.lgohlke.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.concurrent.locks.ReentrantLock;

/**
 * based on http://stackoverflow.com/questions/11187461/redirect-system-out-and-system-err-to-slf4j
 */
public final class SysStreamsLogger {
    private final static Logger SYS_OUT_LOGGER = LoggerFactory.getLogger("SYSOUT");
    private final static Logger SYS_ERR_LOGGER = LoggerFactory.getLogger("SYSERR");
    private static final PrintStream SYSOUT = System.out;
    private static final PrintStream SYSERR = System.err;
    private final static ReentrantLock LOCK = new ReentrantLock();

    private SysStreamsLogger() {
        // ok
    }

    public static void bindSystemStreams(LogLevelFilter... filters) {

        LOCK.lock();
        try {
            // Enable autoflush
            LoggingOutputStream out = new LoggingOutputStream(SYS_OUT_LOGGER);
            LoggingOutputStream err = new LoggingOutputStream(SYS_ERR_LOGGER);

            err.addFilterOnTop(LogLevelFilterFactory.createAll(LogLevel.ERROR, LogLevelFilter.USE.SYSERR));
            out.addFilterOnTop(LogLevelFilterFactory.createAll(LogLevel.INFO, LogLevelFilter.USE.SYSOUT));

            for (LogLevelFilter filter : filters) {
                LogLevelFilter.USE use = filter.useFor();
                switch (use) {
                    case SYSERR:
                        err.addFilterOnTop(filter);
                        break;
                    case SYSOUT:
                        out.addFilterOnTop(filter);
                        break;
                    case BOTH:
                        err.addFilterOnTop(filter);
                        out.addFilterOnTop(filter);
                        break;
                    default:
                        throw new IllegalStateException("there has been a new unknown type:" + use);
                }
            }

            System.setOut(new PrintStream(out, true));
            System.setErr(new PrintStream(err, true));
        } finally {
            LOCK.unlock();
        }
    }

    public static void unbindSystemStreams() {
        LOCK.lock();
        try {
            System.setOut(SYSOUT);
            System.setErr(SYSERR);
        } finally {
            LOCK.unlock();
        }
    }


}