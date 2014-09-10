package com.ofg.goplugins.groovy;

public class TerminationUtil {

    public static void fail() {
        throw new ScriptFailedException();
    }

    public static void fail(String message) {
        throw new ScriptFailedException(message);
    }

    public static boolean failedFromWithin(Throwable e) {
        if (e.getCause() == null) {
            return false;
        }
        if (e.getCause() instanceof ScriptFailedException) {
            for (StackTraceElement element : e.getCause().getStackTrace()) {
                if (element.getClassName().equals(TerminationUtil.class.getName())) {
                    return true;
                }
            }
        }
        return failedFromWithin(e.getCause());
    }
}
