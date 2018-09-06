/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.moduleinfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicReference;

/**
 */
public interface Logger {
    Logger STDOUT = new Logger() {
        public void error(final Throwable cause, final String format, final Object... args) {
            log(cause, format, "ERROR", args);
        }

        public void warn(final Throwable cause, final String format, final Object... args) {
            log(cause, format, "WARN", args);
        }

        public void info(final Throwable cause, final String format, final Object... args) {
            log(cause, format, "INFO", args);
        }

        public void debug(final Throwable cause, final String format, final Object... args) {
            log(cause, format, "DEBUG", args);
        }

        private void log(final Throwable cause, final String format, final String level, final Object[] args) {
            final StringWriter stringWriter = new StringWriter(cause == null ? 128 : 512);
            stringWriter.append("[").append(level).append("] ").append(String.format(format, args));
            if (cause != null) {
                stringWriter.append(": ");
                cause.printStackTrace(new PrintWriter(stringWriter));
            }
            System.out.println(stringWriter.toString());
        }
    };

    default void error(String format, Object... args) {
        error(null, format, args);
    }

    void error(Throwable cause, String format, Object... args);

    default void warn(String format, Object... args) {
        warn(null, format, args);
    }

    void warn(Throwable cause, String format, Object... args);

    default void info(String format, Object... args) {
        info(null, format, args);
    }

    void info(Throwable cause, String format, Object... args);

    default void debug(String format, Object... args) {
        debug(null, format, args);
    }

    void debug(Throwable cause, String format, Object... args);

    AtomicReference<Logger> loggerRef = new AtomicReference<>(STDOUT);

    static void setLogger(Logger logger) {
        loggerRef.set(logger);
    }

    static Logger getLogger() {
        return loggerRef.get();
    }
}
