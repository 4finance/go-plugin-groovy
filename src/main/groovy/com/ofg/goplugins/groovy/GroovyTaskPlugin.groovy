package com.ofg.goplugins.groovy

import com.fasterxml.jackson.databind.ObjectMapper
import com.thoughtworks.go.plugin.api.AbstractGoPlugin
import com.thoughtworks.go.plugin.api.GoPluginIdentifier
import com.thoughtworks.go.plugin.api.annotation.Extension
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger

import groovy.transform.PackageScope
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl
import org.codehaus.groovy.runtime.MethodClosure

import javax.script.Bindings
import javax.script.ScriptContext

@Extension
class GroovyTaskPlugin extends AbstractGoPlugin {

    @Override
    GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier('task', ['1.0'])
    }

    @Override
    GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        switch (request.requestName()) {
            case 'execute':
                return handleExecution(request)
            case 'configuration':
                return handleConfiguration()
            case 'view':
                return handleView()
            case 'validate':
                return handleValidation(request)
            default:
                throw new UnhandledRequestTypeException(request.requestName())
        }
    }

    @PackageScope
    static GoPluginApiResponse handleExecution(GoPluginApiRequest request) {
        try {
            runScript(request)
            return response(success: true, message: 'Groovy script executed successfully')
        } catch (Exception e) {
            if (TerminationUtil.failedFromWithin(e)) {
                logFailureMessageOnly(e)
            } else {
                logFailureFullStacktrace(e)
            }

            return response(success: false, message: 'Failed to execute Groovy script: ' + e.message)
        }
    }

    private static void runScript(GoPluginApiRequest request) {
        def message = new ObjectMapper().readValue(request.requestBody(), Map)

        GroovyScriptEngineImpl engine = new GroovyScriptEngineImpl()
        JobConsoleWriter writer = new JobConsoleWriter()
        engine.context.writer = writer
        engine.context.errorWriter = writer

        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE)
        message.context.with {
            bindings['env'] = environmentVariables
            bindings['workingDir'] = workingDirectory
            bindings['shell'] = new ShellExecutor(workingDirectory, environmentVariables)
            bindings['fail'] = new MethodClosure(new TerminationUtil(), 'fail')
        }

        engine.eval(message.config.Script.value)
    }

    private static void logFailureMessageOnly(Exception e) {
        JobConsoleLogger.consoleLogger.printLine(e.message)
    }

    private static void logFailureFullStacktrace(Exception e) {
        StringWriter out = new StringWriter()
        e.printStackTrace(new PrintWriter(out))
        JobConsoleLogger.consoleLogger.printLine(out.toString())
    }

    private static GoPluginApiResponse handleConfiguration() {
        return response(Script: [required: true])
    }

    private static GoPluginApiResponse handleView() {
        String baseUrl = GroovyTaskPlugin.protectionDomain.codeSource.location.toExternalForm()
        return response(displayValue: 'Groovy',
                        template: new URL(baseUrl + '/task.template.html').text)
    }

    private static GoPluginApiResponse handleValidation(GoPluginApiRequest request) {
        def validation = [:]

        def message = new ObjectMapper().readValue(request.requestBody(), Map)
        if (!message.Script?.value || message.Script.value.allWhitespace) {
            validation['errors'] = [
                    Script: 'Script cannot be empty'
            ]
        }

        return response(validation)
    }

    private static GoPluginApiResponse response(def content) {
        return DefaultGoPluginApiResponse.success(new ObjectMapper().writeValueAsString(content))
    }
}
