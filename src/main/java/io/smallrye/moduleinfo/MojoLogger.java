package io.smallrye.moduleinfo;

import org.apache.maven.plugin.logging.Log;

/**
 *
 */
class MojoLogger implements Logger {
    private final Log log;

    public MojoLogger(final Log log) {
        this.log = log;
    }

    public void error(final Throwable cause, final String format, final Object... args) {
        log.error(String.format(format, args), cause);
    }

    public void warn(final Throwable cause, final String format, final Object... args) {
        log.warn(String.format(format, args), cause);
    }

    public void info(final Throwable cause, final String format, final Object... args) {
        log.info(String.format(format, args), cause);
    }

    public void debug(final Throwable cause, final String format, final Object... args) {
        log.debug(String.format(format, args), cause);
    }
}
