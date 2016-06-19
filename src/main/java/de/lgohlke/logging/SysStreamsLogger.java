package de.lgohlke.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.concurrent.locks.ReentrantLock;

/**
 * based on http://stackoverflow.com/questions/11187461/redirect-system-out-and-system-err-to-slf4j
 */
@Slf4j
public final class SysStreamsLogger {
    private static final Logger SYS_OUT_LOGGER = LoggerFactory.getLogger("SYSOUT");
    private static final Logger SYS_ERR_LOGGER = LoggerFactory.getLogger("SYSERR");
    private static final PrintStream SYSOUT = System.out;
    private static final PrintStream SYSERR = System.err;
    private static final ReentrantLock LOCK = new ReentrantLock();

    private static LoggingOutputStream out;
    private static LoggingOutputStream err;

    private SysStreamsLogger() {
        // ok
    }

    public static void bindSystemStreams(LogLevelFilter... filters) {

        LOCK.lock();
        try {

            if (out != null) {
                log.warn("tried to rebound");
                return;
            }
            // Enable autoflush
            out = new LoggingOutputStream(SYS_OUT_LOGGER);
            err = new LoggingOutputStream(SYS_ERR_LOGGER);

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

            out.close();
            err.close();
            out = null;
            err = null;
        } finally {
            LOCK.unlock();
        }
    }


}