package com.ofg.goplugins.groovy

import com.thoughtworks.go.plugin.api.task.JobConsoleLogger
import groovy.transform.CompileStatic
import org.apache.commons.lang.SystemUtils

@CompileStatic
class ShellExecutor {
    private static final int STATUS_SUCCESS = 0

    private final String workingDir
    private final Map<String, String> environment
    private final JobConsoleLogger logger = JobConsoleLogger.consoleLogger

    ShellExecutor(String workingDir, Map<String, String> environment) {
        this.workingDir = workingDir
        this.environment = environment
    }

    int run(String command) {
        run(command) { String out ->
            logger.printLine out
        }
    }

    int run(String command, Closure closure) {
        int status = runNicely(command, closure)
        if (status != STATUS_SUCCESS) {
            TerminationUtil.fail "The command returned exit code ${status}"
        }

        return status
    }

    int runNicely(String command) {
        runNicely(command) { String out ->
            logger.printLine out
        }
    }

    int runNicely(String command, Closure closure) {
        logger.printLine "[shell] ${command}"

        ProcessBuilder builder = new ProcessBuilder()
                .directory(new File(workingDir))
                .redirectErrorStream(true)
                .command(getCommandLine(command))
        builder.environment() << environment

        Process process = builder.start()
        process.inputStream.eachLine {
            closure.call it
        }

        return process.waitFor()
    }

    private static String[] getCommandLine(String command) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return ['cmd', '/C', command] as String[]
        }
        if (SystemUtils.IS_OS_UNIX) {
            return ['/bin/sh', '-c', command] as String[]
        }
        return command.split(' ')
    }
}
