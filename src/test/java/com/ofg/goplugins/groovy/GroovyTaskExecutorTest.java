package com.ofg.goplugins.groovy;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.task.Console;
import com.thoughtworks.go.plugin.api.task.EnvironmentVariables;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskExecutionContext;

public class GroovyTaskExecutorTest {

	private GroovyTaskExecutor testObject = new GroovyTaskExecutor();

	@Test
	public void testInlineScript() {
		Console console = mock(Console.class);
		runScript("println 'test'", console);
		verify(console).printLine(eq("test"));
	}

	@Test
	public void testSystemCommandSucceeds() {
		Console console = mock(Console.class);
		runScript("shell.run 'echo test'", console);
		verify(console).printLine(eq("test"));
	}

	@Test
	public void testSystemCommandFails() {
		Console console = mock(Console.class);

		ExecutionResult resultRun = runScript("shell.run 'testSystemCommandFails'", console);
		assertThat(resultRun.isSuccessful(), is(false));

		ExecutionResult resultRunNicely = runScript("shell.runNicely 'testSystemCommandFails'", console);
		assertThat(resultRunNicely.isSuccessful(), is(true));
	}

	@Test
	public void testException() {
		Console console = mock(Console.class);
		runScript("throw new NullPointerException()", console);
		verify(console).printLine(contains("NullPointerException"));
	}

	@Test
	public void testFailScript() {
		Console console = mock(Console.class);
		runScript("fail 'testFailScript'", console);
		verify(console).printLine(contains("testFailScript"));
	}

	@Test
	public void testEnvironmentParams() {
		Map<String, String> environmentParams = new HashMap<>();
		environmentParams.put("param1", "value1");

		Console console = mock(Console.class);
		runScript("println \"${env.param1}\"", console, environmentParams);
		verify(console).printLine(eq("value1"));
	}

	private ExecutionResult runScript(String script, Console console) {
		return runScript(script, console, Collections.EMPTY_MAP);
	}

	private ExecutionResult runScript(String script, Console console, Map<String, String> environmentParams) {
		TaskConfig config = new TaskConfig();
		config.add(new Property("Script", script, null));

		TaskExecutionContext context = mock(TaskExecutionContext.class);
		when(context.console()).thenReturn(console);

		EnvironmentVariables env = mock(EnvironmentVariables.class);
		when(env.asMap()).thenReturn(environmentParams);

		when(context.environment()).thenReturn(env);
		when(context.workingDir()).thenReturn(System.getProperty("user.dir"));

		return testObject.execute(config, context);
	}
}
