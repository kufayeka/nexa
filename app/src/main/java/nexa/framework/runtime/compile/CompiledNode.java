package nexa.framework.runtime.compile;

import nexa.framework.runtime.definition.InputExecutionPolicyDefinition;
import nexa.framework.runtime.definition.NodeCategory;
import nexa.framework.runtime.script.CompiledScript;

import java.util.Map;

public record CompiledNode(
        String id,
        NodeCategory category,
        String type,
        boolean enabled,
        InputExecutionPolicyDefinition inputPolicy,
        Map<String, Object> config,
        String language,
        CompiledScript compiledScript) {
}
