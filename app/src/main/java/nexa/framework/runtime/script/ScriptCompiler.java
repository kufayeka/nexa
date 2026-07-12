package nexa.framework.runtime.script;

public interface ScriptCompiler {

    CompiledScript compile(String scriptSource, String sourceName);

    default void clearWorkspace(String workspaceId) {
    }

    default void dispose() {
    }
}
