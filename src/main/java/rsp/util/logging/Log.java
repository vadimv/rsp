package rsp.util.logging;

import rsp.util.ExceptionsUtils;

import java.util.function.Consumer;

public interface Log {

    void log(String message);
    void log(String message, Throwable ex);

    enum Level {
        TRACE, DEBUG, INFO, WARN, ERROR, OFF
    }

    interface Format {
        String format(Level level, String message);
        String format(Level level, String message, Throwable cause);
    }

    interface Reporting {
        void trace(Consumer<Log> logConsumer);
        void debug(Consumer<Log> logConsumer);
        void info(Consumer<Log> logConsumer);
        void warn(Consumer<Log> logConsumer);
        void error(Consumer<Log> logConsumer);
    }

    class SimpleFormat implements Format {

        @Override
        public String format(Level level, String message) {
            return "[" + level.name() + "] " + message;
        }

        @Override
        public String format(Level level, String message, Throwable ex) {
            return "[" + level.name() + "] " + message + "\n"
                    + "Exception: " + ex.getMessage() + "\n"
                    + ExceptionsUtils.stackTraceToString(ex);
        }
    }

    /**
     * A console logger.
     */
    class Default implements Reporting {
        private final Level level;
        private final Log traceLog;
        private final Log debugLog;
        private final Log infoLog;
        private final Log warnLog;
        private final Log errorLog;

        public Default(Level level, Format format, Consumer<String> out) {
            this.level = level;
            this.traceLog = new LogImpl(Level.TRACE, format, out);
            this.debugLog = new LogImpl(Level.DEBUG, format, out);
            this.infoLog = new LogImpl(Level.INFO, format, out);
            this.warnLog = new LogImpl(Level.WARN, format, out);
            this.errorLog = new LogImpl(Level.ERROR, format, out);
        }

        @Override
        public void trace(Consumer<Log> logConsumer) {
            if (level == Level.TRACE) logConsumer.accept(traceLog);
        }

        @Override
        public void debug(Consumer<Log> logConsumer) {
            if (level == Level.TRACE || level == Level.DEBUG) logConsumer.accept(debugLog);
        }

        @Override
        public void info(Consumer<Log> logConsumer) {
            if (level == Level.TRACE
                || level == Level.DEBUG
                || level == Level.INFO) logConsumer.accept(infoLog);
        }

        @Override
        public void warn(Consumer<Log> logConsumer) {
            if (level == Level.TRACE
                || level == Level.DEBUG
                || level == Level.INFO
                || level == Level.WARN) logConsumer.accept(warnLog);
        }

        @Override
        public void error(Consumer<Log> logConsumer) {
            if (level == Level.TRACE
                || level == Level.DEBUG
                || level == Level.INFO
                || level == Level.WARN
                || level == Level.ERROR) logConsumer.accept(errorLog);
        }

        private static class LogImpl implements Log {
            private final Level level;
            private final Format format;
            private final Consumer<String> out;

            private LogImpl(Level level, Format format, Consumer<String> out) {
                this.level = level;
                this.format = format;
                this.out = out;
            }

            @Override
            public void log(String message) {
                out.accept(format.format(level, message));
            }

            @Override
            public void log(String message, Throwable cause) {
                out.accept(format.format(level, message, cause));
            }
        }
    }
}
