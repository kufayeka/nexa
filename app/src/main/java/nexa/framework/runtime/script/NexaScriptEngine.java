package nexa.framework.runtime.script;

public final class NexaScriptEngine implements ScriptEngine {

    private final NexaScriptCompiler compiler;

    public NexaScriptEngine() {
        this.compiler = new NexaScriptCompiler();
    }

    @Override
    public String language() {
        return "nexa";
    }

    @Override
    public ScriptCompiler compiler() {
        return compiler;
    }
}
