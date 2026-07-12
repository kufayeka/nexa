package nexa.framework.runtime.script.nexa;

import nexa.framework.runtime.compile.ValidationException;
import nexa.framework.runtime.message.RuntimeMessage;
import nexa.framework.runtime.script.CompiledScript;
import nexa.framework.runtime.script.DefaultScriptExecutionResult;
import nexa.framework.runtime.script.NexaScriptCompiler;
import nexa.framework.runtime.script.ScriptExecutionControl;
import nexa.framework.runtime.script.ScriptExecutionResult;
import nexa.framework.runtime.script.ScriptRuntimeContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NexaCompiledScript implements CompiledScript {

    private final String sourceName;
    private final String scriptSource;
    private final NexaProgram program;

    public NexaCompiledScript(String sourceName, String scriptSource, NexaProgram program) {
        this.sourceName = sourceName;
        this.scriptSource = scriptSource;
        this.program = program;
    }

    @Override
    public ScriptExecutionResult execute(RuntimeMessage inputMessage, ScriptRuntimeContext runtimeContext) {
        Map<String, List<RuntimeMessage>> emittedByPort = new LinkedHashMap<>();
        ScriptExecutionControl control = new ScriptExecutionControl((port, message) ->
                emittedByPort.computeIfAbsent(port, ignored -> new java.util.ArrayList<>()).add(message));
        NexaRuntime runtime = new NexaRuntime(inputMessage, control);
        try {
            runtime.executeStatements(program.statements());
            return DefaultScriptExecutionResult.of(emittedByPort);
        } catch (NexaRuntime.ReturnSignal ignored) {
            return DefaultScriptExecutionResult.of(emittedByPort);
        } catch (NexaScriptException exception) {
            throw NexaScriptCompiler.runtimeError(sourceName, scriptSource, exception);
        } catch (ValidationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw NexaScriptCompiler.runtimeError(
                    sourceName,
                    scriptSource,
                    new NexaScriptException(exception.getMessage(), 1, 1));
        }
    }
}
