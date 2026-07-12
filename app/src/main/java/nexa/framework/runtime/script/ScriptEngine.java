package nexa.framework.runtime.script;

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
