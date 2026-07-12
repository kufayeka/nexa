package nexa.framework.runtime.domain.scripting.api;

import nexa.framework.runtime.domain.scripting.model.ScriptRuntimeContext;

public interface ScriptEngine {

    String language();

    ScriptCompiler compiler();

    default void clearWorkspace(String workspaceId) {
        compiler().clearWorkspace(workspaceId);
    }

    default void dispose() {
        compiler().dispose();
    }
}


