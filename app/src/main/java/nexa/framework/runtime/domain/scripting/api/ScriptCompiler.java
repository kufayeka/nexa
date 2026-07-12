package nexa.framework.runtime.domain.scripting.api;

public interface ScriptCompiler {

    CompiledScript compile(String scriptSource, String sourceName);

    default void clearWorkspace(String workspaceId) {
    }

    default void dispose() {
    }
}
