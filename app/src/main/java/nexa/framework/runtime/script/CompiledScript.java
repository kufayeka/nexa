package nexa.framework.runtime.script;

import nexa.framework.runtime.message.RuntimeMessage;

public interface CompiledScript {

    ScriptExecutionResult execute(RuntimeMessage inputMessage, ScriptRuntimeContext runtimeContext);

    default void dispose() {
    }
}
