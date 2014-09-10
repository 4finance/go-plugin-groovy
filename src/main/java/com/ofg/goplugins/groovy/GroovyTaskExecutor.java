package com.ofg.goplugins.groovy;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.codehaus.groovy.runtime.MethodClosure;

import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.task.Console;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskExecutionContext;
import com.thoughtworks.go.plugin.api.task.TaskExecutor;

public class GroovyTaskExecutor implements TaskExecutor {

	@Override
	public ExecutionResult execute(TaskConfig config, TaskExecutionContext taskEnvironment) {
		try {
			return runCommand(taskEnvironment, config);
		}
		catch (Exception e) {
			if (TerminationUtil.failedFromWithin(e)) {
				taskEnvironment.console().printLine(e.getMessage());
			}
			else {
				StringWriter out = new StringWriter();
				e.printStackTrace(new PrintWriter(out));
				taskEnvironment.console().printLine(out.toString());
			}
			return ExecutionResult.failure("Failed to execute Groovy script", e);
		}
	}

	private ExecutionResult runCommand(final TaskExecutionContext taskContext, TaskConfig taskConfig) throws IOException, InterruptedException, URISyntaxException, ScriptException {

		GroovyScriptEngineImpl engine = new GroovyScriptEngineImpl();
		engine.getContext().setWriter(new ConsoleWriter(taskContext.console()));
		engine.getContext().setErrorWriter(new ConsoleWriter(taskContext.console()));

		Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put("shell", new ShellExecutor(taskContext));
		bindings.put("fail", new MethodClosure(new TerminationUtil(), "fail"));
		bindings.put("env", new EnvironmentParamsWrapper(taskContext.environment().asMap()));
		bindings.put("workingDir", taskContext.workingDir());
		engine.eval(taskConfig.getValue(GroovyTask.SCRIPT_PROPERTY));

		return ExecutionResult.success("Groovy script executed successfully");
	}

	public static class ConsoleWriter extends Writer {

		private Console console;

		public ConsoleWriter(Console console) {
			this.console = console;
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			String s = new String(cbuf, off, len).replaceAll(System.getProperty("line.separator"), "");
			if (s.trim().length() > 0) {
				console.printLine(s);
			}
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void close() throws IOException {
		}
	}

}
