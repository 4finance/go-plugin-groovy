package com.ofg.goplugins.groovy;

import org.apache.commons.lang.SystemUtils;
import org.codehaus.groovy.runtime.MethodClosure;

import com.thoughtworks.go.plugin.api.task.TaskExecutionContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import groovy.lang.Closure;

public class ShellExecutor {

    private static final int STATUS_SUCCESS = 0;

    private TaskExecutionContext context;

    public ShellExecutor(TaskExecutionContext context) {
        this.context = context;
    }

    public int run(String command) throws Exception {
        return run(command, consoleWriter());
    }

    public int run(String command, Closure closure) throws Exception {
        int status = runNicely(command, closure);
        if (status != STATUS_SUCCESS) {
            TerminationUtil.fail("The command returned exit code " + status);
        }

        return status;
    }

    public int runNicely(String command) throws Exception {
        return runNicely(command, consoleWriter());
    }

    public int runNicely(String command, Closure closure) throws Exception {
        context.console().printLine("[shell] " + command);

        ProcessBuilder builder = new ProcessBuilder()
                .directory(new File(context.workingDir()))
                .redirectErrorStream(true)
                .command(getCommandLine(command));
        builder.environment().putAll(context.environment().asMap());
        Process process = builder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        while (line != null) {
            closure.call(line);
            line = reader.readLine();
        }

        return process.waitFor();
    }

    private Closure consoleWriter() {
        return new MethodClosure(this, "writeToConsole");
    }

    private void writeToConsole(String line) {
        context.console().printLine(line);
    }

    private String[] getCommandLine(String command) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return new String[]{ "cmd", "/C", command };
        }
        if (SystemUtils.IS_OS_UNIX) {
            return new String[]{ "/bin/sh", "-c", command };
        }
        return command.split(" ");
    }
}
