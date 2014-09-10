go-plugin-groovy
================
## Purpose
The purpose of the plugin is to allow writing tasks in go.cd as Groovy scripts. For instance:
```
shell.run "./gradlew clean #{sourceBuildTasks} -PcurrentVersion=${new Date().format('yyMMddHHmmss')}"
```

## Bindings
### Environment variables
The `env` binding exposes environment variables passed to the task by go.cd. The following example illustrates accessing 'DEPLOYMENT_HOST' variable:
```
shell.run "ssh deployuser@${env.DEPLOYMENT_HOST} 'git reset --hard origin/master && git clean -f && git pull'"
```
### Running shell commands
There are two bindings to run shell commands:
- `shell.run`
- `shell.runNicely`

The first option will fail the task script if the command exits with a code other than 0. The second option does not fail the script implicitly.

Both options return the command exit code. This makes `shell.run` a short-hand for conveniently running commands whereas `shell.runNicely` allows more control over exit code handling.

Both `run` and `runNicely` accept either one or two parameters, the first one being the command to run. The single-parameter option will redirect shell command output to go.cd console; the double-parameter option treats the second parameter as a closure to call with each line of command output.

Finally, a call to `fail` will terminate the script:
```
if (0 != shell.runNicely('the command'))
  fail 'Unexpected command output' // fail() can also be called without the message
```

### Working directory
Shell commands as described above are always executed in the pipeline working directory. When explicit access to working directory is needed, `workingDir` can be used:
```
println(new File(workingDir, 'file-name.txt')).text
```
