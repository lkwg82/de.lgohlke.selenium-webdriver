package de.lgohlke.logging;

import lombok.RequiredArgsConstructor;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SysStreamsLoggerTest {

    @RequiredArgsConstructor
    private static class WrappingLogLevelFilter implements LogLevelFilter {
        private final LogLevelFilter filter;
        private boolean applied;

        @Override
        public boolean apply(String message) {
            boolean apply = filter.apply(message);
            applied = apply;
            return apply;
        }

        @Override
        public LogLevel level() {
            return filter.level();
        }

        @Override
        public USE useFor() {
            return USE.BOTH;
        }
    }

    private LogLevelFilter debugFilter = new LogLevelFilter() {

        @Override
        public boolean apply(String message) {
            return message.contains("DEBUG");
        }

        @Override
        public LogLevel level() {
            return LogLevel.DEBUG;
        }

        @Override
        public USE useFor() {
            return USE.BOTH;
        }
    };

    @Test
    public void shouldMatch() {

        WrappingLogLevelFilter wrappingLogLevelFilter = new WrappingLogLevelFilter(debugFilter);

        SysStreamsLogger.bindSystemStreams(wrappingLogLevelFilter);
        try {
            System.out.println("DEBUG xx");
            assertThat(wrappingLogLevelFilter.applied).isTrue();
        } finally {
            SysStreamsLogger.unbindSystemStreams();
        }
    }

    @Test
    public void shouldNotMatch() {

        WrappingLogLevelFilter wrappingLogLevelFilter = new WrappingLogLevelFilter(debugFilter);

        SysStreamsLogger.bindSystemStreams(wrappingLogLevelFilter);
        try {
            System.out.println("WARN xx");
            assertThat(wrappingLogLevelFilter.applied).isFalse();
        } finally {
            SysStreamsLogger.unbindSystemStreams();
        }
    }

}