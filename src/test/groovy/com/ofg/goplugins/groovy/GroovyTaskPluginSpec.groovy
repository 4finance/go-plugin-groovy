package com.ofg.goplugins.groovy

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest
import com.thoughtworks.go.plugin.api.task.Console
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger
import com.thoughtworks.go.plugin.api.task.TaskExecutionContext
import groovy.json.JsonBuilder
import spock.lang.Specification

class GroovyTaskPluginSpec extends Specification {

    private GroovyTaskPlugin subject = new GroovyTaskPlugin()
    private List<String> console

    def setup() {
        Console console = Stub()
        console.printLine(_) >> { String message -> this.console << message }

        TaskExecutionContext context = Stub()
        context.console() >> console

        new JobConsoleLogger() {{
            JobConsoleLogger.context = context
        }}
    }

    def "inline script"() {
        when:
            runScript "println 'test'"
        then:
            output == 'test'
    }

    def "system command succeeds"() {
        when:
            runScript "shell.run 'echo test'"
        then:
            output == '''[shell] echo test
                        |test'''
                    .stripMargin()
    }

    def "system command fails"() {
        when:
            runScript "shell.run 'testSystemCommandFails'"
        then:
            output.contains('[shell] testSystemCommandFails')
            output.contains('com.ofg.goplugins.groovy.ScriptFailedException')

        when:
            runScript "shell.runNicely 'testSystemCommandFails'"
        then:
            output.contains('[shell] testSystemCommandFails')
            !output.contains('com.ofg.goplugins.groovy.ScriptFailedException')
    }

    def "throw exception"() {
        when:
            runScript "throw new NullPointerException()"
        then:
            output.contains('java.lang.NullPointerException')
            output.contains('Caused by: java.lang.NullPointerException')
    }

    def "script fails"() {
        when:
            runScript "fail 'testFailScript'"
        then:
            output.contains('com.ofg.goplugins.groovy.ScriptFailedException: testFailScript')
    }

    def "environment params"() {
        given:
            def params = [param1: 'value1']
        when:
            runScript 'println "${env.param1}"', params
        then:
            output == 'value1'
    }

    private void runScript(String script) {
        runScript script, [:]
    }

    private void runScript(String script, def environment) {
        JsonBuilder json = new JsonBuilder()
        json {
            config {
                Script {
                    value script
                }
            }
            context {
                environmentVariables environment
                workingDirectory System.getProperty('user.dir')
            }
        }

        String body = json.toString()
        GoPluginApiRequest request = Stub()
        request.requestBody() >> body

        console = []
        subject.handleExecution request
    }

    private String getOutput() {
        return console.join('\n')
    }
}
