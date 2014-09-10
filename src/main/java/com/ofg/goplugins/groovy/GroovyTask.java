package com.ofg.goplugins.groovy;

import org.apache.commons.io.IOUtils;

import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.thoughtworks.go.plugin.api.task.Task;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskExecutor;
import com.thoughtworks.go.plugin.api.task.TaskView;

@Extension
public class GroovyTask implements Task {

    public static final String SCRIPT_PROPERTY = "Script";

    @Override
    public TaskConfig config() {
        TaskConfig config = new TaskConfig();
        config.addProperty(SCRIPT_PROPERTY);
        return config;
    }

    @Override
    public TaskExecutor executor() {
        return new GroovyTaskExecutor();
    }

    @Override
    public TaskView view() {
        TaskView taskView = new TaskView() {
            @Override
            public String displayValue() {
                return "Groovy";
            }

            @Override
            public String template() {
                try {
                    return IOUtils.toString(getClass().getResourceAsStream("/task.template.html"), "UTF-8");
                } catch (Exception e) {
                    return "Failed to find template: " + e.getMessage();
                }
            }
        };
        return taskView;
    }

    @Override
    public ValidationResult validate(TaskConfig configuration) {
        ValidationResult validationResult = new ValidationResult();
        if (configuration.getValue(SCRIPT_PROPERTY) == null || configuration.getValue(SCRIPT_PROPERTY).trim().isEmpty()) {
            validationResult.addError(new ValidationError(SCRIPT_PROPERTY, "Script cannot be empty"));
        }
        return validationResult;
    }
}
