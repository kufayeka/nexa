package nexa.framework.runtime.domain.scripting.internal.nexa;
import nexa.framework.runtime.domain.scripting.internal.nexa.NexaRuntimeExtension;
import nexa.framework.runtime.domain.scripting.internal.nexa.NexaRuntime;
import nexa.framework.runtime.domain.scripting.internal.nexa.NexaHostObject;

import java.util.Map;

public final class TestNexaRuntimeExtension implements NexaRuntimeExtension {

    @Override
    public Map<String, Object> globals() {
        return Map.of("TestPlugin", new TestPluginHostObject());
    }

    private static final class TestPluginHostObject implements NexaHostObject {

        @Override
        public Object member(String name, int line, int column) {
            return switch (name) {
                case "upper" -> (NexaRuntime.NexaCallable) (runtime, arguments, callLine, callColumn) ->
                        String.valueOf(arguments.getFirst()).toUpperCase();
                case "sum" -> (NexaRuntime.NexaCallable) (runtime, arguments, callLine, callColumn) ->
                        ((Number) arguments.get(0)).doubleValue() + ((Number) arguments.get(1)).doubleValue();
                default -> throw new NexaScriptException("Member plugin tidak dikenal: " + name, line, column);
            };
        }
    }
}


