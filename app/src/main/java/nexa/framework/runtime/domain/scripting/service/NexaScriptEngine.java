package nexa.framework.runtime.domain.scripting.service;

import nexa.framework.runtime.domain.scripting.api.ScriptEngine;
import nexa.framework.runtime.domain.scripting.api.ScriptCompiler;
import nexa.framework.runtime.domain.scripting.api.CompiledScript;
import nexa.framework.runtime.domain.scripting.api.ScriptExecutionResult;
import nexa.framework.runtime.domain.scripting.model.ScriptRuntimeContext;

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


