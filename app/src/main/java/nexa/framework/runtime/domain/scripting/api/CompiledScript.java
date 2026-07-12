package nexa.framework.runtime.domain.scripting.api;

import nexa.framework.runtime.domain.scripting.model.ScriptRuntimeContext;

import nexa.framework.runtime.domain.execution.model.RuntimeMessage;

public interface CompiledScript {

    ScriptExecutionResult execute(RuntimeMessage inputMessage, ScriptRuntimeContext runtimeContext);

    default void dispose() {
    }
}


