package de.lgohlke.logging;


import lombok.RequiredArgsConstructor;

public class LogLevelFilterFactory {
    private LogLevelFilterFactory() {
        // ok
    }

    public static LogLevelFilter createAll(LogLevel logLevel, LogLevelFilter.USE useFor) {
        return new InnerLogLevelFilter(logLevel, useFor);
    }

    @RequiredArgsConstructor
    private static class InnerLogLevelFilter implements LogLevelFilter {
        private final LogLevel logLevel;
        private final USE      useFor;

        @Override
        public boolean apply(String message) {
            return true;
        }

        @Override
        public LogLevel level() {
            return logLevel;
        }

        @Override
        public USE useFor() {
            return useFor;
        }
    }
}
