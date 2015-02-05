package com.ofg.goplugins.groovy

import groovy.transform.CompileStatic

@CompileStatic
class ScriptFailedException extends RuntimeException {

    ScriptFailedException() {
    }

    ScriptFailedException(String message) {
        super(message)
    }
}
