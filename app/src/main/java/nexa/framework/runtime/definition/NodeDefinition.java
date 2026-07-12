package nexa.framework.runtime.definition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

public final class NodeDefinition {

    private final String id;
    private final NodeCategory category;
    private final String type;
    private final String language;
    private final boolean enabled;
    private final InputExecutionPolicyDefinition inputPolicy;
    private final Map<String, Object> config;

    @JsonCreator
    public NodeDefinition(
            @JsonProperty("id") String id,
            @JsonProperty("category") NodeCategory category,
            @JsonProperty("type") String type,
            @JsonProperty("language") String language,
            @JsonProperty("enabled") Boolean enabled,
            @JsonProperty("inputPolicy") InputExecutionPolicyDefinition inputPolicy,
            @JsonProperty("config") Map<String, Object> config) {
        this.id = id;
        this.category = category;
        this.type = type;
        this.language = language;
        this.enabled = enabled == null || enabled;
        this.inputPolicy = inputPolicy == null ? new InputExecutionPolicyDefinition(null) : inputPolicy;
        this.config = config == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(config));
    }

    public String id() {
        return id;
    }

    public NodeCategory category() {
        return category;
    }

    public String type() {
        return type;
    }

    public String language() {
        return language;
    }

    public boolean enabled() {
        return enabled;
    }

    public InputExecutionPolicyDefinition inputPolicy() {
        return inputPolicy;
    }

    public Map<String, Object> config() {
        return config;
    }
}
