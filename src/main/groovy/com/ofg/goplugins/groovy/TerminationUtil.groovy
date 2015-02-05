package com.ofg.goplugins.groovy

import groovy.transform.CompileStatic

@CompileStatic
class TerminationUtil {
    static void fail() {
        throw new ScriptFailedException()
    }

    static void fail(String message) {
        throw new ScriptFailedException(message)
    }

    static boolean failedFromWithin(Throwable e) {
        if (!e.cause) {
            return false
        }
        if (e.cause instanceof ScriptFailedException && isThisWithinStacktrace(e)) {
            return true
        }
        return failedFromWithin(e.cause)
    }

    private static boolean isThisWithinStacktrace(Throwable throwable) {
        throwable.cause.stackTrace.any { StackTraceElement e ->
            e.className == TerminationUtil.class.name
        }
    }
}
