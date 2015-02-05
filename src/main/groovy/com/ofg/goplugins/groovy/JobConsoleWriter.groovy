package com.ofg.goplugins.groovy

import com.thoughtworks.go.plugin.api.task.JobConsoleLogger
import groovy.transform.CompileStatic

@CompileStatic
class JobConsoleWriter extends Writer {

    private JobConsoleLogger logger = JobConsoleLogger.consoleLogger

    @Override
    void write(char[] cbuf, int off, int len) throws IOException {
        String s = new String(cbuf, off, len).replaceAll(System.getProperty("line.separator"), "")
        if (!s.isAllWhitespace()) {
            logger.printLine s
        }
    }

    @Override
    void flush() throws IOException {
    }

    @Override
    void close() throws IOException {
    }
}
