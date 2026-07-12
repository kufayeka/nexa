package nexa.framework.runtime.script;

import nexa.framework.runtime.message.RuntimeMessage;

import java.util.List;
import java.util.Map;

public interface ScriptExecutionResult {

    Map<String, List<RuntimeMessage>> emittedByPort();

    boolean stopped();
}
