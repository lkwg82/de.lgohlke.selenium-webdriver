package de.lgohlke.logging;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class LoggingOutputStream extends OutputStream {
    private LogLevelFilter[] filters = {};
    private final Logger log;

    /**
     * Used to maintain the contract of {@link #close()}.
     */
    private boolean hasBeenClosed = false;

    /**
     * The internal buffer where data is stored.
     */
    private byte[]     buf = new byte[DEFAULT_BUFFER_LENGTH];

    /**
     * The number of valid bytes in the buffer. This value is always in the
     * range <tt>0</tt> through <tt>buf.length</tt>; elements
     * <tt>buf[0]</tt> through <tt>buf[count-1]</tt> contain valid byte
     * data.
     */
    protected int count=0;

    private static final int DEFAULT_BUFFER_LENGTH = 2048;

    /**
     * Remembers the size of the buffer for speed.
     */
    private int bufLength = DEFAULT_BUFFER_LENGTH;

    private LoggingOutputStream() {
        log = null;
        // illegal
    }

    public LoggingOutputStream(Logger log){
        Preconditions.checkNotNull(log);
        this.log = log;
    }

    /**
     * Closes this output stream and releases any logging resources
     * associated with this stream. The general contract of
     * <code>close</code> is that it closes the output stream. A closed
     * stream cannot perform output operations and cannot be reopened.
     */
    @Override
    public void close() {
        flush();
        hasBeenClosed = true;
    }

    /**
     * Writes the specified byte to this output stream. The general contract
     * for <code>write</code> is that one byte is written to the output
     * stream. The byte to be written is the eight low-order bits of the
     * argument <code>b</code>. The 24 high-order bits of <code>b</code> are
     * ignored.
     *
     * @param b the <code>byte</code> to write
     */
    @Override
    public void write(int b) throws IOException {
        if (hasBeenClosed) {
            throw new IOException("The stream has been closed.");
        }

        // don't log nulls
        if (b == 0) {
            return;
        }

        // would this be writing past the buffer?
        if (count == bufLength) {
            // grow the buffer
            int newBufLength = bufLength + DEFAULT_BUFFER_LENGTH;
            byte[] newBuf = new byte[newBufLength];

            System.arraycopy(buf, 0, newBuf, 0, bufLength);

            buf = newBuf;
            bufLength = newBufLength;
        }

        buf[count] = (byte) b;
        count++;
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be
     * written out. The general contract of <code>flush</code> is that
     * calling it is an indication that, if any bytes previously written
     * have been buffered by the implementation of the output stream, such
     * bytes should immediately be written to their intended destination.
     */
    @Override
    public void flush() {

        if (count == 0) {
            return;
        }

        byte[] theBytes = new byte[count];
        System.arraycopy(buf, 0, theBytes, 0, count);
        logMessageAccordingToLogLevelFilter(theBytes);
        reset();
    }

    private void reset() {
        // not resetting the buffer -- assuming that if it grew that it
        // will likely grow similarly again
        count = 0;
    }

    private void logMessageAccordingToLogLevelFilter(byte[] theBytes) {
        String message = new String(theBytes).trim();

        if (message.isEmpty()) {
            return;
        }
        boolean appliedFilter = false;
        for (LogLevelFilter filter : filters) {
            if (!appliedFilter && filter.apply(message)) {

                logWithLevel(message, filter.level());
                appliedFilter = true;
            }
        }
        if (!appliedFilter) {
            throw new IllegalStateException("no default filter set");
        }
    }

    private void logWithLevel(String message, LogLevel level) {
        if (LogLevel.FATAL.equals(level)) {
            log.error(message);
        } else if (LogLevel.ERROR.equals(level)) {
            log.error(message);
        } else if (LogLevel.WARN.equals(level)) {
            log.warn(message);
        } else if (LogLevel.INFO.equals(level)) {
            log.info(message);
        } else if (LogLevel.DEBUG.equals(level)) {
            log.debug(message);
        } else {
            log.warn(message);
        }
    }

    public void addFilterOnTop(LogLevelFilter filter) {
        List<LogLevelFilter> logLevelFilters = new ArrayList<>(Arrays.asList(filters));
        logLevelFilters.add(0, filter);
        filters = logLevelFilters.toArray(new LogLevelFilter[logLevelFilters.size()+1]);
    }
}
